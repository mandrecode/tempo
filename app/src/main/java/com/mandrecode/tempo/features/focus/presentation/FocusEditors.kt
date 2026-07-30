package com.mandrecode.tempo.features.focus.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mandrecode.tempo.core.ui.adaptive.SheetPlacement
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.presentation.HabitEditor
import com.mandrecode.tempo.features.routines.presentation.RoutinesContract
import com.mandrecode.tempo.features.routines.presentation.RoutinesViewModel
import com.mandrecode.tempo.features.routines.presentation.components.dialogs.DeleteHabitConfirmDialog
import com.mandrecode.tempo.features.tasks.presentation.TaskEditor
import com.mandrecode.tempo.features.tasks.presentation.TasksContract
import com.mandrecode.tempo.features.tasks.presentation.TasksViewModel
import com.mandrecode.tempo.features.tasks.presentation.components.dialogs.DeleteTaskConfirmDialog

/**
 * The task and habit editors, opened from Focus without leaving it.
 *
 * They are the owning tabs' own editors driven by the owning tabs' own view models, not copies:
 * Focus holds a second instance of each view model, so the form logic, validation, auto-save and
 * deletion all behave exactly as they do in Tasks and Routines, with no second implementation to
 * keep in step. The instances are created on the first edit rather than with the screen, so simply
 * looking at your day costs nothing.
 *
 * Always a bottom sheet, never the docked pane the tabs can use on a wide window: Focus has no
 * list-detail layout for a pane to dock beside.
 */
@Composable
internal fun FocusTaskEditor(
    target: FocusContract.TaskEditorTarget,
    onDismiss: () -> Unit,
    viewModel: TasksViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnDismiss by rememberUpdatedState(onDismiss)

    LaunchedEffect(target) {
        viewModel.onEvent(
            when (target) {
                is FocusContract.TaskEditorTarget.Existing ->
                    TasksContract.UiEvent.ShowTaskDialog(task = target.task)

                is FocusContract.TaskEditorTarget.NewSubtask ->
                    TasksContract.UiEvent.ShowTaskDialog(parentTaskId = target.parentTaskId)
            },
        )
    }

    // The sheet closing is the editor closing — Focus tracks the same thing its own way, and both
    // have to agree or a second tap on the item would open nothing. Only after it has actually
    // opened: the form is not visible yet on the first composition, and reporting that as a
    // dismissal would close the editor before it appeared.
    DismissWhenClosed(isOpen = uiState.taskForm.isVisible, onClose = currentOnDismiss)

    if (uiState.taskForm.isVisible) {
        TaskEditor(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            placement = SheetPlacement.BottomSheet,
            dismissRequestKey = 0,
        )
    }

    if (uiState.showDeleteTaskConfirmationDialog && uiState.taskToDelete != null) {
        DeleteTaskConfirmDialog(
            onCancelDeleteTask = { viewModel.onEvent(TasksContract.UiEvent.CancelDeleteTask) },
            onConfirmDeleteTask = { viewModel.onEvent(TasksContract.UiEvent.ConfirmDeleteTask(it)) },
            taskToDelete = uiState.taskToDelete,
            subtasksCount = uiState.taskToDeleteSubtasksCount,
        )
    }
}

/** The habit half of [FocusTaskEditor], on the same terms. */
@Composable
internal fun FocusHabitEditor(
    habit: Habit,
    onDismiss: () -> Unit,
    viewModel: RoutinesViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnDismiss by rememberUpdatedState(onDismiss)

    LaunchedEffect(habit.id) {
        viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet(habit))
    }

    DismissWhenClosed(isOpen = uiState.habitForm.isVisible, onClose = currentOnDismiss)

    if (uiState.habitForm.isVisible) {
        HabitEditor(
            uiState = uiState,
            onEvent = viewModel::onEvent,
            placement = SheetPlacement.BottomSheet,
            dismissRequestKey = 0,
        )
    }

    if (uiState.showDeleteHabitConfirmationDialog) {
        DeleteHabitConfirmDialog(
            onCancel = { viewModel.onEvent(RoutinesContract.UiEvent.HideDeleteHabitConfirmation) },
            onConfirm = { viewModel.onEvent(RoutinesContract.UiEvent.DeleteHabit) },
            habitToDelete = uiState.habitToDelete,
        )
    }
}

/** Reports [onClose] the first time [isOpen] goes true and back to false, never before. */
@Composable
private fun DismissWhenClosed(
    isOpen: Boolean,
    onClose: () -> Unit,
) {
    var hasOpened by remember { mutableStateOf(false) }
    val currentOnClose by rememberUpdatedState(onClose)
    LaunchedEffect(isOpen) {
        if (isOpen) {
            hasOpened = true
        } else if (hasOpened) {
            currentOnClose()
        }
    }
}
