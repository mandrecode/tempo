package com.mandrecode.tempo.infrastructure.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.mandrecode.tempo.MainActivity
import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.infrastructure.reminders.receivers.MarkAsCompletedReceiver
import com.mandrecode.tempo.infrastructure.reminders.receivers.TaskReminderReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single builder for task reminder notifications, shared by the reminder alarm and by the
 * daily missed-reminder catch-up so both post an identical notification. The notification id
 * is [RequestCodeGenerator.forTask], so re-posting for the same task replaces the existing
 * notification instead of stacking a duplicate.
 */
@Singleton
class TaskReminderNotifier
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun notify(task: Task) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            NotificationChannelManager.ensureTaskReminderChannel(context, notificationManager)

            if (!NotificationChannelManager.canPostNotifications(context)) {
                return
            }

            val taskRequestCode = RequestCodeGenerator.forTask(task.id)

            val contentIntent =
                Intent(context, MainActivity::class.java).apply {
                    putExtra(TaskReminderReceiver.EXTRA_TASK_ID, task.id)
                    putExtra(TaskReminderReceiver.EXTRA_OPEN_TASKS, true)
                    // Keep the fired reminder as the completion anchor even if rollover creates
                    // the next occurrence before the user acts on this notification.
                    putOriginalReminderDateIfPeriodic(task)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }

            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    taskRequestCode,
                    contentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            val markAsCompleteIntent =
                Intent(context, MarkAsCompletedReceiver::class.java).apply {
                    putExtra(TaskReminderReceiver.EXTRA_TASK_ID, task.id)
                    // Only embed original reminderDate for periodic tasks so completion uses
                    // the fired occurrence as its anchor.
                    putOriginalReminderDateIfPeriodic(task)
                }

            val markAsCompletePendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    taskRequestCode,
                    markAsCompleteIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            val sb = StringBuilder()
            if (task.description.isNotEmpty()) {
                sb.append(task.description)
            }

            val bigText = sb.toString()

            val notification =
                NotificationCompat
                    .Builder(context, NotificationChannelManager.TASK_REMINDER_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_track_changes)
                    .setContentTitle(task.title)
                    .setContentText(task.description.takeIf { it.isNotEmpty() })
                    .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .addAction(
                        R.drawable.ic_check,
                        context.getString(R.string.mark_as_completed),
                        markAsCompletePendingIntent,
                    ).build()

            notificationManager.notify(taskRequestCode, notification)
        }

        /**
         * Embeds the task's current reminderDate as [MarkAsCompletedReceiver.EXTRA_ORIGINAL_REMINDER_DATE]
         * for periodic tasks only. Receivers/activities use this as the fired occurrence
         * anchor when computing the next occurrence on completion.
         */
        private fun Intent.putOriginalReminderDateIfPeriodic(task: Task) {
            if (task.periodicity != null) {
                task.reminderDate?.let {
                    putExtra(MarkAsCompletedReceiver.EXTRA_ORIGINAL_REMINDER_DATE, it.toString())
                }
            }
        }
    }
