package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import jakarta.inject.Inject
import kotlinx.datetime.LocalDateTime

/**
 * Selects the tasks the missed-reminder catch-up notifies for: still incomplete, and with a
 * reminder that already came and went. Read-only by design — the catch-up must not touch
 * reminder dates, completion state, or recurrence links (see the `task-reminder-rollover` spec).
 */
class GetOverdueIncompleteTasksUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
    ) {
        suspend operator fun invoke(now: LocalDateTime): List<Task> =
            taskRepository.getTasksWithReminders().filter { task ->
                val reminderDate = task.reminderDate
                !task.isCompleted && reminderDate != null && reminderDate < now
            }
    }
