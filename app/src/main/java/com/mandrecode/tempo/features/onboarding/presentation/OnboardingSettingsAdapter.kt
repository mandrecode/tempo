package com.mandrecode.tempo.features.onboarding.presentation

import com.mandrecode.tempo.features.settings.presentation.SettingsContract

internal fun OnboardingContract.UiState.toSettingsUiState(): SettingsContract.UiState =
    SettingsContract.UiState(
        selectedThemeMode = selectedThemeMode,
        availableThemeModes = availableThemeModes,
        useTempoColors = useTempoColors,
        isRoutinesTabEnabled = isRoutinesTabEnabled,
        isTasksTabEnabled = isTasksTabEnabled,
        defaultTab = defaultTab.toSettingsDefaultTab(),
    )

internal fun SettingsContract.UiEvent.toOnboardingEvent(): OnboardingContract.UiEvent? =
    when (this) {
        is SettingsContract.UiEvent.ThemeModeSelected -> OnboardingContract.UiEvent.ThemeModeSelected(mode)
        is SettingsContract.UiEvent.TempoColorsToggled -> OnboardingContract.UiEvent.UseTempoColorsToggled(enabled)
        is SettingsContract.UiEvent.RoutinesTabToggled -> OnboardingContract.UiEvent.RoutinesTabToggled(enabled)
        is SettingsContract.UiEvent.TasksTabToggled -> OnboardingContract.UiEvent.TasksTabToggled(enabled)
        is SettingsContract.UiEvent.DefaultTabSelected ->
            OnboardingContract.UiEvent.DefaultTabSelected(defaultTab.toOnboardingDefaultTab())

        is SettingsContract.UiEvent.AutoRemoveCompletedTasksToggled,
        is SettingsContract.UiEvent.CompletedTaskRetentionDaysChanged,
        is SettingsContract.UiEvent.ExportClicked,
        is SettingsContract.UiEvent.ExportDestinationPicked,
        is SettingsContract.UiEvent.ExportCancelled,
        is SettingsContract.UiEvent.ImportClicked,
        is SettingsContract.UiEvent.ImportFilePicked,
        is SettingsContract.UiEvent.ImportModeChosen,
        is SettingsContract.UiEvent.BackupDialogDismissed,
        -> null
    }

private fun OnboardingContract.DefaultTab.toSettingsDefaultTab(): SettingsContract.DefaultTab =
    when (this) {
        OnboardingContract.DefaultTab.ROUTINES -> SettingsContract.DefaultTab.ROUTINES
        OnboardingContract.DefaultTab.TASKS -> SettingsContract.DefaultTab.TASKS
    }

private fun SettingsContract.DefaultTab.toOnboardingDefaultTab(): OnboardingContract.DefaultTab =
    when (this) {
        SettingsContract.DefaultTab.ROUTINES -> OnboardingContract.DefaultTab.ROUTINES
        SettingsContract.DefaultTab.TASKS -> OnboardingContract.DefaultTab.TASKS
    }
