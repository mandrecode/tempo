package com.mandrecode.tempo.features.tasks.presentation

import androidx.lifecycle.viewModelScope
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.domain.model.MonthDayOption
import com.mandrecode.tempo.core.domain.model.Periodicity
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.core.domain.util.ValidationResult
import com.mandrecode.tempo.core.domain.util.ValidationUtils
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.usecase.CreateTaskUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.ToggleTaskCompletionUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.UpdateTaskUseCase
import com.mandrecode.tempo.features.tasks.presentation.TasksContract.TaskFormState
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime

internal fun Throwable.toUserFacingMessage(): String =
    localizedMessage?.takeIf { it.isNotBlank() }
        ?: message?.takeIf { it.isNotBlank() }
        ?: javaClass.simpleName.takeIf { it.isNotBlank() }
        ?: javaClass.name

internal fun TasksViewModel.createOrUpdateTask(
    title: String,
    description: String,
    categoryId: Long,
    parentTaskId: Long? = null,
    autoSave: Boolean = false,
) {
    when (ValidationUtils.validateTitle(title)) {
        ValidationResult.Empty -> {
            mutableUiState.update { it.copy(taskForm = it.taskForm.copy(titleError = R.string.task_title_required)) }
            return
        }

        ValidationResult.TooLong -> {
            mutableUiState.update { it.copy(taskForm = it.taskForm.copy(titleError = R.string.error_task_title_too_long)) }
            return
        }

        // TooManyItems is never returned by validateTitle; listed here for exhaustive coverage.
        ValidationResult.Valid, ValidationResult.TooManyItems -> Unit
    }

    if (ValidationUtils.validateDescription(description) is ValidationResult.TooLong) {
        mutableUiState.update { it.copy(taskForm = it.taskForm.copy(descriptionError = R.string.error_task_description_too_long)) }
        return
    }

    val taskToEdit = mutableUiState.value.taskForm.editingTask
    val sanitized = sanitizePeriodicityFields()

    if (taskToEdit != null) {
        val shouldUnlink =
            taskToEdit.parentTaskId != null && categoryId != taskToEdit.categoryId
        val editFields = taskToEdit.editFieldsFrom(sanitized)
        val updatedTask =
            taskToEdit.copy(
                title = title,
                description = description,
                categoryId = categoryId,
                priority = editFields.priority,
                reminderDate = editFields.reminderDate,
                periodicity = editFields.periodicity,
                periodicityInterval = editFields.periodicityInterval,
                repeatDays = editFields.repeatDays,
                monthDayOption = editFields.monthDayOption,
                parentTaskId = if (shouldUnlink) null else taskToEdit.parentTaskId,
            )
        updateTask(updatedTask, comesFromDialog = !autoSave)
    } else {
        val newTask =
            Task(
                title = title,
                description = description,
                categoryId = categoryId,
                priority = sanitized.priority,
                reminderDate = sanitized.reminderDate,
                periodicity = sanitized.periodicity,
                periodicityInterval = sanitized.periodicityInterval,
                repeatDays = sanitized.repeatDays,
                monthDayOption = sanitized.monthDayOption,
                parentTaskId = parentTaskId,
            )
        addTask(newTask)

        if (parentTaskId != null) {
            expandTask(parentTaskId)
        }
    }

    if (!autoSave) {
        hideTaskDialog()
    }
}

private data class TaskEditFields(
    val priority: Priority?,
    val reminderDate: LocalDateTime?,
    val periodicity: Periodicity?,
    val periodicityInterval: Int,
    val repeatDays: Set<DayOfWeek>?,
    val monthDayOption: MonthDayOption?,
)

private fun Task.editFieldsFrom(form: TaskFormState): TaskEditFields =
    if (isCompleted) {
        TaskEditFields(
            priority = priority,
            reminderDate = reminderDate,
            periodicity = periodicity,
            periodicityInterval = periodicityInterval,
            repeatDays = repeatDays,
            monthDayOption = monthDayOption,
        )
    } else {
        TaskEditFields(
            priority = form.priority,
            reminderDate = form.reminderDate,
            periodicity = form.periodicity,
            periodicityInterval = form.periodicityInterval,
            repeatDays = form.repeatDays,
            monthDayOption = form.monthDayOption,
        )
    }

