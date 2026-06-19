package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.features.tasks.domain.scheduler.TaskReminderScheduler
import jakarta.inject.Inject

class DeleteTaskUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val taskReminderScheduler: TaskReminderScheduler,
    ) {
        suspend operator fun invoke(task: Task) {
            taskReminderScheduler.cancel(task)
            taskRepository.deleteTask(task)
        }
    }
