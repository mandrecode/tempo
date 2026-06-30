package com.mandrecode.tempo.infrastructure.reminders.receivers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import com.mandrecode.tempo.MainActivity
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.di.IoDispatcher
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.features.tasks.domain.usecase.RollOverduePeriodicTaskUseCase
import com.mandrecode.tempo.infrastructure.notifications.NotificationChannelManager
import com.mandrecode.tempo.infrastructure.notifications.RequestCodeGenerator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TaskReminderReceiver : BroadcastReceiver() {
    @Inject
    lateinit var taskRepository: TaskRepository

    @Inject
    lateinit var rollOverduePeriodicTaskUseCase: RollOverduePeriodicTaskUseCase

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(ioDispatcher).launch {
            try {
                val task = taskRepository.getTaskById(taskId)
                if (shouldProcessTaskReminder(task)) {
                    val activeTask = requireNotNull(task)
                    showNotification(context, activeTask)

                    // Preserve the overdue occurrence and schedule a linked next instance.
                    if (activeTask.periodicity != null && activeTask.parentTaskId == null) {
                        rollOverduePeriodicTaskUseCase(activeTask)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(
        context: Context,
        task: Task,
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        NotificationChannelManager.ensureTaskReminderChannel(context, notificationManager)

        if (!NotificationChannelManager.canPostNotifications(context)) {
            return
        }

        val taskRequestCode = RequestCodeGenerator.forTask(task.id)

        val contentIntent =
            Intent(context, MainActivity::class.java).apply {
                putExtra(EXTRA_TASK_ID, task.id)
                putExtra(EXTRA_OPEN_TASKS, true)
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
                putExtra(EXTRA_TASK_ID, task.id)
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

    companion object {
        const val EXTRA_TASK_ID = "TASK_ID"
        const val EXTRA_OPEN_TASKS = "OPEN_TASKS"

        @VisibleForTesting
        internal fun shouldProcessTaskReminder(task: Task?): Boolean = task != null && !task.isCompleted
    }
}
