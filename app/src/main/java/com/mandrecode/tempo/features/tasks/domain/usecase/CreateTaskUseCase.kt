package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.core.domain.util.TitleDescriptionValidationResult
import com.mandrecode.tempo.core.domain.util.ValidationUtils
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.features.tasks.domain.scheduler.TaskReminderScheduler
import com.mandrecode.tempo.features.tasks.domain.util.TaskReminderDateUtil
import jakarta.inject.Inject

class CreateTaskUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val taskReminderScheduler: TaskReminderScheduler,
    ) {
        sealed class Result {
            data class Success(
                val taskId: Long,
                val scheduleResult: ScheduleResult,
                val reminderAdvanced: Boolean = false,
                val pastReminderWithoutPeriodicity: Boolean = false,
            ) : Result()

            data class ValidationError(
                val type: ValidationErrorType,
            ) : Result()
        }

        enum class ValidationErrorType { TITLE_EMPTY, TITLE_TOO_LONG, DESCRIPTION_TOO_LONG }

        suspend operator fun invoke(task: Task): Result {
            val trimmedTask = task.copy(title = task.title.trim(), description = task.description.trim())
            when (ValidationUtils.validateTitleAndDescription(trimmedTask.title, trimmedTask.description)) {
                TitleDescriptionValidationResult.TitleEmpty ->
                    return Result.ValidationError(ValidationErrorType.TITLE_EMPTY)
                TitleDescriptionValidationResult.TitleTooLong ->
                    return Result.ValidationError(ValidationErrorType.TITLE_TOO_LONG)
                TitleDescriptionValidationResult.DescriptionTooLong ->
                    return Result.ValidationError(ValidationErrorType.DESCRIPTION_TOO_LONG)
                TitleDescriptionValidationResult.Valid -> {}
            }

            val adjustedTask = TaskReminderDateUtil.advanceReminderIfNeeded(trimmedTask)
            val reminderAdvanced = adjustedTask.reminderDate != trimmedTask.reminderDate
            val pastReminderWithoutPeriodicity = TaskReminderDateUtil.isPastReminderWithoutPeriodicity(trimmedTask)
            val maxSortOrder =
                if (adjustedTask.parentTaskId != null) {
                    taskRepository.getMaxSubtaskSortOrder(adjustedTask.parentTaskId)
                } else {
                    taskRepository.getMaxSortOrder(adjustedTask.categoryId)
                }
            val taskWithSortOrder = adjustedTask.copy(sortOrder = maxSortOrder + 1)
            val newTaskId = taskRepository.insertTask(taskWithSortOrder)
            val taskWithId = taskWithSortOrder.copy(id = newTaskId)

            val scheduleResult =
                if (taskWithId.reminderDate != null) {
                    taskReminderScheduler.schedule(taskWithId)
                } else {
                    ScheduleResult.Skipped
                }
            return Result.Success(newTaskId, scheduleResult, reminderAdvanced, pastReminderWithoutPeriodicity)
        }
    }
