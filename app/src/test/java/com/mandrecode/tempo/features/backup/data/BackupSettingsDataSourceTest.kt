package com.mandrecode.tempo.features.backup.data

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.core.domain.model.VacationPeriod
import com.mandrecode.tempo.core.domain.repository.VacationModeRepository
import com.mandrecode.tempo.features.backup.domain.model.BackupSettings
import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Test

class BackupSettingsDataSourceTest {
    private lateinit var themePreferences: ThemePreferencesRepository
    private lateinit var retentionPreferences: CompletedTaskRetentionPreferences
    private lateinit var vacationModeRepository: VacationModeRepository
    private lateinit var storedVacationPeriods: MutableStateFlow<List<VacationPeriod>>
    private lateinit var dataSource: BackupSettingsDataSource

    @Before
    fun setUp() {
        themePreferences = mockk(relaxed = true)
        retentionPreferences = mockk(relaxed = true)
        storedVacationPeriods = MutableStateFlow(emptyList())
        vacationModeRepository =
            mockk(relaxed = true) {
                every { periods } returns storedVacationPeriods
            }
        dataSource =
            BackupSettingsDataSource(
                themePreferences,
                retentionPreferences,
                vacationModeRepository,
            )
    }

    @Test
    fun `snapshot captures every preference`() =
        runTest {
            every { themePreferences.getThemeMode() } returns flowOf(ThemeMode.DARK)
            every { themePreferences.getUseTempoColors() } returns flowOf(true)
            every { retentionPreferences.isEnabled } returns MutableStateFlow(true)
            every { retentionPreferences.retentionDays } returns MutableStateFlow(90)
            storedVacationPeriods.value = listOf(VACATION)

            val snapshot = dataSource.snapshot()

            assertThat(snapshot).isEqualTo(
                BackupSettings(
                    themeMode = ThemeMode.DARK,
                    useTempoColors = true,
                    autoRemoveCompletedTasks = true,
                    completedTaskRetentionDays = 90,
                    vacationPeriods = listOf(VACATION),
                ),
            )
        }

    @Test
    fun `apply writes every preference back`() {
        dataSource.apply(
            BackupSettings(
                themeMode = ThemeMode.LIGHT,
                useTempoColors = true,
                autoRemoveCompletedTasks = true,
                completedTaskRetentionDays = 90,
                vacationPeriods = listOf(VACATION),
            ),
        )

        verify { themePreferences.setThemeMode(ThemeMode.LIGHT) }
        verify { themePreferences.setUseTempoColors(true) }
        verify { retentionPreferences.setEnabled(true) }
        verify { retentionPreferences.setRetentionDays(90) }
        verify { vacationModeRepository.replaceAll(listOf(VACATION)) }
    }

    @Test
    fun `apply restores an empty period list for files predating vacation mode`() {
        dataSource.apply(settings())

        verify { vacationModeRepository.replaceAll(emptyList()) }
    }

    @Test
    fun `apply normalizes the retention days to a supported value`() {
        dataSource.apply(settings(completedTaskRetentionDays = 8))

        verify { retentionPreferences.setRetentionDays(7) }
    }

    private fun settings(completedTaskRetentionDays: Int = 30) =
        BackupSettings(
            themeMode = ThemeMode.SYSTEM,
            useTempoColors = false,
            autoRemoveCompletedTasks = false,
            completedTaskRetentionDays = completedTaskRetentionDays,
        )

    private companion object {
        val VACATION = VacationPeriod(LocalDate(2026, 3, 1), LocalDate(2026, 3, 8))
    }
}
