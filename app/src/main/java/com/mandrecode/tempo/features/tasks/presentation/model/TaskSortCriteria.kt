package com.mandrecode.tempo.features.tasks.presentation.model

import com.mandrecode.tempo.features.tasks.domain.model.Task

/**
 * A single criterion a [SortOption] applies before falling back to the user's manual order.
 *
 * Selectors return `null` for "unset" (no reminder date, no priority). Unset always sorts last,
 * so an undated task never outranks a dated one.
 */
enum class TaskSortCriterion(
    private val selector: (Task) -> Comparable<*>?,
) {
    REMINDER_DATE({ it.reminderDate }),
    PRIORITY({ it.priority?.sortOrder }),
    TITLE({ it.title.lowercase() }),
    ;

    fun compare(
        first: Task,
        second: Task,
    ): Int {
        val firstValue = selector(first)
        val secondValue = selector(second)
        return when {
            firstValue == null && secondValue == null -> 0
            firstValue == null -> 1
            secondValue == null -> -1
            else -> compareValues(firstValue, secondValue)
        }
    }
}

/**
 * The single source of truth for how each [SortOption] orders tasks.
 *
 * Both the comparator used to sort the list and the tie test used to decide which tasks the user
 * may hand-reorder are derived from the same criteria list, so they cannot drift apart — a
 * criterion the comparator applies but the tie test forgets would let a task be dragged past one
 * the sort can actually distinguish it from, and the next emission would silently undo the drag.
 */
object TaskSortCriteria {
    /** The criteria applied before manual order. Empty for [SortOption.MANUAL] — it *is* manual order. */
    fun criteriaFor(sortOption: SortOption): List<TaskSortCriterion> =
        when (sortOption) {
            SortOption.BY_DATE -> listOf(TaskSortCriterion.REMINDER_DATE, TaskSortCriterion.PRIORITY)
            SortOption.BY_PRIORITY -> listOf(TaskSortCriterion.PRIORITY, TaskSortCriterion.REMINDER_DATE)
            SortOption.BY_TITLE -> listOf(TaskSortCriterion.TITLE)
            SortOption.MANUAL -> emptyList()
        }

    /**
     * Total order for [sortOption]: its criteria, then the user's manual order, then id so that
     * the result is reproducible across reloads even for tasks that are equal in every respect.
     */
    fun comparator(sortOption: SortOption): Comparator<Task> {
        val criteria = criteriaFor(sortOption)
        return Comparator { first, second ->
            criteria.firstNotNullOfOrNull { criterion ->
                criterion.compare(first, second).takeIf { it != 0 }
            } ?: compareValuesBy(first, second, { it.sortOrder }, { it.id })
        }
    }

    /**
     * Whether [sortOption] has no criterion left to tell these two tasks apart, so their relative
     * order is the user's to decide.
     */
    fun areTied(
        first: Task,
        second: Task,
        sortOption: SortOption,
    ): Boolean = criteriaFor(sortOption).all { it.compare(first, second) == 0 }
}
