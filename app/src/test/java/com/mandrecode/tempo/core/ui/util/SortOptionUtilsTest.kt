package com.mandrecode.tempo.core.ui.util

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.tasks.presentation.model.SortOption
import org.junit.Test

class SortOptionUtilsTest {
    @Test
    fun `manual sort option returns list icon`() {
        assertThat(getIconForSortOption(SortOption.MANUAL)).isEqualTo(R.drawable.ic_list)
    }

    @Test
    fun `by date sort option returns calendar icon`() {
        assertThat(getIconForSortOption(SortOption.BY_DATE)).isEqualTo(R.drawable.ic_calendar)
    }

    @Test
    fun `by priority sort option returns flag icon`() {
        assertThat(getIconForSortOption(SortOption.BY_PRIORITY)).isEqualTo(R.drawable.ic_flag)
    }

    @Test
    fun `by title sort option returns alpha icon`() {
        assertThat(getIconForSortOption(SortOption.BY_TITLE)).isEqualTo(R.drawable.ic_sort_by_alpha)
    }

    @Test
    fun `all sort options have unique icons`() {
        val icons = SortOption.entries.map { getIconForSortOption(it) }
        assertThat(icons.distinct()).hasSize(SortOption.entries.size)
    }
}
