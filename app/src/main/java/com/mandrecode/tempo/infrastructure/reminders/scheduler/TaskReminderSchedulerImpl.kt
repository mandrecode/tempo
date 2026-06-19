package com.mandrecode.tempo.infrastructure.reminders.scheduler

import android.content.Context
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.scheduler.TaskReminderScheduler
import com.mandrecode.tempo.infrastructure.notifications.NotificationSyncManager
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class TaskReminderSchedulerImpl(
    private val context: Context,
    private val notificationSyncManager: NotificationSyncManager,
    private val taskAlarmScheduler: TaskAlarmScheduler,
    private val currentTimeMillisProvider: () -> Long = { System.currentTimeMillis() },
) : TaskReminderScheduler {
    override fun schedule(task: Task): ScheduleResult {
        val reminderDate = task.reminderDate
        return when {
            task.isCompleted -> ScheduleResult.Skipped
            reminderDate == null -> ScheduleResult.Failure(context.getString(R.string.error_task_no_reminder_date))
            else -> schedulePendingTask(task, reminderDate)
        }
    }

    private fun schedulePendingTask(
        task: Task,
        reminderDate: LocalDateTime,
    ): ScheduleResult {
        val triggerAtMillis = reminderDate.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        val currentTimeMillis = currentTimeMillisProvider()

        return when {
            triggerAtMillis < currentTimeMillis -> ScheduleResult.Skipped
            !taskAlarmScheduler.canScheduleExactAlarms() ->
                ScheduleResult.PermissionError(context.getString(R.string.error_exact_alarm_permission))
            else ->
                try {
                    taskAlarmScheduler.scheduleTaskReminder(task.id, triggerAtMillis)
                    ScheduleResult.Success(reminderDate)
                } catch (_: SecurityException) {
                    ScheduleResult.PermissionError(context.getString(R.string.error_security_exception))
                }
        }
    }

    override fun cancel(task: Task) {
        taskAlarmScheduler.cancelTaskReminder(task.id)
        notificationSyncManager.dismissTaskNotification(task.id)
    }

    override fun dismissNotification(taskId: Long) {
        notificationSyncManager.dismissTaskNotification(taskId)
    }
}
