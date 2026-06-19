package com.mandrecode.tempo.features.tasks.presentation.components.dialogs

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.dialogAction
import com.mandrecode.tempo.core.ui.theme.dialogTitle
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation
import com.mandrecode.tempo.features.tasks.domain.model.Task

@Composable
fun DeleteTaskConfirmDialog(
    onCancelDeleteTask: () -> Unit,
    onConfirmDeleteTask: (Task) -> Unit,
    taskToDelete: Task?,
    subtasksCount: Int,
) {
    val haptic = LocalHapticFeedback.current
    val (confirmInteractionSource, confirmCornerRadius) = rememberPressableButtonAnimation()
    val (cancelInteractionSource, cancelCornerRadius) = rememberPressableButtonAnimation()

    AlertDialog(
        onDismissRequest = onCancelDeleteTask,
        title = {
            Text(
                text = stringResource(R.string.confirm_deletion),
                style = MaterialTheme.typography.dialogTitle,
            )
        },
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
        confirmButton = {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    taskToDelete?.let { task ->
                        onConfirmDeleteTask(task)
                    }
                },
                interactionSource = confirmInteractionSource,
                shape = RoundedCornerShape(confirmCornerRadius.value),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
            ) {
                Text(
                    text = stringResource(R.string.delete),
                    style = MaterialTheme.typography.dialogAction,
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCancelDeleteTask()
                },
                interactionSource = cancelInteractionSource,
                shape = RoundedCornerShape(cancelCornerRadius.value),
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
