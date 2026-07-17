package com.mandrecode.tempo.features.routines.presentation.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.TempoConfirmDialog
import com.mandrecode.tempo.features.routines.domain.model.Habit

@Composable
fun ClearRemindersConfirmDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    habitsWithReminders: List<Habit>,
    modifier: Modifier = Modifier,
) {
    TempoConfirmDialog(
        title = stringResource(R.string.clear_existing_reminders),
        confirmLabel = stringResource(R.string.continue_label),
        onConfirm = onConfirm,
        onCancel = onCancel,
        modifier = modifier,
        isDestructive = false,
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
    )
}
