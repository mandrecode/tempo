package com.mandrecode.tempo.features.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.ValueStepper
import com.mandrecode.tempo.features.focus.domain.model.FocusSession

/**
 * How long a session and a break run.
 *
 * Steppers rather than a fixed row of chips: five presets could not cover everyone's rhythm, and
 * the repeat-reminder interval already established what "how many" looks like in this app.
 */
@Composable
internal fun FocusSection(
    uiState: SettingsContract.UiState,
    onEvent: (SettingsContract.UiEvent) -> Unit,
) {
    SettingsSection(title = stringResource(R.string.settings_focus)) {
        Column(
            modifier = Modifier.padding(SettingsSectionContentPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LengthStepperRow(
                title = stringResource(R.string.focus_session_length),
                minutes = uiState.focusSessionLengthMinutes,
                range = FocusSession.SESSION_LENGTH_RANGE,
                onChange = { onEvent(SettingsContract.UiEvent.FocusSessionLengthChanged(it)) },
            )
            LengthStepperRow(
                title = stringResource(R.string.focus_break_length),
                minutes = uiState.focusBreakLengthMinutes,
                range = FocusSession.BREAK_LENGTH_RANGE,
                onChange = { onEvent(SettingsContract.UiEvent.FocusBreakLengthChanged(it)) },
            )
        }
    }
}

@Composable
private fun LengthStepperRow(
    title: String,
    minutes: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        ValueStepper(
            value = minutes,
            label = stringResource(R.string.focus_session_length_minutes, minutes),
            onValueChange = onChange,
            range = range,
            step = FocusSession.LENGTH_STEP_MINUTES,
        )
    }
}
