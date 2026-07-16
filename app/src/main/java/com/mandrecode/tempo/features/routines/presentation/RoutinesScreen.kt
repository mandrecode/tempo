package com.mandrecode.tempo.features.routines.presentation

import android.content.Context
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
import com.mandrecode.tempo.core.ui.navigation.RoutinesFloatingBarState
import com.mandrecode.tempo.core.ui.navigation.adaptiveScreenContentLayout
import com.mandrecode.tempo.core.ui.navigation.floatingRailContentClearance
import com.mandrecode.tempo.core.ui.navigation.isFloatingNavigationRailLayout
import com.mandrecode.tempo.features.routines.presentation.components.HabitBottomSheet
import com.mandrecode.tempo.features.routines.presentation.components.dialogs.ClearRemindersConfirmDialog
import com.mandrecode.tempo.features.routines.presentation.components.dialogs.DeleteHabitChainConfirmDialog
import com.mandrecode.tempo.features.routines.presentation.components.dialogs.DeleteHabitConfirmDialog
import com.mandrecode.tempo.features.routines.presentation.components.dialogs.EmptyHabitChainConfirmDialog

private val FLOATING_BAR_SNACKBAR_BOTTOM_PADDING = 88.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    modifier: Modifier = Modifier,
    isSingleTabMode: Boolean = false,
    viewModel: RoutinesViewModel = viewModel(),
    pendingNotificationAction: PendingNotificationAction? = null,
    onConsumePendingNotificationAction: () -> Unit = {},
    topBar: @Composable () -> Unit = {},
    showAddHabitRailButton: Boolean = false,
    onFloatingBarStateChange: (RoutinesFloatingBarState) -> Unit = {},
    onDockedEditorVisibilityChange: (Boolean) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val isListScrolledFromTop = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkPermissionsAndSyncReminders()
    }

    val currentOnConsumePendingNotificationAction by rememberUpdatedState(onConsumePendingNotificationAction)

    LaunchedEffect(pendingNotificationAction) {
        when (val action = pendingNotificationAction) {
            is PendingNotificationAction.OpenHabit -> viewModel.openHabitFromNotification(action.habitId)
            is PendingNotificationAction.OpenHabitChain ->
                viewModel.openHabitChainFromNotification(
                    chainId = action.chainId,
                    scheduledDate = action.scheduledDate,
                )

            else -> return@LaunchedEffect
        }
        currentOnConsumePendingNotificationAction()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.checkPermissionsAndSyncReminders()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is RoutinesContract.UiEffect.ShowSnackbar ->
                    showRoutinesSnackbar(effect, context, snackbarHostState, viewModel::onEvent)
            }
        }
    }

    val isRailLayout = isFloatingNavigationRailLayout()
    val editorPlacement = rememberSheetPlacement()
    val isDockedEditor = editorPlacement == SheetPlacement.DockedPane
    val railContentClearance = routinesRailClearance(isRailLayout, isDockedEditor)
    var editorDismissRequestKey by remember { mutableIntStateOf(0) }
    val currentOnDockedEditorVisibilityChange by rememberUpdatedState(onDockedEditorVisibilityChange)
    LaunchedEffect(isDockedEditor, uiState.habitForm.isVisible) {
        currentOnDockedEditorVisibilityChange(isDockedEditor && uiState.habitForm.isVisible)
    }
    val compactSoloAction = isSingleTabMode && isListScrolledFromTop.value
    val onContentEvent: (RoutinesContract.UiEvent) -> Unit = { event ->
        handleRoutinesContentEvent(
            event = event,
            uiState = uiState,
            isDockedEditor = isDockedEditor,
            requestEditorDismiss = { editorDismissRequestKey++ },
            onEvent = viewModel::onEvent,
        )
    }
    val onShowHabitBottomSheet =
        remember(viewModel) {
            { viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet()) }
        }

    SideEffect {
        onFloatingBarStateChange(
            RoutinesFloatingBarState(
                visible = showAddHabitRailButton,
                compactSoloAction = compactSoloAction,
                onAddHabit = onShowHabitBottomSheet,
            ),
        )
    }

    val currentEditorDismissRequestKey = rememberUpdatedState(editorDismissRequestKey)
    val editorContent =
        remember(viewModel) {
            movableContentOf { state: RoutinesContract.UiState, placement: SheetPlacement ->
                HabitEditor(
                    uiState = state,
                    onEvent = viewModel::onEvent,
                    placement = placement,
                    dismissRequestKey = currentEditorDismissRequestKey.value,
                )
            }
        }

    Row(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Scaffold(
                modifier = Modifier.adaptiveScreenContentLayout(railClearance = railContentClearance),
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets(0),
                topBar = topBar,
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                    RoutinesContent(
                        uiState = uiState,
                        onEvent = onContentEvent,
                        onScrolledFromTopChange = { isListScrolledFromTop.value = it },
                        showAddHabitButton = !showAddHabitRailButton,
                        selectedHabitId = uiState.habitForm.editingHabit?.id,
                        selectedHabitChainId = uiState.habitForm.editingHabitChain?.id,
                    )

                    RoutinesDialogs(
                        uiState = uiState,
                        context = context,
                        onEvent = viewModel::onEvent,
                    )

                    RoutinesSnackbar(
                        snackbarHostState = snackbarHostState,
                        isRailLayout = isRailLayout,
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = isDockedEditor && uiState.habitForm.isVisible,
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
                editorContent(uiState, editorPlacement)
            }
        }
    }

    if (!isDockedEditor && uiState.habitForm.isVisible) {
        editorContent(uiState, editorPlacement)
    }
}

