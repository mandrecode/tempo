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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.TempoDatePickerDialog
import com.mandrecode.tempo.core.ui.theme.settingsTimeValue
import com.mandrecode.tempo.util.DateTimeFormatter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

/**
 * Vacation mode: one switch that pauses every habit at once, plus an optional end date.
 *
 * The switch reflects whether a stored period covers today, so a period whose planned end date
 * has passed shows as off without anything having to run in the background.
 */
@Composable
internal fun VacationModeSection(
    uiState: SettingsContract.UiState,
    onEvent: (SettingsContract.UiEvent) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    SettingsSection(title = stringResource(R.string.settings_vacation_mode)) {
        Column {
            SettingsSwitchItem(
                icon = R.drawable.ic_flight,
                title = stringResource(R.string.settings_vacation_mode_pause_habits),
                checked = uiState.vacationModeActive,
                onCheckedChange = {
                    onEvent(SettingsContract.UiEvent.VacationModeToggled(it))
                },
            )
            AnimatedVisibility(
                visible = uiState.vacationModeActive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column {
                    SettingsItemDivider()
                    VacationEndDateItem(
                        endDate = uiState.vacationEndDate,
                        onClick = { showDatePicker = true },
                        onClear = {
                            onEvent(SettingsContract.UiEvent.VacationEndDateChanged(null))
                        },
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val periodStart = uiState.vacationStartDate
        val selectableDates =
            remember(periodStart) {
                if (periodStart == null) {
                    DatePickerDefaults.AllDates
                } else {
                    object : SelectableDates {
                        // The pause cannot end before it began.
                        override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                            utcTimeMillis >= periodStart.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
                    }
                }
            }

        TempoDatePickerDialog(
            initialDate = uiState.vacationEndDate ?: uiState.vacationStartDate,
            onConfirm = { date ->
                showDatePicker = false
                onEvent(SettingsContract.UiEvent.VacationEndDateChanged(date))
            },
            onDismiss = { showDatePicker = false },
            selectableDates = selectableDates,
        )
    }
}

/**
 * Single-line row whose value *is* the control, mirroring the catch-up time row: only the value
 * pill is clickable, plus a clear action once an end date has been picked.
 */
@Composable
private fun VacationEndDateItem(
    endDate: LocalDate?,
    onClick: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(SettingsItemPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIcon(icon = R.drawable.ic_event)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.settings_vacation_mode_until),
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
                text =
                    endDate
                        ?.let { DateTimeFormatter.formatDate(it) }
                        ?: stringResource(R.string.settings_vacation_mode_no_end_date),
                style = MaterialTheme.typography.settingsTimeValue,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
        if (endDate != null) {
            IconButton(onClick = onClear) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.settings_vacation_mode_clear_end_date),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
