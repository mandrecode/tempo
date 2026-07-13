package com.mandrecode.tempo.features.settings.presentation

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository.Companion.DEFAULT_TAB_ROUTINES
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository.Companion.DEFAULT_TAB_TASKS
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import com.mandrecode.tempo.features.tasks.domain.usecase.ConfigureCompletedTaskRetentionUseCase
import com.mandrecode.tempo.util.AppVersionInfo
import com.mandrecode.tempo.util.AppVersionProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private lateinit var viewModel: SettingsViewModel
    private lateinit var themePreferencesRepository: ThemePreferencesRepository
    private lateinit var navigationPreferencesRepository: NavigationPreferencesRepository
    private lateinit var appVersionProvider: AppVersionProvider
    private lateinit var completedTaskRetentionPreferences: CompletedTaskRetentionPreferences
    private lateinit var configureCompletedTaskRetention: ConfigureCompletedTaskRetentionUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        themePreferencesRepository = mockk(relaxed = true)
        navigationPreferencesRepository = mockk(relaxed = true)
        appVersionProvider =
            mockk {
                every { getVersionInfo() } returns AppVersionInfo("1.0", 1)
            }
        completedTaskRetentionPreferences = mockk(relaxed = true)
        configureCompletedTaskRetention = mockk(relaxed = true)

        coEvery { themePreferencesRepository.getThemeMode() } returns flowOf(ThemeMode.SYSTEM)
        coEvery { themePreferencesRepository.getUseTempoColors() } returns flowOf(false)
        coEvery { navigationPreferencesRepository.isRoutinesTabEnabled() } returns flowOf(true)
        coEvery { navigationPreferencesRepository.isTasksTabEnabled() } returns flowOf(true)
        coEvery { navigationPreferencesRepository.getDefaultTab() } returns flowOf(DEFAULT_TAB_ROUTINES)
        every { completedTaskRetentionPreferences.isEnabled } returns MutableStateFlow(false)
        every { completedTaskRetentionPreferences.retentionDays } returns MutableStateFlow(30)

        viewModel = createViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialState has correct default values`() =
        runTest {
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.selectedThemeMode).isEqualTo(ThemeMode.SYSTEM)
            assertThat(viewModel.uiState.value.isRoutinesTabEnabled).isTrue()
            assertThat(viewModel.uiState.value.isTasksTabEnabled).isTrue()
            assertThat(viewModel.uiState.value.defaultTab).isEqualTo(SettingsContract.DefaultTab.ROUTINES)
        }

    @Test
    fun `themeModeSelected calls repository`() =
        runTest {
            advanceUntilIdle()
            viewModel.onEvent(SettingsContract.UiEvent.ThemeModeSelected(ThemeMode.DARK))
            coVerify { themePreferencesRepository.setThemeMode(ThemeMode.DARK) }
        }

    @Test
    fun `tempoColorsToggled calls repository`() =
        runTest {
            advanceUntilIdle()
            viewModel.onEvent(SettingsContract.UiEvent.TempoColorsToggled(true))
            coVerify { themePreferencesRepository.setUseTempoColors(true) }
        }

    @Test
    fun `tempoColorsObserver updates state when repository changes`() =
        runTest {
            coEvery { themePreferencesRepository.getUseTempoColors() } returns flowOf(true)

            // Re-init VM
            viewModel = createViewModel()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.useTempoColors).isTrue()
        }

    @Test
    fun `routinesTabToggled when tasks enabled calls repository`() =
        runTest {
            advanceUntilIdle()
            viewModel.onEvent(SettingsContract.UiEvent.RoutinesTabToggled(false))
            coVerify { navigationPreferencesRepository.setRoutinesTabEnabled(false) }
        }

    @Test
    fun `routinesTabToggled when tasks disabled does not call repository`() =
        runTest {
            coEvery { navigationPreferencesRepository.isTasksTabEnabled() } returns flowOf(false)

            // Re-init VM
            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(SettingsContract.UiEvent.RoutinesTabToggled(false))
            coVerify(exactly = 0) { navigationPreferencesRepository.setRoutinesTabEnabled(false) }
        }

    @Test
    fun `tasksTabToggled when routines enabled calls repository`() =
        runTest {
            advanceUntilIdle()
            viewModel.onEvent(SettingsContract.UiEvent.TasksTabToggled(false))
            coVerify { navigationPreferencesRepository.setTasksTabEnabled(false) }
        }

    @Test
    fun `tasksTabToggled when routines disabled does not call repository`() =
        runTest {
            coEvery { navigationPreferencesRepository.isRoutinesTabEnabled() } returns flowOf(false)

            // Re-init VM
            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(SettingsContract.UiEvent.TasksTabToggled(false))
            coVerify(exactly = 0) { navigationPreferencesRepository.setTasksTabEnabled(false) }
        }

    @Test
    fun `routinesTabDisabled when default tab switches to tasks`() =
        runTest {
            advanceUntilIdle()
            viewModel.onEvent(SettingsContract.UiEvent.RoutinesTabToggled(false))
            coVerify { navigationPreferencesRepository.setDefaultTab(DEFAULT_TAB_TASKS) }
        }

    @Test
    fun `tasksTabDisabled when default tab switches to routines`() =
        runTest {
            coEvery { navigationPreferencesRepository.getDefaultTab() } returns flowOf(DEFAULT_TAB_TASKS)

            // Re-init VM
            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(SettingsContract.UiEvent.TasksTabToggled(false))
            coVerify { navigationPreferencesRepository.setDefaultTab(DEFAULT_TAB_ROUTINES) }
        }

    @Test
    fun `defaultTabSelected routines when enabled calls repository`() =
        runTest {
            advanceUntilIdle()
            viewModel.onEvent(SettingsContract.UiEvent.DefaultTabSelected(SettingsContract.DefaultTab.ROUTINES))
            coVerify { navigationPreferencesRepository.setDefaultTab(DEFAULT_TAB_ROUTINES) }
        }

    @Test
    fun `defaultTabSelected tasks when enabled calls repository`() =
        runTest {
            advanceUntilIdle()
            viewModel.onEvent(SettingsContract.UiEvent.DefaultTabSelected(SettingsContract.DefaultTab.TASKS))
            coVerify { navigationPreferencesRepository.setDefaultTab(DEFAULT_TAB_TASKS) }
        }

    @Test
    fun `themeModeObserver updates state when repository changes`() =
        runTest {
            coEvery { themePreferencesRepository.getThemeMode() } returns flowOf(ThemeMode.LIGHT)

            // Re-init VM
            viewModel = createViewModel()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.selectedThemeMode).isEqualTo(ThemeMode.LIGHT)
        }

    @Test
    fun `availableThemeModes contains all three options`() =
        runTest {
            advanceUntilIdle()
            val availableModes = viewModel.uiState.value.availableThemeModes

            assertThat(availableModes).hasSize(3)
            assertThat(availableModes).contains(ThemeMode.LIGHT)
            assertThat(availableModes).contains(ThemeMode.DARK)
            assertThat(availableModes).contains(ThemeMode.SYSTEM)
        }

    @Test
    fun `initialState loads version info`() =
        runTest {
            advanceUntilIdle()
            val state = viewModel.uiState.value

            assertThat(state.appVersion).isNotEmpty()
        }

    @Test
    fun `retention preferences update settings state`() =
        runTest {
            every { completedTaskRetentionPreferences.isEnabled } returns MutableStateFlow(true)
            every { completedTaskRetentionPreferences.retentionDays } returns MutableStateFlow(45)
            viewModel = createViewModel()

            advanceUntilIdle()

            assertThat(viewModel.uiState.value.autoRemoveCompletedTasksEnabled).isTrue()
            assertThat(viewModel.uiState.value.completedTaskRetentionDays).isEqualTo(45)
        }

    @Test
    fun `auto removal toggle configures current retention`() =
        runTest {
            advanceUntilIdle()

            viewModel.onEvent(SettingsContract.UiEvent.AutoRemoveCompletedTasksToggled(true))

            verify { configureCompletedTaskRetention(true, 30) }
            assertThat(viewModel.uiState.value.autoRemoveCompletedTasksEnabled).isTrue()
        }

    @Test
    fun `retention day change is clamped and configured`() =
        runTest {
            advanceUntilIdle()

            viewModel.onEvent(SettingsContract.UiEvent.CompletedTaskRetentionDaysChanged(500))

            verify { configureCompletedTaskRetention(false, 500) }
            assertThat(viewModel.uiState.value.completedTaskRetentionDays).isEqualTo(365)
        }

    private fun createViewModel(): SettingsViewModel =
        SettingsViewModel(
            themePreferencesRepository,
            navigationPreferencesRepository,
            appVersionProvider,
            completedTaskRetentionPreferences,
            configureCompletedTaskRetention,
        )
}
