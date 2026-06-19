package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.features.tasks.domain.scheduler.TaskReminderScheduler
import jakarta.inject.Inject

class ClearAllTaskRemindersUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val taskReminderScheduler: TaskReminderScheduler,
    ) {
        suspend operator fun invoke() {
            val tasksWithReminders = taskRepository.getTasksWithReminders()
            taskRepository.clearAllReminders()
            tasksWithReminders.forEach { task ->
                taskReminderScheduler.cancel(task)
            }
        }
    }
