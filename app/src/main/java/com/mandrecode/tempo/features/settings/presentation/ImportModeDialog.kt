package com.mandrecode.tempo.features.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.mandrecode.tempo.features.backup.domain.model.ImportMode

/**
 * Mirrors [com.mandrecode.tempo.features.routines.presentation.components.dialogs.DeleteHabitChainConfirmDialog]'s
 * shape for a three-way choice: stacked full-width action buttons (default action
 * first, destructive action styled with the error color) with Cancel as the
 * outlined dismiss button.
 */
@Composable
internal fun ImportModeDialog(onEvent: (SettingsContract.UiEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = { onEvent(SettingsContract.UiEvent.BackupDialogDismissed) },
        title = {
            Text(
                text = stringResource(R.string.backup_import_mode_title),
                style = MaterialTheme.typography.dialogTitle,
            )
        },
        text = { ImportModeDialogContent(onEvent) },
        confirmButton = {},
        dismissButton = { ImportModeCancelButton(onEvent) },
    )
}

@Composable
private fun ImportModeDialogContent(onEvent: (SettingsContract.UiEvent) -> Unit) {
    Column {
        Text(stringResource(R.string.backup_import_mode_message))
        Text(
            text = stringResource(R.string.backup_import_mode_replace_warning),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 8.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))

        ImportModeActionButton(
            label = stringResource(R.string.backup_import_mode_merge),
            onClick = { onEvent(SettingsContract.UiEvent.ImportModeChosen(ImportMode.MERGE)) },
        )
        Spacer(modifier = Modifier.height(8.dp))
        ImportModeActionButton(
            label = stringResource(R.string.backup_import_mode_replace),
            onClick = { onEvent(SettingsContract.UiEvent.ImportModeChosen(ImportMode.REPLACE)) },
            isDestructive = true,
        )
    }
}

@Composable
private fun ImportModeActionButton(
    label: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current
    val (interactionSource, cornerRadius) = rememberPressableButtonAnimation()

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(cornerRadius.value),
        modifier = Modifier.fillMaxWidth(),
        colors =
            if (isDestructive) {
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                )
            } else {
                ButtonDefaults.buttonColors()
            },
    ) {
        Text(text = label, style = MaterialTheme.typography.dialogAction)
    }
}

@Composable
private fun ImportModeCancelButton(onEvent: (SettingsContract.UiEvent) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val (interactionSource, cornerRadius) = rememberPressableButtonAnimation()

    OutlinedButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onEvent(SettingsContract.UiEvent.BackupDialogDismissed)
        },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(cornerRadius.value),
    ) {
        Text(stringResource(R.string.cancel))
    }
}