internal fun TasksViewModel.sanitizePeriodicityFields(): TaskFormState {
    val form = mutableUiState.value.taskForm
    val periodicity = form.periodicity
    val interval =
        when {
            periodicity == null -> 1
            periodicity == Periodicity.HOURLY ->
                form.periodicityInterval.coerceIn(1, Periodicity.MAX_HOURLY_INTERVAL)
            else -> form.periodicityInterval.coerceAtLeast(1)
        }
    return form.copy(
        periodicityInterval = interval,
        repeatDays = if (periodicity == Periodicity.WEEKLY) form.repeatDays else null,
        monthDayOption = if (periodicity == Periodicity.MONTHLY) form.monthDayOption else null,
    )
}

internal fun TasksViewModel.addTask(task: Task) {
    viewModelScope.launch {
        try {
            when (val result = createTaskUseCase(task)) {
                is CreateTaskUseCase.Result.ValidationError -> {
                    val messageRes =
                        when (result.type) {
                            CreateTaskUseCase.ValidationErrorType.TITLE_EMPTY ->
                                R.string.task_title_required
                            CreateTaskUseCase.ValidationErrorType.TITLE_TOO_LONG ->
                                R.string.error_task_title_too_long
                            CreateTaskUseCase.ValidationErrorType.DESCRIPTION_TOO_LONG ->
                                R.string.error_task_description_too_long
                        }
                    showSnackbar(messageRes)
                }

                is CreateTaskUseCase.Result.Success -> {
                    val messageResId =
                        when {
                            result.scheduleResult is ScheduleResult.PermissionError -> {
                                mutableUiState.update { it.copy(showPermissionRequestDialog = true) }
                                R.string.msg_permission_needed
                            }
                            result.scheduleResult is ScheduleResult.Failure ->
                                R.string.msg_task_added_failed_scheduling
                            result.reminderAdvanced ->
                                R.string.msg_task_added_reminder_advanced
                            result.pastReminderWithoutPeriodicity ->
                                R.string.msg_task_added_past_reminder_no_periodicity
                            else -> R.string.msg_task_added_success
                        }

                    mutableUiState.update {
                        it.copy(taskForm = TaskFormState())
                    }
                    showSnackbar(messageResId)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            showSnackbar(
                R.string.msg_task_add_failed,
                listOf(e.toUserFacingMessage()),
            )
        }
    }
}

internal fun TasksViewModel.updateTask(
    task: Task,
    comesFromDialog: Boolean = false,
) {
    viewModelScope.launch {
        try {
            when (val result = updateTaskUseCase(task)) {
                is UpdateTaskUseCase.Result.ValidationError -> {
                    val messageRes =
                        when (result.type) {
                            CreateTaskUseCase.ValidationErrorType.TITLE_EMPTY ->
                                R.string.task_title_required
                            CreateTaskUseCase.ValidationErrorType.TITLE_TOO_LONG ->
                                R.string.error_task_title_too_long
                            CreateTaskUseCase.ValidationErrorType.DESCRIPTION_TOO_LONG ->
                                R.string.error_task_description_too_long
                        }
                    showSnackbar(messageRes)
                }

                is UpdateTaskUseCase.Result.Success -> {
                    val message =
                        when {
                            result.scheduleResult is ScheduleResult.PermissionError -> {
                                mutableUiState.update { it.copy(showPermissionRequestDialog = true) }
                                R.string.msg_permission_needed
                            }
                            result.scheduleResult is ScheduleResult.Failure ->
                                R.string.msg_task_updated_failed_scheduling
                            result.reminderAdvanced ->
                                R.string.msg_task_updated_reminder_advanced
                            result.pastReminderWithoutPeriodicity ->
                                R.string.msg_task_updated_past_reminder_no_periodicity
                            comesFromDialog -> R.string.msg_task_updated_success
                            else -> null
                        }

                    mutableUiState.update { currentState ->
                        if (comesFromDialog) {
                            currentState.copy(taskForm = TaskFormState())
                        } else {
                            currentState
                        }
                    }

                    if (message != null) {
                        showSnackbar(message)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            showSnackbar(R.string.msg_task_update_failed, listOf(e.toUserFacingMessage()))
        }
    }
}

internal fun TasksViewModel.requestDeleteTask(task: Task) {
    viewModelScope.launch {
        val subtasksCount = taskRepository.getSubtasksSync(task.id).size
        mutableUiState.update {
            it.copy(
                showDeleteTaskConfirmationDialog = true,
                taskToDelete = task,
                taskToDeleteSubtasksCount = subtasksCount,
            )
        }
    }
}

internal fun TasksViewModel.cancelDeleteTask() {
    mutableUiState.update {
        it.copy(
            showDeleteTaskConfirmationDialog = false,
            taskToDelete = null,
            taskToDeleteSubtasksCount = 0,
        )
    }
}

internal fun TasksViewModel.confirmDeleteTask(task: Task) {
    deleteTask(task)
    mutableUiState.update {
        it.copy(
            showDeleteTaskConfirmationDialog = false,
            taskToDelete = null,
            taskToDeleteSubtasksCount = 0,
            taskForm = TaskFormState(),
        )
    }
}

internal fun TasksViewModel.deleteTask(task: Task) =
    viewModelScope.launch {
        try {
            deleteTaskUseCase(task)
            showSnackbar(R.string.task_deleted)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            showSnackbar(R.string.error_deleting_task)
        }
    }

internal fun TasksViewModel.toggleTaskCompletion(taskToToggle: Task) {
    viewModelScope.launch {
        if (!taskToToggle.isCompleted) {
            mutableUiState.update { it.copy(showCompletedTasks = true) }
        }

        val pending = pendingOriginalReminderDate
        val (effectiveTask, usesPendingReminderDate) =
            resolveEffectiveTaskForToggle(
                taskToToggle = taskToToggle,
                pendingOriginal = pending,
            )
        try {
            val result = toggleTaskCompletionUseCase(effectiveTask)
            handleToggleResult(taskToToggle.id, result)
            if (usesPendingReminderDate && pending == pendingOriginalReminderDate) {
                pendingOriginalReminderDate = null
            }
            refreshEditingTaskIfNeeded(taskToToggle.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            showSnackbar(R.string.msg_task_update_failed, listOf(e.toUserFacingMessage()))
        }
    }
}

private fun resolveEffectiveTaskForToggle(
    taskToToggle: Task,
    pendingOriginal: Pair<Long, LocalDateTime>?,
): Pair<Task, Boolean> {
    val usesPendingReminderDate =
        pendingOriginal != null &&
            pendingOriginal.first == taskToToggle.id &&
            taskToToggle.periodicity != null
    val effectiveTask =
        if (usesPendingReminderDate) {
            taskToToggle.copy(reminderDate = pendingOriginal.second)
        } else {
            taskToToggle
        }
    return effectiveTask to usesPendingReminderDate
}

private suspend fun TasksViewModel.handleToggleResult(
    taskId: Long,
    result: ToggleTaskCompletionUseCase.Result,
) {
    when (result) {
        is ToggleTaskCompletionUseCase.Result.PeriodicCompleted -> {
            handleSchedulePermissionError(result.updateResult)
        }

        is ToggleTaskCompletionUseCase.Result.PeriodicRolledBack -> {
            expandTask(taskId)
            showSnackbar(R.string.msg_periodic_uncheck_rollback)
            handleSchedulePermissionError(result.updateResult)
        }

        is ToggleTaskCompletionUseCase.Result.ParentToggled -> {
            if (result.isCompleted) {
                collapseTask(taskId)
            } else {
                expandTask(taskId)
            }
            handleSchedulePermissionError(result.updateResult)
        }

        is ToggleTaskCompletionUseCase.Result.SubtaskToggled -> {
            handleSchedulePermissionError(result.updateResult)
        }
    }
}

internal suspend fun TasksViewModel.refreshEditingTaskIfNeeded(toggledTaskId: Long) {
    val editingId =
        mutableUiState.value.taskForm.editingTask
            ?.id
    if (editingId != toggledTaskId) return
    val refreshed =
        try {
            taskRepository.getTaskById(toggledTaskId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    if (refreshed != null) {
        mutableUiState.update {
            it.copy(
                taskForm =
                    it.taskForm.copy(
                        editingTask = refreshed,
                    ),
            )
        }
    }
}

internal suspend fun TasksViewModel.handleSchedulePermissionError(updateResult: UpdateTaskUseCase.Result) {
    if (updateResult is UpdateTaskUseCase.Result.Success &&
        updateResult.scheduleResult is ScheduleResult.PermissionError
    ) {
        mutableUiState.update { it.copy(showPermissionRequestDialog = true) }
        showSnackbar(R.string.msg_permission_needed)
    }
}

internal fun TasksViewModel.showTaskDialog(
    task: Task? = null,
    parentTaskId: Long? = null,
) {
    pendingOriginalReminderDate = null
    viewModelScope.launch {
        val parentTask =
            if (parentTaskId != null) {
                try {
                    taskRepository.getTaskById(parentTaskId)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    showSnackbar(
                        R.string.msg_error_loading_parent,
                        listOf(e.toUserFacingMessage()),
                    )
                    null
                }
            } else {
                null
            }

        mutableUiState.update {
            it.copy(
                taskForm =
                    TaskFormState(
                        isVisible = true,
                        editingTask = task,
                        priority = task?.priority,
                        reminderDate = task?.reminderDate,
                        periodicity = task?.periodicity,
                        periodicityInterval = task?.periodicityInterval ?: 1,
                        repeatDays = task?.repeatDays?.toPersistentSet(),
                        monthDayOption = task?.monthDayOption,
                        parentTaskId = parentTaskId,
                        parentTask = parentTask,
                    ),
            )
        }
    }
}

internal fun TasksViewModel.hideTaskDialog() {
    pendingOriginalReminderDate = null
    mutableUiState.update {
        it.copy(taskForm = TaskFormState())
    }
}

internal fun TasksViewModel.onSetPriority(priority: Priority) {
    mutableUiState.update { it.copy(taskForm = it.taskForm.copy(priority = priority)) }
}

internal fun TasksViewModel.onClearPriority() {
    mutableUiState.update { it.copy(taskForm = it.taskForm.copy(priority = null)) }
}

internal fun TasksViewModel.onSetPeriodicity(periodicity: Periodicity) {
    mutableUiState.update {
        val form = it.taskForm
        val isHourly = periodicity == Periodicity.HOURLY
        val wasHourly = form.periodicity == Periodicity.HOURLY
        val repeatDays =
            when {
                isHourly -> null
                periodicity == Periodicity.WEEKLY && form.repeatDays == null ->
                    form.reminderDate?.let { date ->
                        persistentSetOf(DayOfWeek.fromKotlinDayOfWeek(date.date.dayOfWeek))
                    }
                else -> form.repeatDays
            }
        val monthDayOption = if (isHourly) null else form.monthDayOption
        val interval =
            if (isHourly != wasHourly) 1 else form.periodicityInterval
        it.copy(
            taskForm =
                form.copy(
                    periodicity = periodicity,
                    periodicityInterval = interval,
                    repeatDays = repeatDays,
                    monthDayOption = monthDayOption,
                ),
        )
    }
}

internal fun TasksViewModel.onClearPeriodicity() {
    mutableUiState.update {
        it.copy(
            taskForm =
                it.taskForm.copy(
                    periodicity = null,
                    periodicityInterval = 1,
                    repeatDays = null,
                    monthDayOption = null,
                ),
        )
    }
}

internal fun TasksViewModel.onSetPeriodicityInterval(interval: Int) {
    mutableUiState.update {
        val form = it.taskForm
        val maxInterval =
            if (form.periodicity == Periodicity.HOURLY) Periodicity.MAX_HOURLY_INTERVAL else Int.MAX_VALUE
        val clamped = interval.coerceIn(1, maxInterval)
        it.copy(taskForm = form.copy(periodicityInterval = clamped))
    }
}

internal fun TasksViewModel.onSetRepeatDays(days: Set<DayOfWeek>?) {
    mutableUiState.update { it.copy(taskForm = it.taskForm.copy(repeatDays = days?.toPersistentSet())) }
}

internal fun TasksViewModel.onSetMonthDayOption(option: MonthDayOption?) {
    mutableUiState.update { it.copy(taskForm = it.taskForm.copy(monthDayOption = option)) }
}

internal fun TasksViewModel.openTaskFromNotificationInternal(
    taskId: Long,
    originalReminderDate: LocalDateTime? = null,
) {
    viewModelScope.launch {
        val task = taskRepository.getTaskById(taskId)
        if (task != null) {
            pendingOriginalReminderDate =
                originalReminderDate?.let { taskId to it }
            val sortOption = tasksScreenPreferencesRepository.getSortOption(task.categoryId)
            mutableUiState.update {
                it.copy(
                    selectedCategoryId = task.categoryId,
                    sortOption = sortOption,
                    taskForm =
                        TaskFormState(
                            isVisible = true,
                            editingTask = task,
                            priority = task.priority,
                            reminderDate = task.reminderDate,
                            periodicity = task.periodicity,
                            periodicityInterval = task.periodicityInterval,
                            repeatDays = task.repeatDays?.toPersistentSet(),
                            monthDayOption = task.monthDayOption,
                        ),
                )
            }
        }
    }
}

internal fun TasksViewModel.setReminder(
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    minute: Int,
) {
    val newReminderDate = LocalDateTime(year, month, day, hour, minute)
    mutableUiState.update { it.copy(taskForm = it.taskForm.copy(reminderDate = newReminderDate)) }
}

internal fun TasksViewModel.clearReminder() {
    mutableUiState.update {
        it.copy(
            taskForm =
                it.taskForm.copy(
                    reminderDate = null,
                    periodicity = null,
                    periodicityInterval = 1,
                    repeatDays = null,
                    monthDayOption = null,
                ),
        )
    }
}

internal fun TasksViewModel.toggleTaskExpanded(taskId: Long) {
    mutableUiState.update {
        val newExpandedIds =
            if (taskId in it.expandedTaskIds) {
                it.expandedTaskIds.remove(taskId)
            } else {
                it.expandedTaskIds.add(taskId)
            }
        it.copy(expandedTaskIds = newExpandedIds)
    }
}

internal fun TasksViewModel.expandTask(taskId: Long) {
    mutableUiState.update {
        it.copy(expandedTaskIds = it.expandedTaskIds.add(taskId))
    }
}

internal fun TasksViewModel.collapseTask(taskId: Long) {
    mutableUiState.update {
        it.copy(expandedTaskIds = it.expandedTaskIds.remove(taskId))
    }
}

internal fun TasksViewModel.clearTaskErrors() {
    mutableUiState.update {
        it.copy(taskForm = it.taskForm.copy(titleError = null, descriptionError = null))
    }
}

internal fun TasksViewModel.reorderTasks(
    fromIndex: Int,
    toIndex: Int,
    tasks: List<Task>,
) {
    viewModelScope.launch {
        try {
            reorderTasksUseCase(fromIndex, toIndex, tasks)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            showSnackbar(
                R.string.msg_task_reorder_failed,
                listOf(e.toUserFacingMessage()),
            )
        }
    }
}
