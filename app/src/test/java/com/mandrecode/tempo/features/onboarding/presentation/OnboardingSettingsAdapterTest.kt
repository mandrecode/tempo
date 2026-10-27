package com.mandrecode.tempo.features.onboarding.presentation

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.features.settings.presentation.SettingsContract
import org.junit.Test

class OnboardingSettingsAdapterTest {
    @Test
    fun givenOnboardingState_whenAdapted_thenSettingsValuesArePreserved() {
        val state =
            OnboardingContract
                .UiState(
                    selectedThemeMode = ThemeMode.DARK,
                    useTempoColors = false,
                ).toSettingsUiState()

        assertThat(state.selectedThemeMode).isEqualTo(ThemeMode.DARK)
        assertThat(state.useTempoColors).isFalse()
    }

    @Test
    fun givenSupportedSettingsEvents_whenAdapted_thenMatchingOnboardingEventsAreReturned() {
        assertThat(SettingsContract.UiEvent.ThemeModeSelected(ThemeMode.LIGHT).toOnboardingEvent())
            .isEqualTo(OnboardingContract.UiEvent.ThemeModeSelected(ThemeMode.LIGHT))
        assertThat(SettingsContract.UiEvent.TempoColorsToggled(true).toOnboardingEvent())
            .isEqualTo(OnboardingContract.UiEvent.UseTempoColorsToggled(true))
    }

    @Test
    fun givenSettingsOnlyEvents_whenAdapted_thenTheyAreIgnored() {
        assertThat(SettingsContract.UiEvent.AutoRemoveCompletedTasksToggled(true).toOnboardingEvent()).isNull()
        assertThat(SettingsContract.UiEvent.CompletedTaskRetentionDaysChanged(30).toOnboardingEvent()).isNull()
    }
}
