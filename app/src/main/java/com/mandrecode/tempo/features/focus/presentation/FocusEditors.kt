package com.mandrecode.tempo.features.focus.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
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
 * Focus reuses the Tasks editor and a separate instance of its view model, so validation,
 * auto-save and deletion behave exactly as they do in Tasks. The view model is created on the
 * first edit rather than with the screen, so simply looking at your day costs nothing.
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

    // Opened once per target, not once per composition. The view model outlives a configuration
    // change but this composable does not, and asking it to open again would reset the form the
    // user is half-way through — and the session its draft is saved under.
    var openedFor by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(target) {
        val key = target.editorKey()
        if (openedFor == key && viewModel.uiState.value.taskForm.isVisible) return@LaunchedEffect
        openedFor = key
        viewModel.onEvent(
            when (target) {
                FocusContract.TaskEditorTarget.NewTask ->
                    TasksContract.UiEvent.ShowTaskDialog()

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

/** Which editor a target opens, stable across recreation (a task's id, not its contents). */
private fun FocusContract.TaskEditorTarget.editorKey(): String =
    when (this) {
        FocusContract.TaskEditorTarget.NewTask -> "new"
        is FocusContract.TaskEditorTarget.Existing -> "task:${task.id}"
        is FocusContract.TaskEditorTarget.NewSubtask -> "subtask:$parentTaskId"
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
