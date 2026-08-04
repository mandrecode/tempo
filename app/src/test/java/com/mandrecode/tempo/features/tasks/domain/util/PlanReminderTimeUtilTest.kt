package com.mandrecode.tempo.features.tasks.domain.util

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class PlanReminderTimeUtilTest {
    private val today = LocalDate(2026, 8, 3)
    private val tomorrow = LocalDate(2026, 8, 4)

    @Test
    fun `a future day gets the default morning hour`() {
        val resolved = PlanReminderTimeUtil.resolve(tomorrow, now = LocalDateTime(2026, 8, 3, 14, 30))

        assertThat(resolved).isEqualTo(LocalDateTime(2026, 8, 4, 9, 0))
    }

    @Test
    fun `today before the default hour still gets the default hour`() {
        val resolved = PlanReminderTimeUtil.resolve(today, now = LocalDateTime(2026, 8, 3, 6, 15))

        assertThat(resolved).isEqualTo(LocalDateTime(2026, 8, 3, 9, 0))
    }

    @Test
    fun `today after the default hour gets the next whole hour instead`() {
        val resolved = PlanReminderTimeUtil.resolve(today, now = LocalDateTime(2026, 8, 3, 14, 30))

        assertThat(resolved).isEqualTo(LocalDateTime(2026, 8, 3, 15, 0))
    }

    @Test
    fun `the default hour exactly reached counts as passed`() {
        val resolved = PlanReminderTimeUtil.resolve(today, now = LocalDateTime(2026, 8, 3, 9, 0))

        assertThat(resolved).isEqualTo(LocalDateTime(2026, 8, 3, 10, 0))
    }

    @Test
    fun `late at night the reminder is clamped to the end of the chosen day`() {
        val resolved = PlanReminderTimeUtil.resolve(today, now = LocalDateTime(2026, 8, 3, 23, 30))

        assertThat(resolved).isEqualTo(LocalDateTime(2026, 8, 3, 23, 59))
    }

    /**
     * The one case with no good answer. Inside the last minute of the day there is no time left to
     * put a reminder on, so the task keeps the day it was given and the reminder is what gives way
     * — deliberately, rather than by moving the task to a day nobody asked for.
     */
    @Test
    fun `inside the last minute of the day the task keeps the day and loses the reminder`() {
        val secondsBeforeMidnight = LocalDateTime(2026, 8, 3, 23, 59, 30)

        val resolved = PlanReminderTimeUtil.resolve(today, now = secondsBeforeMidnight)

        assertThat(resolved.date).isEqualTo(today)
        assertThat(resolved).isEqualTo(LocalDateTime(2026, 8, 3, 23, 59))
        // Behind `now`, so UpdateTaskUseCase will skip scheduling rather than arm a past alarm.
        assertThat(resolved).isLessThan(secondsBeforeMidnight)
    }

    @Test
    fun `planning today never spills into tomorrow`() {
        val resolved = PlanReminderTimeUtil.resolve(today, now = LocalDateTime(2026, 8, 3, 23, 5))

        assertThat(resolved.date).isEqualTo(today)
    }

    @Test
    fun `tomorrow is unaffected by how late today is`() {
        val resolved = PlanReminderTimeUtil.resolve(tomorrow, now = LocalDateTime(2026, 8, 3, 23, 45))

        assertThat(resolved).isEqualTo(LocalDateTime(2026, 8, 4, 9, 0))
    }
}
