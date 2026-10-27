package com.mandrecode.tempo.features.backup.data

import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import com.mandrecode.tempo.core.domain.repository.VacationModeRepository
import com.mandrecode.tempo.features.backup.domain.model.BackupSettings
import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first

/**
 * Bridges the app's preference repositories and the backup [BackupSettings]
 * snapshot: reads the current configuration for export and applies a restored
 * configuration on Replace imports.
 */
class BackupSettingsDataSource
    @Inject
    constructor(
        private val themePreferences: ThemePreferencesRepository,
        private val retentionPreferences: CompletedTaskRetentionPreferences,
        private val vacationModeRepository: VacationModeRepository,
    ) {
        suspend fun snapshot(): BackupSettings =
            BackupSettings(
                themeMode = themePreferences.getThemeMode().first(),
                useTempoColors = themePreferences.getUseTempoColors().first(),
                autoRemoveCompletedTasks = retentionPreferences.isEnabled.value,
                completedTaskRetentionDays = retentionPreferences.retentionDays.value,
                vacationPeriods = vacationModeRepository.periods.value,
            )

        /** Applies restored settings on a Replace import. */
        fun apply(settings: BackupSettings) {
            themePreferences.setThemeMode(settings.themeMode)
            themePreferences.setUseTempoColors(settings.useTempoColors)
            retentionPreferences.setEnabled(settings.autoRemoveCompletedTasks)
            retentionPreferences.setRetentionDays(
                CompletedTaskRetentionPreferences.normalizeRetentionDays(
                    settings.completedTaskRetentionDays,
                ),
            )
            vacationModeRepository.replaceAll(settings.vacationPeriods)
        }
    }
