package com.mandrecode.tempo.features.routines.presentation.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.dialogAction
import com.mandrecode.tempo.core.ui.theme.dialogTitle
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation
import com.mandrecode.tempo.features.routines.domain.model.HabitChain

@Composable
fun DeleteHabitChainConfirmDialog(
    onCancel: () -> Unit,
    onConfirm: (deleteHabits: Boolean) -> Unit,
    habitChainToDelete: HabitChain?,
) {
    val haptic = LocalHapticFeedback.current
    val (keepHabitsInteractionSource, keepHabitsCornerRadius) = rememberPressableButtonAnimation()
    val (deleteAllInteractionSource, deleteAllCornerRadius) = rememberPressableButtonAnimation()
    val (cancelInteractionSource, cancelCornerRadius) = rememberPressableButtonAnimation()

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = stringResource(R.string.delete_habit_chain_title),
                style = MaterialTheme.typography.dialogTitle,
            )
        },
        text = {
            Column {
                Text(stringResource(R.string.delete_habit_chain_message))
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onConfirm(false)
                    },
                    interactionSource = keepHabitsInteractionSource,
                    shape = RoundedCornerShape(keepHabitsCornerRadius.value),
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                ) {
                    Text(
                        text = stringResource(R.string.delete_chain_keep_habits),
                        style = MaterialTheme.typography.dialogAction,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onConfirm(true)
                    },
                    interactionSource = deleteAllInteractionSource,
                    shape = RoundedCornerShape(deleteAllCornerRadius.value),
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                ) {
                    Text(
                        text = stringResource(R.string.delete_chain_and_habits),
                        style = MaterialTheme.typography.dialogAction,
                    )
                }
            }
        },
        confirmButton = {},
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
