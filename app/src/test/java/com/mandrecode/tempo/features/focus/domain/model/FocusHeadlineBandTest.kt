package com.mandrecode.tempo.features.focus.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FocusHeadlineBandTest {
    @Test
    fun `nothing scheduled has its own band rather than reading as complete`() {
        assertThat(FocusHeadlineBand.resolve(scheduledCount = 0, completedCount = 0))
            .isEqualTo(FocusHeadlineBand.NOTHING_SCHEDULED)
    }

    @Test
    fun `nothing done yet is the first band`() {
        assertThat(FocusHeadlineBand.resolve(scheduledCount = 9, completedCount = 0))
            .isEqualTo(FocusHeadlineBand.JUST_STARTED)
    }

    @Test
    fun `just under a third is still the first band`() {
        // 2/9 ≈ 0.22
        assertThat(FocusHeadlineBand.resolve(scheduledCount = 9, completedCount = 2))
            .isEqualTo(FocusHeadlineBand.JUST_STARTED)
    }

    @Test
    fun `exactly a third crosses into the second band`() {
        assertThat(FocusHeadlineBand.resolve(scheduledCount = 9, completedCount = 3))
            .isEqualTo(FocusHeadlineBand.UNDER_WAY)
    }

    @Test
    fun `just under two thirds is still the second band`() {
        // 5/9 ≈ 0.56
        assertThat(FocusHeadlineBand.resolve(scheduledCount = 9, completedCount = 5))
            .isEqualTo(FocusHeadlineBand.UNDER_WAY)
    }

    @Test
    fun `exactly two thirds crosses into the third band`() {
        assertThat(FocusHeadlineBand.resolve(scheduledCount = 9, completedCount = 6))
            .isEqualTo(FocusHeadlineBand.NEARLY_THERE)
    }

    @Test
    fun `one item left is the third band, never complete`() {
        assertThat(FocusHeadlineBand.resolve(scheduledCount = 9, completedCount = 8))
            .isEqualTo(FocusHeadlineBand.NEARLY_THERE)
    }

    @Test
    fun `everything done is the final band`() {
        assertThat(FocusHeadlineBand.resolve(scheduledCount = 9, completedCount = 9))
            .isEqualTo(FocusHeadlineBand.COMPLETE)
    }

    @Test
    fun `completing more than was scheduled still reads as complete`() {
        assertThat(FocusHeadlineBand.resolve(scheduledCount = 2, completedCount = 5))
            .isEqualTo(FocusHeadlineBand.COMPLETE)
    }

    @Test
    fun `a single scheduled item goes straight from just started to complete`() {
        assertThat(FocusHeadlineBand.resolve(scheduledCount = 1, completedCount = 0))
            .isEqualTo(FocusHeadlineBand.JUST_STARTED)
        assertThat(FocusHeadlineBand.resolve(scheduledCount = 1, completedCount = 1))
            .isEqualTo(FocusHeadlineBand.COMPLETE)
    }
}
