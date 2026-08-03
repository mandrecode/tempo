package com.mandrecode.tempo.features.focus.presentation

import com.mandrecode.tempo.core.domain.model.DailyFocusActivity
import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import com.mandrecode.tempo.features.focus.domain.model.FocusHeadlineBand
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.model.UndatedTask
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

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
        /**
         * What the routine editor is open on: a habit, or a chain. One field rather than two,
         * because the sheet can only show one of them and two nullables would encode a state it
         * cannot render.
         */
        val routineEditor: RoutineEditorTarget? = null,
        /**
         * The planning sheet, when it is open. Undated work has no place in a day, so the one
         * thing Focus can offer it is somewhere to be given one.
         */
        val planSheet: PlanSheetState? = null,
        /**
         * A start waiting on the user's word, because taking it would end the session already
         * running. Held rather than acted on: replacing a session is not something to discover
         * afterwards.
         */
        val pendingStart: PendingStart? = null,
        val defaultSessionLengthMinutes: Int = 25,
        val breakLengthMinutes: Int = 5,
        /**
         * Whether today falls inside a vacation period. Vacation is a broad "relax without breaking
         * streaks" toggle rather than a habits-only one, so the day says so here too — and the
         * streak beside it is counted on those terms.
         */
        val isVacationModeActive: Boolean = false,
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
         * The task the session screen is about.
         *
         * A preview wins over the running session, not the other way round: tapping a queued card
         * is asking for *that* task, and answering with whatever happens to be counting down was
         * the screen refusing to open the thing that was tapped.
         */
        val sessionTaskId: Long? get() = previewTaskId ?: session?.taskId

        /**
         * The session that screen should count down — none while previewing another task, so the
         * ring stands still on the work you are looking at rather than on the work under way.
         */
        val screenSession: FocusSession? get() = session?.takeIf { it.taskId == sessionTaskId }

        /**
         * The entry the session screen is showing: the task being previewed, or the one the running
         * session is on.
         */
        val sessionEntry: FocusAgendaItem.TaskEntry? get() = entryFor(sessionTaskId)

        val sessionSubtasks: List<Task> get() = sessionEntry?.subtasks.orEmpty()

        /**
         * The entry for the task the timer is actually on, for the card that counts it down.
         *
         * Not [sessionEntry], which follows the screen's subject: while the sheet sits open on some
         * other task the card underneath is still the running one, and it should go on describing
         * the work it is timing.
         */
        val runningEntry: FocusAgendaItem.TaskEntry? get() = entryFor(session?.taskId)

        /** The task the last session ran on, for the choices offered once it has ended. */
        val lastSessionEntry: FocusAgendaItem.TaskEntry? get() = entryFor(lastSessionTaskId)

        /**
         * The row is searched too, not only the sections. It normally mirrors them, but this is a
         * lookup and a duplicate costs nothing, whereas missing the task the session is on costs
         * the screen its subject.
         */
        private fun entryFor(taskId: Long?): FocusAgendaItem.TaskEntry? {
            if (taskId == null) return null
            return (upNext + overdue + todayItems)
                .filterIsInstance<FocusAgendaItem.TaskEntry>()
                .firstOrNull { it.task.id == taskId }
        }
    }

    /**
     * The plan sheet: the undated tasks it opened with, and what has happened to them since.
     *
     * [rows] is held against the ids the sheet opened with rather than against "whatever is still
     * undated". Planning a task is exactly what takes it out of that second answer, so a sheet
     * driven by it would erase each row the moment it did its job — and the planned/unplanned split
     * only exists because the sheet remembers what it started with.
     */
    data class PlanSheetState(
        val rows: ImmutableList<UndatedTask> = persistentListOf(),
        /**
         * What each task's reminder was when the sheet opened — all null in practice, since these
         * are the undated ones, but held explicitly because undo means "put the sheet back", not
         * "clear the date".
         */
        val originalReminders: ImmutableMap<Long, LocalDateTime?> = persistentMapOf(),
        val isLoading: Boolean = true,
    ) {
        val planned: List<UndatedTask> get() = rows.filter { it.task.reminderDate != null }

        val unplanned: List<UndatedTask> get() = rows.filter { it.task.reminderDate == null }

        /**
         * Everything this sheet session changed, however it was changed — a chip, or the full
         * editor opened over the sheet. One notion rather than two, so undo puts back exactly what
         * the sheet is answerable for.
         */
        val changedTaskIds: List<Long>
            get() =
                rows
                    .filter { it.task.reminderDate != originalReminders[it.task.id] }
                    .map { it.task.id }

        /** Whether closing has anything to offer back. */
        val hasChanges: Boolean get() = changedTaskIds.isNotEmpty()

        /**
         * A sheet where nothing is planned yet is one plain list: headers that only ever say
         * "Unplanned" over everything are labelling the obvious.
         */
        val showsSectionHeaders: Boolean get() = planned.isNotEmpty()
    }

    /** A start that would replace a running session, waiting to be confirmed. */
    data class PendingStart(
        val taskId: Long,
        val taskTitle: String,
        val lengthMinutes: Int?,
        val replacingTaskTitle: String,
    )

    /** Why the task editor is open: to change a task, or to add a subtask beneath one. */
    sealed interface TaskEditorTarget {
        data class Existing(
            val task: Task,
        ) : TaskEditorTarget

        data class NewSubtask(
            val parentTaskId: Long,
        ) : TaskEditorTarget
    }

    /**
     * What the routine editor is open on. Routines drives both from one form, so Focus does too
     * rather than standing up a second view model for chains.
     */
    sealed interface RoutineEditorTarget {
        data class SingleHabit(
            val habit: Habit,
        ) : RoutineEditorTarget

        data class Chain(
            val chain: HabitChain,
        ) : RoutineEditorTarget
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

        /**
         * Opening a chain is opening its card, the same way a task or a habit opens. The chevron
         * beside it still only folds the chain out; the two are different questions.
         */
        data class EditChain(
            val chain: HabitChain,
        ) : UiEvent

        data class AddSubtask(
            val parentTaskId: Long,
        ) : UiEvent

        data object ConfirmPendingStart : UiEvent

        data object DismissPendingStart : UiEvent

        data object DismissEditor : UiEvent

        /** Opens the plan sheet. Focus used to answer this by leaving for Tasks. */
        data object UndatedTasksClicked : UiEvent

        /** One quick-plan choice: [date], at whatever time `PlanReminderTimeUtil` makes of it. */
        data class PlanTask(
            val taskId: Long,
            val date: LocalDate,
        ) : UiEvent

        /**
         * Takes a task's date back off, returning it to the unplanned section.
         *
         * The same chip that set the day, pressed again. Planning several tasks at speed means
         * mis-tapping some of them, and a choice that cannot be taken back is one people make
         * slowly.
         */
        data class UnplanTask(
            val taskId: Long,
        ) : UiEvent

        /**
         * Closes the sheet, however it was closed — the button, the handle, back, the scrim or
         * Escape. All of them leave the planning in place and offer to take the batch back, because
         * every one of them is the same act: leaving, having planned something.
         */
        data object ClosePlanSheet : UiEvent

        data object UndoPlanBatch : UiEvent

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
        /** The session gets its own slide-in screen, so opening it is navigation. */
        data object OpenSessionScreen : UiEffect

        /**
         * The sheet has closed on [count] newly planned tasks, and the way back is still open.
         *
         * Offered here rather than inside the sheet because it is about what the sheet did, and by
         * the time there is anything to say the sheet is gone.
         */
        data class PlanBatchConfirmed(
            val count: Int,
        ) : UiEffect
    }
}
