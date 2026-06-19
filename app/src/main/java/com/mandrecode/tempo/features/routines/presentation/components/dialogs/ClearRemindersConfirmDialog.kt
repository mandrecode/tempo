package com.mandrecode.tempo.features.routines.presentation.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.dialogAction
import com.mandrecode.tempo.core.ui.theme.dialogTitle
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation
import com.mandrecode.tempo.features.routines.domain.model.Habit

@Composable
fun ClearRemindersConfirmDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    habitsWithReminders: List<Habit>,
) {
    val haptic = LocalHapticFeedback.current
    val (confirmInteractionSource, confirmCornerRadius) = rememberPressableButtonAnimation()
    val (cancelInteractionSource, cancelCornerRadius) = rememberPressableButtonAnimation()

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = stringResource(R.string.clear_existing_reminders),
                style = MaterialTheme.typography.dialogTitle,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    buildAnnotatedString {
                        append(stringResource(R.string.clear_reminders_dialog_prefix))
                        if (habitsWithReminders.size > 1) {
                            append(stringResource(R.string.clear_reminders_dialog_plural_has))
                        } else {
                            append(stringResource(R.string.clear_reminders_dialog_singular_has))
                        }
                        append(stringResource(R.string.clear_reminders_dialog_middle))
                        if (habitsWithReminders.size > 1) {
                            append(stringResource(R.string.clear_reminders_dialog_plural_suffix))
                        } else {
                            append(stringResource(R.string.clear_reminders_dialog_singular_suffix))
                        }
                    },
                )

                Spacer(modifier = Modifier.height(4.dp))

                habitsWithReminders.forEach { habit ->
                    Text(
                        buildAnnotatedString {
                            append("• ")
                            withStyle(
                                style =
                                    SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                    ),
                            ) {
                                append(habit.title)
                            }
                        },
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.chain_reminder_apply_all),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onConfirm()
                },
                interactionSource = confirmInteractionSource,
                shape = RoundedCornerShape(confirmCornerRadius.value),
            ) {
                Text(
                    text = stringResource(R.string.continue_label),
                    style = MaterialTheme.typography.dialogAction,
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCancel()
                },
                interactionSource = cancelInteractionSource,
                shape = RoundedCornerShape(cancelCornerRadius.value),
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