private fun routinesRailClearance(
    isRailLayout: Boolean,
    isDockedEditor: Boolean,
) = when {
    !isRailLayout -> 0.dp
    isDockedEditor -> FloatingRailExpandedContentStartPadding
    else -> FloatingRailContentStartPadding
}

private fun handleRoutinesContentEvent(
    event: RoutinesContract.UiEvent,
    uiState: RoutinesContract.UiState,
    isDockedEditor: Boolean,
    requestEditorDismiss: () -> Unit,
    onEvent: (RoutinesContract.UiEvent) -> Unit,
) {
    if (event.targetsOpenDockedEditor(uiState, isDockedEditor)) {
        requestEditorDismiss()
    } else {
        onEvent(event)
    }
}

private fun RoutinesContract.UiEvent.targetsOpenDockedEditor(
    uiState: RoutinesContract.UiState,
    isDockedEditor: Boolean,
): Boolean {
    if (!isDockedEditor || !uiState.habitForm.isVisible) return false
    return when (this) {
        is RoutinesContract.UiEvent.ShowHabitBottomSheet ->
            habit?.id != null && habit.id == uiState.habitForm.editingHabit?.id

        is RoutinesContract.UiEvent.ShowHabitChainBottomSheet ->
            habitChain?.id != null && habitChain.id == uiState.habitForm.editingHabitChain?.id

        else -> false
    }
}

@Composable
private fun HabitEditor(
    uiState: RoutinesContract.UiState,
    onEvent: (RoutinesContract.UiEvent) -> Unit,
    placement: SheetPlacement,
    dismissRequestKey: Int,
) {
    HabitBottomSheet(
        formState = uiState.habitForm,
        selectedDate = uiState.selectedDate,
        habits = uiState.habits,
        habitChains = uiState.habitChains,
        onSelectTab = { onEvent(RoutinesContract.UiEvent.SetSelectedTab(it)) },
        onSetHabitType = { onEvent(RoutinesContract.UiEvent.SetHabitType(it)) },
        onSetReminder = { year, month, day, hour, minute ->
            onEvent(RoutinesContract.UiEvent.SetReminder(year, month, day, hour, minute))
        },
        onClearReminder = { onEvent(RoutinesContract.UiEvent.ClearReminder) },
        onSetColorKey = { onEvent(RoutinesContract.UiEvent.SetColorKey(it)) },
        onClearColor = { onEvent(RoutinesContract.UiEvent.ClearColor) },
        onSetIcon = { onEvent(RoutinesContract.UiEvent.SetIcon(it)) },
        onClearIcon = { onEvent(RoutinesContract.UiEvent.ClearIcon) },
        onDismiss = { onEvent(RoutinesContract.UiEvent.HideHabitBottomSheet) },
        onClearErrors = { onEvent(RoutinesContract.UiEvent.ClearHabitErrors) },
        onConfirmHabit = { title, description ->
            onEvent(RoutinesContract.UiEvent.CreateOrUpdateHabit(title, description, autoSave = false))
        },
        onConfirmHabitChain = { title, description, ids ->
            onEvent(
                RoutinesContract.UiEvent.CreateOrUpdateHabitChain(title, description, ids, autoSave = false),
            )
        },
        onAutoSaveHabit = { title, description ->
            onEvent(RoutinesContract.UiEvent.CreateOrUpdateHabit(title, description, autoSave = true))
        },
        onAutoSaveHabitChain = { title, description, ids ->
            onEvent(
                RoutinesContract.UiEvent.CreateOrUpdateHabitChain(title, description, ids, autoSave = true),
            )
        },
        onDeleteHabit =
            uiState.habitForm.editingHabit?.let { editingHabit ->
                { onEvent(RoutinesContract.UiEvent.ShowDeleteHabitConfirmation(editingHabit)) }
            },
        onDeleteHabitChain =
            uiState.habitForm.editingHabitChain?.let { editingHabitChain ->
                { onEvent(RoutinesContract.UiEvent.ShowDeleteHabitChainConfirmation(editingHabitChain)) }
            },
        onSetRepeatDays = { onEvent(RoutinesContract.UiEvent.SetRepeatDays(it)) },
        onToggleHabitCompletion = { habitId, isCompleted ->
            onEvent(RoutinesContract.UiEvent.ToggleHabitCompletion(habitId, isCompleted))
        },
        modifier =
            if (placement == SheetPlacement.DockedPane) {
                Modifier
            } else {
                Modifier.padding(start = floatingRailContentClearance())
            },
        placement = placement,
        dismissRequestKey = dismissRequestKey,
    )
}

