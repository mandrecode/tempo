package com.mandrecode.tempo.features.tasks.presentation.components.dialogs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.TempoConfirmDialog

@Composable
fun DeleteCompletedConfirmationDialog(
    onCancelDeleteCompletedTasks: () -> Unit,
    onConfirmDeleteCompletedTasks: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TempoConfirmDialog(
        title = stringResource(R.string.confirm_deletion),
        confirmLabel = stringResource(R.string.delete),
        onConfirm = onConfirmDeleteCompletedTasks,
        onCancel = onCancelDeleteCompletedTasks,
        modifier = modifier,
        text = { Text(stringResource(R.string.delete_completed_tasks_confirm_message)) },
    )
}
