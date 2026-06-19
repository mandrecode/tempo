package com.mandrecode.tempo.features.routines.domain.util

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class ReminderDateUtilTest {
    // Wednesday 2026-03-04 12:00
    private val now = LocalDateTime(2026, 3, 4, 12, 0)

    @Test
    fun `empty repeat days returns from date unchanged`() {
        val from = LocalDateTime(2020, 1, 1, 10, 0)

        val result = ReminderDateUtil.calculateNextReminderDate(from, emptySet(), now)

        assertThat(result).isEqualTo(from)
    }

    @Test
    fun `from date already in the future returns next matching day after from`() {
        // Friday 2026-03-06 09:00 — already after now
        val from = LocalDateTime(2026, 3, 6, 9, 0)

        val result =
            ReminderDateUtil.calculateNextReminderDate(
                from,
                setOf(DayOfWeek.SATURDAY),
                now,
            )

        // Next Saturday after from is 2026-03-07
        assertThat(result).isEqualTo(LocalDateTime(2026, 3, 7, 9, 0))
    }

    @Test
    fun `from date in the past advances to next matching day after now`() {
        // Wednesday 2020-01-01 10:00
        val from = LocalDateTime(2020, 1, 1, 10, 0)

        val result =
            ReminderDateUtil.calculateNextReminderDate(
                from,
                setOf(DayOfWeek.FRIDAY),
                now,
            )

        // Next Friday after now (Wed 2026-03-04) is 2026-03-06
        assertThat(result).isEqualTo(LocalDateTime(2026, 3, 6, 10, 0))
    }

    @Test
    fun `preserves time of day from original reminder`() {
        val from = LocalDateTime(2020, 6, 15, 7, 30)

        val result =
            ReminderDateUtil.calculateNextReminderDate(
                from,
                setOf(DayOfWeek.MONDAY),
                now,
            )

        assertThat(result.hour).isEqualTo(7)
        assertThat(result.minute).isEqualTo(30)
    }

    @Test
    fun `picks earliest matching day when multiple repeat days`() {
        val from = LocalDateTime(2020, 1, 1, 10, 0)

        val result =
            ReminderDateUtil.calculateNextReminderDate(
                from,
                setOf(DayOfWeek.THURSDAY, DayOfWeek.SUNDAY),
                now,
            )

        // Now is Wed 2026-03-04, next Thursday is 2026-03-05 (before Sunday 2026-03-08)
        assertThat(result).isEqualTo(LocalDateTime(2026, 3, 5, 10, 0))
    }

    @Test
    fun `result lands on a day that is in repeat days`() {
        val from = LocalDateTime(2020, 1, 1, 10, 0)
        val repeatDays = setOf(DayOfWeek.TUESDAY, DayOfWeek.SATURDAY)

        val result = ReminderDateUtil.calculateNextReminderDate(from, repeatDays, now)

        val resultDayOfWeek = DayOfWeek.fromKotlinDayOfWeek(result.dayOfWeek)
        assertThat(repeatDays).contains(resultDayOfWeek)
    }

    @Test
    fun `result is strictly after now`() {
        val from = LocalDateTime(2020, 1, 1, 10, 0)

        val result =
            ReminderDateUtil.calculateNextReminderDate(
                from,
                setOf(DayOfWeek.WEDNESDAY),
                now,
            )

        assertThat(result > now).isTrue()
        // Now is Wed 2026-03-04 12:00, so next Wednesday is 2026-03-11
        assertThat(result).isEqualTo(LocalDateTime(2026, 3, 11, 10, 0))
    }

    @Test
    fun `boundary - same day as now but earlier time skips to next week`() {
        // now is Wed 2026-03-04 12:00, from is same day but 8:00
        val from = LocalDateTime(2026, 3, 4, 8, 0)

        val result =
            ReminderDateUtil.calculateNextReminderDate(
                from,
                setOf(DayOfWeek.WEDNESDAY),
                now,
            )

        // 2026-03-04 08:00 < now, so must advance to next Wed 2026-03-11
        assertThat(result).isEqualTo(LocalDateTime(2026, 3, 11, 8, 0))
    }

    @Test
    fun `boundary - same day as now but later time still skips to next week`() {
        // from is Wed 2026-03-04 14:00 (after now 12:00), but the loop starts from from+1 day
        val from = LocalDateTime(2026, 3, 4, 14, 0)

        val result =
            ReminderDateUtil.calculateNextReminderDate(
                from,
                setOf(DayOfWeek.WEDNESDAY),
                now,
            )

        // The loop increments from before checking, so next Wed is 2026-03-11
        assertThat(result).isEqualTo(LocalDateTime(2026, 3, 11, 14, 0))
    }

    @Test
    fun `every day repeat finds tomorrow`() {
        val from = LocalDateTime(2020, 1, 1, 10, 0)
        val allDays =
            setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY,
            )

        val result = ReminderDateUtil.calculateNextReminderDate(from, allDays, now)

        // Now is Wed 2026-03-04 12:00, next day matching all days is Thu 2026-03-05
        assertThat(result).isEqualTo(LocalDateTime(2026, 3, 5, 10, 0))
    }
}
