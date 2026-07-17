package com.mandrecode.tempo.features.routines.presentation.components.dialogs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.TempoConfirmDialog
import com.mandrecode.tempo.features.routines.domain.model.Habit

@Composable
fun DeleteHabitConfirmDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    habitToDelete: Habit?,
    modifier: Modifier = Modifier,
) {
    TempoConfirmDialog(
        title = stringResource(R.string.confirm_deletion),
        confirmLabel = stringResource(R.string.delete),
        onConfirm = onConfirm,
        onCancel = onCancel,
        modifier = modifier,
        text = {
            val habitTitle = habitToDelete?.title ?: stringResource(R.string.this_habit)
            Text(
                buildAnnotatedString {
                    val messagePrefix = stringResource(R.string.delete_habit_message_prefix).trimEnd()
                    append(messagePrefix)
                    append(" ")
                    withStyle(
                        style =
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                            ),
                    ) {
                        append(habitTitle)
                    }
                    append(stringResource(R.string.delete_habit_message_suffix).trimStart())
                },
            )
        },
    )
}
