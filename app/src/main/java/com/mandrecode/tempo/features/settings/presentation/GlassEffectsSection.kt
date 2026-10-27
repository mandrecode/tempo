package com.mandrecode.tempo.features.settings.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mandrecode.tempo.R

/**
 * The glass look (#423) on or off. Off swaps the frosted, blurred surfaces for plain, solid ones:
 * for anyone who finds translucency harder to read, or would rather spend no GPU on blur.
 */
@Composable
internal fun GlassEffectsSection(
    uiState: SettingsContract.UiState,
    onEvent: (SettingsContract.UiEvent) -> Unit,
) {
    SettingsSection(title = stringResource(R.string.settings_glass)) {
        item {
            SettingsSwitchItem(
                icon = R.drawable.ic_blur,
                title = stringResource(R.string.settings_glass_effects),
                checked = uiState.glassEffects,
                onCheckedChange = { onEvent(SettingsContract.UiEvent.GlassEffectsToggled(it)) },
            )
        }
    }
}
