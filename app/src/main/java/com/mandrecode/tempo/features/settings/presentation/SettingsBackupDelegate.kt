package com.mandrecode.tempo.features.settings.presentation

import android.net.Uri
import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.backup.domain.model.ImportMode
import com.mandrecode.tempo.features.backup.domain.model.ImportOutcome
import com.mandrecode.tempo.features.backup.domain.usecase.ExportBackupUseCase
import com.mandrecode.tempo.features.backup.domain.usecase.ImportBackupUseCase
import com.mandrecode.tempo.infrastructure.backup.BackupFileDataSource
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

/**
 * Owns the Settings Backup flows (export/import) on behalf of [SettingsViewModel]:
 * runs the use cases, tracks the in-flight export payload and picked import file,
 * and pushes state/effect updates through the [Host].
 */
class SettingsBackupDelegate
    @Inject
    constructor(
        private val exportBackup: ExportBackupUseCase,
        private val importBackup: ImportBackupUseCase,
        private val backupFileDataSource: BackupFileDataSource,
    ) {
        /** The ViewModel surface the delegate acts through. */
        data class Host(
            val scope: CoroutineScope,
            val updateState: (transform: (SettingsContract.UiState) -> SettingsContract.UiState) -> Unit,
            val sendEffect: (SettingsContract.UiEffect) -> Unit,
        )

        private var pendingExportJson: String? = null
        private var pendingImportUri: Uri? = null

        /** Handles backup-related events; returns false for events it does not own. */
        fun onEvent(
            event: SettingsContract.UiEvent,
            host: Host,
        ): Boolean {
            when (event) {
                is SettingsContract.UiEvent.ExportClicked -> handleExportClicked(host)

                is SettingsContract.UiEvent.ExportDestinationPicked ->
                    handleExportDestinationPicked(event.uri, host)

                is SettingsContract.UiEvent.ExportCancelled -> {
                    pendingExportJson = null
                }

                is SettingsContract.UiEvent.ImportClicked ->
                    host.sendEffect(SettingsContract.UiEffect.LaunchImportPicker)

                is SettingsContract.UiEvent.ImportFilePicked -> {
                    pendingImportUri = event.uri
                    host.updateState {
                        it.copy(backupDialog = SettingsContract.BackupDialog.ChooseImportMode)
                    }
                }

                is SettingsContract.UiEvent.ImportModeChosen -> handleImportModeChosen(event.mode, host)

                is SettingsContract.UiEvent.BackupDialogDismissed -> {
                    pendingImportUri = null
                    host.updateState { it.copy(backupDialog = null) }
                }

                else -> return false
            }
            return true
        }

        private fun handleExportClicked(host: Host) {
            host.scope.launch {
                host.updateState { it.copy(backupInProgress = true) }
                try {
                    val export = exportBackup()
                    pendingExportJson = export.json
                    host.sendEffect(
                        SettingsContract.UiEffect.LaunchExportPicker(export.suggestedFileName),
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    host.sendEffect(
                        SettingsContract.UiEffect.ShowMessage(R.string.backup_export_error),
                    )
                } finally {
                    host.updateState { it.copy(backupInProgress = false) }
                }
            }
        }

        private fun handleExportDestinationPicked(
            uri: Uri,
            host: Host,
        ) {
            host.scope.launch {
                host.updateState { it.copy(backupInProgress = true) }
                try {
                    val json = pendingExportJson ?: exportBackup().json
                    pendingExportJson = null
                    backupFileDataSource.write(uri, json)
                    host.sendEffect(exportSuccessMessage(uri))
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    host.sendEffect(
                        SettingsContract.UiEffect.ShowMessage(R.string.backup_export_error),
                    )
                } finally {
                    host.updateState { it.copy(backupInProgress = false) }
                }
            }
        }

        private fun handleImportModeChosen(
            mode: ImportMode,
            host: Host,
        ) {
            val uri = pendingImportUri ?: return
            pendingImportUri = null
            host.scope.launch {
                host.updateState { it.copy(backupDialog = null, backupInProgress = true) }
                val dialog =
                    try {
                        importBackup(backupFileDataSource.read(uri), mode).toDialog()
                    } catch (_: IOException) {
                        SettingsContract.BackupDialog.ImportFailed(
                            SettingsContract.ImportError.ReadFailed,
                        )
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        // Unexpected failure below the repository; the import
                        // transaction has rolled back, so local data is intact.
                        SettingsContract.BackupDialog.ImportFailed(
                            SettingsContract.ImportError.Unexpected,
                        )
                    }
                host.updateState { it.copy(backupDialog = dialog, backupInProgress = false) }
            }
        }

        /** Names the destination folder when recognizable, e.g. "Exported to Downloads". */
        private fun exportSuccessMessage(uri: Uri): SettingsContract.UiEffect.ShowMessage =
            when (val location = backupFileDataSource.locationLabel(uri)) {
                null -> SettingsContract.UiEffect.ShowMessage(R.string.backup_export_success)
                else -> SettingsContract.UiEffect.ShowMessage(R.string.backup_export_success_at, listOf(location))
            }
    }

private fun ImportOutcome.toDialog(): SettingsContract.BackupDialog =
    when (this) {
        is ImportOutcome.Success ->
            SettingsContract.BackupDialog.ImportSucceeded(
                imported = summary.totalImported,
                skipped = summary.totalSkipped,
                conflicts = summary.conflicts.toImmutableList(),
            )

        is ImportOutcome.UnsupportedVersion ->
            SettingsContract.BackupDialog.ImportFailed(
                SettingsContract.ImportError.UnsupportedVersion(fileVersion, maxSupported),
            )

        is ImportOutcome.CorruptFile ->
            SettingsContract.BackupDialog.ImportFailed(SettingsContract.ImportError.CorruptFile)

        is ImportOutcome.ValidationFailed ->
            SettingsContract.BackupDialog.ImportFailed(
                SettingsContract.ImportError.ValidationFailed(issues.toImmutableList()),
            )
    }
