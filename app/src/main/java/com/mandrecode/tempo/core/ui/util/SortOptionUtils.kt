package com.mandrecode.tempo.core.ui.util

import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.tasks.presentation.model.SortOption

/**
 * Utility function to get the icon resource for a sort option.
 * Used by both FloatingActionButtons and SortBottomSheet.
 */
fun getIconForSortOption(option: SortOption): Int =
    when (option) {
        SortOption.MANUAL -> R.drawable.ic_list
        SortOption.BY_DATE -> R.drawable.ic_calendar
        SortOption.BY_PRIORITY -> R.drawable.ic_flag
        SortOption.BY_TITLE -> R.drawable.ic_sort_by_alpha
    }
