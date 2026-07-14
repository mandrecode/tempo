package com.mandrecode.tempo.features.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository.Companion.DEFAULT_TAB_ROUTINES
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository.Companion.DEFAULT_TAB_TASKS
import com.mandrecode.tempo.core.data.preferences.OnboardingPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val themePreferencesRepository: ThemePreferencesRepository,
        private val navigationPreferencesRepository: NavigationPreferencesRepository,
        private val onboardingPreferencesRepository: OnboardingPreferencesRepository,
    ) : ViewModel() {
        private val mutableUiState = MutableStateFlow(OnboardingContract.UiState())
        val uiState: StateFlow<OnboardingContract.UiState> = mutableUiState.asStateFlow()

        private val effectChannel = Channel<OnboardingContract.UiEffect>(Channel.BUFFERED)
        val uiEffect = effectChannel.receiveAsFlow()

        init {
            if (!onboardingPreferencesRepository.isCompleted.value && onboardingPreferencesRepository.markStarted()) {
                themePreferencesRepository.setUseTempoColors(true)
            }
            observePreferences()
        }

        fun onEvent(event: OnboardingContract.UiEvent) {
            when (event) {
                OnboardingContract.UiEvent.NextClicked -> moveToNextPage()
                OnboardingContract.UiEvent.BackClicked -> moveToPreviousPage()
                OnboardingContract.UiEvent.SkipClicked,
                OnboardingContract.UiEvent.FinishClicked,
                -> completeOnboarding()

                is OnboardingContract.UiEvent.TempoColorsSelected ->
                    themePreferencesRepository.setUseTempoColors(event.enabled)

                is OnboardingContract.UiEvent.ThemeModeSelected ->
                    themePreferencesRepository.setThemeMode(event.mode)

                is OnboardingContract.UiEvent.RoutinesTabToggled -> setRoutinesTabEnabled(event.enabled)
                is OnboardingContract.UiEvent.TasksTabToggled -> setTasksTabEnabled(event.enabled)
                is OnboardingContract.UiEvent.DefaultTabSelected -> setDefaultTab(event.defaultTab)
            }
        }

        private fun observePreferences() {
            viewModelScope.launch {
                themePreferencesRepository.getThemeMode().collect { themeMode ->
                    mutableUiState.update { it.copy(selectedThemeMode = themeMode) }
                }
            }
            viewModelScope.launch {
                themePreferencesRepository.getUseTempoColors().collect { useTempoColors ->
                    mutableUiState.update { it.copy(useTempoColors = useTempoColors) }
                }
            }
            viewModelScope.launch {
                combine(
                    navigationPreferencesRepository.isRoutinesTabEnabled(),
                    navigationPreferencesRepository.isTasksTabEnabled(),
                    navigationPreferencesRepository.getDefaultTab(),
                ) { routinesEnabled, tasksEnabled, defaultTab ->
                    Triple(routinesEnabled, tasksEnabled, defaultTab)
                }.collect { (routinesEnabled, tasksEnabled, defaultTab) ->
                    mutableUiState.update {
                        it.copy(
                            isRoutinesTabEnabled = routinesEnabled,
                            isTasksTabEnabled = tasksEnabled,
                            defaultTab = defaultTab.toDefaultTab(),
                        )
                    }
                }
            }
        }

        private fun moveToNextPage() {
            mutableUiState.update { state ->
                state.copy(currentPage = (state.currentPage + 1).coerceAtMost(OnboardingContract.PAGE_COUNT - 1))
            }
        }

        private fun moveToPreviousPage() {
            mutableUiState.update { state ->
                state.copy(currentPage = (state.currentPage - 1).coerceAtLeast(0))
            }
        }

        private fun setRoutinesTabEnabled(enabled: Boolean) {
            val state = mutableUiState.value
            if (!enabled && !state.isTasksTabEnabled) return

            navigationPreferencesRepository.setRoutinesTabEnabled(enabled)
            if (!enabled && state.defaultTab == OnboardingContract.DefaultTab.ROUTINES) {
                navigationPreferencesRepository.setDefaultTab(DEFAULT_TAB_TASKS)
            }
        }

        private fun setTasksTabEnabled(enabled: Boolean) {
            val state = mutableUiState.value
            if (!enabled && !state.isRoutinesTabEnabled) return

            navigationPreferencesRepository.setTasksTabEnabled(enabled)
            if (!enabled && state.defaultTab == OnboardingContract.DefaultTab.TASKS) {
                navigationPreferencesRepository.setDefaultTab(DEFAULT_TAB_ROUTINES)
            }
        }

        private fun setDefaultTab(defaultTab: OnboardingContract.DefaultTab) {
            val state = mutableUiState.value
            val enabled =
                when (defaultTab) {
                    OnboardingContract.DefaultTab.ROUTINES -> state.isRoutinesTabEnabled
                    OnboardingContract.DefaultTab.TASKS -> state.isTasksTabEnabled
                }
            if (!enabled) return

            navigationPreferencesRepository.setDefaultTab(defaultTab.preferenceValue)
        }

        private fun completeOnboarding() {
            val destination = mutableUiState.value.resolvedDefaultTab()
            onboardingPreferencesRepository.setCompleted()
            effectChannel.trySend(OnboardingContract.UiEffect.Exit(destination))
        }

        private fun String.toDefaultTab(): OnboardingContract.DefaultTab =
            if (this == DEFAULT_TAB_TASKS) {
                OnboardingContract.DefaultTab.TASKS
            } else {
                OnboardingContract.DefaultTab.ROUTINES
            }

        private val OnboardingContract.DefaultTab.preferenceValue: String
            get() =
                when (this) {
                    OnboardingContract.DefaultTab.ROUTINES -> DEFAULT_TAB_ROUTINES
                    OnboardingContract.DefaultTab.TASKS -> DEFAULT_TAB_TASKS
                }

        private fun OnboardingContract.UiState.resolvedDefaultTab(): OnboardingContract.DefaultTab =
            when {
                defaultTab == OnboardingContract.DefaultTab.ROUTINES && isRoutinesTabEnabled -> defaultTab
                defaultTab == OnboardingContract.DefaultTab.TASKS && isTasksTabEnabled -> defaultTab
                isRoutinesTabEnabled -> OnboardingContract.DefaultTab.ROUTINES
                else -> OnboardingContract.DefaultTab.TASKS
            }
    }
