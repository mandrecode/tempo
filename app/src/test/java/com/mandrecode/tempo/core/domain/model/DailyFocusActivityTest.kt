package com.mandrecode.tempo.core.domain.model

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.LocalDate
import org.junit.Test

class DailyFocusActivityTest {
    private val date = LocalDate(2026, 7, 29)

    private fun activity(
        scheduled: Int,
        completed: Int,
    ) = DailyFocusActivity(date = date, scheduledCount = scheduled, completedCount = completed)

    @Test
    fun `nothing scheduled reads as quiet`() {
        assertThat(activity(scheduled = 0, completed = 0).state).isEqualTo(FocusDayState.QUIET)
    }

    @Test
    fun `nothing completed reads as quiet`() {
        assertThat(activity(scheduled = 4, completed = 0).state).isEqualTo(FocusDayState.QUIET)
    }

    @Test
    fun `a scheduled day with nothing done is indistinguishable from an unscheduled one`() {
        assertThat(activity(scheduled = 4, completed = 0).state)
            .isEqualTo(activity(scheduled = 0, completed = 0).state)
    }

    @Test
    fun `partial completion reads as some`() {
        assertThat(activity(scheduled = 4, completed = 1).state).isEqualTo(FocusDayState.SOME)
        assertThat(activity(scheduled = 4, completed = 3).state).isEqualTo(FocusDayState.SOME)
    }

    @Test
    fun `full completion reads as all`() {
        assertThat(activity(scheduled = 4, completed = 4).state).isEqualTo(FocusDayState.ALL)
    }

    @Test
    fun `completing more than was scheduled still reads as all`() {
        // Possible when work is completed and then deleted during the day.
        assertThat(activity(scheduled = 2, completed = 5).state).isEqualTo(FocusDayState.ALL)
    }

    @Test
    fun `hasActivity tracks completions rather than state`() {
        assertThat(activity(scheduled = 4, completed = 1).hasActivity).isTrue()
        assertThat(activity(scheduled = 4, completed = 0).hasActivity).isFalse()
    }

    @Test
    fun `isUnscheduled distinguishes the two quiet days`() {
        assertThat(activity(scheduled = 0, completed = 0).isUnscheduled).isTrue()
        assertThat(activity(scheduled = 4, completed = 0).isUnscheduled).isFalse()
    }
}
