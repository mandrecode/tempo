package com.mandrecode.tempo.features.onboarding.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.settings.presentation.CompletedTaskRetentionSection
import com.mandrecode.tempo.features.settings.presentation.DefaultTabSection
import com.mandrecode.tempo.features.settings.presentation.SettingsContract
import com.mandrecode.tempo.features.settings.presentation.TabsAndNavigationSection

@Composable
internal fun OnboardingSetupPage(
    uiState: OnboardingContract.UiState,
    onEvent: (OnboardingContract.UiEvent) -> Unit,
) {
    val settingsUiState = uiState.toSettingsUiState()
    val onSettingsEvent = { event: SettingsContract.UiEvent -> onEvent(event.toOnboardingEvent()) }

    Column(
        modifier = Modifier.widthIn(max = OnboardingMaxWidth).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.onboarding_setup_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.onboarding_setup_description),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TabsAndNavigationSection(uiState = settingsUiState, onEvent = onSettingsEvent)
        DefaultTabSection(uiState = settingsUiState, onEvent = onSettingsEvent)
        CompletedTaskRetentionSection(uiState = settingsUiState, onEvent = onSettingsEvent)
    }
}
