package com.mandrecode.tempo.infrastructure.reminders.scheduler.android

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mandrecode.tempo.infrastructure.notifications.RequestCodeGenerator
import com.mandrecode.tempo.infrastructure.reminders.receivers.MissedReminderCatchUpReceiver
import com.mandrecode.tempo.infrastructure.reminders.scheduler.MissedReminderAlarmScheduler

class AndroidMissedReminderAlarmScheduler(
    private val context: Context,
) : MissedReminderAlarmScheduler {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun canScheduleExactAlarms(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

    /**
     * Falls back to an inexact alarm when the exact-alarm grant is missing: a catch-up delayed
     * by minutes is still useful, and the feature must not depend on a permission the user can
     * refuse.
     */
    override fun scheduleCatchUp(triggerAtMillis: Long) {
        val pendingIntent = catchUpPendingIntent()
        if (canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        }
    }

    override fun cancelCatchUp() {
        alarmManager.cancel(catchUpPendingIntent())
    }

    private fun catchUpPendingIntent(): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            RequestCodeGenerator.forMissedReminderCatchUp(),
            Intent(context, MissedReminderCatchUpReceiver::class.java),
            FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
