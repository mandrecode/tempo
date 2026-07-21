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

/** Shown when the picked import file is encrypted; [dialog.attemptsFailed] flags a wrong retry. */
@Composable
internal fun ImportPassphraseDialog(
    dialog: SettingsContract.BackupDialog.EnterImportPassphrase,
    onEvent: (SettingsContract.UiEvent) -> Unit,
) {
    var passphrase by remember(dialog.attemptsFailed) { mutableStateOf("") }

    TempoConfirmDialog(
        title = stringResource(R.string.backup_import_passphrase_title),
        confirmLabel = stringResource(R.string.backup_import_passphrase_confirm),
        onConfirm = { onEvent(SettingsContract.UiEvent.ImportPassphraseEntered(passphrase)) },
        onCancel = { onEvent(SettingsContract.UiEvent.BackupDialogDismissed) },
        isDestructive = false,
        confirmEnabled = passphrase.isNotEmpty(),
    ) {
        Column {
            Text(stringResource(R.string.backup_import_passphrase_message))
            OutlinedTextField(
                value = passphrase,
                onValueChange = { passphrase = it },
                label = { Text(stringResource(R.string.backup_import_passphrase_label)) },
                singleLine = true,
                isError = dialog.attemptsFailed,
                supportingText =
                    if (dialog.attemptsFailed) {
                        { Text(stringResource(R.string.backup_import_passphrase_error)) }
                    } else {
                        null
                    },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
            )
        }
    }
}
