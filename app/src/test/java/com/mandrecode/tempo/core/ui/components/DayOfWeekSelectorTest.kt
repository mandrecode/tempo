package com.mandrecode.tempo.core.ui.components

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.domain.model.DayOfWeek.FRIDAY
import com.mandrecode.tempo.core.domain.model.DayOfWeek.MONDAY
import com.mandrecode.tempo.core.domain.model.DayOfWeek.SUNDAY
import org.junit.Test

class DayOfWeekSelectorTest {
    @Test
    fun `an every-day habit shows all seven days on`() {
        assertThat(displayedRepeatDays(null, nullMeansEveryDay = true)).isEqualTo(DayOfWeek.ALL_DAYS)
    }

    @Test
    fun `a weekly task with no days picked shows none on`() {
        assertThat(displayedRepeatDays(null, nullMeansEveryDay = false)).isEmpty()
    }

    @Test
    fun `turning one day off an every-day habit keeps the other six`() {
        val toggle = toggledRepeatDays(null, SUNDAY, nullMeansEveryDay = true)

        assertThat(toggle).isEqualTo(RepeatDaysToggle.Changed(DayOfWeek.ALL_DAYS - SUNDAY))
    }

    @Test
    fun `turning the last day back on stores every day as null`() {
        val toggle = toggledRepeatDays(DayOfWeek.ALL_DAYS - SUNDAY, SUNDAY, nullMeansEveryDay = true)

        assertThat(toggle).isEqualTo(RepeatDaysToggle.Changed(null))
    }

    @Test
    fun `the last day of a habit cannot be turned off`() {
        assertThat(toggledRepeatDays(setOf(MONDAY), MONDAY, nullMeansEveryDay = true))
            .isEqualTo(RepeatDaysToggle.Refused)
    }

    @Test
    fun `a weekly task can clear its last day back to the default`() {
        assertThat(toggledRepeatDays(setOf(MONDAY), MONDAY, nullMeansEveryDay = false))
            .isEqualTo(RepeatDaysToggle.Changed(null))
    }

    @Test
    fun `picking a day adds it`() {
        assertThat(toggledRepeatDays(setOf(MONDAY), FRIDAY, nullMeansEveryDay = false))
            .isEqualTo(RepeatDaysToggle.Changed(setOf(MONDAY, FRIDAY)))
    }
}
