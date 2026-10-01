package com.mandrecode.tempo.features.focus.domain.model

import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.datetime.LocalTime

/**
 * One task row of the Focus agenda, ordered across categories.
 */
sealed interface FocusAgendaItem {
    val id: String
    val isCompleted: Boolean

    /** When the item is due today, or `null` for something with no time of its own. */
    val dueTime: LocalTime?

    /** The task priority. */
    val priority: Priority?

    data class TaskEntry(
        val task: Task,
        val subtasks: List<Task> = emptyList(),
        /** Resolved once in the use case so the card does not have to look it up. */
        val categoryName: String? = null,
        /**
         * What this task has had out of today. A task with time behind it and no tick is a state of
         * its own: worked on, not finished — and the card says so instead of offering to start as
         * if nothing had happened.
         */
        val focusToday: TaskFocusToday = TaskFocusToday(),
    ) : FocusAgendaItem {
        val sessionsToday: Int get() = focusToday.sessions
        val minutesToday: Int get() = focusToday.minutes

        override val id: String = "task_${task.id}"
        override val isCompleted: Boolean = task.isCompleted
        override val dueTime: LocalTime? = task.reminderDate?.time
        override val priority: Priority? = task.priority
    }
}

/**
 * Everything the Focus screen shows, already ordered.
 *
 * [upNext] is drawn from [overdue] and [today] rather than removed from them. A single card
 * directly above the same row read as two things to do; a shortlist you swipe through reads as
 * what it is — a way to start something without hunting for it — so the day's own list stays whole.
 */
data class FocusAgenda(
    /**
     * The tasks worth starting next, best first — the row you swipe through. Drawn from [today]
     * before [overdue]: the day you are in outranks the one you already missed.
     */
    val upNext: List<FocusAgendaItem.TaskEntry> = emptyList(),
    val overdue: List<FocusAgendaItem> = emptyList(),
    val today: List<FocusAgendaItem> = emptyList(),
    val undatedTaskCount: Int = 0,
) {
    /** Up next is a view onto the sections, so counting it again would inflate the day. */
    private val allItems: List<FocusAgendaItem> get() = overdue + today

    val scheduledCount: Int get() = allItems.size
    val completedCount: Int get() = allItems.count { it.isCompleted }
    val isEmpty: Boolean get() = allItems.isEmpty()
}
