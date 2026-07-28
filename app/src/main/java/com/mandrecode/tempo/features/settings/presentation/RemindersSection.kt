package com.mandrecode.tempo.features.settings.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.TempoTimePickerDialog
import com.mandrecode.tempo.core.ui.theme.settingsTimeValue
import com.mandrecode.tempo.util.DateTimeFormatter
import kotlinx.datetime.LocalTime

@Composable
internal fun RemindersSection(
    uiState: SettingsContract.UiState,
    onEvent: (SettingsContract.UiEvent) -> Unit,
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val catchUpTime = uiState.missedReminderCatchUpTime

    SettingsSection(title = stringResource(R.string.settings_reminders)) {
        Column {
            SettingsSwitchItem(
                icon = R.drawable.ic_reminder,
                title = stringResource(R.string.settings_repeat_missed_reminders),
                checked = uiState.missedReminderCatchUpEnabled,
                onCheckedChange = {
                    onEvent(SettingsContract.UiEvent.MissedReminderCatchUpToggled(it))
                },
            )
            AnimatedVisibility(
                visible = uiState.missedReminderCatchUpEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column {
                    SettingsItemDivider()
                    CatchUpTimeItem(
                        time = catchUpTime,
                        onClick = { showTimePicker = true },
                    )
                }
            }
        }
    }

    if (showTimePicker) {
        TempoTimePickerDialog(
            initialHour = catchUpTime.hour,
            initialMinute = catchUpTime.minute,
            onConfirm = { hour, minute ->
                showTimePicker = false
                onEvent(
                    SettingsContract.UiEvent.MissedReminderCatchUpTimeChanged(
                        LocalTime(hour = hour, minute = minute),
                    ),
                )
            },
            onDismiss = { showTimePicker = false },
        )
    }
}

/**
 * Single-line row whose value *is* the control: only the clock-face time is clickable, so the
 * rest of the row stays inert and no chevron or restating subtitle is needed.
 */
@Composable
private fun CatchUpTimeItem(
    time: LocalTime,
    onClick: () -> Unit,
) {
    val context = LocalContext.current

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(SettingsItemPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIcon(icon = R.drawable.ic_timer)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.settings_missed_reminders_time),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ) {
            Text(
                text = DateTimeFormatter.formatTimeOfDay(time, context),
                style = MaterialTheme.typography.settingsTimeValue,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}
