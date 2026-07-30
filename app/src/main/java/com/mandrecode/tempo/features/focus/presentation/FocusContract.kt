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
        val upNext: ImmutableList<FocusAgendaItem.TaskEntry> = persistentListOf(),
        val overdue: ImmutableList<FocusAgendaItem> = persistentListOf(),
        val todayItems: ImmutableList<FocusAgendaItem> = persistentListOf(),
        val undatedTaskCount: Int = 0,
        val expandedChainIds: ImmutableList<Long> = persistentListOf(),
        /**
         * Tasks whose subtasks are unfolded. Folded is the resting state: the day is a list of what
         * is on today, and every step of every task opened by default buried that under itself.
         */
        val expandedTaskIds: ImmutableList<Long> = persistentListOf(),
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
         * What the task editor is open on, if anything. Focus hosts the sheets itself rather than
         * sending you to the owning tab: opening an item should not move you off the day you were
         * looking at.
         */
        val taskEditor: TaskEditorTarget? = null,
        val editingHabit: Habit? = null,
        val defaultSessionLengthMinutes: Int = 25,
        val breakLengthMinutes: Int = 5,
    ) {
        val headlineBand: FocusHeadlineBand
            get() = FocusHeadlineBand.resolve(scheduledCount, completedCount)

        /** Progress for the hero's indicator, `0f` when there is nothing to make progress on. */
        val progress: Float
            get() = if (scheduledCount <= 0) 0f else (completedCount.toFloat() / scheduledCount).coerceIn(0f, 1f)

        val isDayEmpty: Boolean get() = overdue.isEmpty() && todayItems.isEmpty()

        /**
         * The day's sections without whatever the running session is on.
         *
         * That task already has the card above, counting down. Leaving it in the list as well read
         * as two things when it is one, and as work still waiting when it is under way.
         */
        val visibleOverdue: List<FocusAgendaItem> get() = overdue.withoutSessionTask()

        val visibleToday: List<FocusAgendaItem> get() = todayItems.withoutSessionTask()

        private fun List<FocusAgendaItem>.withoutSessionTask(): List<FocusAgendaItem> {
            val taskId = session?.taskId ?: return this
            return filterNot { it is FocusAgendaItem.TaskEntry && it.task.id == taskId }
        }

        /**
         * The entry the session screen is showing: the running session's task, or the one being
         * previewed before a session starts.
         */
        val sessionEntry: FocusAgendaItem.TaskEntry?
            get() {
                val taskId = session?.taskId ?: previewTaskId ?: return null
                // The row is searched too, not only the sections. It normally mirrors them, but
                // this is a lookup and a duplicate costs nothing, whereas missing the task the
                // session is on costs the screen its subject.
                return (upNext + overdue + todayItems)
                    .filterIsInstance<FocusAgendaItem.TaskEntry>()
                    .firstOrNull { it.task.id == taskId }
            }

        val sessionSubtasks: List<Task> get() = sessionEntry?.subtasks.orEmpty()
    }

    /** Why the task editor is open: to change a task, or to add a subtask beneath one. */
    sealed interface TaskEditorTarget {
        data class Existing(
            val task: Task,
        ) : TaskEditorTarget

        data class NewSubtask(
            val parentTaskId: Long,
        ) : TaskEditorTarget
    }

    /** What the completion sheet reports: plain facts, no score and no streak. */
    data class FinishedSession(
        val taskTitle: String,
        val minutes: Int,
        /** A finished break offers going back to the work, not another break. */
        val wasBreak: Boolean = false,
        /** What a break would run for, so the offer says the length Settings actually holds. */
        val breakMinutes: Int = 5,
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

        data class ToggleSubtasksExpanded(
            val taskId: Long,
        ) : UiEvent

        data class EditTask(
            val task: Task,
        ) : UiEvent

        data class EditHabit(
            val habit: Habit,
        ) : UiEvent

        data class AddSubtask(
            val parentTaskId: Long,
        ) : UiEvent

        data object DismissEditor : UiEvent

        data object UndatedTasksClicked : UiEvent

        /**
         * [lengthMinutes] applies to this start alone and never changes the setting. [taskId]
         * names the card you started from; without one it is whatever the screen is showing.
         */
        data class StartSession(
            val lengthMinutes: Int? = null,
            val taskId: Long? = null,
        ) : UiEvent

        data object PauseSession : UiEvent

        data object ResumeSession : UiEvent

        data object StopSession : UiEvent

        /** Finishes the work itself, not just the timer — ticks the task and ends the session. */
        data object CompleteSessionTask : UiEvent

        /** Opens the session screen for an Up next card without starting its timer. */
        data class PreviewUpNext(
            val taskId: Long,
        ) : UiEvent

        data object ClearSessionPreview : UiEvent

        data object OpenSessionScreen : UiEvent

        data object StartAnotherSession : UiEvent

        data object TakeBreak : UiEvent

        /** Ends the break early and starts a fresh session on the same task. */
        data object BackToWork : UiEvent

        data object DismissFinishedSession : UiEvent
    }

    sealed interface UiEffect {
        /** The undated list has no Focus equivalent, so that one really does hand over to Tasks. */
        data object OpenTasksTab : UiEffect

        /** The session gets its own slide-in screen, so opening it is navigation. */
        data object OpenSessionScreen : UiEffect
    }
}
