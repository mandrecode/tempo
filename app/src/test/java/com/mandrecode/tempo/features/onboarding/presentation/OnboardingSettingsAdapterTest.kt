package com.mandrecode.tempo.features.onboarding.presentation

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.TempoTab
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.features.settings.presentation.SettingsContract
import kotlinx.collections.immutable.persistentSetOf
import org.junit.Test

class OnboardingSettingsAdapterTest {
    @Test
    fun givenOnboardingState_whenAdapted_thenSettingsValuesArePreserved() {
        val routinesState =
            OnboardingContract
                .UiState(
                    selectedThemeMode = ThemeMode.DARK,
                    useTempoColors = false,
                    enabledTabs = persistentSetOf(TempoTab.FOCUS, TempoTab.ROUTINES),
                    defaultTab = TempoTab.ROUTINES,
                ).toSettingsUiState()
        val tasksState =
            OnboardingContract
                .UiState(
                    defaultTab = TempoTab.TASKS,
                ).toSettingsUiState()

        assertThat(routinesState.selectedThemeMode).isEqualTo(ThemeMode.DARK)
        assertThat(routinesState.useTempoColors).isFalse()
        assertThat(routinesState.enabledTabs).containsExactly(TempoTab.FOCUS, TempoTab.ROUTINES)
        assertThat(routinesState.defaultTab).isEqualTo(TempoTab.ROUTINES)
        assertThat(tasksState.defaultTab).isEqualTo(TempoTab.TASKS)
    }

    @Test
    fun givenSupportedSettingsEvents_whenAdapted_thenMatchingOnboardingEventsAreReturned() {
        assertThat(SettingsContract.UiEvent.ThemeModeSelected(ThemeMode.LIGHT).toOnboardingEvent())
            .isEqualTo(OnboardingContract.UiEvent.ThemeModeSelected(ThemeMode.LIGHT))
        assertThat(SettingsContract.UiEvent.TempoColorsToggled(true).toOnboardingEvent())
            .isEqualTo(OnboardingContract.UiEvent.UseTempoColorsToggled(true))
        assertThat(SettingsContract.UiEvent.TabToggled(TempoTab.ROUTINES, false).toOnboardingEvent())
            .isEqualTo(OnboardingContract.UiEvent.TabToggled(TempoTab.ROUTINES, false))
        assertThat(SettingsContract.UiEvent.TabToggled(TempoTab.TASKS, false).toOnboardingEvent())
            .isEqualTo(OnboardingContract.UiEvent.TabToggled(TempoTab.TASKS, false))
        assertThat(
            SettingsContract.UiEvent.DefaultTabSelected(TempoTab.ROUTINES).toOnboardingEvent(),
        ).isEqualTo(OnboardingContract.UiEvent.DefaultTabSelected(TempoTab.ROUTINES))
        assertThat(
            SettingsContract.UiEvent.DefaultTabSelected(TempoTab.TASKS).toOnboardingEvent(),
        ).isEqualTo(OnboardingContract.UiEvent.DefaultTabSelected(TempoTab.TASKS))
    }

    @Test
    fun givenSettingsOnlyEvents_whenAdapted_thenTheyAreIgnored() {
        assertThat(SettingsContract.UiEvent.AutoRemoveCompletedTasksToggled(true).toOnboardingEvent()).isNull()
        assertThat(SettingsContract.UiEvent.CompletedTaskRetentionDaysChanged(30).toOnboardingEvent()).isNull()
    }
}
