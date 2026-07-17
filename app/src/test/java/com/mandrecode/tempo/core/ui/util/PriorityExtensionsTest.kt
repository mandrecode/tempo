package com.mandrecode.tempo.core.ui.util

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.core.ui.theme.PastelGreenDark
import com.mandrecode.tempo.core.ui.theme.PastelGreenLight
import com.mandrecode.tempo.core.ui.theme.PastelRedDark
import com.mandrecode.tempo.core.ui.theme.PastelRedLight
import com.mandrecode.tempo.core.ui.theme.PastelYellowDark
import com.mandrecode.tempo.core.ui.theme.PastelYellowLight
import org.junit.Test

class PriorityExtensionsTest {
    @Test
    fun `high priority returns correct titleResId`() {
        assertThat(Priority.HIGH.titleResId).isEqualTo(R.string.priority_high)
    }

    @Test
    fun `medium priority returns correct titleResId`() {
        assertThat(Priority.MEDIUM.titleResId).isEqualTo(R.string.priority_medium)
    }

    @Test
    fun `low priority returns correct titleResId`() {
        assertThat(Priority.LOW.titleResId).isEqualTo(R.string.priority_low)
    }

    @Test
    fun `each priority has a distinct color in light theme`() {
        val colors = Priority.entries.map { priorityColor(it, isDarkTheme = false) }
        assertThat(colors.distinct()).hasSize(Priority.entries.size)
    }

    @Test
    fun `each priority has a distinct color in dark theme`() {
        val colors = Priority.entries.map { priorityColor(it, isDarkTheme = true) }
        assertThat(colors.distinct()).hasSize(Priority.entries.size)
    }

    @Test
    fun `high priority uses the red tonal palette in light theme`() {
        assertThat(priorityColor(Priority.HIGH, isDarkTheme = false)).isEqualTo(PastelRedLight)
    }

    @Test
    fun `high priority uses the red tonal palette in dark theme`() {
        assertThat(priorityColor(Priority.HIGH, isDarkTheme = true)).isEqualTo(PastelRedDark)
    }

    @Test
    fun `medium priority uses the yellow tonal palette in light theme`() {
        assertThat(priorityColor(Priority.MEDIUM, isDarkTheme = false)).isEqualTo(PastelYellowLight)
    }

    @Test
    fun `medium priority uses the yellow tonal palette in dark theme`() {
        assertThat(priorityColor(Priority.MEDIUM, isDarkTheme = true)).isEqualTo(PastelYellowDark)
    }

    @Test
    fun `low priority uses the green tonal palette in light theme`() {
        assertThat(priorityColor(Priority.LOW, isDarkTheme = false)).isEqualTo(PastelGreenLight)
    }

    @Test
    fun `low priority uses the green tonal palette in dark theme`() {
        assertThat(priorityColor(Priority.LOW, isDarkTheme = true)).isEqualTo(PastelGreenDark)
    }
}
