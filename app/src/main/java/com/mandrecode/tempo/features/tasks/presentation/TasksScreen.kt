package com.mandrecode.tempo.features.tasks.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.adaptive.DockedEditorPadding
import com.mandrecode.tempo.core.ui.adaptive.DockedEditorWidth
import com.mandrecode.tempo.core.ui.adaptive.SheetPlacement
import com.mandrecode.tempo.core.ui.adaptive.dockedEditorEnterTransition
import com.mandrecode.tempo.core.ui.adaptive.dockedEditorExitTransition
import com.mandrecode.tempo.core.ui.adaptive.rememberSheetPlacement
import com.mandrecode.tempo.core.ui.components.ExpressiveSnackbarHost
import com.mandrecode.tempo.core.ui.components.HandleReminderPermissions
import com.mandrecode.tempo.core.ui.components.PermissionRevokedDialog
import com.mandrecode.tempo.core.ui.navigation.FloatingRailContentStartPadding
import com.mandrecode.tempo.core.ui.navigation.FloatingRailExpandedContentStartPadding
import com.mandrecode.tempo.core.ui.navigation.PendingNotificationAction
import com.mandrecode.tempo.core.ui.navigation.TasksFloatingBarState
import com.mandrecode.tempo.core.ui.navigation.adaptiveScreenContentLayout
import com.mandrecode.tempo.core.ui.navigation.isFloatingNavigationRailLayout
import com.mandrecode.tempo.core.ui.util.rememberFrozenWhileHidden
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
    viewModel: TasksViewModel = viewModel(),
    pendingNotificationAction: PendingNotificationAction? = null,
    onConsumePendingNotificationAction: () -> Unit = {},
    topBar: @Composable () -> Unit = {},
    showAddTaskRailButton: Boolean = false,
    onFloatingBarStateChange: (TasksFloatingBarState) -> Unit = {},
    onDockedEditorVisibilityChange: (Boolean) -> Unit = {},
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
        when (val action = pendingNotificationAction) {
            is PendingNotificationAction.OpenTask -> {
                viewModel.openTaskFromNotification(action.taskId, action.originalReminderDate)
                currentOnConsumePendingNotificationAction()
            }

            PendingNotificationAction.OpenNewTaskDialog -> {
                viewModel.onEvent(TasksContract.UiEvent.ShowTaskDialog())
                currentOnConsumePendingNotificationAction()
            }

            else -> Unit
        }
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

    val isRailLayout = isFloatingNavigationRailLayout()
    val editorPlacement = rememberSheetPlacement()
    val isDockedEditor = editorPlacement == SheetPlacement.DockedPane
    val railContentClearance =
        when {
            !isRailLayout -> 0.dp
            isDockedEditor -> FloatingRailExpandedContentStartPadding
            else -> FloatingRailContentStartPadding
        }
    var editorDismissRequestKey by remember { mutableIntStateOf(0) }
    val currentOnDockedEditorVisibilityChange by rememberUpdatedState(onDockedEditorVisibilityChange)
    LaunchedEffect(isDockedEditor, uiState.taskForm.isVisible) {
        currentOnDockedEditorVisibilityChange(isDockedEditor && uiState.taskForm.isVisible)
    }
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
    val onContentEvent: (TasksContract.UiEvent) -> Unit = { event ->
        val isSelectedCardActivated =
            isDockedEditor &&
                uiState.taskForm.isVisible &&
                event is TasksContract.UiEvent.ShowTaskDialog &&
                event.task?.id != null &&
                event.task.id == uiState.taskForm.editingTask?.id
        if (isSelectedCardActivated) {
            editorDismissRequestKey++
        } else {
            viewModel.onEvent(event)
        }
    }

    SideEffect {
        onFloatingBarStateChange(
            TasksFloatingBarState(
                visible = showAddTaskRailButton,
                compactSoloAction = compactSoloAction,
                hasCompletedTasks = hasCompletedTasks,
                sortOption = uiState.sortOption,
                onAddTask = onAddTask,
                onSort = onSort,
                sortMenuExpanded = isRailLayout && uiState.showSortBottomSheet,
                onSelectSortOption = { viewModel.onEvent(TasksContract.UiEvent.SetSortOption(it)) },
                onDismissSort = { viewModel.onEvent(TasksContract.UiEvent.HideSortMenu) },
                onClearCompleted = onClearCompleted,
            ),
        )
    }

    val currentEditorDismissRequestKey = rememberUpdatedState(editorDismissRequestKey)
    val editorContent =
        remember(viewModel) {
            movableContentOf { state: TasksContract.UiState, placement: SheetPlacement ->
                TaskEditor(
                    uiState = state,
                    onEvent = viewModel::onEvent,
                    placement = placement,
                    dismissRequestKey = currentEditorDismissRequestKey.value,
                )
            }
        }
    // TempoDockedSheet resets taskForm the instant dismissal is requested, before the docked
    // pane's own shrink-out animation finishes. Freezing the last visible state here stops the
    // exiting pane from recomposing into a blank "new task" form mid-animation.
    val frozenUiState = rememberFrozenWhileHidden(uiState, isLive = uiState.taskForm.isVisible)

    Box(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Scaffold(
                    modifier = Modifier.adaptiveScreenContentLayout(railClearance = railContentClearance),
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets(0),
                    topBar = topBar,
                ) {
                    Box(
                        modifier = Modifier.padding(it).fillMaxSize(),
                    ) {
                        TasksContent(
                            uiState = uiState,
                            onEvent = onContentEvent,
                            onScrolledFromTopChange = { isListScrolledFromTop.value = it },
                            selectedTaskId = uiState.taskForm.editingTask?.id,
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
            AnimatedVisibility(
                visible = isDockedEditor && uiState.taskForm.isVisible,
                enter = dockedEditorEnterTransition(),
                exit = dockedEditorExitTransition(),
            ) {
                Box(
                    modifier =
                        Modifier
                            .width(DockedEditorWidth)
                            .fillMaxHeight()
                            .padding(
                                end = DockedEditorPadding,
                                top = DockedEditorPadding,
                                bottom = DockedEditorPadding,
                            ),
                ) {
                    editorContent(frozenUiState, editorPlacement)
                }
            }
        }

        if (uiState.showPermissionRevokedDialog) {
            PermissionRevokedDialog(
                permissionInfo = uiState.permissionInfo,
                context = context,
                dismissPermissionRevokedDialog = {
                    viewModel.onEvent(TasksContract.UiEvent.DismissPermissionRevokedDialog)
                },
                confirmClearAllReminders = {
                    viewModel.onEvent(TasksContract.UiEvent.ConfirmClearAllReminders)
                },
                notificationPrefixRes = R.string.permission_revoked_notifications_prefix,
                fallbackRes = R.string.permission_revoked_fallback_tasks,
            )
        }

        if (!isDockedEditor && uiState.taskForm.isVisible) {
            editorContent(uiState, editorPlacement)
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
                onConfirmDeleteCompletedTasks = {
                    viewModel.onEvent(TasksContract.UiEvent.ConfirmDeleteCompletedTasks)
                },
            )
        }

        if (uiState.showSortBottomSheet && !isRailLayout) {
            SortBottomSheet(
                currentSortOption = uiState.sortOption,
                onSelectSortOption = {
                    viewModel.onEvent(TasksContract.UiEvent.SetSortOption(it))
                },
                onDismiss = { viewModel.onEvent(TasksContract.UiEvent.HideSortMenu) },
            )
        }
    }
}

