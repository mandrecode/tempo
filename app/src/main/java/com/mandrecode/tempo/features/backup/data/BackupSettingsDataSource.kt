package com.mandrecode.tempo.features.backup.data

import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import com.mandrecode.tempo.core.domain.model.TempoTab
import com.mandrecode.tempo.core.domain.repository.VacationModeRepository
import com.mandrecode.tempo.core.domain.util.TabPreferencesPolicy
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
        private val navigationPreferences: NavigationPreferencesRepository,
        private val retentionPreferences: CompletedTaskRetentionPreferences,
        private val vacationModeRepository: VacationModeRepository,
    ) {
        suspend fun snapshot(): BackupSettings =
            BackupSettings(
                themeMode = themePreferences.getThemeMode().first(),
                useTempoColors = themePreferences.getUseTempoColors().first(),
                enabledTabs = navigationPreferences.enabledTabs().first(),
                defaultTab = navigationPreferences.getDefaultTab().first(),
                autoRemoveCompletedTasks = retentionPreferences.isEnabled.value,
                completedTaskRetentionDays = retentionPreferences.retentionDays.value,
                vacationPeriods = vacationModeRepository.periods.value,
            )

        /**
         * Applies restored settings, re-establishing app invariants a hand-edited
         * file could violate: at least one tab stays enabled and the default tab
         * is always an enabled one.
         */
        fun apply(settings: BackupSettings) {
            themePreferences.setThemeMode(settings.themeMode)
            themePreferences.setUseTempoColors(settings.useTempoColors)
            val enabledTabs = TabPreferencesPolicy.resolveEnabledTabs(settings.enabledTabs)
            TempoTab.entries.forEach { tab ->
                navigationPreferences.setTabEnabled(tab, tab in enabledTabs)
            }
            navigationPreferences.setDefaultTab(
                TabPreferencesPolicy.resolveDefaultTab(settings.defaultTab, enabledTabs),
            )
            retentionPreferences.setEnabled(settings.autoRemoveCompletedTasks)
            retentionPreferences.setRetentionDays(
                CompletedTaskRetentionPreferences.normalizeRetentionDays(
                    settings.completedTaskRetentionDays,
                ),
            )
            vacationModeRepository.replaceAll(settings.vacationPeriods)
        }
    }
