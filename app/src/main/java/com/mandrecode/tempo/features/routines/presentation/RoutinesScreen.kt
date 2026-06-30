package com.mandrecode.tempo.features.routines.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalConfiguration
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
import com.mandrecode.tempo.core.ui.navigation.RoutinesFloatingBarState
import com.mandrecode.tempo.core.ui.navigation.floatingRailContentPadding
import com.mandrecode.tempo.features.routines.presentation.components.HabitBottomSheet
import com.mandrecode.tempo.features.routines.presentation.components.dialogs.ClearRemindersConfirmDialog
import com.mandrecode.tempo.features.routines.presentation.components.dialogs.DeleteHabitChainConfirmDialog
import com.mandrecode.tempo.features.routines.presentation.components.dialogs.DeleteHabitConfirmDialog
import com.mandrecode.tempo.features.routines.presentation.components.dialogs.EmptyHabitChainConfirmDialog

private val FLOATING_BAR_SNACKBAR_BOTTOM_PADDING = 84.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    modifier: Modifier = Modifier,
    isSingleTabMode: Boolean = false,
    viewModel: RoutinesViewModel = hiltViewModel(),
    pendingNotificationAction: PendingNotificationAction? = null,
    onConsumePendingNotificationAction: () -> Unit = {},
    topBar: @Composable () -> Unit = {},
    showAddHabitRailButton: Boolean = false,
    onFloatingBarStateChange: (RoutinesFloatingBarState) -> Unit = {},
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
                is RoutinesContract.UiEffect.ShowSnackbar -> {
                    @Suppress("LocalContextGetResourceValueCall")
                    val message =
                        if (effect.formatArgs.isNotEmpty()) {
                            context.getString(effect.messageResId, *effect.formatArgs.toTypedArray())
                        } else {
                            context.getString(effect.messageResId)
                        }
                    snackbarHostState.showSnackbar(message)
                }
            }
        }
    }

    val isLandscape = LocalConfiguration.current.screenWidthDp >= 600
    val shouldShowFloatingRail = !isLandscape || !uiState.habitForm.isVisible
    val compactSoloAction = isSingleTabMode && isListScrolledFromTop.value
    val onShowHabitBottomSheet =
        remember(viewModel) {
            { viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet()) }
        }

    SideEffect {
        onFloatingBarStateChange(
            RoutinesFloatingBarState(
                visible = shouldShowFloatingRail && showAddHabitRailButton,
                compactSoloAction = compactSoloAction,
                onAddHabit = onShowHabitBottomSheet,
            ),
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.floatingRailContentPadding(isLandscape),
            topBar = topBar,
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                RoutinesContent(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    onScrolledFromTopChange = { isListScrolledFromTop.value = it },
                    showAddHabitButton = !showAddHabitRailButton,
                )

                // Show habit bottom sheet
                if (uiState.habitForm.isVisible) {
                    HabitBottomSheet(
                        formState = uiState.habitForm,
                        selectedDate = uiState.selectedDate,
                        habits = uiState.habits,
                        habitChains = uiState.habitChains,
                        onSelectTab = { viewModel.onEvent(RoutinesContract.UiEvent.SetSelectedTab(it)) },
                        onSetHabitType = { viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(it)) },
                        onSetReminder = { y, mo, d, h, mi ->
                            viewModel.onEvent(RoutinesContract.UiEvent.SetReminder(y, mo, d, h, mi))
                        },
                        onClearReminder = { viewModel.onEvent(RoutinesContract.UiEvent.ClearReminder) },
                        onSetColorKey = { viewModel.onEvent(RoutinesContract.UiEvent.SetColorKey(it)) },
                        onClearColor = { viewModel.onEvent(RoutinesContract.UiEvent.ClearColor) },
                        onSetIcon = { viewModel.onEvent(RoutinesContract.UiEvent.SetIcon(it)) },
                        onClearIcon = { viewModel.onEvent(RoutinesContract.UiEvent.ClearIcon) },
                        onDismiss = { viewModel.onEvent(RoutinesContract.UiEvent.HideHabitBottomSheet) },
                        onClearErrors = { viewModel.onEvent(RoutinesContract.UiEvent.ClearHabitErrors) },
                        onConfirmHabit = { title, desc ->
                            viewModel.onEvent(
                                RoutinesContract.UiEvent.CreateOrUpdateHabit(title, desc, autoSave = false),
                            )
                        },
                        onConfirmHabitChain = { title, desc, ids ->
                            viewModel.onEvent(
                                RoutinesContract.UiEvent.CreateOrUpdateHabitChain(
                                    title,
                                    desc,
                                    ids,
                                    autoSave = false,
                                ),
                            )
                        },
                        onAutoSaveHabit = { title, desc ->
                            viewModel.onEvent(
                                RoutinesContract.UiEvent.CreateOrUpdateHabit(title, desc, autoSave = true),
                            )
                        },
                        onAutoSaveHabitChain = { title, desc, ids ->
                            viewModel.onEvent(
                                RoutinesContract.UiEvent.CreateOrUpdateHabitChain(
                                    title,
                                    desc,
                                    ids,
                                    autoSave = true,
                                ),
                            )
                        },
                        onDeleteHabit =
                            uiState.habitForm.editingHabit?.let { editingHabit ->
                                {
                                    viewModel.onEvent(
                                        RoutinesContract.UiEvent.ShowDeleteHabitConfirmation(editingHabit),
                                    )
                                }
                            },
                        onDeleteHabitChain =
                            uiState.habitForm.editingHabitChain?.let { editingHabitChain ->
                                {
                                    viewModel.onEvent(
                                        RoutinesContract.UiEvent.ShowDeleteHabitChainConfirmation(editingHabitChain),
                                    )
                                }
                            },
                        onSetRepeatDays = { viewModel.onEvent(RoutinesContract.UiEvent.SetRepeatDays(it)) },
                        onToggleHabitCompletion = { habitId, isCompleted ->
                            viewModel.onEvent(RoutinesContract.UiEvent.ToggleHabitCompletion(habitId, isCompleted))
                        },
                    )
                }

                RoutinesDialogs(
                    uiState = uiState,
                    context = context,
                    onEvent = viewModel::onEvent,
                )

                RoutinesSnackbar(
                    snackbarHostState = snackbarHostState,
                    isLandscape = isLandscape,
                )
            }
        }
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
    isLandscape: Boolean,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom =
                        if (isLandscape) {
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
