package com.mandrecode.tempo.features.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.backup.domain.model.BackupEntityKind
import com.mandrecode.tempo.features.backup.domain.model.ConflictReason
import com.mandrecode.tempo.features.backup.domain.model.ImportConflict
import com.mandrecode.tempo.features.backup.domain.model.ImportMode
import com.mandrecode.tempo.features.backup.domain.model.ValidationIssue
import com.mandrecode.tempo.features.backup.domain.model.ValidationIssueKind

@Composable
internal fun BackupSection(
    uiState: SettingsContract.UiState,
    onEvent: (SettingsContract.UiEvent) -> Unit,
) {
    SettingsSection(title = stringResource(R.string.settings_backup)) {
        Column {
            SettingsItem(
                icon = R.drawable.ic_file_upload,
                title = stringResource(R.string.backup_export_title),
                subtitle = stringResource(R.string.backup_export_description),
                trailingIcon = R.drawable.ic_chevron_right,
                onClick = { onEvent(SettingsContract.UiEvent.ExportClicked) },
            )
            SettingsItemDivider()
            SettingsItem(
                icon = R.drawable.ic_file_download,
                title = stringResource(R.string.backup_import_title),
                subtitle = stringResource(R.string.backup_import_description),
                trailingIcon = R.drawable.ic_chevron_right,
                onClick = { onEvent(SettingsContract.UiEvent.ImportClicked) },
            )
        }
    }
    BackupDialogs(uiState = uiState, onEvent = onEvent)
}

@Composable
private fun BackupDialogs(
    uiState: SettingsContract.UiState,
    onEvent: (SettingsContract.UiEvent) -> Unit,
) {
    when (val dialog = uiState.backupDialog) {
        is SettingsContract.BackupDialog.ChooseImportMode ->
            ImportModeDialog(onEvent = onEvent)

        is SettingsContract.BackupDialog.ImportSucceeded ->
            ImportSucceededDialog(dialog = dialog, onEvent = onEvent)

        is SettingsContract.BackupDialog.ImportFailed ->
            ImportFailedDialog(error = dialog.error, onEvent = onEvent)

        null -> if (uiState.backupInProgress) BackupProgressDialog()
    }
}

@Composable
private fun ImportModeDialog(onEvent: (SettingsContract.UiEvent) -> Unit) {
    AlertDialog(
        onDismissRequest = { onEvent(SettingsContract.UiEvent.BackupDialogDismissed) },
        title = { Text(stringResource(R.string.backup_import_mode_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.backup_import_mode_message))
                Text(
                    text = stringResource(R.string.backup_import_mode_replace_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onEvent(SettingsContract.UiEvent.ImportModeChosen(ImportMode.MERGE)) },
            ) {
                Text(stringResource(R.string.backup_import_mode_merge))
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = { onEvent(SettingsContract.UiEvent.BackupDialogDismissed) },
                ) {
                    Text(stringResource(R.string.cancel))
                }
                TextButton(
                    onClick = { onEvent(SettingsContract.UiEvent.ImportModeChosen(ImportMode.REPLACE)) },
                ) {
                    Text(
                        text = stringResource(R.string.backup_import_mode_replace),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
    )
}

@Composable
private fun ImportSucceededDialog(
    dialog: SettingsContract.BackupDialog.ImportSucceeded,
    onEvent: (SettingsContract.UiEvent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onEvent(SettingsContract.UiEvent.BackupDialogDismissed) },
        title = { Text(stringResource(R.string.backup_import_success_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(stringResource(R.string.backup_import_summary_imported, dialog.imported))
                Text(stringResource(R.string.backup_import_summary_skipped, dialog.skipped))
                Text(stringResource(R.string.backup_import_summary_conflicts, dialog.conflicts.size))
                dialog.conflicts.forEach { conflict ->
                    Text(
                        text = conflict.label(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onEvent(SettingsContract.UiEvent.BackupDialogDismissed) }) {
                Text(stringResource(R.string.ok))
            }
        },
    )
}

@Composable
private fun ImportFailedDialog(
    error: SettingsContract.ImportError,
    onEvent: (SettingsContract.UiEvent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onEvent(SettingsContract.UiEvent.BackupDialogDismissed) },
        title = { Text(stringResource(R.string.backup_import_error_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(error.message())
                if (error is SettingsContract.ImportError.ValidationFailed) {
                    error.issues.forEach { issue ->
                        Text(
                            text = issue.label(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onEvent(SettingsContract.UiEvent.BackupDialogDismissed) }) {
                Text(stringResource(R.string.ok))
            }
        },
    )
}

@Composable
private fun ValidationIssue.label(): String =
    stringResource(
        when (kind) {
            ValidationIssueKind.DUPLICATE_ID -> R.string.backup_issue_duplicate_id
            ValidationIssueKind.UNKNOWN_CATEGORY_REFERENCE -> R.string.backup_issue_unknown_category
            ValidationIssueKind.UNKNOWN_PARENT_TASK_REFERENCE -> R.string.backup_issue_unknown_parent_task
            ValidationIssueKind.UNKNOWN_CHAIN_REFERENCE -> R.string.backup_issue_unknown_chain
            ValidationIssueKind.UNKNOWN_HABIT_REFERENCE -> R.string.backup_issue_unknown_habit
        },
        detail,
    )

@Composable
private fun BackupProgressDialog() {
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text(stringResource(R.string.backup_in_progress)) },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        },
        confirmButton = {},
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SettingsContract.ImportError.message(): String =
    when (this) {
        is SettingsContract.ImportError.UnsupportedVersion ->
            stringResource(R.string.backup_import_error_unsupported_version, fileVersion, maxSupported)

        is SettingsContract.ImportError.CorruptFile ->
            stringResource(R.string.backup_import_error_corrupt)

        is SettingsContract.ImportError.ValidationFailed ->
            stringResource(R.string.backup_import_error_validation)

        is SettingsContract.ImportError.ReadFailed ->
            stringResource(R.string.backup_import_error_read)

        is SettingsContract.ImportError.Unexpected ->
            stringResource(R.string.backup_import_error_unexpected)
    }

@Composable
private fun ImportConflict.label(): String {
    val kindLabel =
        stringResource(
            when (kind) {
                BackupEntityKind.CATEGORY -> R.string.backup_entity_category
                BackupEntityKind.TASK -> R.string.backup_entity_task
                BackupEntityKind.HABIT -> R.string.backup_entity_habit
                BackupEntityKind.HABIT_CHAIN -> R.string.backup_entity_habit_chain
            },
        )
    val reasonLabel =
        stringResource(
            when (reason) {
                ConflictReason.CONTENT_MISMATCH -> R.string.backup_conflict_reason_content
                ConflictReason.PARENT_CONFLICTED -> R.string.backup_conflict_reason_parent
            },
        )
    return stringResource(R.string.backup_conflict_entry, kindLabel, displayName, reasonLabel)
}
