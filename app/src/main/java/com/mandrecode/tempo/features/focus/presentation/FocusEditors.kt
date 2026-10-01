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
import com.mandrecode.tempo.features.tasks.presentation.TaskEditor
import com.mandrecode.tempo.features.tasks.presentation.TasksContract
import com.mandrecode.tempo.features.tasks.presentation.TasksViewModel
import com.mandrecode.tempo.features.tasks.presentation.components.dialogs.DeleteTaskConfirmDialog

/**
 * The task editor, opened from Focus without leaving it.
 *
 * They are the Tasks tab's editor driven by its own view model:
 * Focus holds a second instance of the Tasks view model, so the form logic, validation, auto-save and
 * deletion all behave exactly as they do in Tasks, with no second implementation to
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
