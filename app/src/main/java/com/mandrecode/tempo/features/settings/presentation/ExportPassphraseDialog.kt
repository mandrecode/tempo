package com.mandrecode.tempo.features.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.TempoConfirmDialog

/**
 * Shown before every export: the user sets the passphrase that will encrypt the file.
 * There is no recovery if it's forgotten, so a confirmation field guards against typos
 * and the confirm action stays disabled until both fields are non-empty and match.
 */
@Composable
internal fun ExportPassphraseDialog(onEvent: (SettingsContract.UiEvent) -> Unit) {
    var passphrase by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    val mismatch = confirmation.isNotEmpty() && passphrase != confirmation

    TempoConfirmDialog(
        title = stringResource(R.string.backup_export_passphrase_title),
        confirmLabel = stringResource(R.string.backup_export_passphrase_confirm),
        onConfirm = {
            onEvent(SettingsContract.UiEvent.ExportPassphraseConfirmed(passphrase, confirmation))
        },
        onCancel = { onEvent(SettingsContract.UiEvent.BackupDialogDismissed) },
        isDestructive = false,
        confirmEnabled = passphrase.isNotEmpty() && passphrase == confirmation,
    ) {
        Column {
            Text(stringResource(R.string.backup_export_passphrase_message))
            OutlinedTextField(
                value = passphrase,
                onValueChange = { passphrase = it },
                label = { Text(stringResource(R.string.backup_export_passphrase_label)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
            )
            OutlinedTextField(
                value = confirmation,
                onValueChange = { confirmation = it },
                label = { Text(stringResource(R.string.backup_export_passphrase_confirm_label)) },
                singleLine = true,
                isError = mismatch,
                supportingText =
                    if (mismatch) {
                        { Text(stringResource(R.string.backup_export_passphrase_mismatch)) }
                    } else {
                        null
                    },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
            )
        }
    }
}
