package com.mandrecode.tempo.features.focus.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mandrecode.tempo.core.domain.model.VacationPeriod
import com.mandrecode.tempo.core.domain.repository.VacationModeRepository
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
import kotlinx.collections.immutable.ImmutableList
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
        private val vacationModeRepository: VacationModeRepository,
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

                is FocusContract.UiEvent.ToggleChainExpanded ->
                    mutableUiState.update {
                        it.copy(expandedChainIds = it.expandedChainIds.toggling(event.chainId))
                    }

                is FocusContract.UiEvent.ToggleSubtasksExpanded ->
                    mutableUiState.update {
                        it.copy(expandedTaskIds = it.expandedTaskIds.toggling(event.taskId))
                    }

                FocusContract.UiEvent.UndatedTasksClicked ->
                    sendEffect(FocusContract.UiEffect.OpenTasksTab)

                else -> onEditorEvent(event)
            }
        }

        /** Which sheet or dialog is open — state changes only, no work behind them. */
        private fun onEditorEvent(event: FocusContract.UiEvent) {
            when (event) {
                is FocusContract.UiEvent.EditTask ->
                    mutableUiState.update {
                        it.copy(
                            taskEditor = FocusContract.TaskEditorTarget.Existing(event.task),
                            routineEditor = null,
                        )
                    }

                is FocusContract.UiEvent.AddSubtask ->
                    mutableUiState.update {
                        it.copy(
                            taskEditor = FocusContract.TaskEditorTarget.NewSubtask(event.parentTaskId),
                            routineEditor = null,
                        )
                    }

                // One sheet at a time: opening a routine closes whatever the task editor had.
                is FocusContract.UiEvent.EditHabit ->
                    mutableUiState.update {
                        it.copy(
                            routineEditor = FocusContract.RoutineEditorTarget.SingleHabit(event.habit),
                            taskEditor = null,
                        )
                    }

                is FocusContract.UiEvent.EditChain ->
                    mutableUiState.update {
                        it.copy(
                            routineEditor = FocusContract.RoutineEditorTarget.Chain(event.chain),
                            taskEditor = null,
                        )
                    }

                FocusContract.UiEvent.DismissEditor ->
                    mutableUiState.update { it.copy(taskEditor = null, routineEditor = null) }

                FocusContract.UiEvent.DismissPendingStart ->
                    mutableUiState.update { it.copy(pendingStart = null) }

                FocusContract.UiEvent.ConfirmPendingStart -> {
                    val pending = mutableUiState.value.pendingStart ?: return
                    mutableUiState.update { it.copy(pendingStart = null) }
                    startSessionOn(pending.taskId, pending.lengthMinutes, force = true)
                }

                else -> onSessionEvent(event)
            }
        }

        /**
         * Timer controls. Split from the completion-sheet handling below so neither `when` grows
         * past what is readable, and starting is inlined here because it has exactly one caller.
         */
        private fun onSessionEvent(event: FocusContract.UiEvent) {
            when (event) {
                is FocusContract.UiEvent.StartSession ->
                    startSessionOn(event.taskId, event.lengthMinutes)

                FocusContract.UiEvent.BackToWork ->
                    mutableUiState.value.session
                        ?.takeIf { it.isBreak }
                        ?.let { startSessionOn(taskId = it.taskId, lengthMinutes = null) }

                FocusContract.UiEvent.PauseSession -> focusSessionUseCases.pause()
                FocusContract.UiEvent.ResumeSession -> focusSessionUseCases.resume()
                FocusContract.UiEvent.StopSession -> viewModelScope.launch { finishSession() }

                FocusContract.UiEvent.CompleteSessionTask ->
                    viewModelScope.launch { completeSessionTask() }

                FocusContract.UiEvent.OpenSessionScreen -> {
                    // Reaching for the running session is asking for it by name, so a preview left
                    // over from some other card must not be what opens instead.
                    focusSessionRepository.setPreviewTaskId(null)
                    sendEffect(FocusContract.UiEffect.OpenSessionScreen)
                }

                is FocusContract.UiEvent.PreviewUpNext -> {
                    // Look at the work first: the screen opens on the task with its timer at full
                    // length, and starting stays a separate, deliberate tap.
                    focusSessionRepository.setPreviewTaskId(event.taskId)
                    sendEffect(FocusContract.UiEffect.OpenSessionScreen)
                }

                FocusContract.UiEvent.ClearSessionPreview ->
                    focusSessionRepository.setPreviewTaskId(null)

                else -> onCompletionSheetEvent(event)
            }
        }

        /** What the sheet offers once time is up. */
        private fun onCompletionSheetEvent(event: FocusContract.UiEvent) {
            val finished = mutableUiState.value.finishedSession
            val taskId = mutableUiState.value.lastSessionTaskId
            when (event) {
                FocusContract.UiEvent.StartAnotherSession ->
                    viewModelScope.launch {
                        mutableUiState.update { it.copy(finishedSession = null) }
                        if (finished != null && taskId != null) {
                            focusSessionUseCases.start(taskId, finished.taskTitle)
                            sendEffect(FocusContract.UiEffect.OpenSessionScreen)
                        }
                    }

                FocusContract.UiEvent.TakeBreak ->
                    viewModelScope.launch {
                        mutableUiState.update { it.copy(finishedSession = null) }
                        if (finished != null && taskId != null) {
                            // A real countdown, not a dismissal: the break notifies when it is over
                            // so the user is not the one who has to remember to come back.
                            focusSessionUseCases.start(taskId, finished.taskTitle, isBreak = true)
                        }
                    }

                FocusContract.UiEvent.DismissFinishedSession ->
                    mutableUiState.update { it.copy(finishedSession = null) }
                else -> Unit
            }
        }

        /**
         * Ends the running session because the user said so.
         *
         * No sheet: stopping and marking done are already decisions, and asking "what next?" the
         * moment one is made is asking a question that has just been answered. The banked minutes
         * need no announcement either — they are already in the hero and the heatmap on the screen
         * this returns to. The sheet belongs to the one ending nobody chose, see [onSessionVanished].
         */
        private suspend fun finishSession() {
            val ended = focusSessionUseCases.end() ?: return
            mutableUiState.update { it.copy(lastSessionTaskId = ended.taskId) }
        }

        /**
         * Starts on the card that was tapped, or on whatever the screen is already showing when
         * nothing names one. Also the way out of a break: starting replaces whatever is running,
         * and a break banks no minutes, so cutting one short costs nothing.
         */
        private fun startSessionOn(
            taskId: Long?,
            lengthMinutes: Int?,
            force: Boolean = false,
        ) {
            val state = mutableUiState.value
            val task =
                taskId
                    ?.let { id -> state.upNext.firstOrNull { it.task.id == id } }
                    ?.task
                    ?: (state.sessionEntry ?: state.upNext.firstOrNull())?.task
                    ?: return

            // Starting replaces whatever is running and banks only what it earned, so swapping
            // tasks mid-session is a real loss of the current one's remaining time. Ask first —
            // unless it is the same task, where "starting" is just carrying on.
            // Only a focus session on a different task is at risk of being thrown away; the same
            // task is carrying on, and a break has nothing to lose.
            val replaced = state.session?.takeIf { !it.isBreak && it.taskId != task.id }
            if (!force && replaced != null) {
                mutableUiState.update {
                    it.copy(
                        pendingStart =
                            FocusContract.PendingStart(
                                taskId = task.id,
                                taskTitle = task.title,
                                lengthMinutes = lengthMinutes,
                                replacingTaskTitle = replaced.taskTitle,
                            ),
                    )
                }
                return
            }

            viewModelScope.launch {
                focusSessionUseCases.start(
                    taskId = task.id,
                    taskTitle = task.title,
                    lengthMinutes = lengthMinutes,
                )
                focusSessionRepository.setPreviewTaskId(null)
                // Starting is a commitment to the work, so the app follows the user into it rather
                // than leaving them to find what they just began.
                sendEffect(FocusContract.UiEffect.OpenSessionScreen)
            }
        }

        /**
         * Finishing the work, not just the timer: a session you cut short is not the same as a job
         * done, so completing has to be its own action.
         *
         * Falls back to the task the last session ran on, because the completion sheet offers this
         * after the session has already ended — by then there is no running session to read from.
         */
        private suspend fun completeSessionTask() {
            val state = mutableUiState.value
            val entry = state.sessionEntry ?: state.lastSessionEntry
            if (entry != null && !entry.task.isCompleted) {
                toggleTaskCompletion(entry.task)
                // Only the timer that was on this work: finishing a task you had merely opened to
                // look at must not end the session running on another one.
                if (state.session?.taskId == entry.task.id) finishSession()
            }
            // Whether or not there was anything left to tick, the screen has said its piece. Leaving
            // the preview standing on a finished task left the button there with nothing to do,
            // which read as it being broken.
            focusSessionRepository.setPreviewTaskId(null)
            mutableUiState.update { it.copy(finishedSession = null) }
        }

        private fun observeSession() {
            viewModelScope.launch {
                focusSessionRepository.activeSession.collect { session ->
                    val previous = mutableUiState.value.session
                    mutableUiState.update { it.copy(session = session) }
                    // The alarm receiver ends an expired session wherever the app happens to be,
                    // so a session disappearing is the only signal there is. Having run out is what
                    // tells the two endings apart: one the user stopped still had time on it, and
                    // only the one nobody chose is worth asking a question about.
                    if (session == null && previous != null && previous.hasExpired(clock.now())) {
                        mutableUiState.update { state ->
                            state.copy(
                                lastSessionTaskId = previous.taskId,
                                finishedSession =
                                    FocusContract.FinishedSession(
                                        taskTitle = previous.taskTitle,
                                        minutes = previous.plannedLength.inWholeMinutes.toInt(),
                                        wasBreak = previous.isBreak,
                                        breakMinutes = focusSessionRepository.breakLengthMinutes.value,
                                    ),
                            )
                        }
                    }
                }
            }
            viewModelScope.launch {
                focusSessionRepository.defaultLengthMinutes.collect { minutes ->
                    mutableUiState.update { it.copy(defaultSessionLengthMinutes = minutes) }
                }
            }
            viewModelScope.launch {
                focusSessionRepository.previewTaskId.collect { taskId ->
                    mutableUiState.update { it.copy(previewTaskId = taskId) }
                }
            }
            viewModelScope.launch {
                focusSessionRepository.breakLengthMinutes.collect { minutes ->
                    mutableUiState.update { it.copy(breakLengthMinutes = minutes) }
                }
            }
        }

        private fun observeDay() {
            viewModelScope.launch {
                val day = today
                // The vacation periods are collected here rather than on their own, because the
                // streak is counted on their terms: pausing while this screen is open has to move
                // the number as well as the badge, and two collectors would have let the badge
                // appear beside a streak still counted the old way.
                combine(
                    getFocusAgenda(day),
                    getFocusHistory(day),
                    vacationModeRepository.periods,
                ) { agenda, history, periods ->
                    Triple(agenda, history, periods)
                }.collect { (agenda, history, periods) ->
                    val streak = getFocusStreak(day)
                    mutableUiState.update { state ->
                        state.copy(
                            isLoading = false,
                            today = day,
                            streakDays = streak,
                            isVacationModeActive = VacationPeriod.activeOn(periods, day) != null,
                            history = history.toPersistentList(),
                            scheduledCount = agenda.scheduledCount,
                            completedCount = agenda.completedCount,
                            focusMinutes = history.lastOrNull { it.date == day }?.focusMinutes ?: 0,
                            upNext = agenda.upNext.toPersistentList(),
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
            val chain =
                (mutableUiState.value.overdue + mutableUiState.value.todayItems)
                    .filterIsInstance<FocusAgendaItem.ChainEntry>()
                    .firstOrNull { it.chain.id == chainId } ?: return
            chain.habits.forEach { habit ->
                toggleHabitCompletion(habit.id, isCompleted, today)
            }
        }

        private fun sendEffect(effect: FocusContract.UiEffect) {
            viewModelScope.launch { effectChannel.send(effect) }
        }
    }

/** Adds [id] when absent, removes it when present — the shape both expansion toggles need. */
private fun ImmutableList<Long>.toggling(id: Long): ImmutableList<Long> =
    if (id in this) (this - id).toPersistentList() else (this + id).toPersistentList()
