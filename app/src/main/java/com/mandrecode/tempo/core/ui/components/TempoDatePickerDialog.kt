package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.mandrecode.tempo.R
import kotlinx.coroutines.flow.drop
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TempoDatePickerDialog(
    initialDate: LocalDate?,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    selectableDates: SelectableDates = DatePickerDefaults.AllDates,
    confirmLabel: String? = null,
    alternativeConfirmLabel: String? = null,
    onAlternativeConfirm: ((LocalDate) -> Unit)? = null,
    isAlternativeConfirmEnabled: (LocalDate) -> Boolean = { true },
) {
    val haptic = LocalHapticFeedback.current

    // M3 DatePicker works in UTC millis. We need to convert our local date to UTC millis at start of day.
    val initialMillis = initialDate?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds()

    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis = initialMillis,
            selectableDates = selectableDates,
        )
    val selectedDate = datePickerState.selectedDateMillis?.toUtcLocalDate()

    // Add haptic feedback when date picker values change
    LaunchedEffect(datePickerState, haptic) {
        snapshotFlow {
            datePickerState.selectedDateMillis to datePickerState.displayedMonthMillis
        }.drop(1)
            .collect { (selectedDate, _) ->
                if (selectedDate != null) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            }
    }

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Row {
                if (alternativeConfirmLabel != null && onAlternativeConfirm != null) {
                    TextButton(
                        enabled = selectedDate?.let(isAlternativeConfirmEnabled) == true,
                        onClick = {
                            selectedDate?.let(onAlternativeConfirm)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                    ) {
                        Text(alternativeConfirmLabel)
                    }
                }
                TextButton(
                    enabled = selectedDate != null,
                    onClick = {
                        selectedDate?.let { date ->
                            onConfirm(date)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    },
                ) {
                    Text(confirmLabel ?: stringResource(R.string.ok))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun Long.toUtcLocalDate(): LocalDate =
    Instant
        .fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.UTC)
        .date