@Suppress("LocalContextGetResourceValueCall")
private suspend fun showRoutinesSnackbar(
    snackbar: RoutinesContract.UiEffect.ShowSnackbar,
    context: Context,
    snackbarHostState: SnackbarHostState,
    onEvent: (RoutinesContract.UiEvent) -> Unit,
) {
    val message =
        if (snackbar.formatArgs.isNotEmpty()) {
            context.getString(snackbar.messageResId, *snackbar.formatArgs.toTypedArray())
        } else {
            context.getString(snackbar.messageResId)
        }
    val actionLabel = snackbar.actionResId?.let(context::getString)
    val result =
        snackbarHostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Long,
        )
    snackbar.deletionToken?.let { token ->
        onEvent(
            if (result == SnackbarResult.ActionPerformed) {
                RoutinesContract.UiEvent.UndoDeletion(token)
            } else {
                RoutinesContract.UiEvent.DismissDeletionUndo(token)
            },
        )
    }
}

@Composable
private fun RoutinesDialogs(
    uiState: RoutinesContract.UiState,
    context: android.content.Context,
    onEvent: (RoutinesContract.UiEvent) -> Unit,
) {
    // Delete confirmation dialogs
    if (uiState.showDeleteHabitConfirmationDialog) {
        DeleteHabitConfirmDialog(
            onCancel = { onEvent(RoutinesContract.UiEvent.HideDeleteHabitConfirmation) },
            onConfirm = { onEvent(RoutinesContract.UiEvent.DeleteHabit) },
            habitToDelete = uiState.habitToDelete,
        )
    }

    if (uiState.showDeleteHabitChainConfirmationDialog) {
        DeleteHabitChainConfirmDialog(
            onCancel = { onEvent(RoutinesContract.UiEvent.HideDeleteHabitChainConfirmation) },
            onConfirm = { deleteHabits ->
                onEvent(RoutinesContract.UiEvent.DeleteHabitChain(deleteHabits))
            },
            habitChainToDelete = uiState.habitChainToDelete,
        )
    }

    // Empty habit chain confirmation dialog
    if (uiState.showEmptyHabitChainConfirmationDialog) {
        EmptyHabitChainConfirmDialog(
            onCancel = { onEvent(RoutinesContract.UiEvent.HideEmptyHabitChainConfirmation) },
            onConfirm = { onEvent(RoutinesContract.UiEvent.ConfirmDeleteEmptyHabitChain) },
            habitChain = uiState.habitForm.editingHabitChain,
        )
    }

    // Clear reminders confirmation dialog
    if (uiState.showClearRemindersConfirmationDialog) {
        ClearRemindersConfirmDialog(
            onCancel = { onEvent(RoutinesContract.UiEvent.HideClearRemindersConfirmation) },
            onConfirm = { onEvent(RoutinesContract.UiEvent.ConfirmClearRemindersAndProceed) },
            habitsWithReminders = uiState.habitsWithRemindersToBeCleared,
        )
    }

    // Permission revoked dialog
    if (uiState.showPermissionRevokedDialog) {
        PermissionRevokedDialog(
            permissionInfo = uiState.permissionInfo,
            context = context,
            dismissPermissionRevokedDialog = { onEvent(RoutinesContract.UiEvent.DismissPermissionRevokedDialog) },
            confirmClearAllReminders = { onEvent(RoutinesContract.UiEvent.ConfirmClearAllHabitReminders) },
            notificationPrefixRes = R.string.permission_revoked_notifications_prefix_habits,
            fallbackRes = R.string.permission_revoked_fallback_habits,
        )
    }

    // Permission request handler
    if (!uiState.showPermissionRevokedDialog) {
        HandleReminderPermissions(
            show = uiState.showPermissionRequestDialog,
            onGrantPermissions = {
                onEvent(RoutinesContract.UiEvent.DismissPermissionRequestDialog)
                onEvent(RoutinesContract.UiEvent.OnPermissionsGranted)
            },
            onDismiss = { onEvent(RoutinesContract.UiEvent.DismissPermissionRequestDialog) },
        )
    }
}

@Composable
private fun RoutinesSnackbar(
    snackbarHostState: SnackbarHostState,
    isRailLayout: Boolean,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(
                    bottom =
                        if (isRailLayout) {
                            16.dp
                        } else {
                            FLOATING_BAR_SNACKBAR_BOTTOM_PADDING
                        },
                ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        ExpressiveSnackbarHost(snackbarHostState)
    }
}
