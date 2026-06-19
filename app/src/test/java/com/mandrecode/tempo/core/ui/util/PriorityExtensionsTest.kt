package com.mandrecode.tempo.core.ui.util

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.Priority
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
    fun `each priority has a distinct color`() {
        val colors = Priority.entries.map { it.color }
        assertThat(colors.distinct()).hasSize(Priority.entries.size)
    }

    @Test
    fun `high priority color is red-ish`() {
        val color = Priority.HIGH.color
        assertThat(color.red).isGreaterThan(0.9f)
    }

    @Test
    fun `low priority color is green-ish`() {
        val color = Priority.LOW.color
        assertThat(color.green).isGreaterThan(0.8f)
    }
}
