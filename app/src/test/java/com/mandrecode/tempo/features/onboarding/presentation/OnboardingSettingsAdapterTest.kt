package com.mandrecode.tempo.features.onboarding.presentation

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.features.settings.presentation.SettingsContract
import org.junit.Test

class OnboardingSettingsAdapterTest {
    @Test
    fun givenOnboardingState_whenAdapted_thenSettingsValuesArePreserved() {
        val routinesState =
            OnboardingContract
                .UiState(
                    selectedThemeMode = ThemeMode.DARK,
                    useTempoColors = false,
                    isRoutinesTabEnabled = true,
                    isTasksTabEnabled = false,
                    defaultTab = OnboardingContract.DefaultTab.ROUTINES,
                ).toSettingsUiState()
        val tasksState =
            OnboardingContract
                .UiState(
                    defaultTab = OnboardingContract.DefaultTab.TASKS,
                ).toSettingsUiState()

        assertThat(routinesState.selectedThemeMode).isEqualTo(ThemeMode.DARK)
        assertThat(routinesState.useTempoColors).isFalse()
        assertThat(routinesState.isRoutinesTabEnabled).isTrue()
        assertThat(routinesState.isTasksTabEnabled).isFalse()
        assertThat(routinesState.defaultTab).isEqualTo(SettingsContract.DefaultTab.ROUTINES)
        assertThat(tasksState.defaultTab).isEqualTo(SettingsContract.DefaultTab.TASKS)
    }

    @Test
    fun givenSupportedSettingsEvents_whenAdapted_thenMatchingOnboardingEventsAreReturned() {
        assertThat(SettingsContract.UiEvent.ThemeModeSelected(ThemeMode.LIGHT).toOnboardingEvent())
            .isEqualTo(OnboardingContract.UiEvent.ThemeModeSelected(ThemeMode.LIGHT))
        assertThat(SettingsContract.UiEvent.TempoColorsToggled(true).toOnboardingEvent())
            .isEqualTo(OnboardingContract.UiEvent.UseTempoColorsToggled(true))
        assertThat(SettingsContract.UiEvent.RoutinesTabToggled(false).toOnboardingEvent())
            .isEqualTo(OnboardingContract.UiEvent.RoutinesTabToggled(false))
        assertThat(SettingsContract.UiEvent.TasksTabToggled(false).toOnboardingEvent())
            .isEqualTo(OnboardingContract.UiEvent.TasksTabToggled(false))
        assertThat(
            SettingsContract.UiEvent.DefaultTabSelected(SettingsContract.DefaultTab.ROUTINES).toOnboardingEvent(),
        ).isEqualTo(OnboardingContract.UiEvent.DefaultTabSelected(OnboardingContract.DefaultTab.ROUTINES))
        assertThat(
            SettingsContract.UiEvent.DefaultTabSelected(SettingsContract.DefaultTab.TASKS).toOnboardingEvent(),
        ).isEqualTo(OnboardingContract.UiEvent.DefaultTabSelected(OnboardingContract.DefaultTab.TASKS))
    }

    @Test
    fun givenSettingsOnlyEvents_whenAdapted_thenTheyAreIgnored() {
        assertThat(SettingsContract.UiEvent.AutoRemoveCompletedTasksToggled(true).toOnboardingEvent()).isNull()
        assertThat(SettingsContract.UiEvent.CompletedTaskRetentionDaysChanged(30).toOnboardingEvent()).isNull()
    }
}
