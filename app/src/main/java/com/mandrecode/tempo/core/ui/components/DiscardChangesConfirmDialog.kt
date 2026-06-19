package com.mandrecode.tempo.core.ui.components

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
import androidx.compose.ui.res.stringResource
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.dialogAction
import com.mandrecode.tempo.core.ui.theme.dialogTitle
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation

@Composable
fun DiscardChangesConfirmDialog(
    onCancelDiscard: () -> Unit,
    onConfirmDiscard: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val (confirmInteractionSource, confirmCornerRadius) = rememberPressableButtonAnimation()
    val (cancelInteractionSource, cancelCornerRadius) = rememberPressableButtonAnimation()

    AlertDialog(
        onDismissRequest = onCancelDiscard,
        title = {
            Text(
                text = stringResource(R.string.discard_changes),
                style = MaterialTheme.typography.dialogTitle,
            )
        },
        text = {
            Text(stringResource(R.string.discard_changes_message))
        },
        confirmButton = {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onConfirmDiscard()
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
                    text = stringResource(R.string.discard),
                    style = MaterialTheme.typography.dialogAction,
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCancelDiscard()
                },
                interactionSource = cancelInteractionSource,
                shape = RoundedCornerShape(cancelCornerRadius.value),
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
