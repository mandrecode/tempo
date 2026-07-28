package com.mandrecode.tempo.features.backup.data.mapper

import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.core.domain.model.VacationPeriod
import com.mandrecode.tempo.features.backup.data.model.SettingsBackupDto
import com.mandrecode.tempo.features.backup.data.model.VacationPeriodBackupDto
import com.mandrecode.tempo.features.backup.domain.model.BackupDefaultTab
import com.mandrecode.tempo.features.backup.domain.model.BackupSettings
import kotlinx.datetime.LocalDate

internal fun BackupSettings.toDto(): SettingsBackupDto =
    SettingsBackupDto(
        themeMode = themeMode.name,
        useTempoColors = useTempoColors,
        routinesTabEnabled = routinesTabEnabled,
        tasksTabEnabled = tasksTabEnabled,
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

internal fun SettingsBackupDto.toDomain(): BackupSettings =
    BackupSettings(
        themeMode = enumValueOf<ThemeMode>(themeMode),
        useTempoColors = useTempoColors,
        routinesTabEnabled = routinesTabEnabled,
        tasksTabEnabled = tasksTabEnabled,
        defaultTab = enumValueOf<BackupDefaultTab>(defaultTab),
        autoRemoveCompletedTasks = autoRemoveCompletedTasks,
        completedTaskRetentionDays = completedTaskRetentionDays,
        vacationPeriods = vacationPeriods.toDomainPeriods(),
    )

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
