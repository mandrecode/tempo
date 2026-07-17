package com.mandrecode.tempo.features.onboarding.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.onboardingPageHeadlineRegular
import com.mandrecode.tempo.core.ui.theme.onboardingPageHeadlineShort
import com.mandrecode.tempo.features.settings.presentation.DefaultTabSection
import com.mandrecode.tempo.features.settings.presentation.SettingsContract
import com.mandrecode.tempo.features.settings.presentation.TabsAndNavigationSection

@Composable
internal fun OnboardingSetupPage(
    uiState: OnboardingContract.UiState,
    layout: OnboardingLayout,
    onEvent: (OnboardingContract.UiEvent) -> Unit,
) {
    val settingsUiState = uiState.toSettingsUiState()
    val onSettingsEvent = { event: SettingsContract.UiEvent ->
        val onboardingEvent = event.toOnboardingEvent()
        if (onboardingEvent != null) onEvent(onboardingEvent)
    }

    AdaptiveOnboardingPage(
        layout = layout,
        introAlignment = Alignment.Start,
        intro = {
            Text(
                text = stringResource(R.string.onboarding_setup_title),
                style =
                    if (layout.isShort) {
                        MaterialTheme.typography.onboardingPageHeadlineShort
                    } else {
                        MaterialTheme.typography.onboardingPageHeadlineRegular
                    },
            )
            Text(
                text = stringResource(R.string.onboarding_setup_description),
                style =
                    if (layout.isShort) {
                        MaterialTheme.typography.bodyMedium
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        body = {
            TabsAndNavigationSection(uiState = settingsUiState, onEvent = onSettingsEvent)
            DefaultTabSection(uiState = settingsUiState, onEvent = onSettingsEvent)
        },
    )
}
