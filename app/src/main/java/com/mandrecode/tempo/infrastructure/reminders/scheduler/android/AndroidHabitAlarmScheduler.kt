package com.mandrecode.tempo.infrastructure.reminders.scheduler.android

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mandrecode.tempo.infrastructure.notifications.RequestCodeGenerator
import com.mandrecode.tempo.infrastructure.reminders.receivers.HabitReminderReceiver
import com.mandrecode.tempo.infrastructure.reminders.scheduler.HabitAlarmScheduler
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

class AndroidHabitAlarmScheduler(
    private val context: Context,
) : HabitAlarmScheduler {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun canScheduleExactAlarms(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

    override fun scheduleHabitReminder(
        habitId: Long,
        triggerAtMillis: Long,
    ) {
        val intent =
            Intent(context, HabitReminderReceiver::class.java).apply {
                putExtra(HabitReminderReceiver.EXTRA_HABIT_ID, habitId)
                putExtra(HabitReminderReceiver.EXTRA_IS_CHAIN, false)
                putExtra(HabitReminderReceiver.EXTRA_SCHEDULED_DATE, triggerAtMillis.toScheduledDateString())
            }
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                RequestCodeGenerator.forHabit(habitId),
                intent,
                FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent,
        )
    }

    override fun cancelHabitReminder(habitId: Long) {
        val intent = Intent(context, HabitReminderReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                RequestCodeGenerator.forHabit(habitId),
                intent,
                FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        alarmManager.cancel(pendingIntent)
    }

    override fun scheduleHabitChainReminder(
        habitChainId: Long,
        triggerAtMillis: Long,
    ) {
        val intent =
            Intent(context, HabitReminderReceiver::class.java).apply {
                putExtra(HabitReminderReceiver.EXTRA_HABIT_CHAIN_ID, habitChainId)
                putExtra(HabitReminderReceiver.EXTRA_IS_CHAIN, true)
            }
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                RequestCodeGenerator.forHabitChain(habitChainId),
                intent,
                FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent,
        )
    }

    override fun cancelHabitChainReminder(habitChainId: Long) {
        val intent = Intent(context, HabitReminderReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                RequestCodeGenerator.forHabitChain(habitChainId),
                intent,
                FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        alarmManager.cancel(pendingIntent)
    }

    private fun Long.toScheduledDateString(): String =
        Instant
            .fromEpochMilliseconds(this)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toString()
}
