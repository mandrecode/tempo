package com.mandrecode.tempo.features.tasks.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.ExpressiveSnackbarHost
import com.mandrecode.tempo.core.ui.components.HandleReminderPermissions
import com.mandrecode.tempo.core.ui.components.PermissionRevokedDialog
import com.mandrecode.tempo.core.ui.navigation.PendingNotificationAction
import com.mandrecode.tempo.core.ui.navigation.TasksFloatingBarState
import com.mandrecode.tempo.core.ui.navigation.floatingRailContentPadding
import com.mandrecode.tempo.core.ui.navigation.isFloatingNavigationRailLayout
import com.mandrecode.tempo.features.tasks.presentation.components.CategoryEditSheet
import com.mandrecode.tempo.features.tasks.presentation.components.TaskBottomSheet
import com.mandrecode.tempo.features.tasks.presentation.components.dialogs.DeleteCategoryDialog
import com.mandrecode.tempo.features.tasks.presentation.components.dialogs.DeleteCompletedConfirmationDialog
import com.mandrecode.tempo.features.tasks.presentation.components.dialogs.DeleteTaskConfirmDialog
import com.mandrecode.tempo.features.tasks.presentation.components.sections.SortBottomSheet

private val FLOATING_BAR_SNACKBAR_BOTTOM_PADDING = 88.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    isSingleTabMode: Boolean = false,
    viewModel: TasksViewModel = hiltViewModel(),
    pendingNotificationAction: PendingNotificationAction? = null,
    onConsumePendingNotificationAction: () -> Unit = {},
    topBar: @Composable () -> Unit = {},
    showAddTaskRailButton: Boolean = false,
    onFloatingBarStateChange: (TasksFloatingBarState) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnConsumePendingNotificationAction by rememberUpdatedState(onConsumePendingNotificationAction)
    val isListScrolledFromTop = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkPermissionsAndSyncReminders()
    }

    LaunchedEffect(pendingNotificationAction) {
        val action = pendingNotificationAction as? PendingNotificationAction.OpenTask ?: return@LaunchedEffect
        viewModel.openTaskFromNotification(action.taskId, action.originalReminderDate)
        currentOnConsumePendingNotificationAction()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    viewModel.checkPermissionsAndSyncReminders()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is TasksContract.UiEffect.ShowSnackbar -> {
                    @Suppress("LocalContextGetResourceValueCall")
                    val message =
                        if (effect.formatArgs.isNotEmpty()) {
                            context.getString(effect.messageResId, *effect.formatArgs.toTypedArray())
                        } else {
                            context.getString(effect.messageResId)
                        }
                    val actionLabel = effect.actionResId?.let(context::getString)
                    val result =
                        snackbarHostState.showSnackbar(
                            message = message,
                            actionLabel = actionLabel,
                            duration = if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Long,
                        )
                    effect.deletionToken?.let { token ->
                        viewModel.onEvent(
                            if (result == SnackbarResult.ActionPerformed) {
                                TasksContract.UiEvent.UndoDeletion(token)
                            } else {
                                TasksContract.UiEvent.DismissDeletionUndo(token)
                            },
                        )
                    }
                }
            }
        }
    }

    if (uiState.showPermissionRevokedDialog) {
        PermissionRevokedDialog(
            permissionInfo = uiState.permissionInfo,
            context = context,
            dismissPermissionRevokedDialog = { viewModel.onEvent(TasksContract.UiEvent.DismissPermissionRevokedDialog) },
            confirmClearAllReminders = { viewModel.onEvent(TasksContract.UiEvent.ConfirmClearAllReminders) },
            notificationPrefixRes = R.string.permission_revoked_notifications_prefix,
            fallbackRes = R.string.permission_revoked_fallback_tasks,
        )
    }

    val isRailLayout = isFloatingNavigationRailLayout()
    val isSheetVisible =
        uiState.taskForm.isVisible ||
            uiState.categoryForm.isVisible ||
            uiState.showSortBottomSheet
    val shouldShowFloatingRail = !isRailLayout || !isSheetVisible
    val hasCompletedTasks =
        remember(uiState.tasks, uiState.selectedCategoryId) {
            uiState.tasks.any {
                it.categoryId == uiState.selectedCategoryId && it.isCompleted && it.parentTaskId == null
            }
        }
    val onAddTask =
        remember(viewModel) {
            { viewModel.onEvent(TasksContract.UiEvent.ShowTaskDialog()) }
        }
    val onSort =
        remember(viewModel) {
            { viewModel.onEvent(TasksContract.UiEvent.ShowSortMenu) }
        }
    val onClearCompleted =
        remember(viewModel) {
            { viewModel.onEvent(TasksContract.UiEvent.RequestDeleteCompletedTasks) }
        }
    val compactSoloAction = isSingleTabMode && isListScrolledFromTop.value

    SideEffect {
        onFloatingBarStateChange(
            TasksFloatingBarState(
                visible = shouldShowFloatingRail && showAddTaskRailButton,
                compactSoloAction = compactSoloAction,
                hasCompletedTasks = hasCompletedTasks,
                sortOption = uiState.sortOption,
                onAddTask = onAddTask,
                onSort = onSort,
                onClearCompleted = onClearCompleted,
            ),
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.floatingRailContentPadding(isRailLayout),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0),
            topBar = topBar,
        ) {
            Box(
                modifier = Modifier.padding(it).fillMaxSize(),
            ) {
                TasksContent(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    onScrolledFromTopChange = { isListScrolledFromTop.value = it },
                )

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                            .padding(
                                bottom =
                                    if (isRailLayout) {
                                        20.dp
                                    } else {
                                        FLOATING_BAR_SNACKBAR_BOTTOM_PADDING
                                    },
                            ),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    ExpressiveSnackbarHost(snackbarHostState)
                }
            }
        }
    }

    if (!uiState.showPermissionRevokedDialog) {
        HandleReminderPermissions(
            show = uiState.showPermissionRequestDialog,
            onGrantPermissions = {
                viewModel.onEvent(TasksContract.UiEvent.DismissPermissionRequestDialog)
                viewModel.onEvent(TasksContract.UiEvent.OnPermissionsGranted)
            },
            onDismiss = {
                viewModel.onEvent(TasksContract.UiEvent.DismissPermissionRequestDialog)
            },
        )
    }

    if (uiState.taskForm.isVisible) {
        val categoryIdFromContext =
            when {
                uiState.taskForm.editingTask != null ->
                    uiState.taskForm.editingTask?.categoryId
                        ?: uiState.selectedCategoryId

                uiState.taskForm.parentTask != null ->
                    uiState.taskForm.parentTask?.categoryId
                        ?: uiState.selectedCategoryId

                else -> uiState.selectedCategoryId
            }

        TaskBottomSheet(
            categories = uiState.categories,
            selectedCategoryIdFromFilter = categoryIdFromContext,
            formState = uiState.taskForm,
            onSetPriority = { priority -> viewModel.onEvent(TasksContract.UiEvent.SetPriority(priority)) },
            onClearPriority = { viewModel.onEvent(TasksContract.UiEvent.ClearPriority) },
            onSetReminder = { year, month, day, hour, minute ->
                viewModel.onEvent(TasksContract.UiEvent.SetReminder(year, month, day, hour, minute))
            },
            onClearReminder = { viewModel.onEvent(TasksContract.UiEvent.ClearReminder) },
            onSetPeriodicity = { periodicity -> viewModel.onEvent(TasksContract.UiEvent.SetPeriodicity(periodicity)) },
            onClearPeriodicity = { viewModel.onEvent(TasksContract.UiEvent.ClearPeriodicity) },
            onSetPeriodicityInterval = { interval ->
                viewModel.onEvent(TasksContract.UiEvent.SetPeriodicityInterval(interval))
            },
            onSetRepeatDays = { days -> viewModel.onEvent(TasksContract.UiEvent.SetRepeatDays(days)) },
            onSetMonthDayOption = { option -> viewModel.onEvent(TasksContract.UiEvent.SetMonthDayOption(option)) },
            onDismiss = { viewModel.onEvent(TasksContract.UiEvent.HideTaskDialog) },
            onClearErrors = { viewModel.onEvent(TasksContract.UiEvent.ClearTaskErrors) },
            onConfirm = { title, description, categoryId ->
                viewModel.onEvent(
                    TasksContract.UiEvent.CreateOrUpdateTask(
                        title,
                        description,
                        categoryId,
                        parentTaskId = uiState.taskForm.parentTaskId,
                        autoSave = false,
                    ),
                )
            },
            onAutoSave = { title, description, categoryId ->
                viewModel.onEvent(
                    TasksContract.UiEvent.CreateOrUpdateTask(
                        title,
                        description,
                        categoryId,
                        parentTaskId = uiState.taskForm.parentTaskId,
                        autoSave = true,
                    ),
                )
            },
            task = uiState.taskForm.editingTask,
            onDelete =
                uiState.taskForm.editingTask?.let { editingTask ->
                    {
                        viewModel.onEvent(TasksContract.UiEvent.RequestDeleteTask(editingTask))
                    }
                },
            onToggleCompletion = { task ->
                viewModel.onEvent(TasksContract.UiEvent.ToggleTaskCompletion(task))
            },
        )
    }

    if (uiState.categoryForm.isVisible) {
        val editingCategory = uiState.categoryForm.editingCategory
        CategoryEditSheet(
            category = editingCategory,
            categories = uiState.categories,
            nameError = uiState.categoryForm.nameError,
            onDismiss = { viewModel.onEvent(TasksContract.UiEvent.HideCategoryDialog) },
            onClearError = { viewModel.onEvent(TasksContract.UiEvent.ClearCategoryError) },
            onSave = { name, color, icon, isDefault ->
                if (editingCategory != null) {
                    viewModel.onEvent(
                        TasksContract.UiEvent.UpdateCategory(
                            editingCategory.copy(
                                name = name,
                                color = color,
                                icon = icon,
                                isDefault = isDefault,
                            ),
                        ),
                    )
                } else {
                    viewModel.onEvent(
                        TasksContract.UiEvent.AddCategory(
                            name = name,
                            color = color,
                            icon = icon,
                        ),
                    )
                }
            },
            onDelete =
                if (editingCategory != null && !editingCategory.isDefault) {
                    {
                        viewModel.onEvent(TasksContract.UiEvent.RequestDeleteCategory(editingCategory))
                    }
                } else {
                    null
                },
        )
    }

    uiState.categoryToDelete?.let { categoryToDelete ->
        if (uiState.showDeleteCategoryConfirmationDialog) {
            DeleteCategoryDialog(
                onCancelDeleteCategory = { viewModel.onEvent(TasksContract.UiEvent.CancelDeleteCategory) },
                onDeleteCategory = { viewModel.onEvent(TasksContract.UiEvent.DeleteCategory(it)) },
                categoryToDelete = categoryToDelete,
            )
        }
    }

    if (uiState.showDeleteTaskConfirmationDialog && uiState.taskToDelete != null) {
        DeleteTaskConfirmDialog(
            onCancelDeleteTask = { viewModel.onEvent(TasksContract.UiEvent.CancelDeleteTask) },
            onConfirmDeleteTask = { viewModel.onEvent(TasksContract.UiEvent.ConfirmDeleteTask(it)) },
            taskToDelete = uiState.taskToDelete,
            subtasksCount = uiState.taskToDeleteSubtasksCount,
        )
    }

    if (uiState.showDeleteCompletedConfirmationDialog) {
        DeleteCompletedConfirmationDialog(
            onCancelDeleteCompletedTasks = { viewModel.onEvent(TasksContract.UiEvent.CancelDeleteCompletedTasks) },
            onConfirmDeleteCompletedTasks = { viewModel.onEvent(TasksContract.UiEvent.ConfirmDeleteCompletedTasks) },
        )
    }

    if (uiState.showSortBottomSheet) {
        SortBottomSheet(
            currentSortOption = uiState.sortOption,
            onSelectSortOption = {
                viewModel.onEvent(TasksContract.UiEvent.SetSortOption(it))
            },
            onDismiss = { viewModel.onEvent(TasksContract.UiEvent.HideSortMenu) },
        )
    }
}
