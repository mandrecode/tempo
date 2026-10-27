package com.mandrecode.tempo.features.onboarding.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.OnboardingPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import com.mandrecode.tempo.core.domain.model.TempoTab
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
    private lateinit var enabledTabs: MutableStateFlow<Set<TempoTab>>
    private lateinit var defaultTab: MutableStateFlow<TempoTab>

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        themePreferencesRepository = mockk(relaxed = true)
        navigationPreferencesRepository = mockk(relaxed = true)
        onboardingPreferencesRepository = mockk(relaxed = true)
        useTempoColors = MutableStateFlow(false)
        themeMode = MutableStateFlow(ThemeMode.SYSTEM)
        enabledTabs = MutableStateFlow(TempoTab.entries.toSet())
        defaultTab = MutableStateFlow(TempoTab.ROUTINES)

        every { themePreferencesRepository.getUseTempoColors() } returns useTempoColors
        every { themePreferencesRepository.getThemeMode() } returns themeMode
        every { navigationPreferencesRepository.enabledTabs() } returns enabledTabs
        every { navigationPreferencesRepository.getDefaultTab() } returns defaultTab
        every { onboardingPreferencesRepository.isCompleted } returns MutableStateFlow(false)
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
            enabledTabs.value = setOf(TempoTab.ROUTINES)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(OnboardingContract.UiEvent.TabToggled(TempoTab.ROUTINES, false))

            verify(exactly = 0) { navigationPreferencesRepository.setTabEnabled(TempoTab.ROUTINES, false) }
        }

    @Test
    fun givenRoutinesAreDefault_whenRoutinesDisabled_thenDefaultMovesToFirstEnabledTab() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(OnboardingContract.UiEvent.TabToggled(TempoTab.ROUTINES, false))

            verify { navigationPreferencesRepository.setTabEnabled(TempoTab.ROUTINES, false) }
            verify { navigationPreferencesRepository.setDefaultTab(TempoTab.FOCUS) }
        }

    @Test
    fun givenRoutinesDefault_whenDisabledPreferenceEmitsBeforeDefaultUpdate_thenUiUsesFirstEnabledTab() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            enabledTabs.value = enabledTabs.value - TempoTab.ROUTINES
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.enabledTabs).doesNotContain(TempoTab.ROUTINES)
            assertThat(viewModel.uiState.value.defaultTab).isEqualTo(TempoTab.FOCUS)
        }

    @Test
    fun givenOnlyTasksEnabled_whenTasksDisabled_thenInvariantPreventsWrite() =
        runTest {
            enabledTabs.value = setOf(TempoTab.TASKS)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(OnboardingContract.UiEvent.TabToggled(TempoTab.TASKS, false))

            verify(exactly = 0) { navigationPreferencesRepository.setTabEnabled(TempoTab.TASKS, false) }
        }

    @Test
    fun givenTasksAreDefault_whenTasksDisabled_thenDefaultMovesToFirstEnabledTab() =
        runTest {
            defaultTab.value = TempoTab.TASKS
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(OnboardingContract.UiEvent.TabToggled(TempoTab.TASKS, false))

            verify { navigationPreferencesRepository.setTabEnabled(TempoTab.TASKS, false) }
            verify { navigationPreferencesRepository.setDefaultTab(TempoTab.FOCUS) }
        }

    @Test
    fun givenAllTabsEnabled_whenDisablingEveryTabInTurn_thenTheLastOneRemainsEnabled() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(OnboardingContract.UiEvent.TabToggled(TempoTab.FOCUS, false))
            viewModel.onEvent(OnboardingContract.UiEvent.TabToggled(TempoTab.ROUTINES, false))
            viewModel.onEvent(OnboardingContract.UiEvent.TabToggled(TempoTab.TASKS, false))

            assertThat(viewModel.uiState.value.enabledTabs).containsExactly(TempoTab.TASKS)
            verify { navigationPreferencesRepository.setTabEnabled(TempoTab.FOCUS, false) }
            verify { navigationPreferencesRepository.setTabEnabled(TempoTab.ROUTINES, false) }
            verify(exactly = 0) { navigationPreferencesRepository.setTabEnabled(TempoTab.TASKS, false) }
        }

    @Test
    fun givenAllTabsEnabled_whenDisablingInReverseOrder_thenTheLastOneRemainsEnabled() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(OnboardingContract.UiEvent.TabToggled(TempoTab.TASKS, false))
            viewModel.onEvent(OnboardingContract.UiEvent.TabToggled(TempoTab.ROUTINES, false))
            viewModel.onEvent(OnboardingContract.UiEvent.TabToggled(TempoTab.FOCUS, false))

            assertThat(viewModel.uiState.value.enabledTabs).containsExactly(TempoTab.FOCUS)
            verify { navigationPreferencesRepository.setTabEnabled(TempoTab.TASKS, false) }
            verify { navigationPreferencesRepository.setTabEnabled(TempoTab.ROUTINES, false) }
            verify(exactly = 0) { navigationPreferencesRepository.setTabEnabled(TempoTab.FOCUS, false) }
        }

    @Test
    fun givenEnabledTabs_whenSelectedAsDefault_thenBothSelectionsArePersisted() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(
                OnboardingContract.UiEvent.DefaultTabSelected(TempoTab.ROUTINES),
            )
            viewModel.onEvent(
                OnboardingContract.UiEvent.DefaultTabSelected(TempoTab.TASKS),
            )

            verify { navigationPreferencesRepository.setDefaultTab(TempoTab.ROUTINES) }
            verify { navigationPreferencesRepository.setDefaultTab(TempoTab.TASKS) }
        }

    @Test
    fun givenDisabledTasksTab_whenSelectedAsDefault_thenSelectionIsIgnored() =
        runTest {
            enabledTabs.value = enabledTabs.value - TempoTab.TASKS
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(
                OnboardingContract.UiEvent.DefaultTabSelected(TempoTab.TASKS),
            )

            verify(exactly = 0) { navigationPreferencesRepository.setDefaultTab(TempoTab.TASKS) }
        }

    @Test
    fun givenTasksDefault_whenSkipped_thenCompletionAndTasksExitAreEmitted() =
        runTest {
            defaultTab.value = TempoTab.TASKS
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onEvent(OnboardingContract.UiEvent.SkipClicked)
                advanceUntilIdle()

                assertThat(awaitItem())
                    .isEqualTo(OnboardingContract.UiEffect.Exit(TempoTab.TASKS))
                cancelAndIgnoreRemainingEvents()
            }
            verify { onboardingPreferencesRepository.setCompleted() }
        }

    @Test
    fun givenCorruptedStateWithNoEnabledTabs_whenFinished_thenDefaultTabFallbackIsEmitted() =
        runTest {
            enabledTabs.value = emptySet()
            defaultTab.value = TempoTab.TASKS
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onEvent(OnboardingContract.UiEvent.FinishClicked)
                advanceUntilIdle()

                assertThat(awaitItem())
                    .isEqualTo(OnboardingContract.UiEffect.Exit(TempoTab.DEFAULT))
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun givenDisabledStoredDefault_whenFinished_thenEnabledTabFallbackIsEmitted() =
        runTest {
            enabledTabs.value = setOf(TempoTab.FOCUS, TempoTab.TASKS)
            defaultTab.value = TempoTab.ROUTINES
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onEvent(OnboardingContract.UiEvent.FinishClicked)
                advanceUntilIdle()

                assertThat(awaitItem())
                    .isEqualTo(OnboardingContract.UiEffect.Exit(TempoTab.FOCUS))
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
