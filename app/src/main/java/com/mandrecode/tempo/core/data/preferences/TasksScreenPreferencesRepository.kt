package com.mandrecode.tempo.core.data.preferences

import com.mandrecode.tempo.features.tasks.presentation.model.SortOption

/**
 * Persists per-screen UI state for the Tasks screen.
 *
 * High-value view-state such as sorting and completed-section
 * expansion survives navigation, process death, and cold start.
 * Per-category sort options are stored so each category remembers
 * its own preference independently.
 */
interface TasksScreenPreferencesRepository {
    /** Returns the persisted [SortOption] for the given category, or the default. */
    fun getSortOption(categoryId: Long): SortOption

    /** Persists the [SortOption] for the given category. */
    fun setSortOption(
        categoryId: Long,
        sortOption: SortOption,
    )

    /** Returns the persisted completed-section expansion state. */
    fun getShowCompletedTasks(): Boolean

    /** Persists the completed-section expansion state. */
    fun setShowCompletedTasks(show: Boolean)

    /** Returns the persisted selected category ID, or the default (Inbox). */
    fun getSelectedCategoryId(): Long

    /** Persists the selected category ID. */
    fun setSelectedCategoryId(categoryId: Long)
}
