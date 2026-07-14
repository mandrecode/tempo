package com.mandrecode.tempo.features.onboarding.presentation

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mandrecode.tempo.core.ui.theme.TempoTheme

@Preview(name = "Tasks - Light", device = "id:pixel_9", showBackground = true)
@Preview(
    name = "Tasks - Dark",
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun OnboardingTasksPreview() {
    TempoTheme(useTempoColors = true) {
        OnboardingContent(
            uiState = OnboardingContract.UiState(),
            onEvent = {},
        )
    }
}

@Preview(name = "Setup - Light", device = "id:pixel_9", showBackground = true)
@Preview(
    name = "Setup - Dark",
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun OnboardingSetupPreview() {
    TempoTheme(useTempoColors = true) {
        OnboardingContent(
            uiState =
                OnboardingContract.UiState(
                    currentPage = 3,
                    autoRemoveCompletedTasksEnabled = true,
                ),
            onEvent = {},
        )
    }
}
