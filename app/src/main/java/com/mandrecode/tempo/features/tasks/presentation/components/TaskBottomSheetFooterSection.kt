package com.mandrecode.tempo.features.tasks.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.editor.EditorBottomSheetFooter

@Composable
internal fun TaskBottomSheetFooter(
    isEditingTask: Boolean,
    taskTitle: String,
    autoSaveEnabled: Boolean,
    onDelete: (() -> Unit)?,
    onRequestDismiss: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    EditorBottomSheetFooter(
        hasDeleteAction = isEditingTask && onDelete != null,
        deleteLabel = stringResource(R.string.delete_task),
        onDelete = onDelete,
        autoSaveEnabled = autoSaveEnabled,
        confirmEnabled = taskTitle.isNotBlank(),
        confirmLabel =
            if (isEditingTask) {
                stringResource(R.string.update)
            } else {
                stringResource(R.string.add_task)
            },
        onRequestDismiss = onRequestDismiss,
        onConfirmClick = onConfirmClick,
    )
}
