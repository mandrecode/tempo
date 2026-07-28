package com.mandrecode.tempo.features.settings.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.TempoTimePickerDialog
import com.mandrecode.tempo.util.DateTimeFormatter
import kotlinx.datetime.LocalTime

@Composable
internal fun RemindersSection(
    uiState: SettingsContract.UiState,
    onEvent: (SettingsContract.UiEvent) -> Unit,
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current
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
                    SettingsItem(
                        icon = R.drawable.ic_timer,
                        title = stringResource(R.string.settings_missed_reminders_time),
                        subtitle =
                            stringResource(
                                R.string.settings_missed_reminders_time_description,
                                DateTimeFormatter.formatTimeOfDay(catchUpTime, context),
                            ),
                        trailingIcon = R.drawable.ic_chevron_right,
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
