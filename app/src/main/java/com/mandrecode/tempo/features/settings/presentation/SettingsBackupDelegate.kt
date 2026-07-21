package com.mandrecode.tempo.features.settings.presentation

import android.net.Uri
import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.backup.domain.model.ImportMode
import com.mandrecode.tempo.features.backup.domain.model.ImportOutcome
import com.mandrecode.tempo.features.backup.domain.repository.BackupRepository
import com.mandrecode.tempo.features.backup.domain.usecase.ExportBackupUseCase
import com.mandrecode.tempo.features.backup.domain.usecase.ImportBackupUseCase
import com.mandrecode.tempo.infrastructure.backup.BackupFileDataSource
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

/**
 * Owns the Settings Backup flows (export/import) on behalf of [SettingsViewModel]:
 * runs the use cases, tracks in-flight export/import state (including a picked
 * file's content and passphrase while its dialogs are up), and pushes state/effect
 * updates through the [Host].
 */
class SettingsBackupDelegate
    @Inject
    constructor(
        private val exportBackup: ExportBackupUseCase,
        private val importBackup: ImportBackupUseCase,
        private val backupRepository: BackupRepository,
        private val backupFileDataSource: BackupFileDataSource,
    ) {
        /** The ViewModel surface that the delegate acts through. */
        data class Host(
            val scope: CoroutineScope,
            val updateState: (transform: (SettingsContract.UiState) -> SettingsContract.UiState) -> Unit,
            val sendEffect: (SettingsContract.UiEffect) -> Unit,
        )

        private var pendingExportPayload: String? = null
        private var pendingImportContent: String? = null
        private var pendingImportPassphrase: String? = null
        private var pendingImportMode: ImportMode? = null

        /**
         * Tracks the in-flight file read from [handleImportFilePicked] so it can be cancelled —
         * without this, a dialog dismissed while the read is still in progress would get
         * silently resurrected when the read completes afterward and writes a new dialog state.
         */
        private var importReadJob: Job? = null

        /** Handles backup-related events; returns false for events it does not own. */
        fun onEvent(
            event: SettingsContract.UiEvent,
            host: Host,
        ): Boolean {
            when (event) {
                is SettingsContract.UiEvent.ExportClicked ->
                    host.updateState {
                        it.copy(backupDialog = SettingsContract.BackupDialog.EnterExportPassphrase)
                    }

                is SettingsContract.UiEvent.ExportPassphraseConfirmed ->
                    handleExportPassphraseConfirmed(event, host)

                is SettingsContract.UiEvent.ExportDestinationPicked ->
                    handleExportDestinationPicked(event.uri, host)

                is SettingsContract.UiEvent.ExportCancelled -> {
                    pendingExportPayload = null
                }

                is SettingsContract.UiEvent.ImportClicked ->
                    host.sendEffect(SettingsContract.UiEffect.LaunchImportPicker)

                is SettingsContract.UiEvent.ImportFilePicked -> handleImportFilePicked(event.uri, host)

                is SettingsContract.UiEvent.ImportPassphraseEntered ->
                    handleImportPassphraseEntered(event.passphrase, host)

                is SettingsContract.UiEvent.ImportModeChosen -> handleImportModeChosen(event.mode, host)

                is SettingsContract.UiEvent.BackupDialogDismissed -> {
                    // If a file read is still in flight, cancelling it means neither of its own
                    // updateState calls (success or IOException branch) will ever run — reset
                    // backupInProgress here too, or it would stay stuck true (spinner/disabled UI).
                    importReadJob?.cancel()
                    clearPendingImportState()
                    host.updateState { it.copy(backupDialog = null, backupInProgress = false) }
                }

                else -> return false
            }
            return true
        }

        private fun handleExportPassphraseConfirmed(
            event: SettingsContract.UiEvent.ExportPassphraseConfirmed,
            host: Host,
        ) {
            if (event.passphrase.isEmpty() || event.passphrase != event.confirmation) return
            host.updateState { it.copy(backupDialog = null) }
            host.scope.launch {
                host.updateState { it.copy(backupInProgress = true) }
                try {
                    val export = exportBackup(event.passphrase.toCharArray())
                    pendingExportPayload = export.json
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
            val payload = pendingExportPayload ?: return
            pendingExportPayload = null
            host.scope.launch {
                host.updateState { it.copy(backupInProgress = true) }
                try {
                    backupFileDataSource.write(uri, payload)
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

        private fun handleImportFilePicked(
            uri: Uri,
            host: Host,
        ) {
            importReadJob?.cancel()
            importReadJob =
                host.scope.launch {
                    host.updateState { it.copy(backupInProgress = true) }
                    val content =
                        try {
                            backupFileDataSource.read(uri)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: IOException) {
                            host.updateState {
                                it.copy(
                                    backupInProgress = false,
                                    backupDialog =
                                        SettingsContract.BackupDialog.ImportFailed(
                                            SettingsContract.ImportError.ReadFailed,
                                        ),
                                )
                            }
                            return@launch
                        }
                    pendingImportContent = content
                    val dialog =
                        if (backupRepository.isEncryptedBackup(content)) {
                            SettingsContract.BackupDialog.EnterImportPassphrase()
                        } else {
                            SettingsContract.BackupDialog.ChooseImportMode
                        }
                    host.updateState { it.copy(backupInProgress = false, backupDialog = dialog) }
                }
        }

        private fun handleImportPassphraseEntered(
            passphrase: String,
            host: Host,
        ) {
            pendingImportPassphrase = passphrase
            val rememberedMode = pendingImportMode
            if (rememberedMode != null) {
                attemptImport(rememberedMode, host)
            } else {
                host.updateState { it.copy(backupDialog = SettingsContract.BackupDialog.ChooseImportMode) }
            }
        }

        private fun handleImportModeChosen(
            mode: ImportMode,
            host: Host,
        ) {
            pendingImportMode = mode
            attemptImport(mode, host)
        }

        private fun attemptImport(
            mode: ImportMode,
            host: Host,
        ) {
            val content = pendingImportContent ?: return
            val passphrase = pendingImportPassphrase
            host.scope.launch {
                host.updateState { it.copy(backupDialog = null, backupInProgress = true) }
                val dialog =
                    try {
                        val outcome = importBackup(content, mode, passphrase?.toCharArray())
                        if (outcome is ImportOutcome.WrongPassphrase) {
                            pendingImportPassphrase = null
                        } else {
                            clearPendingImportState()
                        }
                        outcome.toDialog()
                    } catch (_: IOException) {
                        clearPendingImportState()
                        SettingsContract.BackupDialog.ImportFailed(
                            SettingsContract.ImportError.ReadFailed,
                        )
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        // Unexpected failure below the repository; the import
                        // transaction has rolled back, so local data is intact.
                        clearPendingImportState()
                        SettingsContract.BackupDialog.ImportFailed(
                            SettingsContract.ImportError.Unexpected,
                        )
                    }
                host.updateState { it.copy(backupDialog = dialog, backupInProgress = false) }
            }
        }

        private fun clearPendingImportState() {
            pendingImportContent = null
            pendingImportPassphrase = null
            pendingImportMode = null
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

        is ImportOutcome.WrongPassphrase ->
            SettingsContract.BackupDialog.EnterImportPassphrase(attemptsFailed = true)
    }
