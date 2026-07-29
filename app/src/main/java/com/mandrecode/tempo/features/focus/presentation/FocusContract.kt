package com.mandrecode.tempo.features.focus.presentation

import com.mandrecode.tempo.core.domain.model.DailyFocusActivity
import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import com.mandrecode.tempo.features.focus.domain.model.FocusHeadlineBand
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate

/**
 * Contract for the Focus screen following the MVI pattern.
 */
object FocusContract {
    data class UiState(
        val isLoading: Boolean = true,
        val today: LocalDate? = null,
        val streakDays: Int = 0,
        val history: ImmutableList<DailyFocusActivity> = persistentListOf(),
        val scheduledCount: Int = 0,
        val completedCount: Int = 0,
        val focusMinutes: Int = 0,
        val upNext: FocusAgendaItem? = null,
        val overdue: ImmutableList<FocusAgendaItem> = persistentListOf(),
        val todayItems: ImmutableList<FocusAgendaItem> = persistentListOf(),
        val undatedTaskCount: Int = 0,
        val expandedChainIds: ImmutableList<Long> = persistentListOf(),
        val session: FocusSession? = null,
        /** Set when a session has just finished, driving the completion sheet. */
        val finishedSession: FinishedSession? = null,
        /** The task the last session ran on, so "another session" restarts the same work. */
        val lastSessionTaskId: Long? = null,
        /**
         * The task the session screen is showing when no session is running — opened from Up next
         * to look at the work before committing to a timer.
         */
        val previewTaskId: Long? = null,
        /**
         * The item whose editor Focus is showing. Focus hosts the sheets itself rather than
         * sending you to the owning tab: opening an item should not move you off the day you were
         * looking at.
         */
        val editingTask: Task? = null,
        val editingHabit: Habit? = null,
        val defaultSessionLengthMinutes: Int = 25,
    ) {
        val headlineBand: FocusHeadlineBand
            get() = FocusHeadlineBand.resolve(scheduledCount, completedCount)

        /** Progress for the hero's indicator, `0f` when there is nothing to make progress on. */
        val progress: Float
            get() = if (scheduledCount <= 0) 0f else (completedCount.toFloat() / scheduledCount).coerceIn(0f, 1f)

        val isDayEmpty: Boolean get() = upNext == null && overdue.isEmpty() && todayItems.isEmpty()

        /**
         * The entry the session screen is showing: the running session's task, or the one being
         * previewed before a session starts.
         */
        val sessionEntry: FocusAgendaItem.TaskEntry?
            get() {
                val taskId = session?.taskId ?: previewTaskId ?: return null
                return (listOfNotNull(upNext) + overdue + todayItems)
                    .filterIsInstance<FocusAgendaItem.TaskEntry>()
                    .firstOrNull { it.task.id == taskId }
            }

        val sessionSubtasks: List<Task> get() = sessionEntry?.subtasks.orEmpty()
    }

    /** What the completion sheet reports: plain facts, no score and no streak. */
    data class FinishedSession(
        val taskTitle: String,
        val minutes: Int,
        /** A finished break offers going back to the work, not another break. */
        val wasBreak: Boolean = false,
    )

    sealed interface UiEvent {
        data class ToggleTaskCompletion(
            val task: Task,
        ) : UiEvent

        data class ToggleHabitCompletion(
            val habitId: Long,
            val isCompleted: Boolean,
        ) : UiEvent

        data class ToggleChainCompletion(
            val chainId: Long,
            val isCompleted: Boolean,
        ) : UiEvent

        data class ToggleChainExpanded(
            val chainId: Long,
        ) : UiEvent

        data class EditTask(
            val task: Task,
        ) : UiEvent

        data class EditHabit(
            val habit: Habit,
        ) : UiEvent

        data object DismissEditor : UiEvent

        data object UndatedTasksClicked : UiEvent

        data object StartSession : UiEvent

        data object PauseSession : UiEvent

        data object ResumeSession : UiEvent

        data object StopSession : UiEvent

        /** Finishes the work itself, not just the timer — ticks the task and ends the session. */
        data object CompleteSessionTask : UiEvent

        /** Opens the session screen for the Up next item without starting its timer. */
        data object PreviewUpNext : UiEvent

        data object ClearSessionPreview : UiEvent

        data object OpenSessionScreen : UiEvent

        data object StartAnotherSession : UiEvent

        data object TakeBreak : UiEvent

        data object DismissFinishedSession : UiEvent
    }

    sealed interface UiEffect {
        /** The undated list has no Focus equivalent, so that one really does hand over to Tasks. */
        data object OpenTasksTab : UiEffect

        /** The session gets its own slide-in screen, so opening it is navigation. */
        data object OpenSessionScreen : UiEffect
    }
}
