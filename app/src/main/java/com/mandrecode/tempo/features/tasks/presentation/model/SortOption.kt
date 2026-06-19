package com.mandrecode.tempo.features.tasks.presentation.model

import androidx.annotation.StringRes
import com.mandrecode.tempo.R

enum class SortOption(
    val value: String,
    @StringRes val labelResId: Int,
) {
    MANUAL("Manual", R.string.sort_option_manual),
    BY_DATE("Date", R.string.sort_option_date),
    BY_PRIORITY("Priority", R.string.sort_option_priority),
    BY_TITLE("Title", R.string.sort_option_title),
}
