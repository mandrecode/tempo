package com.mandrecode.tempo.features.backup.data.mapper

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.domain.model.MonthDayOption
import com.mandrecode.tempo.core.domain.model.Periodicity
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.features.backup.data.model.TaskBackupDto
import com.mandrecode.tempo.features.backup.domain.model.BackupData
import com.mandrecode.tempo.features.backup.domain.model.BackupDefaultTab
import com.mandrecode.tempo.features.backup.domain.model.BackupSettings
import com.mandrecode.tempo.features.backup.domain.model.ChainMembership
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.model.HabitType
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupMapperTest {
    @Test
    fun `domain to dto to domain round trip preserves every field`() {
        val data = fullyPopulatedBackupData()

        val roundTripped =
            data
                .toDto(schemaVersion = 1, appVersion = "1.0.0", exportedAt = LocalDateTime(2026, 7, 21, 10, 0))
                .toDomain()

        assertThat(roundTripped).isEqualTo(data)
    }

    private fun fullyPopulatedBackupData(): BackupData =
        BackupData(
            categories = listOf(Category(id = 1, name = "Work", color = "red", icon = "star", sortOrder = 2)),
            tasks =
                listOf(
                    Task(
                        id = 10,
                        title = "Report",
                        description = "Quarterly",
                        isCompleted = true,
                        categoryId = 1,
                        priority = Priority.HIGH,
                        reminderDate = LocalDateTime(2026, 8, 1, 9, 0),
                        periodicity = Periodicity.MONTHLY,
                        periodicityInterval = 2,
                        repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.SUNDAY),
                        monthDayOption = MonthDayOption.LAST_DAY,
                        parentTaskId = null,
                        sortOrder = 3,
                        completedAt = LocalDateTime(2026, 7, 1, 12, 30),
                        nextInstanceId = 11,
                    ),
                ),
            habits =
                listOf(
                    Habit(
                        id = 20,
                        title = "Run",
                        description = "5k",
                        icon = "shoe",
                        colorKey = "green",
                        reminderDate = LocalDateTime(2026, 7, 22, 7, 0),
                        isCompleted = false,
                        habitType = HabitType.QUIT,
                        createdDate = LocalDateTime(2026, 1, 1, 8, 0),
                        completionHistory = "2026-01-02,2026-01-03",
                        repeatDays = setOf(DayOfWeek.TUESDAY),
                    ),
                ),
            habitChains =
                listOf(
                    HabitChain(
                        id = 30,
                        title = "Morning",
                        description = "Wake up routine",
                        colorKey = "blue",
                        icon = "sun",
                        periodicReminder = LocalDateTime(2026, 7, 22, 6, 30),
                        createdDate = LocalDateTime(2026, 1, 1, 8, 0),
                        completionHistory = "2026-01-05",
                        repeatDays = setOf(DayOfWeek.MONDAY),
                    ),
                ),
            chainMemberships = listOf(ChainMembership(chainId = 30, habitId = 20, sortOrder = 0)),
            settings = sampleSettings(),
        )

    private fun sampleSettings(): BackupSettings =
        BackupSettings(
            themeMode = ThemeMode.DARK,
            useTempoColors = true,
            routinesTabEnabled = true,
            tasksTabEnabled = false,
            defaultTab = BackupDefaultTab.ROUTINES,
            autoRemoveCompletedTasks = true,
            completedTaskRetentionDays = 90,
        )

    @Test
    fun `minimal data with nulls and defaults round trips unchanged`() {
        val data =
            BackupData(
                categories = listOf(Category(id = -1, name = "Inbox", isDefault = true, sortOrder = -1)),
                tasks = listOf(Task(id = 1, title = "Bare", description = "", categoryId = -1)),
                habits =
                    listOf(
                        Habit(
                            id = 2,
                            title = "Bare habit",
                            description = "",
                            createdDate = LocalDateTime(2026, 1, 1, 0, 0),
                        ),
                    ),
                habitChains = emptyList(),
                chainMemberships = emptyList(),
            )

        val roundTripped =
            data
                .toDto(schemaVersion = 1, appVersion = "", exportedAt = LocalDateTime(2026, 1, 1, 0, 0))
                .toDomain()

        assertThat(roundTripped).isEqualTo(data)
    }

    @Test
    fun `unknown day of week number fails decoding`() {
        val dto =
            com.mandrecode.tempo.features.backup.data.model
                .BackupFileDto(
                    schemaVersion = 1,
                    categories = listOf(),
                    tasks =
                        listOf(
                            TaskBackupDto(id = 1, title = "T", categoryId = 1, repeatDays = listOf(9)),
                        ),
                )

        assertThrows(IllegalArgumentException::class.java) { dto.toDomain() }
    }
}
