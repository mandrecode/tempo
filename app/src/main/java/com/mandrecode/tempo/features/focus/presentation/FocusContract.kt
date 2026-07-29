package com.mandrecode.tempo.features.focus.presentation

import com.mandrecode.tempo.core.domain.model.DailyFocusActivity
import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import com.mandrecode.tempo.features.focus.domain.model.FocusHeadlineBand
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
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
        val defaultSessionLengthMinutes: Int = 25,
        val isSessionImmersive: Boolean = false,
    ) {
        val headlineBand: FocusHeadlineBand
            get() = FocusHeadlineBand.resolve(scheduledCount, completedCount)

        /** Progress for the hero's indicator, `0f` when there is nothing to make progress on. */
        val progress: Float
            get() = if (scheduledCount <= 0) 0f else (completedCount.toFloat() / scheduledCount).coerceIn(0f, 1f)

        val isDayEmpty: Boolean get() = overdue.isEmpty() && todayItems.isEmpty()

        /** Subtasks of the task the session is running on, for the immersive checklist. */
        val sessionSubtasks: List<Task>
            get() {
                val taskId = session?.taskId ?: return emptyList()
                return (overdue + todayItems)
                    .filterIsInstance<FocusAgendaItem.TaskEntry>()
                    .firstOrNull { it.task.id == taskId }
                    ?.subtasks
                    .orEmpty()
            }
    }

    /** What the completion sheet reports: plain facts, no score and no streak. */
    data class FinishedSession(
        val taskTitle: String,
        val minutes: Int,
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
            val habitId: Long,
        ) : UiEvent

        data object UndatedTasksClicked : UiEvent

        data object StartSession : UiEvent

        data object PauseSession : UiEvent

        data object ResumeSession : UiEvent

        data object StopSession : UiEvent

        data object ExpandSession : UiEvent

        data object CollapseSession : UiEvent

        data object StartAnotherSession : UiEvent

        data object TakeBreak : UiEvent

        data object DismissFinishedSession : UiEvent
    }

    sealed interface UiEffect {
        /** Focus edits through the tab that owns the item rather than duplicating its editor. */
        data class OpenTaskInTasksTab(
            val taskId: Long,
        ) : UiEffect

        data class OpenHabitInRoutinesTab(
            val habitId: Long,
        ) : UiEffect

        data object OpenTasksTab : UiEffect
    }
}
