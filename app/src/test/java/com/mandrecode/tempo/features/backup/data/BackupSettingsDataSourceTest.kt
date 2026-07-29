package com.mandrecode.tempo.features.backup.data

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import com.mandrecode.tempo.core.domain.model.TempoTab
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
    private lateinit var navigationPreferences: NavigationPreferencesRepository
    private lateinit var retentionPreferences: CompletedTaskRetentionPreferences
    private lateinit var vacationModeRepository: VacationModeRepository
    private lateinit var storedVacationPeriods: MutableStateFlow<List<VacationPeriod>>
    private lateinit var dataSource: BackupSettingsDataSource

    @Before
    fun setUp() {
        themePreferences = mockk(relaxed = true)
        navigationPreferences = mockk(relaxed = true)
        retentionPreferences = mockk(relaxed = true)
        storedVacationPeriods = MutableStateFlow(emptyList())
        vacationModeRepository =
            mockk(relaxed = true) {
                every { periods } returns storedVacationPeriods
            }
        dataSource =
            BackupSettingsDataSource(
                themePreferences,
                navigationPreferences,
                retentionPreferences,
                vacationModeRepository,
            )
    }

    @Test
    fun `snapshot captures every preference`() =
        runTest {
            every { themePreferences.getThemeMode() } returns flowOf(ThemeMode.DARK)
            every { themePreferences.getUseTempoColors() } returns flowOf(true)
            every { navigationPreferences.enabledTabs() } returns flowOf(setOf(TempoTab.FOCUS, TempoTab.TASKS))
            every { navigationPreferences.getDefaultTab() } returns flowOf(TempoTab.TASKS)
            every { retentionPreferences.isEnabled } returns MutableStateFlow(true)
            every { retentionPreferences.retentionDays } returns MutableStateFlow(90)
            storedVacationPeriods.value = listOf(VACATION)

            val snapshot = dataSource.snapshot()

            assertThat(snapshot).isEqualTo(
                BackupSettings(
                    themeMode = ThemeMode.DARK,
                    useTempoColors = true,
                    enabledTabs = setOf(TempoTab.FOCUS, TempoTab.TASKS),
                    defaultTab = TempoTab.TASKS,
                    autoRemoveCompletedTasks = true,
                    completedTaskRetentionDays = 90,
                    vacationPeriods = listOf(VACATION),
                ),
            )
        }

    @Test
    fun `snapshot maps a routines default tab`() =
        runTest {
            every { themePreferences.getThemeMode() } returns flowOf(ThemeMode.SYSTEM)
            every { themePreferences.getUseTempoColors() } returns flowOf(false)
            every { navigationPreferences.enabledTabs() } returns flowOf(TempoTab.entries.toSet())
            every { navigationPreferences.getDefaultTab() } returns flowOf(TempoTab.ROUTINES)
            every { retentionPreferences.isEnabled } returns MutableStateFlow(false)
            every { retentionPreferences.retentionDays } returns MutableStateFlow(30)

            assertThat(dataSource.snapshot().defaultTab).isEqualTo(TempoTab.ROUTINES)
        }

    @Test
    fun `apply writes every preference back`() {
        dataSource.apply(
            BackupSettings(
                themeMode = ThemeMode.LIGHT,
                useTempoColors = true,
                enabledTabs = TempoTab.entries.toSet(),
                defaultTab = TempoTab.TASKS,
                autoRemoveCompletedTasks = true,
                completedTaskRetentionDays = 90,
                vacationPeriods = listOf(VACATION),
            ),
        )

        verify { themePreferences.setThemeMode(ThemeMode.LIGHT) }
        verify { themePreferences.setUseTempoColors(true) }
        TempoTab.entries.forEach { tab ->
            verify { navigationPreferences.setTabEnabled(tab, true) }
        }
        verify { navigationPreferences.setDefaultTab(TempoTab.TASKS) }
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
    fun `apply keeps at least one tab enabled`() {
        dataSource.apply(settings(enabledTabs = emptySet()))

        verify { navigationPreferences.setTabEnabled(TempoTab.DEFAULT, true) }
        verify { navigationPreferences.setDefaultTab(TempoTab.DEFAULT) }
    }

    @Test
    fun `apply moves the default tab off a disabled tab`() {
        dataSource.apply(
            settings(
                enabledTabs = setOf(TempoTab.TASKS),
                defaultTab = TempoTab.ROUTINES,
            ),
        )

        verify { navigationPreferences.setTabEnabled(TempoTab.ROUTINES, false) }
        verify { navigationPreferences.setDefaultTab(TempoTab.TASKS) }
    }

    @Test
    fun `apply normalizes the retention days to a supported value`() {
        dataSource.apply(settings(completedTaskRetentionDays = 8))

        verify { retentionPreferences.setRetentionDays(7) }
    }

    private fun settings(
        enabledTabs: Set<TempoTab> = TempoTab.entries.toSet(),
        defaultTab: TempoTab = TempoTab.ROUTINES,
        completedTaskRetentionDays: Int = 30,
    ) = BackupSettings(
        themeMode = ThemeMode.SYSTEM,
        useTempoColors = false,
        enabledTabs = enabledTabs,
        defaultTab = defaultTab,
        autoRemoveCompletedTasks = false,
        completedTaskRetentionDays = completedTaskRetentionDays,
    )

    private companion object {
        val VACATION = VacationPeriod(LocalDate(2026, 3, 1), LocalDate(2026, 3, 8))
    }
}
