package com.mandrecode.tempo.features.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.OnboardingPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import com.mandrecode.tempo.core.domain.model.TempoTab
import com.mandrecode.tempo.core.domain.util.TabPreferencesPolicy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentSet
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
            observePreferences()
        }

        fun onEvent(event: OnboardingContract.UiEvent) {
            when (event) {
                OnboardingContract.UiEvent.NextClicked -> moveToNextPage()
                OnboardingContract.UiEvent.BackClicked -> moveToPreviousPage()
                OnboardingContract.UiEvent.SkipClicked,
                OnboardingContract.UiEvent.FinishClicked,
                -> completeOnboarding()

                is OnboardingContract.UiEvent.UseTempoColorsToggled ->
                    themePreferencesRepository.setUseTempoColors(event.useTempoColors)

                is OnboardingContract.UiEvent.ThemeModeSelected ->
                    themePreferencesRepository.setThemeMode(event.mode)

                is OnboardingContract.UiEvent.TabToggled -> setTabEnabled(event.tab, event.enabled)
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
                    navigationPreferencesRepository.enabledTabs(),
                    navigationPreferencesRepository.getDefaultTab(),
                ) { enabledTabs, defaultTab ->
                    enabledTabs to defaultTab
                }.collect { (enabledTabs, defaultTab) ->
                    mutableUiState.update {
                        it.copy(
                            enabledTabs = enabledTabs.toPersistentSet(),
                            defaultTab = TabPreferencesPolicy.resolveDefaultTab(defaultTab, enabledTabs),
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

        private fun setTabEnabled(
            tab: TempoTab,
            enabled: Boolean,
        ) {
            val state = mutableUiState.value
            if (!enabled && !TabPreferencesPolicy.canDisable(state.enabledTabs, tab)) return

            val resolvedTabs = TabPreferencesPolicy.withTabEnabled(state.enabledTabs, tab, enabled)
            val resolvedDefault = TabPreferencesPolicy.resolveDefaultTab(state.defaultTab, resolvedTabs)
            mutableUiState.update {
                it.copy(
                    enabledTabs = resolvedTabs.toPersistentSet(),
                    defaultTab = resolvedDefault,
                )
            }
            navigationPreferencesRepository.setTabEnabled(tab, enabled)
            if (resolvedDefault != state.defaultTab) {
                navigationPreferencesRepository.setDefaultTab(resolvedDefault)
            }
        }

        private fun setDefaultTab(defaultTab: TempoTab) {
            if (defaultTab !in mutableUiState.value.enabledTabs) return

            mutableUiState.update { it.copy(defaultTab = defaultTab) }
            navigationPreferencesRepository.setDefaultTab(defaultTab)
        }

        private fun completeOnboarding() {
            val state = mutableUiState.value
            val destination = TabPreferencesPolicy.resolveDefaultTab(state.defaultTab, state.enabledTabs)
            onboardingPreferencesRepository.setCompleted()
            viewModelScope.launch {
                effectChannel.send(OnboardingContract.UiEffect.Exit(destination))
            }
        }
    }
