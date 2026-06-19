package com.mandrecode.tempo.infrastructure.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.mandrecode.tempo.R

object NotificationChannelManager {
    private const val TAG = "NotificationChannelManager"
    const val TASK_REMINDER_CHANNEL_ID = "task_reminder_channel"
    const val HABIT_REMINDER_CHANNEL_ID = "habit_reminder_channel"
    const val HABIT_CHAIN_LIVE_ACTIVITY_CHANNEL_ID = "habit_chain_live_activity_channel"

    fun ensureTaskReminderChannel(
        context: Context,
        notificationManager: NotificationManager,
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel =
            NotificationChannel(
                TASK_REMINDER_CHANNEL_ID,
                context.getString(R.string.notification_task_reminders_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.notification_task_reminders_channel_description)
            }
        notificationManager.createNotificationChannel(channel)
    }

    fun ensureHabitReminderChannel(
        context: Context,
        notificationManager: NotificationManager,
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel =
            NotificationChannel(
                HABIT_REMINDER_CHANNEL_ID,
                context.getString(R.string.notification_habit_reminders_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.notification_habit_reminders_channel_description)
            }
        notificationManager.createNotificationChannel(channel)
    }

    fun ensureLiveActivityChannel(
        context: Context,
        notificationManager: NotificationManager,
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel =
            NotificationChannel(
                HABIT_CHAIN_LIVE_ACTIVITY_CHANNEL_ID,
                context.getString(R.string.notification_habit_chain_live_activity_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.notification_habit_chain_live_activity_channel_description)
            }
        notificationManager.createNotificationChannel(channel)
    }

    fun canPostNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        val hasPermission =
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            Log.w(
                TAG,
                "POST_NOTIFICATIONS permission not granted; notification post " +
                    "will be silently dropped on Android 13+",
            )
        }
        return hasPermission
    }
}
