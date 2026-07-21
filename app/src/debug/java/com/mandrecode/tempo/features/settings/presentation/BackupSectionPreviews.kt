package com.mandrecode.tempo.features.settings.presentation

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.backup.domain.model.BackupEntityKind
import com.mandrecode.tempo.features.backup.domain.model.ConflictReason
import com.mandrecode.tempo.features.backup.domain.model.ImportConflict
import kotlinx.collections.immutable.persistentListOf

@Preview(name = "Backup section", showBackground = true)
@Composable
private fun BackupSectionPreview() {
    TempoTheme {
        BackupSection(uiState = SettingsContract.UiState(), onEvent = {})
    }
}

@Preview(
    name = "Backup section - Dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun BackupSectionDarkPreview() {
    TempoTheme {
        BackupSection(uiState = SettingsContract.UiState(), onEvent = {})
    }
}

@Preview(name = "Import mode dialog", showBackground = true)
@Composable
private fun ImportModeDialogPreview() {
    TempoTheme {
        BackupSection(
            uiState =
                SettingsContract.UiState(
                    backupDialog = SettingsContract.BackupDialog.ChooseImportMode,
                ),
            onEvent = {},
        )
    }
}

@Preview(name = "Import result dialog", showBackground = true)
@Composable
private fun ImportSucceededDialogPreview() {
    TempoTheme {
        BackupSection(
            uiState =
                SettingsContract.UiState(
                    backupDialog =
                        SettingsContract.BackupDialog.ImportSucceeded(
                            imported = 12,
                            skipped = 4,
                            conflicts =
                                persistentListOf(
                                    ImportConflict(
                                        kind = BackupEntityKind.CATEGORY,
                                        displayName = "Work",
                                        reason = ConflictReason.CONTENT_MISMATCH,
                                    ),
                                    ImportConflict(
                                        kind = BackupEntityKind.TASK,
                                        displayName = "Quarterly report",
                                        reason = ConflictReason.PARENT_CONFLICTED,
                                    ),
                                ),
                        ),
                ),
            onEvent = {},
        )
    }
}

@Preview(name = "Import failed dialog", showBackground = true)
@Composable
private fun ImportFailedDialogPreview() {
    TempoTheme {
        BackupSection(
            uiState =
                SettingsContract.UiState(
                    backupDialog =
                        SettingsContract.BackupDialog.ImportFailed(
                            SettingsContract.ImportError.UnsupportedVersion(
                                fileVersion = 3,
                                maxSupported = 1,
                            ),
                        ),
                ),
            onEvent = {},
        )
    }
}
