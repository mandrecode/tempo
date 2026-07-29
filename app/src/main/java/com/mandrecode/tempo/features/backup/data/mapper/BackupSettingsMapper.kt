package com.mandrecode.tempo.features.backup.data.mapper

import com.mandrecode.tempo.core.domain.model.TempoTab
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.core.domain.model.VacationPeriod
import com.mandrecode.tempo.core.domain.util.TabPreferencesPolicy
import com.mandrecode.tempo.features.backup.data.model.SettingsBackupDto
import com.mandrecode.tempo.features.backup.data.model.VacationPeriodBackupDto
import com.mandrecode.tempo.features.backup.domain.model.BackupSettings
import kotlinx.datetime.LocalDate

internal fun BackupSettings.toDto(): SettingsBackupDto =
    SettingsBackupDto(
        themeMode = themeMode.name,
        useTempoColors = useTempoColors,
        // Legacy booleans stay populated so a file written here still restores sensibly on a
        // build that predates the tab registry.
        routinesTabEnabled = TempoTab.ROUTINES in enabledTabs,
        tasksTabEnabled = TempoTab.TASKS in enabledTabs,
        enabledTabs = enabledTabs.map { it.name },
        defaultTab = defaultTab.name,
        autoRemoveCompletedTasks = autoRemoveCompletedTasks,
        completedTaskRetentionDays = completedTaskRetentionDays,
        vacationPeriods =
            vacationPeriods.map { period ->
                VacationPeriodBackupDto(
                    start = period.start.toString(),
                    end = period.endInclusive?.toString(),
                )
            },
    )

internal fun SettingsBackupDto.toDomain(): BackupSettings {
    val restoredTabs =
        enabledTabs
            ?.mapNotNull { name -> TempoTab.entries.firstOrNull { it.name == name } }
            ?.toSet()
            // Pre-Focus file: rebuild from the legacy booleans, with Focus enabled since the file
            // could not have expressed a preference about it.
            ?: buildSet {
                add(TempoTab.FOCUS)
                if (routinesTabEnabled) add(TempoTab.ROUTINES)
                if (tasksTabEnabled) add(TempoTab.TASKS)
            }

    return BackupSettings(
        themeMode = enumValueOf<ThemeMode>(themeMode),
        useTempoColors = useTempoColors,
        enabledTabs = TabPreferencesPolicy.resolveEnabledTabs(restoredTabs),
        defaultTab = TempoTab.entries.firstOrNull { it.name == defaultTab } ?: TempoTab.DEFAULT,
        autoRemoveCompletedTasks = autoRemoveCompletedTasks,
        completedTaskRetentionDays = completedTaskRetentionDays,
        vacationPeriods = vacationPeriods.toDomainPeriods(),
    )
}

/**
 * Parses the backup's vacation periods, dropping entries whose dates are unreadable and
 * normalizing the rest, so a hand-edited file cannot install overlapping or inverted ranges.
 */
private fun List<VacationPeriodBackupDto>.toDomainPeriods(): List<VacationPeriod> =
    VacationPeriod.normalize(
        mapNotNull { dto ->
            val start = dto.start.toLocalDateOrNull() ?: return@mapNotNull null
            val end = dto.end?.let { it.toLocalDateOrNull() ?: return@mapNotNull null }
            VacationPeriod(start = start, endInclusive = end)
        },
    )

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()
