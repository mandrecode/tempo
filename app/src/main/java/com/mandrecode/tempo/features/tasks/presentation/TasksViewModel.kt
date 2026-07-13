package com.mandrecode.tempo.features.tasks.presentation

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import com.mandrecode.tempo.core.data.preferences.TasksScreenPreferencesRepository
import com.mandrecode.tempo.core.di.DefaultDispatcher
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.features.tasks.domain.usecase.ClearAllTaskRemindersUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.CreateCategoryUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.CreateTaskUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.DeleteCategoryUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.DeleteCompletedTasksUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.DeleteTaskUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.ReorderCategoriesUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.ReorderTasksUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.RestoreDeletedCategoryUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.RestoreDeletedTasksUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.SetDefaultCategoryUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.ToggleTaskCompletionUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.UpdateCategoryUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.UpdateTaskUseCase
import com.mandrecode.tempo.infrastructure.permissions.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.LocalDateTime

@HiltViewModel
class TasksViewModel
    @Inject
    constructor(
        internal val taskRepository: TaskRepository,
        internal val categoryRepository: CategoryRepository,
        internal val createTaskUseCase: CreateTaskUseCase,
        internal val updateTaskUseCase: UpdateTaskUseCase,
        internal val deleteTaskUseCase: DeleteTaskUseCase,
        internal val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase,
        internal val createCategoryUseCase: CreateCategoryUseCase,
        internal val updateCategoryUseCase: UpdateCategoryUseCase,
        internal val deleteCategoryUseCase: DeleteCategoryUseCase,
        internal val setDefaultCategoryUseCase: SetDefaultCategoryUseCase,
        internal val reorderCategoriesUseCase: ReorderCategoriesUseCase,
        internal val deleteCompletedTasksUseCase: DeleteCompletedTasksUseCase,
        internal val clearAllRemindersUseCase: ClearAllTaskRemindersUseCase,
        internal val reorderTasksUseCase: ReorderTasksUseCase,
        internal val permissionChecker: PermissionChecker,
        internal val tasksScreenPreferencesRepository: TasksScreenPreferencesRepository,
        @DefaultDispatcher internal val defaultDispatcher: CoroutineDispatcher,
        internal val restoreDeletedTasksUseCase: RestoreDeletedTasksUseCase? = null,
        internal val restoreDeletedCategoryUseCase: RestoreDeletedCategoryUseCase? = null,
    ) : ViewModel() {
        private val initialSelectedCategoryId = tasksScreenPreferencesRepository.getSelectedCategoryId()

        private val _uiState =
            MutableStateFlow(
                TasksContract.UiState(
                    selectedCategoryId = initialSelectedCategoryId,
                    sortOption =
                        tasksScreenPreferencesRepository.getSortOption(initialSelectedCategoryId),
                    showCompletedTasks = tasksScreenPreferencesRepository.getShowCompletedTasks(),
                ),
            )
        internal val mutableUiState: MutableStateFlow<TasksContract.UiState>
            get() = _uiState
        val uiState: StateFlow<TasksContract.UiState> = _uiState.asStateFlow()

        private val _uiEffect = Channel<TasksContract.UiEffect>(Channel.BUFFERED)
        val uiEffect = _uiEffect.receiveAsFlow()

        // For periodic tasks opened from a reminder notification, TaskReminderReceiver
        // pre-advances reminderDate to the next occurrence before the user sees the
        // sheet. We stash the original reminderDate here so that if the user toggles
        // completion from the bottom sheet, the use case computes the next occurrence
        // from the correct anchor (mirrors MarkAsCompletedReceiver). Cleared after the
        // first toggle, when a different task is opened, or when the form is dismissed.
        internal var pendingOriginalReminderDate: Pair<Long, LocalDateTime>? = null
        internal val pendingDeletionSnapshots = mutableMapOf<Long, PendingTaskDeletion>()
        internal var nextDeletionToken = 0L

        internal suspend fun showSnackbar(
            @StringRes messageResId: Int,
            formatArgs: List<Any> = emptyList(),
            @StringRes actionResId: Int? = null,
            deletionToken: Long? = null,
        ) {
            _uiEffect.send(
                TasksContract.UiEffect.ShowSnackbar(
                    messageResId = messageResId,
                    formatArgs = formatArgs,
                    actionResId = actionResId,
                    deletionToken = deletionToken,
                ),
            )
        }

        init {
            loadData()
        }

        fun onEvent(event: TasksContract.UiEvent) {
            when (event) {
                is TasksContract.UiEvent.CategorySelected -> selectCategory(event.categoryId)
                is TasksContract.UiEvent.ShowCategoryDialog -> showCategoryDialog(event.category)
                is TasksContract.UiEvent.HideCategoryDialog -> hideCategoryDialog()
                is TasksContract.UiEvent.AddCategory -> addCategory(event.name, event.color, event.icon)
                is TasksContract.UiEvent.UpdateCategory -> updateCategory(event.category)
                is TasksContract.UiEvent.RequestDeleteCategory -> requestDeleteCategory(event.category)
                is TasksContract.UiEvent.CancelDeleteCategory -> cancelDeleteCategory()
                is TasksContract.UiEvent.DeleteCategory -> deleteCategory(event.category)
                is TasksContract.UiEvent.ClearCategoryError -> clearCategoryError()
                is TasksContract.UiEvent.SetDefaultCategory -> setDefaultCategory(event.categoryId)
                is TasksContract.UiEvent.ReorderCategories ->
                    reorderCategories(event.fromIndex, event.toIndex, event.categories)
                is TasksContract.UiEvent.ShowTaskDialog -> showTaskDialog(event.task, event.parentTaskId)
                is TasksContract.UiEvent.HideTaskDialog -> hideTaskDialog()
                is TasksContract.UiEvent.CreateOrUpdateTask ->
                    createOrUpdateTask(
                        event.title,
                        event.description,
                        event.categoryId,
                        event.parentTaskId,
                        event.autoSave,
                    )
                is TasksContract.UiEvent.ToggleTaskCompletion -> toggleTaskCompletion(event.task)
                is TasksContract.UiEvent.RequestDeleteTask -> requestDeleteTask(event.task)
                is TasksContract.UiEvent.CancelDeleteTask -> cancelDeleteTask()
                is TasksContract.UiEvent.ConfirmDeleteTask -> confirmDeleteTask(event.task)
                is TasksContract.UiEvent.ClearTaskErrors -> clearTaskErrors()
                is TasksContract.UiEvent.SetPriority -> onSetPriority(event.priority)
                is TasksContract.UiEvent.ClearPriority -> onClearPriority()
                is TasksContract.UiEvent.SetReminder ->
                    setReminder(
                        event.year,
                        event.month,
                        event.day,
                        event.hour,
                        event.minute,
                    )
                is TasksContract.UiEvent.ClearReminder -> clearReminder()
                is TasksContract.UiEvent.SetPeriodicity -> onSetPeriodicity(event.periodicity)
                is TasksContract.UiEvent.ClearPeriodicity -> onClearPeriodicity()
                is TasksContract.UiEvent.SetPeriodicityInterval -> onSetPeriodicityInterval(event.interval)
                is TasksContract.UiEvent.SetRepeatDays -> onSetRepeatDays(event.days)
                is TasksContract.UiEvent.SetMonthDayOption -> onSetMonthDayOption(event.option)
                is TasksContract.UiEvent.SetSortOption -> setSortOption(event.sortOption)
                is TasksContract.UiEvent.ShowSortMenu -> showSortMenu()
                is TasksContract.UiEvent.HideSortMenu -> hideSortMenu()
                is TasksContract.UiEvent.ToggleCompletedTasksVisibility -> toggleCompletedTasksVisibility()
                is TasksContract.UiEvent.ToggleTaskExpanded -> toggleTaskExpanded(event.taskId)
                is TasksContract.UiEvent.ReorderTasks ->
                    reorderTasks(
                        event.fromIndex,
                        event.toIndex,
                        event.tasks,
                    )
                is TasksContract.UiEvent.ReorderSubtasks ->
                    reorderTasks(
                        event.fromIndex,
                        event.toIndex,
                        event.subtasks,
                    )
                is TasksContract.UiEvent.RequestDeleteCompletedTasks -> requestDeleteCompletedTasks()
                is TasksContract.UiEvent.CancelDeleteCompletedTasks -> cancelDeleteCompletedTasks()
                is TasksContract.UiEvent.ConfirmDeleteCompletedTasks -> confirmDeleteCompletedTasks()
                is TasksContract.UiEvent.DismissPermissionRequestDialog -> dismissPermissionRequestDialog()
                is TasksContract.UiEvent.DismissPermissionRevokedDialog -> dismissPermissionRevokedDialog()
                is TasksContract.UiEvent.ConfirmClearAllReminders -> confirmClearAllReminders()
                is TasksContract.UiEvent.OnPermissionsGranted -> onPermissionsGranted()
                is TasksContract.UiEvent.UndoDeletion -> undoDeletion(event.token)
                is TasksContract.UiEvent.DismissDeletionUndo -> dismissDeletionUndo(event.token)
            }
        }

        fun getSubtasks(parentId: Long): Flow<List<Task>> = taskRepository.getSubtasks(parentId)

        fun openTaskFromNotification(
            taskId: Long,
            originalReminderDate: LocalDateTime? = null,
        ) {
            openTaskFromNotificationInternal(taskId, originalReminderDate)
        }

        fun checkPermissionsAndSyncReminders() {
            checkPermissionsAndSyncRemindersInternal()
        }
    }
