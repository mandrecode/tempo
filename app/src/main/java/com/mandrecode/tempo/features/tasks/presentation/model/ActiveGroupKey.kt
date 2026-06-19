package com.mandrecode.tempo.features.tasks.presentation.model

import com.mandrecode.tempo.core.domain.model.Priority
import kotlinx.datetime.LocalDate

/**
 * Represents the grouping key for active (uncompleted) tasks.
 *
 * The key type changes based on the active [SortOption]:
 * - [ByDate]     – groups by reminder date
 * - [ByPriority] – groups by priority level
 * - [ByTitle]    – flat list sorted alphabetically (no grouping)
 * - [Flat]       – no grouping (manual sorting)
 */
sealed interface ActiveGroupKey {
    /** Bucket for all active tasks whose reminder date is in the past. */
    data object Overdue : ActiveGroupKey

    /** Groups active tasks by their reminder date. */
    data class ByDate(
        val date: LocalDate?,
    ) : ActiveGroupKey

    /** Groups active tasks by their priority level. */
    data class ByPriority(
        val priority: Priority?,
    ) : ActiveGroupKey

    /** Alphabetically sorted flat list. */
    data object ByTitle : ActiveGroupKey

    /** No grouping – all active tasks in a single flat list. */
    data object Flat : ActiveGroupKey
}
