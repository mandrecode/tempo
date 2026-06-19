package com.mandrecode.tempo.infrastructure.reminders.scheduler

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.infrastructure.notifications.NotificationSyncManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Before
import org.junit.Test

class TaskReminderSchedulerImplTest {
    private lateinit var context: Context
    private lateinit var notificationSyncManager: NotificationSyncManager
    private lateinit var taskAlarmScheduler: TaskAlarmScheduler
    private lateinit var scheduler: TaskReminderSchedulerImpl

    private val fixedNowMillis = 1_780_000_000_000L

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        notificationSyncManager = mockk(relaxed = true)
        taskAlarmScheduler = mockk(relaxed = true)

        every { context.getString(R.string.error_task_no_reminder_date) } returns "task reminder missing"
        every { context.getString(R.string.error_exact_alarm_permission) } returns "exact alarm permission"
        every { context.getString(R.string.error_security_exception) } returns "security"
        every { taskAlarmScheduler.canScheduleExactAlarms() } returns true

        scheduler =
            TaskReminderSchedulerImpl(
                context = context,
                notificationSyncManager = notificationSyncManager,
                taskAlarmScheduler = taskAlarmScheduler,
                currentTimeMillisProvider = { fixedNowMillis },
            )
    }

    @Test
    fun `schedule returns failure when reminder is missing`() {
        val task = Task(id = 1L, title = "No reminder", description = "", reminderDate = null)

        val result = scheduler.schedule(task)

        assertThat(result).isEqualTo(ScheduleResult.Failure("task reminder missing"))
        verify(exactly = 0) { taskAlarmScheduler.scheduleTaskReminder(any(), any()) }
    }

    @Test
    fun `schedule skips completed tasks`() {
        val task =
            Task(
                id = 1L,
                title = "Done",
                description = "",
                isCompleted = true,
                reminderDate = LocalDateTime(2030, 1, 1, 8, 0),
            )

        val result = scheduler.schedule(task)

        assertThat(result).isEqualTo(ScheduleResult.Skipped)
        verify(exactly = 0) { taskAlarmScheduler.scheduleTaskReminder(any(), any()) }
    }

    @Test
    fun `schedule delegates to alarm scheduler for future reminders`() {
        val reminderDate = LocalDateTime(2030, 1, 1, 8, 0)
        val task = Task(id = 42L, title = "Future", description = "", reminderDate = reminderDate)
        val expectedTriggerAtMillis =
            reminderDate.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

        val result = scheduler.schedule(task)

        assertThat(result).isEqualTo(ScheduleResult.Success(reminderDate))
        verify(exactly = 1) { taskAlarmScheduler.scheduleTaskReminder(42L, expectedTriggerAtMillis) }
    }

    @Test
    fun `schedule skips past reminders`() {
        val pastReminder = LocalDateTime(2020, 1, 1, 8, 0)
        val task = Task(id = 1L, title = "Past", description = "", reminderDate = pastReminder)

        val result = scheduler.schedule(task)

        assertThat(result).isEqualTo(ScheduleResult.Skipped)
        verify(exactly = 0) { taskAlarmScheduler.scheduleTaskReminder(any(), any()) }
    }

    @Test
    fun `schedule returns permission error when exact alarms are unavailable`() {
        every { taskAlarmScheduler.canScheduleExactAlarms() } returns false
        val task =
            Task(
                id = 1L,
                title = "Permission blocked",
                description = "",
                reminderDate = LocalDateTime(2030, 1, 1, 8, 0),
            )

        val result = scheduler.schedule(task)

        assertThat(result).isEqualTo(ScheduleResult.PermissionError("exact alarm permission"))
        verify(exactly = 0) { taskAlarmScheduler.scheduleTaskReminder(any(), any()) }
    }

    @Test
    fun `cancel delegates cancellation and notification dismissal`() {
        val task = Task(id = 77L, title = "Cancel me", description = "")

        scheduler.cancel(task)

        verify(exactly = 1) { taskAlarmScheduler.cancelTaskReminder(77L) }
        verify(exactly = 1) { notificationSyncManager.dismissTaskNotification(77L) }
    }
}
