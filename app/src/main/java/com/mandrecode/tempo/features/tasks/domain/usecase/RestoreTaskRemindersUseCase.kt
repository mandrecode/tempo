package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import jakarta.inject.Inject
import kotlinx.datetime.LocalDateTime

/**
 * Puts a set of tasks back to the reminders they had, for undoing a batch of planning in one go.
 *
 * Each task is re-read before it is written, so undoing only ever moves the reminder: a title
 * edited, a priority set or a subtask added between the planning and the undo survives it. Tasks
 * that have since been deleted are skipped rather than resurrected.
 *
 * Deliberately not a transaction. There is no batch write behind it, and a partial undo — some
 * tasks put back, some not — is a state the user can see and act on, where an all-or-nothing
 * rollback of an undo would not be.
 */
class RestoreTaskRemindersUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val updateTask: UpdateTaskUseCase,
    ) {
        suspend operator fun invoke(reminders: Map<Long, LocalDateTime?>) {
            reminders.forEach { (taskId, reminderDate) ->
                val task = taskRepository.getTaskById(taskId) ?: return@forEach
                if (task.reminderDate == reminderDate) return@forEach
                updateTask(task.copy(reminderDate = reminderDate))
            }
        }
    }
