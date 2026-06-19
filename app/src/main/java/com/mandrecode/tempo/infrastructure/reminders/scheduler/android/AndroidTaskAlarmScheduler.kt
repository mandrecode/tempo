package com.mandrecode.tempo.infrastructure.reminders.scheduler.android

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mandrecode.tempo.infrastructure.notifications.RequestCodeGenerator
import com.mandrecode.tempo.infrastructure.reminders.receivers.TaskReminderReceiver
import com.mandrecode.tempo.infrastructure.reminders.scheduler.TaskAlarmScheduler

class AndroidTaskAlarmScheduler(
    private val context: Context,
) : TaskAlarmScheduler {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun canScheduleExactAlarms(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

    override fun scheduleTaskReminder(
        taskId: Long,
        triggerAtMillis: Long,
    ) {
        val intent =
            Intent(context, TaskReminderReceiver::class.java).apply {
                putExtra(TaskReminderReceiver.EXTRA_TASK_ID, taskId)
            }
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                RequestCodeGenerator.forTask(taskId),
                intent,
                FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent,
        )
    }

    override fun cancelTaskReminder(taskId: Long) {
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                RequestCodeGenerator.forTask(taskId),
                intent,
                FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        alarmManager.cancel(pendingIntent)
    }
}
