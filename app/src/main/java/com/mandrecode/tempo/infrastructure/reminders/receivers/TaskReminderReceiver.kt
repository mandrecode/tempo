package com.mandrecode.tempo.infrastructure.reminders.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.mandrecode.tempo.core.di.IoDispatcher
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.features.tasks.domain.usecase.RollOverduePeriodicTaskUseCase
import com.mandrecode.tempo.infrastructure.notifications.TaskReminderNotifier
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
    lateinit var taskReminderNotifier: TaskReminderNotifier

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
                    taskReminderNotifier.notify(activeTask)

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

    companion object {
        const val EXTRA_TASK_ID = "TASK_ID"
        const val EXTRA_OPEN_TASKS = "OPEN_TASKS"

        @VisibleForTesting
        internal fun shouldProcessTaskReminder(task: Task?): Boolean = task != null && !task.isCompleted
    }
}
