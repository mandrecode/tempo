package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.features.tasks.domain.scheduler.TaskReminderScheduler
import jakarta.inject.Inject

class DeleteCompletedTasksUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val taskReminderScheduler: TaskReminderScheduler,
    ) {
        suspend operator fun invoke(categoryId: Long) =
            taskRepository.deleteCompletedTasksWithSnapshot(categoryId).also { snapshot ->
                snapshot.tasks.forEach { taskReminderScheduler.cancel(it) }
            }
    }
