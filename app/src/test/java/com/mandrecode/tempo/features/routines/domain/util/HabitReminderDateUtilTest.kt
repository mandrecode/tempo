package com.mandrecode.tempo.features.routines.domain.util

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import kotlin.time.Clock

class HabitReminderDateUtilTest {
    @Test
    fun `null reminder returns null`() {
        val result = HabitReminderDateUtil.advanceReminderIfNeeded(null, setOf(DayOfWeek.MONDAY))

        assertThat(result).isNull()
    }

    @Test
    fun `future reminder on matching day is returned unchanged`() {
        // 2099-06-15 is a Monday
        val future = LocalDateTime(2099, 6, 15, 10, 0)

        val result = HabitReminderDateUtil.advanceReminderIfNeeded(future, setOf(DayOfWeek.MONDAY))

        assertThat(result).isEqualTo(future)
    }

    @Test
    fun `future reminder on non-matching day advances to next matching day`() {
        // 2099-06-15 is a Monday, but habit only repeats on Sunday
        val future = LocalDateTime(2099, 6, 15, 10, 0)

        val result = HabitReminderDateUtil.advanceReminderIfNeeded(future, setOf(DayOfWeek.SUNDAY))

        assertThat(result).isNotNull()
        // Should advance to Sunday 2099-06-21
        assertThat(result).isEqualTo(LocalDateTime(2099, 6, 21, 10, 0))
    }

    @Test
    fun `future reminder on non-matching day preserves time`() {
        // 2099-06-15 is a Monday, habit repeats on Friday
        val future = LocalDateTime(2099, 6, 15, 14, 30)

        val result = HabitReminderDateUtil.advanceReminderIfNeeded(future, setOf(DayOfWeek.FRIDAY))

        assertThat(result).isNotNull()
        assertThat(result!!.hour).isEqualTo(14)
        assertThat(result.minute).isEqualTo(30)
        val resultDay = DayOfWeek.fromKotlinDayOfWeek(result.dayOfWeek)
        assertThat(resultDay).isEqualTo(DayOfWeek.FRIDAY)
    }

    @Test
    fun `past reminder with repeat days advances to future`() {
        val past = LocalDateTime(2020, 1, 1, 10, 0)
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        val result = HabitReminderDateUtil.advanceReminderIfNeeded(past, setOf(DayOfWeek.WEDNESDAY))

        assertThat(result).isNotNull()
        assertThat(result!! > now).isTrue()
    }

    @Test
    fun `past reminder preserves hour and minute`() {
        val past = LocalDateTime(2020, 3, 10, 14, 30)

        val result = HabitReminderDateUtil.advanceReminderIfNeeded(past, setOf(DayOfWeek.MONDAY))

        assertThat(result).isNotNull()
        assertThat(result!!.hour).isEqualTo(14)
        assertThat(result.minute).isEqualTo(30)
    }

    @Test
    fun `past reminder lands on a day in repeat days`() {
        val past = LocalDateTime(2020, 1, 1, 10, 0)
        val targetDays = setOf(DayOfWeek.TUESDAY, DayOfWeek.FRIDAY)

        val result = HabitReminderDateUtil.advanceReminderIfNeeded(past, targetDays)

        val resultDay = DayOfWeek.fromKotlinDayOfWeek(result!!.dayOfWeek)
        assertThat(targetDays).contains(resultDay)
    }

    @Test
    fun `null repeat days defaults to daily and advances`() {
        val past = LocalDateTime(2020, 1, 1, 10, 0)
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        val result = HabitReminderDateUtil.advanceReminderIfNeeded(past, null)

        assertThat(result).isNotNull()
        assertThat(result!! > now).isTrue()
    }

    @Test
    fun `empty repeat days defaults to daily and advances`() {
        val past = LocalDateTime(2020, 1, 1, 10, 0)
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

        val result = HabitReminderDateUtil.advanceReminderIfNeeded(past, emptySet())

        assertThat(result).isNotNull()
        assertThat(result!! > now).isTrue()
    }

    // --- nextUpcomingTime ---

    @Test
    fun `nextUpcomingTime returns today when now is before target time`() {
        val now = LocalDateTime(2025, 6, 15, 12, 0)

        val result = HabitReminderDateUtil.nextUpcomingTime(hour = 21, minute = 0, now = now)

        assertThat(result).isEqualTo(LocalDateTime(2025, 6, 15, 21, 0))
    }

    @Test
    fun `nextUpcomingTime returns tomorrow when now is after target time`() {
        val now = LocalDateTime(2025, 6, 15, 22, 30)

        val result = HabitReminderDateUtil.nextUpcomingTime(hour = 21, minute = 0, now = now)

        assertThat(result).isEqualTo(LocalDateTime(2025, 6, 16, 21, 0))
    }

    @Test
    fun `nextUpcomingTime returns tomorrow when now equals target time`() {
        val now = LocalDateTime(2025, 6, 15, 21, 0)

        val result = HabitReminderDateUtil.nextUpcomingTime(hour = 21, minute = 0, now = now)

        assertThat(result).isEqualTo(LocalDateTime(2025, 6, 16, 21, 0))
    }
}
