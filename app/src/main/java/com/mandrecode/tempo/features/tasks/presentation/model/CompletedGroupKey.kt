package com.mandrecode.tempo.features.tasks.presentation.model

import com.mandrecode.tempo.core.domain.model.Priority
import kotlinx.datetime.LocalDate

/**
 * Represents the grouping key for completed tasks.
 *
 * The key type changes based on the active [SortOption]:
 * - [ByDate]     – groups by completion date (default for date sorting)
 * - [ByPriority] – groups by priority level (for priority sorting)
 * - [ByTitle]    – flat list sorted alphabetically (for title sorting)
 * - [Flat]       – no grouping (for manual sorting)
 */
sealed interface CompletedGroupKey {
    /** Groups completed tasks by the date they were completed. */
    data class ByDate(
        val date: LocalDate?,
    ) : CompletedGroupKey

    /** Groups completed tasks by their priority level. */
    data class ByPriority(
        val priority: Priority?,
    ) : CompletedGroupKey

    /** Alphabetically sorted flat list. */
    data object ByTitle : CompletedGroupKey

    /** No grouping – all completed tasks in a single flat list. */
    data object Flat : CompletedGroupKey
}
