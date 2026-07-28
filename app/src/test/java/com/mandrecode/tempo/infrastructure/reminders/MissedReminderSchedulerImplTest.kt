package com.mandrecode.tempo.infrastructure.reminders

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.tasks.domain.repository.MissedReminderPreferences
import com.mandrecode.tempo.infrastructure.reminders.scheduler.MissedReminderAlarmScheduler
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

class MissedReminderSchedulerImplTest {
    private val enabled = MutableStateFlow(true)
    private val catchUpTime = MutableStateFlow(LocalTime(hour = 9, minute = 0))
    private val alarmScheduler: MissedReminderAlarmScheduler = mockk(relaxed = true)

    private val preferences =
        object : MissedReminderPreferences {
            override val isEnabled = enabled
            override val catchUpTime = this@MissedReminderSchedulerImplTest.catchUpTime

            override fun setEnabled(enabled: Boolean) = Unit

            override fun setCatchUpTime(time: LocalTime) = Unit
        }

    @Test
    fun `cancels the catch-up when disabled`() {
        enabled.value = false
        scheduler(at = LocalDateTime(2026, 7, 28, 6, 0)).sync()

        verify(exactly = 1) { alarmScheduler.cancelCatchUp() }
        verify(exactly = 0) { alarmScheduler.scheduleCatchUp(any()) }
    }

    @Test
    fun `arms today when the catch-up time is still ahead`() {
        scheduler(at = LocalDateTime(2026, 7, 28, 6, 0)).sync()

        verify { alarmScheduler.scheduleCatchUp(epochMillisOf(LocalDateTime(2026, 7, 28, 9, 0))) }
    }

    @Test
    fun `arms tomorrow when the catch-up time already passed today`() {
        scheduler(at = LocalDateTime(2026, 7, 28, 9, 1)).sync()

        verify { alarmScheduler.scheduleCatchUp(epochMillisOf(LocalDateTime(2026, 7, 29, 9, 0))) }
    }

    @Test
    fun `arms tomorrow when invoked exactly at the catch-up time`() {
        scheduler(at = LocalDateTime(2026, 7, 28, 9, 0)).sync()

        verify { alarmScheduler.scheduleCatchUp(epochMillisOf(LocalDateTime(2026, 7, 29, 9, 0))) }
    }

    @Test
    fun `rolls over month and year boundaries`() {
        scheduler(at = LocalDateTime(2026, 12, 31, 23, 30)).sync()

        verify { alarmScheduler.scheduleCatchUp(epochMillisOf(LocalDateTime(2027, 1, 1, 9, 0))) }
    }

    @Test
    fun `honours a changed catch-up time`() {
        catchUpTime.value = LocalTime(hour = 7, minute = 15)
        scheduler(at = LocalDateTime(2026, 7, 28, 6, 0)).sync()

        verify { alarmScheduler.scheduleCatchUp(epochMillisOf(LocalDateTime(2026, 7, 28, 7, 15))) }
    }

    @Test
    fun `repeated sync arms the same single trigger`() {
        val scheduler = scheduler(at = LocalDateTime(2026, 7, 28, 6, 0))

        scheduler.sync()
        scheduler.sync()

        verify(exactly = 2) { alarmScheduler.scheduleCatchUp(epochMillisOf(LocalDateTime(2026, 7, 28, 9, 0))) }
        verify(exactly = 0) { alarmScheduler.cancelCatchUp() }
    }

    @Test
    fun `defers exact-vs-inexact alarm choice to the alarm scheduler`() {
        scheduler(at = LocalDateTime(2026, 7, 28, 6, 0)).sync()

        // The permission check belongs to the alarm scheduler, so arming must not consult it here.
        verify(exactly = 0) { alarmScheduler.canScheduleExactAlarms() }
    }

    @Test
    fun `arming uses an instant in the future`() {
        val now = LocalDateTime(2026, 7, 28, 6, 0)

        scheduler(at = now).sync()

        val expected = epochMillisOf(LocalDateTime(2026, 7, 28, 9, 0))
        assertThat(expected).isGreaterThan(epochMillisOf(now))
    }

    private fun scheduler(at: LocalDateTime): MissedReminderSchedulerImpl =
        MissedReminderSchedulerImpl(preferences, alarmScheduler, fixedClock(at))

    private fun fixedClock(at: LocalDateTime): Clock =
        object : Clock {
            override fun now(): Instant = at.toInstant(TimeZone.currentSystemDefault())
        }

    private fun epochMillisOf(dateTime: LocalDateTime): Long =
        dateTime
            .toInstant(TimeZone.currentSystemDefault())
            .toEpochMilliseconds()
}
