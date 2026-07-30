package com.mandrecode.tempo.features.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.TempoTimePickerDialog
import com.mandrecode.tempo.core.ui.theme.settingsTimeValue
import com.mandrecode.tempo.features.focus.domain.model.FocusSession

/**
 * How long a session and a break run.
 *
 * Set on a clock face rather than a row of presets: five choices could not cover everyone's rhythm,
 * and hours-and-minutes is how a length is actually said. Same row shape as the catch-up time above
 * it, where the value is the control and nothing restates it.
 */
@Composable
internal fun FocusSection(
    uiState: SettingsContract.UiState,
    onEvent: (SettingsContract.UiEvent) -> Unit,
) {
    SettingsSection(title = stringResource(R.string.settings_focus)) {
        Column {
            DurationItem(
                icon = R.drawable.ic_timer,
                title = stringResource(R.string.focus_session_length),
                minutes = uiState.focusSessionLengthMinutes,
                maxMinutes = FocusSession.SESSION_LENGTH_RANGE.last,
                onChange = { onEvent(SettingsContract.UiEvent.FocusSessionLengthChanged(it)) },
            )
            SettingsItemDivider()
            DurationItem(
                icon = R.drawable.ic_coffee,
                title = stringResource(R.string.focus_break_length),
                minutes = uiState.focusBreakLengthMinutes,
                maxMinutes = FocusSession.BREAK_LENGTH_RANGE.last,
                onChange = { onEvent(SettingsContract.UiEvent.FocusBreakLengthChanged(it)) },
            )
        }
    }
}

@Composable
private fun DurationItem(
    icon: Int,
    title: String,
    minutes: Int,
    maxMinutes: Int,
    onChange: (Int) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(SettingsItemPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIcon(icon = icon)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Surface(
            onClick = { showPicker = true },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            Text(
                text = durationLabel(minutes),
                style = MaterialTheme.typography.settingsTimeValue,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }

    if (showPicker) {
        TempoTimePickerDialog(
            initialHour = minutes / MINUTES_PER_HOUR,
            initialMinute = minutes % MINUTES_PER_HOUR,
            // The clock face runs to 23 hours whatever we do, so the cap is applied on the way out.
            // Something longer lands on the cap rather than being refused: rejecting a confirmed
            // choice with no room to say why is worse than quietly taking the nearest we allow.
            onConfirm = { hour, minute ->
                showPicker = false
                onChange((hour * MINUTES_PER_HOUR + minute).coerceIn(1, maxMinutes))
            },
            onDismiss = { showPicker = false },
            is24Hour = true,
        )
    }
}

/** "45 min", "1 h", "1 h 30 min" — a whole hour drops its zero minutes. */
@Composable
private fun durationLabel(minutes: Int): String {
    val hours = minutes / MINUTES_PER_HOUR
    val remainder = minutes % MINUTES_PER_HOUR
    return when {
        hours == 0 -> stringResource(R.string.focus_session_length_minutes, remainder)
        remainder == 0 -> stringResource(R.string.focus_duration_hours, hours)
        else -> stringResource(R.string.focus_duration_hours_minutes, hours, remainder)
    }
}

private const val MINUTES_PER_HOUR = 60
