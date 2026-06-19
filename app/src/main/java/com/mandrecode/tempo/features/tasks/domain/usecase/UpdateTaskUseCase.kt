package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.core.domain.util.ValidationResult
import com.mandrecode.tempo.core.domain.util.ValidationUtils
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.features.tasks.domain.scheduler.TaskReminderScheduler
import com.mandrecode.tempo.features.tasks.domain.util.TaskReminderDateUtil
import jakarta.inject.Inject

class UpdateTaskUseCase
    @Inject
    constructor(
        private val taskRepository: TaskRepository,
        private val taskReminderScheduler: TaskReminderScheduler,
    ) {
        sealed class Result {
            data class Success(
                val scheduleResult: ScheduleResult,
                val reminderAdvanced: Boolean = false,
                val pastReminderWithoutPeriodicity: Boolean = false,
            ) : Result()

            data class ValidationError(
                val type: CreateTaskUseCase.ValidationErrorType,
            ) : Result()
        }

        suspend operator fun invoke(task: Task): Result {
            val trimmedTask = task.copy(title = task.title.trim(), description = task.description.trim())
            when (ValidationUtils.validateTitle(trimmedTask.title)) {
                ValidationResult.Empty -> return Result.ValidationError(CreateTaskUseCase.ValidationErrorType.TITLE_EMPTY)
                ValidationResult.TooLong -> return Result.ValidationError(CreateTaskUseCase.ValidationErrorType.TITLE_TOO_LONG)
                ValidationResult.Valid -> {}
                else -> {}
            }
            if (ValidationUtils.validateDescription(trimmedTask.description) is ValidationResult.TooLong) {
                return Result.ValidationError(CreateTaskUseCase.ValidationErrorType.DESCRIPTION_TOO_LONG)
            }

            val (adjustedTask, reminderAdvanced, pastReminderWithoutPeriodicity) =
                if (trimmedTask.isCompleted) {
                    Triple(trimmedTask, false, false)
                } else {
                    val advancedTask = TaskReminderDateUtil.advanceReminderIfNeeded(trimmedTask)
                    val reminderWasAdvanced = advancedTask.reminderDate != trimmedTask.reminderDate
                    val isPastReminderNoPeriodicity = TaskReminderDateUtil.isPastReminderWithoutPeriodicity(trimmedTask)
                    Triple(advancedTask, reminderWasAdvanced, isPastReminderNoPeriodicity)
                }

            taskRepository.updateTask(adjustedTask)

            val scheduleResult =
                if (adjustedTask.isCompleted || adjustedTask.reminderDate == null) {
                    taskReminderScheduler.cancel(adjustedTask)
                    ScheduleResult.Skipped
                } else {
                    taskReminderScheduler.dismissNotification(adjustedTask.id)
                    val result = taskReminderScheduler.schedule(adjustedTask)
                    if (result is ScheduleResult.Skipped) {
                        // schedule() early-returned (e.g. past reminder without periodicity);
                        // proactively cancel any previously-scheduled alarm so a stale
                        // future alarm can't fire after the user moved the reminder back.
                        taskReminderScheduler.cancel(adjustedTask)
                    }
                    result
                }
            return Result.Success(scheduleResult, reminderAdvanced, pastReminderWithoutPeriodicity)
        }
    }