@Composable
private fun TaskEditor(
    uiState: TasksContract.UiState,
    onEvent: (TasksContract.UiEvent) -> Unit,
    placement: SheetPlacement,
    dismissRequestKey: Int,
) {
    TaskBottomSheet(
        categories = uiState.categories,
        selectedCategoryIdFromFilter = uiState.taskEditorCategoryId(),
        formState = uiState.taskForm,
        onSetPriority = { onEvent(TasksContract.UiEvent.SetPriority(it)) },
        onClearPriority = { onEvent(TasksContract.UiEvent.ClearPriority) },
        onSetReminder = { year, month, day, hour, minute ->
            onEvent(TasksContract.UiEvent.SetReminder(year, month, day, hour, minute))
        },
        onClearReminder = { onEvent(TasksContract.UiEvent.ClearReminder) },
        onSetPeriodicity = { onEvent(TasksContract.UiEvent.SetPeriodicity(it)) },
        onClearPeriodicity = { onEvent(TasksContract.UiEvent.ClearPeriodicity) },
        onSetPeriodicityInterval = { onEvent(TasksContract.UiEvent.SetPeriodicityInterval(it)) },
        onSetRepeatDays = { onEvent(TasksContract.UiEvent.SetRepeatDays(it)) },
        onSetMonthDayOption = { onEvent(TasksContract.UiEvent.SetMonthDayOption(it)) },
        onDismiss = { onEvent(TasksContract.UiEvent.HideTaskDialog) },
        onClearErrors = { onEvent(TasksContract.UiEvent.ClearTaskErrors) },
        onConfirm = { title, description, categoryId ->
            onEvent(
                TasksContract.UiEvent.CreateOrUpdateTask(
                    title = title,
                    description = description,
                    categoryId = categoryId,
                    parentTaskId = uiState.taskForm.parentTaskId,
                    autoSave = false,
                ),
            )
        },
        onAutoSave = { title, description, categoryId ->
            onEvent(
                TasksContract.UiEvent.CreateOrUpdateTask(
                    title = title,
                    description = description,
                    categoryId = categoryId,
                    parentTaskId = uiState.taskForm.parentTaskId,
                    autoSave = true,
                ),
            )
        },
        task = uiState.taskForm.editingTask,
        onDelete =
            uiState.taskForm.editingTask?.let { editingTask ->
                { onEvent(TasksContract.UiEvent.RequestDeleteTask(editingTask)) }
            },
        onToggleCompletion = { onEvent(TasksContract.UiEvent.ToggleTaskCompletion(it)) },
        placement = placement,
        dismissRequestKey = dismissRequestKey,
    )
}

private fun TasksContract.UiState.taskEditorCategoryId(): Long =
    when {
        taskForm.editingTask != null -> taskForm.editingTask.categoryId
        taskForm.parentTask != null -> taskForm.parentTask.categoryId
        else -> selectedCategoryId
    }
