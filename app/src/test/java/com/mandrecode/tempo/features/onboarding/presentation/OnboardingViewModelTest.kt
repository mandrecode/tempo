package com.mandrecode.tempo.features.onboarding.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository.Companion.DEFAULT_TAB_ROUTINES
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository.Companion.DEFAULT_TAB_TASKS
import com.mandrecode.tempo.core.data.preferences.OnboardingPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import com.mandrecode.tempo.core.domain.model.ThemeMode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var themePreferencesRepository: ThemePreferencesRepository
    private lateinit var navigationPreferencesRepository: NavigationPreferencesRepository
    private lateinit var onboardingPreferencesRepository: OnboardingPreferencesRepository
    private lateinit var useTempoColors: MutableStateFlow<Boolean>
    private lateinit var themeMode: MutableStateFlow<ThemeMode>
    private lateinit var routinesEnabled: MutableStateFlow<Boolean>
    private lateinit var tasksEnabled: MutableStateFlow<Boolean>
    private lateinit var defaultTab: MutableStateFlow<String>

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        themePreferencesRepository = mockk(relaxed = true)
        navigationPreferencesRepository = mockk(relaxed = true)
        onboardingPreferencesRepository = mockk(relaxed = true)
        useTempoColors = MutableStateFlow(false)
        themeMode = MutableStateFlow(ThemeMode.SYSTEM)
        routinesEnabled = MutableStateFlow(true)
        tasksEnabled = MutableStateFlow(true)
        defaultTab = MutableStateFlow(DEFAULT_TAB_ROUTINES)

        every { themePreferencesRepository.getUseTempoColors() } returns useTempoColors
        every { themePreferencesRepository.getThemeMode() } returns themeMode
        every { navigationPreferencesRepository.isRoutinesTabEnabled() } returns routinesEnabled
        every { navigationPreferencesRepository.isTasksTabEnabled() } returns tasksEnabled
        every { navigationPreferencesRepository.getDefaultTab() } returns defaultTab
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun givenAppearancePage_whenUseTempoColorsToggled_thenSettingsRepositoryIsUpdated() {
        val viewModel = createViewModel()

        viewModel.onEvent(OnboardingContract.UiEvent.UseTempoColorsToggled(false))

        verify { themePreferencesRepository.setUseTempoColors(false) }
    }

    @Test
    fun givenAppearancePage_whenThemeModeSelected_thenSettingsRepositoryIsUpdated() {
        val viewModel = createViewModel()

        viewModel.onEvent(OnboardingContract.UiEvent.ThemeModeSelected(ThemeMode.DARK))

        verify { themePreferencesRepository.setThemeMode(ThemeMode.DARK) }
    }

    @Test
    fun givenFirstPage_whenNextAndBackClicked_thenPageChangesInOrder() =
        runTest {
            val viewModel = createViewModel()

            viewModel.onEvent(OnboardingContract.UiEvent.NextClicked)
            assertThat(viewModel.uiState.value.currentPage).isEqualTo(1)

            viewModel.onEvent(OnboardingContract.UiEvent.BackClicked)
            assertThat(viewModel.uiState.value.currentPage).isEqualTo(0)
        }

    @Test
    fun givenPageBoundary_whenNavigatingPastIt_thenPageRemainsInBounds() =
        runTest {
            val viewModel = createViewModel()

            viewModel.onEvent(OnboardingContract.UiEvent.BackClicked)
            assertThat(viewModel.uiState.value.currentPage).isEqualTo(0)

            repeat(OnboardingContract.PAGE_COUNT + 1) {
                viewModel.onEvent(OnboardingContract.UiEvent.NextClicked)
            }
            assertThat(viewModel.uiState.value.currentPage).isEqualTo(OnboardingContract.PAGE_COUNT - 1)
        }

    @Test
    fun givenOnlyRoutinesEnabled_whenRoutinesDisabled_thenInvariantPreventsWrite() =
        runTest {
            tasksEnabled.value = false
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(OnboardingContract.UiEvent.RoutinesTabToggled(false))

            verify(exactly = 0) { navigationPreferencesRepository.setRoutinesTabEnabled(false) }
        }

    @Test
    fun givenRoutinesAreDefault_whenRoutinesDisabled_thenDefaultMovesToTasks() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(OnboardingContract.UiEvent.RoutinesTabToggled(false))

            verify { navigationPreferencesRepository.setRoutinesTabEnabled(false) }
            verify { navigationPreferencesRepository.setDefaultTab(DEFAULT_TAB_TASKS) }
        }

    @Test
    fun givenOnlyTasksEnabled_whenTasksDisabled_thenInvariantPreventsWrite() =
        runTest {
            routinesEnabled.value = false
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(OnboardingContract.UiEvent.TasksTabToggled(false))

            verify(exactly = 0) { navigationPreferencesRepository.setTasksTabEnabled(false) }
        }

    @Test
    fun givenTasksAreDefault_whenTasksDisabled_thenDefaultMovesToRoutines() =
        runTest {
            defaultTab.value = DEFAULT_TAB_TASKS
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(OnboardingContract.UiEvent.TasksTabToggled(false))

            verify { navigationPreferencesRepository.setTasksTabEnabled(false) }
            verify { navigationPreferencesRepository.setDefaultTab(DEFAULT_TAB_ROUTINES) }
        }

    @Test
    fun givenEnabledTabs_whenSelectedAsDefault_thenBothSelectionsArePersisted() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(
                OnboardingContract.UiEvent.DefaultTabSelected(OnboardingContract.DefaultTab.ROUTINES),
            )
            viewModel.onEvent(
                OnboardingContract.UiEvent.DefaultTabSelected(OnboardingContract.DefaultTab.TASKS),
            )

            verify { navigationPreferencesRepository.setDefaultTab(DEFAULT_TAB_ROUTINES) }
            verify { navigationPreferencesRepository.setDefaultTab(DEFAULT_TAB_TASKS) }
        }

    @Test
    fun givenDisabledTasksTab_whenSelectedAsDefault_thenSelectionIsIgnored() =
        runTest {
            tasksEnabled.value = false
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(
                OnboardingContract.UiEvent.DefaultTabSelected(OnboardingContract.DefaultTab.TASKS),
            )

            verify(exactly = 0) { navigationPreferencesRepository.setDefaultTab(DEFAULT_TAB_TASKS) }
        }

    @Test
    fun givenTasksDefault_whenSkipped_thenCompletionAndTasksExitAreEmitted() =
        runTest {
            defaultTab.value = DEFAULT_TAB_TASKS
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onEvent(OnboardingContract.UiEvent.SkipClicked)
                advanceUntilIdle()

                assertThat(awaitItem())
                    .isEqualTo(OnboardingContract.UiEffect.Exit(OnboardingContract.DefaultTab.TASKS))
                cancelAndIgnoreRemainingEvents()
            }
            verify { onboardingPreferencesRepository.setCompleted() }
        }

    @Test
    fun givenCorruptedStateWithNoEnabledTabs_whenFinished_thenRoutinesFallbackIsEmitted() =
        runTest {
            routinesEnabled.value = false
            tasksEnabled.value = false
            defaultTab.value = DEFAULT_TAB_TASKS
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onEvent(OnboardingContract.UiEvent.FinishClicked)
                advanceUntilIdle()

                assertThat(awaitItem())
                    .isEqualTo(OnboardingContract.UiEffect.Exit(OnboardingContract.DefaultTab.ROUTINES))
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenDisabledStoredDefault_whenFinished_thenEnabledTabFallbackIsEmitted() =
        runTest {
            routinesEnabled.value = false
            defaultTab.value = DEFAULT_TAB_ROUTINES
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onEvent(OnboardingContract.UiEvent.FinishClicked)
                advanceUntilIdle()

                assertThat(awaitItem())
                    .isEqualTo(OnboardingContract.UiEffect.Exit(OnboardingContract.DefaultTab.TASKS))
                cancelAndIgnoreRemainingEvents()
            }
        }

    private fun createViewModel(): OnboardingViewModel =
        OnboardingViewModel(
            themePreferencesRepository = themePreferencesRepository,
            navigationPreferencesRepository = navigationPreferencesRepository,
            onboardingPreferencesRepository = onboardingPreferencesRepository,
        )
}
