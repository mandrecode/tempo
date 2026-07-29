package com.mandrecode.tempo.features.settings.presentation

import android.net.Uri
import androidx.annotation.StringRes
import com.mandrecode.tempo.core.domain.model.TempoTab
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.features.backup.domain.model.ImportConflict
import com.mandrecode.tempo.features.backup.domain.model.ImportMode
import com.mandrecode.tempo.features.backup.domain.model.ValidationIssue
import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import com.mandrecode.tempo.features.tasks.domain.repository.MissedReminderPreferences
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Contract for Settings screen following MVI pattern.
 */
object SettingsContract {
    /**
     * UI State for Settings screen.
     */
    data class UiState(
        val selectedThemeMode: ThemeMode = ThemeMode.SYSTEM,
        val availableThemeModes: ImmutableList<ThemeMode> =
            persistentListOf(
                ThemeMode.LIGHT,
                ThemeMode.DARK,
                ThemeMode.SYSTEM,
            ),
        val useTempoColors: Boolean = false,
        val appVersion: String = "",
        val enabledTabs: ImmutableSet<TempoTab> = TempoTab.entries.toPersistentSet(),
        val defaultTab: TempoTab = TempoTab.DEFAULT,
        val autoRemoveCompletedTasksEnabled: Boolean = false,
        val completedTaskRetentionDays: Int = CompletedTaskRetentionPreferences.DEFAULT_RETENTION_DAYS,
        val missedReminderCatchUpEnabled: Boolean = MissedReminderPreferences.DEFAULT_ENABLED,
        val missedReminderCatchUpTime: LocalTime = MissedReminderPreferences.DEFAULT_CATCH_UP_TIME,
        val focusSessionLengthMinutes: Int = 25,
        val vacationModeActive: Boolean = false,
        val vacationStartDate: LocalDate? = null,
        val vacationEndDate: LocalDate? = null,
        val backupInProgress: Boolean = false,
        val backupDialog: BackupDialog? = null,
    )

    /** Modal state of the Settings Backup section. */
    sealed interface BackupDialog {
        /** Export was requested; the user must set a passphrase before the file is written. */
        data object EnterExportPassphrase : BackupDialog

        /** A file was picked for import; the user chooses Merge or Replace. */
        data object ChooseImportMode : BackupDialog

        /** The picked file is encrypted; the user must enter the passphrase used to create it. */
        data class EnterImportPassphrase(
            val attemptsFailed: Boolean = false,
        ) : BackupDialog

        data class ImportSucceeded(
            val imported: Int,
            val skipped: Int,
            val conflicts: ImmutableList<ImportConflict>,
        ) : BackupDialog

        data class ImportFailed(
            val error: ImportError,
        ) : BackupDialog
    }

    /** User-facing import failure, mirroring the domain outcome plus file-read errors. */
    sealed interface ImportError {
        data class UnsupportedVersion(
            val fileVersion: Int,
            val maxSupported: Int,
        ) : ImportError

        data object CorruptFile : ImportError

        data class ValidationFailed(
            val issues: ImmutableList<ValidationIssue>,
        ) : ImportError

        data object ReadFailed : ImportError

        /** Unexpected failure while applying the import; the database was rolled back. */
        data object Unexpected : ImportError
    }

    /**
     * UI Events that can be triggered from the Settings screen.
     */
    sealed interface UiEvent {
        data class ThemeModeSelected(
            val mode: ThemeMode,
        ) : UiEvent

        data class TempoColorsToggled(
            val enabled: Boolean,
        ) : UiEvent

        data class TabToggled(
            val tab: TempoTab,
            val enabled: Boolean,
        ) : UiEvent

        data class DefaultTabSelected(
            val defaultTab: TempoTab,
        ) : UiEvent

        data class AutoRemoveCompletedTasksToggled(
            val enabled: Boolean,
        ) : UiEvent

        data class CompletedTaskRetentionDaysChanged(
            val days: Int,
        ) : UiEvent

        data class MissedReminderCatchUpToggled(
            val enabled: Boolean,
        ) : UiEvent

        data class MissedReminderCatchUpTimeChanged(
            val time: LocalTime,
        ) : UiEvent

        data class FocusSessionLengthChanged(
            val minutes: Int,
        ) : UiEvent

        data class VacationModeToggled(
            val enabled: Boolean,
        ) : UiEvent

        /** `null` clears the planned end, leaving the pause open-ended. */
        data class VacationEndDateChanged(
            val endInclusive: LocalDate?,
        ) : UiEvent

        data object ExportClicked : UiEvent

        data class ExportPassphraseConfirmed(
            val passphrase: CharArray,
            val confirmation: CharArray,
        ) : UiEvent

        data class ExportDestinationPicked(
            val uri: Uri,
        ) : UiEvent

        data object ExportCancelled : UiEvent

        data object ImportClicked : UiEvent

        data class ImportFilePicked(
            val uri: Uri,
        ) : UiEvent

        data class ImportPassphraseEntered(
            val passphrase: CharArray,
        ) : UiEvent

        data class ImportModeChosen(
            val mode: ImportMode,
        ) : UiEvent

        data object BackupDialogDismissed : UiEvent
    }

    /**
     * One-time UI Effects for Settings screen.
     */
    sealed interface UiEffect {
        data class LaunchExportPicker(
            val suggestedFileName: String,
        ) : UiEffect

        data object LaunchImportPicker : UiEffect

        data class ShowMessage(
            @param:StringRes val message: Int,
            val formatArgs: List<Any> = emptyList(),
        ) : UiEffect
    }
}
