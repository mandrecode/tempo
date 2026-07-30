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

    /**
     * Suffixed because the first version of this channel was created at low importance. A channel's
     * importance cannot be raised after the system has it, so lifting it takes a new channel.
     */
    const val FOCUS_SESSION_CHANNEL_ID = "focus_session_channel_v2"
    private const val LEGACY_FOCUS_SESSION_CHANNEL_ID = "focus_session_channel"

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

    /**
     * The same importance the habit-chain live activity uses. A running session is the same kind of
     * thing — something under way that you want to see arrive and be able to glance at — so it gets
     * the same standing in the shade rather than being filed away silently.
     */
    fun ensureFocusSessionChannel(
        context: Context,
        notificationManager: NotificationManager,
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        // Nothing posts to the old low-importance channel any more; leaving it would show the user
        // a dead entry in the app's notification settings.
        notificationManager.deleteNotificationChannel(LEGACY_FOCUS_SESSION_CHANNEL_ID)
        val channel =
            NotificationChannel(
                FOCUS_SESSION_CHANNEL_ID,
                context.getString(R.string.notification_focus_session_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.notification_focus_session_channel_description)
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
