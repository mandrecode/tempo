package com.mandrecode.tempo.infrastructure.reminders.scheduler

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.infrastructure.notifications.NotificationSyncManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Before
import org.junit.Test

class HabitReminderSchedulerImplTest {
    private lateinit var context: Context
    private lateinit var notificationSyncManager: NotificationSyncManager
    private lateinit var habitAlarmScheduler: HabitAlarmScheduler
    private lateinit var scheduler: HabitReminderSchedulerImpl

    private val fixedNowMillis = 1_780_000_000_000L

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        notificationSyncManager = mockk(relaxed = true)
        habitAlarmScheduler = mockk(relaxed = true)

        every { context.getString(R.string.error_habit_no_reminder_date) } returns "habit reminder missing"
        every { context.getString(R.string.error_habit_chain_no_reminder_date) } returns "chain reminder missing"
        every { context.getString(R.string.error_exact_alarm_permission) } returns "exact alarm permission"
        every { context.getString(R.string.error_security_exception) } returns "security"
        every { habitAlarmScheduler.canScheduleExactAlarms() } returns true

        scheduler =
            HabitReminderSchedulerImpl(
                context = context,
                notificationSyncManager = notificationSyncManager,
                habitAlarmScheduler = habitAlarmScheduler,
                currentTimeMillisProvider = { fixedNowMillis },
            )
    }

    @Test
    fun `scheduleHabit returns failure when no reminder date`() =
        runTest {
            val habit =
                Habit(
                    id = 1L,
                    title = "Test Habit",
                    description = "Test Description",
                    reminderDate = null,
                    createdDate = LocalDateTime(2024, 1, 1, 0, 0),
                )

            val result = scheduler.scheduleHabit(habit)

            assertThat(result).isEqualTo(ScheduleResult.Failure("habit reminder missing"))
            verify(exactly = 0) { habitAlarmScheduler.scheduleHabitReminder(any(), any()) }
        }

    @Test
    fun `scheduleHabit delegates to alarm scheduler for future reminders`() =
        runTest {
            val reminderDate = LocalDateTime(2030, 1, 20, 8, 0)
            val habit =
                Habit(
                    id = 99L,
                    title = "Hydrate",
                    description = "Drink water",
                    reminderDate = reminderDate,
                    createdDate = LocalDateTime(2024, 1, 1, 0, 0),
                )
            val expectedTriggerAtMillis =
                reminderDate.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

            val result = scheduler.scheduleHabit(habit)

            assertThat(result).isEqualTo(ScheduleResult.Success(reminderDate))
            verify(exactly = 1) { habitAlarmScheduler.scheduleHabitReminder(99L, expectedTriggerAtMillis) }
        }

    @Test
    fun `scheduleHabit skips completed habits`() =
        runTest {
            val habit =
                Habit(
                    id = 77L,
                    title = "Done",
                    description = "Already completed today",
                    reminderDate = LocalDateTime(2030, 1, 20, 8, 0),
                    isCompleted = true,
                    createdDate = LocalDateTime(2024, 1, 1, 0, 0),
                )

            val result = scheduler.scheduleHabit(habit)

            assertThat(result).isEqualTo(ScheduleResult.Skipped)
            verify(exactly = 0) { habitAlarmScheduler.scheduleHabitReminder(any(), any()) }
        }

    @Test
    fun `scheduleHabitChain delegates to alarm scheduler for future reminders`() =
        runTest {
            val reminderDate = LocalDateTime(2030, 3, 10, 7, 30)
            val habitChain =
                HabitChain(
                    id = 31L,
                    title = "Morning chain",
                    description = "Wake up flow",
                    periodicReminder = reminderDate,
                    createdDate = LocalDateTime(2024, 1, 1, 0, 0),
                )
            val expectedTriggerAtMillis =
                reminderDate.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

            val result = scheduler.scheduleHabitChain(habitChain)

            assertThat(result).isEqualTo(ScheduleResult.Success(reminderDate))
            verify(exactly = 1) { habitAlarmScheduler.scheduleHabitChainReminder(31L, expectedTriggerAtMillis) }
        }

    @Test
    fun `cancelHabit delegates cancellation and notification dismissal`() =
        runTest {
            val habit =
                Habit(
                    id = 5L,
                    title = "Read",
                    description = "20 minutes",
                    reminderDate = LocalDateTime(2030, 3, 10, 7, 30),
                    createdDate = LocalDateTime(2024, 1, 1, 0, 0),
                )

            scheduler.cancelHabit(habit)

            verify(exactly = 1) { habitAlarmScheduler.cancelHabitReminder(5L) }
            verify(exactly = 1) { notificationSyncManager.dismissHabitNotification(5L) }
        }

    @Test
    fun `cancelHabitChain delegates cancellation and notification dismissal`() =
        runTest {
            val chain =
                HabitChain(
                    id = 8L,
                    title = "Evening chain",
                    description = "Wind down",
                    periodicReminder = LocalDateTime(2030, 3, 10, 7, 30),
                    createdDate = LocalDateTime(2024, 1, 1, 0, 0),
                )

            scheduler.cancelHabitChain(chain)

            verify(exactly = 1) { habitAlarmScheduler.cancelHabitChainReminder(8L) }
            verify(exactly = 1) { notificationSyncManager.dismissHabitChainNotification(8L) }
        }

    @Test
    fun `scheduleHabit returns permission error when exact alarms are unavailable`() =
        runTest {
            every { habitAlarmScheduler.canScheduleExactAlarms() } returns false
            val habit =
                Habit(
                    id = 1L,
                    title = "Test Habit",
                    description = "Test Description",
                    reminderDate = LocalDateTime(2030, 1, 20, 8, 0),
                    createdDate = LocalDateTime(2024, 1, 1, 0, 0),
                )

            val result = scheduler.scheduleHabit(habit)

            assertThat(result).isEqualTo(ScheduleResult.PermissionError("exact alarm permission"))
            verify(exactly = 0) { habitAlarmScheduler.scheduleHabitReminder(any(), any()) }
        }
}
