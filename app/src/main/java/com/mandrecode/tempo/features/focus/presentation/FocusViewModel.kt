package com.mandrecode.tempo.features.focus.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
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
        private val toggleHabitCompletion: ToggleHabitCompletionUseCase,
        private val clock: Clock,
    ) : ViewModel() {
        private val mutableUiState = MutableStateFlow(FocusContract.UiState())
        val uiState: StateFlow<FocusContract.UiState> = mutableUiState.asStateFlow()

        private val effectChannel = Channel<FocusContract.UiEffect>(Channel.BUFFERED)
        val uiEffect = effectChannel.receiveAsFlow()

        private val today: LocalDate get() = clock.todayIn(TimeZone.currentSystemDefault())

        init {
            observeDay()
            // The screen being opened is itself a reason to recount: work may have been added or
            // rescheduled from another tab since the last completion.
            viewModelScope.launch { recordDailyActivity(today) }
        }

        fun onEvent(event: FocusContract.UiEvent) {
            when (event) {
                is FocusContract.UiEvent.ToggleTaskCompletion ->
                    viewModelScope.launch { toggleTaskCompletion(event.task) }

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
