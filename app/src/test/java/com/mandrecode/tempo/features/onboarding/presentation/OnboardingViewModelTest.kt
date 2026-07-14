package com.mandrecode.tempo.features.onboarding.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository.Companion.DEFAULT_TAB_ROUTINES
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository.Companion.DEFAULT_TAB_TASKS
import com.mandrecode.tempo.core.data.preferences.OnboardingPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import com.mandrecode.tempo.features.tasks.domain.usecase.ConfigureCompletedTaskRetentionUseCase
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
    private lateinit var completedTaskRetentionPreferences: CompletedTaskRetentionPreferences
    private lateinit var configureCompletedTaskRetention: ConfigureCompletedTaskRetentionUseCase
    private lateinit var useTempoColors: MutableStateFlow<Boolean>
    private lateinit var themeMode: MutableStateFlow<ThemeMode>
    private lateinit var routinesEnabled: MutableStateFlow<Boolean>
    private lateinit var tasksEnabled: MutableStateFlow<Boolean>
    private lateinit var defaultTab: MutableStateFlow<String>
    private lateinit var retentionEnabled: MutableStateFlow<Boolean>
    private lateinit var retentionDays: MutableStateFlow<Int>
    private lateinit var onboardingCompleted: MutableStateFlow<Boolean>

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        themePreferencesRepository = mockk(relaxed = true)
        navigationPreferencesRepository = mockk(relaxed = true)
        onboardingPreferencesRepository = mockk(relaxed = true)
        completedTaskRetentionPreferences = mockk(relaxed = true)
        configureCompletedTaskRetention = mockk(relaxed = true)
        useTempoColors = MutableStateFlow(false)
        themeMode = MutableStateFlow(ThemeMode.SYSTEM)
        routinesEnabled = MutableStateFlow(true)
        tasksEnabled = MutableStateFlow(true)
        defaultTab = MutableStateFlow(DEFAULT_TAB_ROUTINES)
        retentionEnabled = MutableStateFlow(false)
        retentionDays = MutableStateFlow(30)
        onboardingCompleted = MutableStateFlow(false)

        every { themePreferencesRepository.getUseTempoColors() } returns useTempoColors
        every { themePreferencesRepository.getThemeMode() } returns themeMode
        every { navigationPreferencesRepository.isRoutinesTabEnabled() } returns routinesEnabled
        every { navigationPreferencesRepository.isTasksTabEnabled() } returns tasksEnabled
        every { navigationPreferencesRepository.getDefaultTab() } returns defaultTab
        every { completedTaskRetentionPreferences.isEnabled } returns retentionEnabled
        every { completedTaskRetentionPreferences.retentionDays } returns retentionDays
        every { onboardingPreferencesRepository.isCompleted } returns onboardingCompleted
        every { onboardingPreferencesRepository.markStarted() } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun givenIncompleteOnboarding_whenCreated_thenTempoColorsAreSelectedByDefault() {
        createViewModel()

        verify(exactly = 1) { themePreferencesRepository.setUseTempoColors(true) }
    }

    @Test
    fun givenCompletedOnboarding_whenReplayed_thenCurrentColorsArePreserved() {
        onboardingCompleted.value = true

        createViewModel()

        verify(exactly = 0) { themePreferencesRepository.setUseTempoColors(any()) }
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
    fun givenRetentionChange_whenEventReceived_thenExistingConfigurationUseCaseIsCalled() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(OnboardingContract.UiEvent.CompletedTaskRetentionDaysChanged(45))

            verify { configureCompletedTaskRetention(false, 45) }
        }

    @Test
    fun givenTasksDefault_whenSkipped_thenCompletionAndTasksExitAreEmitted() =
        runTest {
            defaultTab.value = DEFAULT_TAB_TASKS
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onEvent(OnboardingContract.UiEvent.SkipClicked)

                assertThat(awaitItem())
                    .isEqualTo(OnboardingContract.UiEffect.Exit(OnboardingContract.DefaultTab.TASKS))
                cancelAndIgnoreRemainingEvents()
            }
            verify { onboardingPreferencesRepository.setCompleted() }
        }

    private fun createViewModel(): OnboardingViewModel =
        OnboardingViewModel(
            themePreferencesRepository = themePreferencesRepository,
            navigationPreferencesRepository = navigationPreferencesRepository,
            onboardingPreferencesRepository = onboardingPreferencesRepository,
            completedTaskRetentionPreferences = completedTaskRetentionPreferences,
            configureCompletedTaskRetention = configureCompletedTaskRetention,
        )
}
