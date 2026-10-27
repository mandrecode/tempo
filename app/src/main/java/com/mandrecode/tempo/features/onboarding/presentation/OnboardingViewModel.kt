package com.mandrecode.tempo.features.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mandrecode.tempo.core.data.preferences.OnboardingPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val themePreferencesRepository: ThemePreferencesRepository,
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

        private fun completeOnboarding() {
            onboardingPreferencesRepository.setCompleted()
            viewModelScope.launch {
                effectChannel.send(OnboardingContract.UiEffect.Exit)
            }
        }
    }
