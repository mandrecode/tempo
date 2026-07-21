package com.mandrecode.tempo.infrastructure.backup

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mandrecode.tempo.features.backup.domain.scheduler.BackupReminderScheduler
import com.mandrecode.tempo.features.routines.domain.repository.HabitChainRepository
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.features.tasks.domain.scheduler.TaskReminderScheduler
import com.mandrecode.tempo.infrastructure.reminders.workers.RescheduleRemindersWorker
import jakarta.inject.Inject

class BackupReminderSchedulerImpl
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val habitRepository: HabitRepository,
        private val habitChainRepository: HabitChainRepository,
        private val taskReminderScheduler: TaskReminderScheduler,
        private val habitReminderScheduler: HabitReminderScheduler,
        private val workManager: WorkManager,
    ) : BackupReminderScheduler {
        override suspend fun cancelAllReminders() {
            taskRepository.getTasksWithReminders().forEach { taskReminderScheduler.cancel(it) }
            habitRepository.getHabitsWithReminders().forEach { habitReminderScheduler.cancelHabit(it) }
            habitChainRepository.getHabitChainsWithReminders().forEach {
                habitReminderScheduler.cancelHabitChain(it)
            }
        }

        override fun rescheduleAllReminders() {
            workManager.enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<RescheduleRemindersWorker>().build(),
            )
        }

        companion object {
            const val WORK_NAME = "BackupRescheduleRemindersWorker"
        }
    }
