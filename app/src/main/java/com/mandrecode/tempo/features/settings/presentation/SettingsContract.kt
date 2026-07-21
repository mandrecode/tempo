package com.mandrecode.tempo.features.settings.presentation

import android.net.Uri
import androidx.annotation.StringRes
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.features.backup.domain.model.ImportConflict
import com.mandrecode.tempo.features.backup.domain.model.ImportMode
import com.mandrecode.tempo.features.backup.domain.model.ValidationIssue
import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

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
        val isRoutinesTabEnabled: Boolean = true,
        val isTasksTabEnabled: Boolean = true,
        val defaultTab: DefaultTab = DefaultTab.ROUTINES,
        val autoRemoveCompletedTasksEnabled: Boolean = false,
        val completedTaskRetentionDays: Int = CompletedTaskRetentionPreferences.DEFAULT_RETENTION_DAYS,
        val backupInProgress: Boolean = false,
        val backupDialog: BackupDialog? = null,
    )

    /** Modal state of the Settings Backup section. */
    sealed interface BackupDialog {
        /** A file was picked for import; the user chooses Merge or Replace. */
        data object ChooseImportMode : BackupDialog

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

        data class RoutinesTabToggled(
            val enabled: Boolean,
        ) : UiEvent

        data class TasksTabToggled(
            val enabled: Boolean,
        ) : UiEvent

        data class DefaultTabSelected(
            val defaultTab: DefaultTab,
        ) : UiEvent

        data class AutoRemoveCompletedTasksToggled(
            val enabled: Boolean,
        ) : UiEvent

        data class CompletedTaskRetentionDaysChanged(
            val days: Int,
        ) : UiEvent

        data object ExportClicked : UiEvent

        data class ExportDestinationPicked(
            val uri: Uri,
        ) : UiEvent

        data object ExportCancelled : UiEvent

        data object ImportClicked : UiEvent

        data class ImportFilePicked(
            val uri: Uri,
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
        ) : UiEffect
    }

    /**
     * Represents the default tab option.
     */
    enum class DefaultTab {
        ROUTINES,
        TASKS,
    }
}
