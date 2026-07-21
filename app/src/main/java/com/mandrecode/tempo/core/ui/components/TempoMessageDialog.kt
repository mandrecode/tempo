package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.dialogAction
import com.mandrecode.tempo.core.ui.theme.dialogTitle
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation

/**
 * Shared single-action acknowledgement dialog (info/result/error) used where
 * [TempoConfirmDialog]'s confirm/cancel pair doesn't apply. Matches the same
 * title style, button styling, pressable-corner animation, and haptics.
 */
@Composable
fun TempoMessageDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmLabel: String = stringResource(R.string.ok),
    text: @Composable () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val (confirmInteractionSource, confirmCornerRadius) = rememberPressableButtonAnimation()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.dialogTitle,
            )
        },
        text = text,
        confirmButton = {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onDismiss()
                },
                interactionSource = confirmInteractionSource,
                shape = RoundedCornerShape(confirmCornerRadius.value),
            ) {
                Text(
                    text = confirmLabel,
                    style = MaterialTheme.typography.dialogAction,
                )
            }
        },
    )
}
