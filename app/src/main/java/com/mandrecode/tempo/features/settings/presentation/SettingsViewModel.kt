package com.mandrecode.tempo.features.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository.Companion.DEFAULT_TAB_ROUTINES
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository.Companion.DEFAULT_TAB_TASKS
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import com.mandrecode.tempo.features.tasks.domain.usecase.ConfigureCompletedTaskRetentionUseCase
import com.mandrecode.tempo.util.AppVersionProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val themePreferencesRepository: ThemePreferencesRepository,
        private val navigationPreferencesRepository: NavigationPreferencesRepository,
        private val appVersionProvider: AppVersionProvider,
        private val completedTaskRetentionPreferences: CompletedTaskRetentionPreferences,
        private val configureCompletedTaskRetention: ConfigureCompletedTaskRetentionUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SettingsContract.UiState())
        val uiState: StateFlow<SettingsContract.UiState> = _uiState.asStateFlow()

        init {
            observeThemeMode()
            observeTempoColors()
            loadVersionInfo()
            observeTabPreferences()
            observeCompletedTaskRetention()
        }

        private fun observeThemeMode() {
            viewModelScope.launch {
                themePreferencesRepository.getThemeMode().collect { mode ->
                    _uiState.update { it.copy(selectedThemeMode = mode) }
                }
            }
        }

        private fun observeTempoColors() {
            viewModelScope.launch {
                themePreferencesRepository.getUseTempoColors().collect { enabled ->
                    _uiState.update { it.copy(useTempoColors = enabled) }
                }
            }
        }

        private fun loadVersionInfo() {
            val versionInfo = appVersionProvider.getVersionInfo()
            _uiState.update {
                it.copy(appVersion = versionInfo.versionName)
            }
        }

        private fun observeTabPreferences() {
            viewModelScope.launch {
                combine(
                    navigationPreferencesRepository.isRoutinesTabEnabled(),
                    navigationPreferencesRepository.isTasksTabEnabled(),
                    navigationPreferencesRepository.getDefaultTab(),
                ) { routinesEnabled, tasksEnabled, defaultTab ->
                    Triple(routinesEnabled, tasksEnabled, defaultTab)
                }.collect { (routinesEnabled, tasksEnabled, defaultTab) ->
                    _uiState.update {
                        it.copy(
                            isRoutinesTabEnabled = routinesEnabled,
                            isTasksTabEnabled = tasksEnabled,
                            defaultTab =
                                when (defaultTab) {
                                    DEFAULT_TAB_TASKS -> SettingsContract.DefaultTab.TASKS
                                    else -> SettingsContract.DefaultTab.ROUTINES
                                },
                        )
                    }
                }
            }
        }

        fun onEvent(event: SettingsContract.UiEvent) {
            when (event) {
                is SettingsContract.UiEvent.ThemeModeSelected -> {
                    themePreferencesRepository.setThemeMode(event.mode)
                }

                is SettingsContract.UiEvent.TempoColorsToggled -> {
                    themePreferencesRepository.setUseTempoColors(event.enabled)
                }

                is SettingsContract.UiEvent.RoutinesTabToggled -> {
                    handleRoutinesTabToggle(event.enabled)
                }

                is SettingsContract.UiEvent.TasksTabToggled -> {
                    handleTasksTabToggle(event.enabled)
                }

                is SettingsContract.UiEvent.DefaultTabSelected -> {
                    handleDefaultTabSelection(event.defaultTab)
                }

                is SettingsContract.UiEvent.AutoRemoveCompletedTasksToggled -> {
                    updateCompletedTaskRetention(
                        enabled = event.enabled,
                        days = _uiState.value.completedTaskRetentionDays,
                    )
                }

                is SettingsContract.UiEvent.CompletedTaskRetentionDaysChanged -> {
                    updateCompletedTaskRetention(
                        enabled = _uiState.value.autoRemoveCompletedTasksEnabled,
                        days = event.days,
                    )
                }
            }
        }

        private fun observeCompletedTaskRetention() {
            viewModelScope.launch {
                combine(
                    completedTaskRetentionPreferences.isEnabled,
                    completedTaskRetentionPreferences.retentionDays,
                ) { enabled, days -> enabled to days }
                    .collect { (enabled, days) ->
                        _uiState.update {
                            it.copy(
                                autoRemoveCompletedTasksEnabled = enabled,
                                completedTaskRetentionDays = days,
                            )
                        }
                    }
            }
        }

        private fun updateCompletedTaskRetention(
            enabled: Boolean,
            days: Int,
        ) {
            configureCompletedTaskRetention(enabled, days)
            _uiState.update {
                it.copy(
                    autoRemoveCompletedTasksEnabled = enabled,
                    completedTaskRetentionDays =
                        days.coerceIn(
                            CompletedTaskRetentionPreferences.MIN_RETENTION_DAYS,
                            CompletedTaskRetentionPreferences.MAX_RETENTION_DAYS,
                        ),
                )
            }
        }

        private fun handleRoutinesTabToggle(enabled: Boolean) {
            val currentState = _uiState.value

            // Prevent disabling if it's the only enabled tab
            if (!enabled && !currentState.isTasksTabEnabled) {
                return // Cannot disable both tabs
            }

            navigationPreferencesRepository.setRoutinesTabEnabled(enabled)

            // If disabling the default tab, switch to the other tab
            if (!enabled && currentState.defaultTab == SettingsContract.DefaultTab.ROUTINES) {
                navigationPreferencesRepository.setDefaultTab(DEFAULT_TAB_TASKS)
            }
        }

        private fun handleTasksTabToggle(enabled: Boolean) {
            val currentState = _uiState.value

            // Prevent disabling if it's the only enabled tab
            if (!enabled && !currentState.isRoutinesTabEnabled) {
                return // Cannot disable both tabs
            }

            navigationPreferencesRepository.setTasksTabEnabled(enabled)

            // If disabling the default tab, switch to the other tab
            if (!enabled && currentState.defaultTab == SettingsContract.DefaultTab.TASKS) {
                navigationPreferencesRepository.setDefaultTab(DEFAULT_TAB_ROUTINES)
            }
        }

        private fun handleDefaultTabSelection(defaultTab: SettingsContract.DefaultTab) {
            val currentState = _uiState.value

            // Only allow selecting a tab that is enabled
            when (defaultTab) {
                SettingsContract.DefaultTab.ROUTINES -> {
                    if (currentState.isRoutinesTabEnabled) {
                        navigationPreferencesRepository.setDefaultTab(DEFAULT_TAB_ROUTINES)
                    }
                }

                SettingsContract.DefaultTab.TASKS -> {
                    if (currentState.isTasksTabEnabled) {
                        navigationPreferencesRepository.setDefaultTab(DEFAULT_TAB_TASKS)
                    }
                }
            }
        }
    }
