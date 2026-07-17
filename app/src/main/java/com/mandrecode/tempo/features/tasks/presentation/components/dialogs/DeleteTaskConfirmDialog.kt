package com.mandrecode.tempo.features.tasks.presentation.components.dialogs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.TempoConfirmDialog
import com.mandrecode.tempo.features.tasks.domain.model.Task

@Composable
fun DeleteTaskConfirmDialog(
    onCancelDeleteTask: () -> Unit,
    onConfirmDeleteTask: (Task) -> Unit,
    taskToDelete: Task?,
    subtasksCount: Int,
    modifier: Modifier = Modifier,
) {
    TempoConfirmDialog(
        title = stringResource(R.string.confirm_deletion),
        confirmLabel = stringResource(R.string.delete),
        onConfirm = { taskToDelete?.let(onConfirmDeleteTask) },
        onCancel = onCancelDeleteTask,
        modifier = modifier,
        text = {
            val taskTitle = taskToDelete?.title ?: stringResource(R.string.this_task)
            Text(
                buildAnnotatedString {
                    val messagePrefix = stringResource(R.string.delete_task_message_prefix).trimEnd()
                    append(messagePrefix)
                    append(" ")
                    withStyle(
                        style =
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                            ),
                    ) {
                        append(taskTitle)
                    }
                    if (subtasksCount > 0) {
                        append(" ")
                        append(
                            stringResource(
                                R.string.delete_task_with_subtasks_message,
                                subtasksCount,
                                pluralStringResource(R.plurals.subtasks_count, subtasksCount),
                            ).trimStart(),
                        )
                    }
                    append(stringResource(R.string.delete_task_message_suffix).trimStart())
                },
            )
        },
    )
}
