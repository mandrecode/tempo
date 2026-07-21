package com.mandrecode.tempo.features.backup.data

import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository.Companion.DEFAULT_TAB_ROUTINES
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository.Companion.DEFAULT_TAB_TASKS
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import com.mandrecode.tempo.features.backup.domain.model.BackupDefaultTab
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
    ) {
        suspend fun snapshot(): BackupSettings =
            BackupSettings(
                themeMode = themePreferences.getThemeMode().first(),
                useTempoColors = themePreferences.getUseTempoColors().first(),
                routinesTabEnabled = navigationPreferences.isRoutinesTabEnabled().first(),
                tasksTabEnabled = navigationPreferences.isTasksTabEnabled().first(),
                defaultTab =
                    when (navigationPreferences.getDefaultTab().first()) {
                        DEFAULT_TAB_TASKS -> BackupDefaultTab.TASKS
                        else -> BackupDefaultTab.ROUTINES
                    },
                autoRemoveCompletedTasks = retentionPreferences.isEnabled.value,
                completedTaskRetentionDays = retentionPreferences.retentionDays.value,
            )

        /**
         * Applies restored settings, re-establishing app invariants a hand-edited
         * file could violate: at least one tab stays enabled and the default tab
         * is always an enabled one.
         */
        fun apply(settings: BackupSettings) {
            themePreferences.setThemeMode(settings.themeMode)
            themePreferences.setUseTempoColors(settings.useTempoColors)
            val routinesEnabled = settings.routinesTabEnabled || !settings.tasksTabEnabled
            navigationPreferences.setRoutinesTabEnabled(routinesEnabled)
            navigationPreferences.setTasksTabEnabled(settings.tasksTabEnabled)
            val defaultTab =
                when {
                    settings.defaultTab == BackupDefaultTab.TASKS && settings.tasksTabEnabled ->
                        DEFAULT_TAB_TASKS

                    routinesEnabled -> DEFAULT_TAB_ROUTINES
                    else -> DEFAULT_TAB_TASKS
                }
            navigationPreferences.setDefaultTab(defaultTab)
            retentionPreferences.setEnabled(settings.autoRemoveCompletedTasks)
            retentionPreferences.setRetentionDays(
                CompletedTaskRetentionPreferences.normalizeRetentionDays(
                    settings.completedTaskRetentionDays,
                ),
            )
        }
    }
