package com.mandrecode.tempo.features.focus.domain.model

import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.datetime.LocalTime

/**
 * One row of the Focus agenda. Tasks, habits and chains sit in the same list rather than in
 * per-type sections, so the ordering below is what the user actually reads down.
 */
sealed interface FocusAgendaItem {
    val id: String
    val isCompleted: Boolean

    /** When the item is due today, or `null` for something with no time of its own. */
    val dueTime: LocalTime?

    /** Only tasks carry a priority; everything else ranks as unprioritised. */
    val priority: Priority?

    data class TaskEntry(
        val task: Task,
        val subtasks: List<Task> = emptyList(),
    ) : FocusAgendaItem {
        override val id: String = "task_${task.id}"
        override val isCompleted: Boolean = task.isCompleted
        override val dueTime: LocalTime? = task.reminderDate?.time
        override val priority: Priority? = task.priority
    }

    data class HabitEntry(
        val habit: Habit,
        override val isCompleted: Boolean,
    ) : FocusAgendaItem {
        override val id: String = "habit_${habit.id}"
        override val dueTime: LocalTime? = habit.reminderDate?.time
        override val priority: Priority? = null
    }

    data class ChainEntry(
        val chain: HabitChain,
        val habits: List<Habit>,
        override val isCompleted: Boolean,
    ) : FocusAgendaItem {
        override val id: String = "chain_${chain.id}"
        override val dueTime: LocalTime? = chain.periodicReminder?.time
        override val priority: Priority? = null
    }
}

/**
 * Everything the Focus screen shows, already ordered.
 *
 * [upNext] is drawn from [overdue] and [today] rather than removed from them: the card is a
 * spotlight on one item, not a fourth section, so the item still appears in its own section below.
 */
data class FocusAgenda(
    val upNext: FocusAgendaItem? = null,
    val overdue: List<FocusAgendaItem> = emptyList(),
    val today: List<FocusAgendaItem> = emptyList(),
    val undatedTaskCount: Int = 0,
) {
    val scheduledCount: Int get() = overdue.size + today.size
    val completedCount: Int get() = overdue.count { it.isCompleted } + today.count { it.isCompleted }
    val isEmpty: Boolean get() = overdue.isEmpty() && today.isEmpty()
}
