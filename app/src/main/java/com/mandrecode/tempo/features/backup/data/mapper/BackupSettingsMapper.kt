package com.mandrecode.tempo.features.backup.data.mapper

import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.features.backup.data.model.SettingsBackupDto
import com.mandrecode.tempo.features.backup.domain.model.BackupDefaultTab
import com.mandrecode.tempo.features.backup.domain.model.BackupSettings

internal fun BackupSettings.toDto(): SettingsBackupDto =
    SettingsBackupDto(
        themeMode = themeMode.name,
        useTempoColors = useTempoColors,
        routinesTabEnabled = routinesTabEnabled,
        tasksTabEnabled = tasksTabEnabled,
        defaultTab = defaultTab.name,
        autoRemoveCompletedTasks = autoRemoveCompletedTasks,
        completedTaskRetentionDays = completedTaskRetentionDays,
    )

internal fun SettingsBackupDto.toDomain(): BackupSettings =
    BackupSettings(
        themeMode = enumValueOf<ThemeMode>(themeMode),
        useTempoColors = useTempoColors,
        routinesTabEnabled = routinesTabEnabled,
        tasksTabEnabled = tasksTabEnabled,
        defaultTab = enumValueOf<BackupDefaultTab>(defaultTab),
        autoRemoveCompletedTasks = autoRemoveCompletedTasks,
        completedTaskRetentionDays = completedTaskRetentionDays,
    )
