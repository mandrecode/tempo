package com.mandrecode.tempo.features.settings.presentation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.core.ui.theme.TempoDarkPrimary
import com.mandrecode.tempo.core.ui.theme.TempoLightPrimary
import com.mandrecode.tempo.core.ui.theme.TempoTheme

// region SettingsContent Previews

private val defaultLightState =
    SettingsContract.UiState(
        selectedThemeMode = ThemeMode.LIGHT,
        isRoutinesTabEnabled = true,
        isTasksTabEnabled = true,
        defaultTab = SettingsContract.DefaultTab.ROUTINES,
        appVersion = "1.0",
    )

private val tempoColorsState =
    SettingsContract.UiState(
        selectedThemeMode = ThemeMode.DARK,
        useTempoColors = true,
        isRoutinesTabEnabled = true,
        isTasksTabEnabled = false,
        defaultTab = SettingsContract.DefaultTab.ROUTINES,
        appVersion = "1.0",
    )

private val systemState =
    SettingsContract.UiState(
        selectedThemeMode = ThemeMode.SYSTEM,
        isRoutinesTabEnabled = false,
        isTasksTabEnabled = true,
        defaultTab = SettingsContract.DefaultTab.TASKS,
        appVersion = "1.0",
    )

@Preview(name = "Light - Didi Default", showBackground = true, device = "id:pixel_9")
@Composable
private fun SettingsContentLightDefaultPreview() {
    TempoTheme(useTempoColors = false) {
        SettingsContent(uiState = defaultLightState, onEvent = {}, onOnboardingClick = {})
    }
}

@Preview(name = "Dark - Didi Default", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsContentDarkDefaultPreview() {
    TempoTheme(useTempoColors = false) {
        SettingsContent(
            uiState = defaultLightState.copy(selectedThemeMode = ThemeMode.DARK),
            onEvent = {},
            onOnboardingClick = {},
        )
    }
}

@Preview(name = "Light - Tempo Colors", showBackground = true, device = "id:pixel_9")
@Composable
private fun SettingsContentLightTempoPreview() {
    TempoTheme(useTempoColors = true) {
        SettingsContent(
            uiState = tempoColorsState.copy(selectedThemeMode = ThemeMode.LIGHT),
            onEvent = {},
            onOnboardingClick = {},
        )
    }
}

@Preview(name = "Dark - Tempo Colors", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsContentDarkTempoPreview() {
    TempoTheme(useTempoColors = true) {
        SettingsContent(uiState = tempoColorsState, onEvent = {}, onOnboardingClick = {})
    }
}

@Preview(name = "Light - System Partial Tabs", showBackground = true, device = "id:pixel_9")
@Composable
private fun SettingsContentLightSystemPreview() {
    TempoTheme {
        SettingsContent(uiState = systemState, onEvent = {}, onOnboardingClick = {})
    }
}

@Preview(name = "Dark - System Partial Tabs", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsContentDarkSystemPreview() {
    TempoTheme {
        SettingsContent(uiState = systemState, onEvent = {}, onOnboardingClick = {})
    }
}

// endregion

// region ColorDot Previews

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ColorDotTempoPreview() {
    TempoTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ColorDot(color = TempoLightPrimary)
            ColorDot(color = TempoDarkPrimary)
            ColorDot(color = Color.White)
        }
    }
}

// endregion
