package com.mandrecode.tempo.features.focus.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.focus.presentation.FocusContract

/**
 * Asks before a start ends the session already running.
 *
 * Starting replaces whatever is going and banks only the minutes it earned, so swapping tasks
 * mid-session throws the rest away. That is worth a question rather than something to notice
 * afterwards — and it only appears for a different task, since restarting the same one is just
 * carrying on.
 */
@Composable
internal fun ReplaceSessionDialog(
    pending: FocusContract.PendingStart,
    onEvent: (FocusContract.UiEvent) -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = { onEvent(FocusContract.UiEvent.DismissPendingStart) },
        title = { Text(stringResource(R.string.focus_replace_session_title)) },
        text = {
            Text(
                stringResource(
                    R.string.focus_replace_session_message,
                    pending.replacingTaskTitle,
                    pending.taskTitle,
                ),
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEvent(FocusContract.UiEvent.ConfirmPendingStart)
                },
            ) {
                Text(stringResource(R.string.focus_replace_session_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(FocusContract.UiEvent.DismissPendingStart) }) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
