package com.mandrecode.tempo.features.backup.data

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository.Companion.DEFAULT_TAB_ROUTINES
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository.Companion.DEFAULT_TAB_TASKS
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.features.backup.domain.model.BackupDefaultTab
import com.mandrecode.tempo.features.backup.domain.model.BackupSettings
import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class BackupSettingsDataSourceTest {
    private lateinit var themePreferences: ThemePreferencesRepository
    private lateinit var navigationPreferences: NavigationPreferencesRepository
    private lateinit var retentionPreferences: CompletedTaskRetentionPreferences
    private lateinit var dataSource: BackupSettingsDataSource

    @Before
    fun setUp() {
        themePreferences = mockk(relaxed = true)
        navigationPreferences = mockk(relaxed = true)
        retentionPreferences = mockk(relaxed = true)
        dataSource =
            BackupSettingsDataSource(themePreferences, navigationPreferences, retentionPreferences)
    }

    @Test
    fun `snapshot captures every preference`() =
        runTest {
            every { themePreferences.getThemeMode() } returns flowOf(ThemeMode.DARK)
            every { themePreferences.getUseTempoColors() } returns flowOf(true)
            every { navigationPreferences.isRoutinesTabEnabled() } returns flowOf(false)
            every { navigationPreferences.isTasksTabEnabled() } returns flowOf(true)
            every { navigationPreferences.getDefaultTab() } returns flowOf(DEFAULT_TAB_TASKS)
            every { retentionPreferences.isEnabled } returns MutableStateFlow(true)
            every { retentionPreferences.retentionDays } returns MutableStateFlow(90)

            val snapshot = dataSource.snapshot()

            assertThat(snapshot).isEqualTo(
                BackupSettings(
                    themeMode = ThemeMode.DARK,
                    useTempoColors = true,
                    routinesTabEnabled = false,
                    tasksTabEnabled = true,
                    defaultTab = BackupDefaultTab.TASKS,
                    autoRemoveCompletedTasks = true,
                    completedTaskRetentionDays = 90,
                ),
            )
        }

    @Test
    fun `snapshot maps a routines default tab`() =
        runTest {
            every { themePreferences.getThemeMode() } returns flowOf(ThemeMode.SYSTEM)
            every { themePreferences.getUseTempoColors() } returns flowOf(false)
            every { navigationPreferences.isRoutinesTabEnabled() } returns flowOf(true)
            every { navigationPreferences.isTasksTabEnabled() } returns flowOf(true)
            every { navigationPreferences.getDefaultTab() } returns flowOf(DEFAULT_TAB_ROUTINES)
            every { retentionPreferences.isEnabled } returns MutableStateFlow(false)
            every { retentionPreferences.retentionDays } returns MutableStateFlow(30)

            assertThat(dataSource.snapshot().defaultTab).isEqualTo(BackupDefaultTab.ROUTINES)
        }

    @Test
    fun `apply writes every preference back`() {
        dataSource.apply(
            BackupSettings(
                themeMode = ThemeMode.LIGHT,
                useTempoColors = true,
                routinesTabEnabled = true,
                tasksTabEnabled = true,
                defaultTab = BackupDefaultTab.TASKS,
                autoRemoveCompletedTasks = true,
                completedTaskRetentionDays = 90,
            ),
        )

        verify { themePreferences.setThemeMode(ThemeMode.LIGHT) }
        verify { themePreferences.setUseTempoColors(true) }
        verify { navigationPreferences.setRoutinesTabEnabled(true) }
        verify { navigationPreferences.setTasksTabEnabled(true) }
        verify { navigationPreferences.setDefaultTab(DEFAULT_TAB_TASKS) }
        verify { retentionPreferences.setEnabled(true) }
        verify { retentionPreferences.setRetentionDays(90) }
    }

    @Test
    fun `apply keeps at least one tab enabled`() {
        dataSource.apply(
            settings(routinesTabEnabled = false, tasksTabEnabled = false),
        )

        verify { navigationPreferences.setRoutinesTabEnabled(true) }
        verify { navigationPreferences.setDefaultTab(DEFAULT_TAB_ROUTINES) }
    }

    @Test
    fun `apply moves the default tab off a disabled tab`() {
        dataSource.apply(
            settings(
                routinesTabEnabled = false,
                tasksTabEnabled = true,
                defaultTab = BackupDefaultTab.ROUTINES,
            ),
        )

        verify { navigationPreferences.setDefaultTab(DEFAULT_TAB_TASKS) }
    }

    @Test
    fun `apply normalizes the retention days to a supported value`() {
        dataSource.apply(settings(completedTaskRetentionDays = 8))

        verify { retentionPreferences.setRetentionDays(7) }
    }

    private fun settings(
        routinesTabEnabled: Boolean = true,
        tasksTabEnabled: Boolean = true,
        defaultTab: BackupDefaultTab = BackupDefaultTab.ROUTINES,
        completedTaskRetentionDays: Int = 30,
    ) = BackupSettings(
        themeMode = ThemeMode.SYSTEM,
        useTempoColors = false,
        routinesTabEnabled = routinesTabEnabled,
        tasksTabEnabled = tasksTabEnabled,
        defaultTab = defaultTab,
        autoRemoveCompletedTasks = false,
        completedTaskRetentionDays = completedTaskRetentionDays,
    )
}
