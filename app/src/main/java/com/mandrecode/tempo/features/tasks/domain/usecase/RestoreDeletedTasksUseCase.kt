package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.core.domain.model.RestoreResult
import com.mandrecode.tempo.features.tasks.domain.model.TaskDeletionSnapshot
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.features.tasks.domain.scheduler.TaskReminderScheduler
import jakarta.inject.Inject

class RestoreDeletedTasksUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val taskReminderScheduler: TaskReminderScheduler,
    ) {
        suspend operator fun invoke(snapshot: TaskDeletionSnapshot): RestoreResult {
            taskRepository.restoreDeletedTasks(snapshot)
            snapshot.tasks.forEach(taskReminderScheduler::cancel)
            val results =
                snapshot.tasks
                    .filter { it.reminderDate != null }
                    .map(taskReminderScheduler::schedule)
            return RestoreResult(results)
        }
    }
