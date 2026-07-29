package com.mandrecode.tempo.features.focus.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import com.mandrecode.tempo.features.focus.domain.repository.FocusSessionRepository
import com.mandrecode.tempo.features.focus.domain.usecase.FocusSessionUseCases
import com.mandrecode.tempo.features.focus.domain.usecase.GetFocusAgendaUseCase
import com.mandrecode.tempo.features.focus.domain.usecase.GetFocusHistoryUseCase
import com.mandrecode.tempo.features.focus.domain.usecase.GetFocusStreakUseCase
import com.mandrecode.tempo.features.focus.domain.usecase.RecordDailyActivityUseCase
import com.mandrecode.tempo.features.routines.domain.usecase.ToggleHabitCompletionUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.ToggleTaskCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject
import kotlin.time.Clock

@HiltViewModel
class FocusViewModel
    @Inject
    constructor(
        private val getFocusAgenda: GetFocusAgendaUseCase,
        private val getFocusHistory: GetFocusHistoryUseCase,
        private val getFocusStreak: GetFocusStreakUseCase,
        private val recordDailyActivity: RecordDailyActivityUseCase,
        private val toggleTaskCompletion: ToggleTaskCompletionUseCase,
        private val focusSessionRepository: FocusSessionRepository,
        private val focusSessionUseCases: FocusSessionUseCases,
        private val toggleHabitCompletion: ToggleHabitCompletionUseCase,
        private val clock: Clock,
    ) : ViewModel() {
        private val mutableUiState = MutableStateFlow(FocusContract.UiState())
        val uiState: StateFlow<FocusContract.UiState> = mutableUiState.asStateFlow()

        private val effectChannel = Channel<FocusContract.UiEffect>(Channel.BUFFERED)
        val uiEffect = effectChannel.receiveAsFlow()

        private val today: LocalDate get() = clock.todayIn(TimeZone.currentSystemDefault())

        init {
            observeSession()
            observeDay()
            // The screen being opened is itself a reason to recount: work may have been added or
            // rescheduled from another tab since the last completion.
            viewModelScope.launch { recordDailyActivity(today) }
        }

        fun onEvent(event: FocusContract.UiEvent) {
            when (event) {
                is FocusContract.UiEvent.ToggleTaskCompletion ->
                    viewModelScope.launch {
                        toggleTaskCompletion(event.task)
                        // Finishing the work is finishing the session: no point counting down on
                        // something already done.
                        val session = mutableUiState.value.session
                        if (session?.taskId == event.task.id && !event.task.isCompleted) {
                            finishSession()
                        }
                    }

                is FocusContract.UiEvent.ToggleHabitCompletion ->
                    viewModelScope.launch {
                        toggleHabitCompletion(event.habitId, event.isCompleted, today)
                    }

                is FocusContract.UiEvent.ToggleChainCompletion ->
                    viewModelScope.launch { toggleChain(event.chainId, event.isCompleted) }

                is FocusContract.UiEvent.ToggleChainExpanded -> toggleChainExpanded(event.chainId)

                is FocusContract.UiEvent.EditTask ->
                    sendEffect(FocusContract.UiEffect.OpenTaskInTasksTab(event.task.id))

                is FocusContract.UiEvent.EditHabit ->
                    sendEffect(FocusContract.UiEffect.OpenHabitInRoutinesTab(event.habitId))

                FocusContract.UiEvent.UndatedTasksClicked ->
                    sendEffect(FocusContract.UiEffect.OpenTasksTab)

                else -> onSessionEvent(event)
            }
        }

        /** Session controls, split out to keep [onEvent] readable as the list grows. */
        private fun onSessionEvent(event: FocusContract.UiEvent) {
            when (event) {
                FocusContract.UiEvent.StartSession -> startSessionOnUpNext()
                FocusContract.UiEvent.PauseSession -> focusSessionUseCases.pause()
                FocusContract.UiEvent.ResumeSession -> focusSessionUseCases.resume()
                FocusContract.UiEvent.StopSession -> viewModelScope.launch { finishSession() }
                FocusContract.UiEvent.OpenSessionScreen ->
                    sendEffect(FocusContract.UiEffect.OpenSessionScreen)

                FocusContract.UiEvent.StartAnotherSession ->
                    viewModelScope.launch {
                        val finished = mutableUiState.value.finishedSession
                        val taskId = mutableUiState.value.lastSessionTaskId
                        dismissFinishedSession()
                        if (finished != null && taskId != null) {
                            focusSessionUseCases.start(taskId = taskId, taskTitle = finished.taskTitle)
                        }
                    }

                FocusContract.UiEvent.TakeBreak -> dismissFinishedSession()
                FocusContract.UiEvent.DismissFinishedSession -> dismissFinishedSession()
                else -> Unit
            }
        }

        /**
         * Starts a session on whatever Up next is currently spotlighting. Only tasks can host a
         * session — a habit is a checkbox, not a stretch of work.
         */
        private fun startSessionOnUpNext() {
            val upNext = mutableUiState.value.upNext as? FocusAgendaItem.TaskEntry ?: return
            viewModelScope.launch {
                focusSessionUseCases.start(taskId = upNext.task.id, taskTitle = upNext.task.title)
            }
        }

        /** Ends the running session and raises the completion sheet with what it banked. */
        private suspend fun finishSession() {
            val now = clock.now()
            val running = mutableUiState.value.session
            val minutes = running?.bankableMinutes(now) ?: 0
            val ended = focusSessionUseCases.end() ?: return
            mutableUiState.update {
                it.copy(
                    lastSessionTaskId = ended.taskId,
                    finishedSession =
                        FocusContract.FinishedSession(
                            taskTitle = ended.taskTitle,
                            minutes = minutes,
                        ),
                )
            }
        }

        private fun dismissFinishedSession() {
            mutableUiState.update { it.copy(finishedSession = null) }
        }

        private fun observeSession() {
            viewModelScope.launch {
                focusSessionRepository.activeSession.collect { session ->
                    mutableUiState.update { it.copy(session = session) }
                }
            }
            viewModelScope.launch {
                focusSessionRepository.defaultLengthMinutes.collect { minutes ->
                    mutableUiState.update { it.copy(defaultSessionLengthMinutes = minutes) }
                }
            }
        }

        private fun observeDay() {
            viewModelScope.launch {
                val day = today
                combine(
                    getFocusAgenda(day),
                    getFocusHistory(day),
                ) { agenda, history ->
                    agenda to history
                }.collect { (agenda, history) ->
                    val streak = getFocusStreak(day)
                    mutableUiState.update { state ->
                        state.copy(
                            isLoading = false,
                            today = day,
                            streakDays = streak,
                            history = history.toPersistentList(),
                            scheduledCount = agenda.scheduledCount,
                            completedCount = agenda.completedCount,
                            focusMinutes = history.lastOrNull { it.date == day }?.focusMinutes ?: 0,
                            upNext = agenda.upNext,
                            overdue = agenda.overdue.toPersistentList(),
                            todayItems = agenda.today.toPersistentList(),
                            undatedTaskCount = agenda.undatedTaskCount,
                        )
                    }
                }
            }
        }

        private suspend fun toggleChain(
            chainId: Long,
            isCompleted: Boolean,
        ) {
            val chain = mutableUiState.value.chainEntry(chainId) ?: return
            chain.habits.forEach { habit ->
                toggleHabitCompletion(habit.id, isCompleted, today)
            }
        }

        private fun FocusContract.UiState.chainEntry(chainId: Long) =
            (overdue + todayItems)
                .filterIsInstance<FocusAgendaItem.ChainEntry>()
                .firstOrNull { it.chain.id == chainId }

        private fun toggleChainExpanded(chainId: Long) {
            mutableUiState.update { state ->
                val expanded = state.expandedChainIds
                val updated =
                    if (chainId in expanded) expanded - chainId else expanded + chainId
                state.copy(expandedChainIds = updated.toPersistentList())
            }
        }

        private fun sendEffect(effect: FocusContract.UiEffect) {
            viewModelScope.launch { effectChannel.send(effect) }
        }
    }
