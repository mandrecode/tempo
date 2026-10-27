package com.mandrecode.tempo.core.ui.components

import android.text.format.DateFormat
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.glass.DialogWindowGlass
import kotlinx.coroutines.flow.drop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TempoTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    /**
     * Defaults to the device's clock preference. Pass `true` for a length rather than a moment —
     * "1:30 PM" is not a duration anyone means.
     */
    is24Hour: Boolean = DateFormat.is24HourFormat(LocalContext.current),
) {
    val haptic = LocalHapticFeedback.current
    val timePickerState =
        rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = is24Hour,
        )

    // Add haptic feedback when time picker values change
    LaunchedEffect(timePickerState, haptic) {
        snapshotFlow { timePickerState.hour to timePickerState.minute }
            .drop(1)
            .collect {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
    }

    TimePickerDialog(
        onDismissRequest = onDismiss,
        title = {},
        confirmButton = {
            DialogConfirmButton(
                label = stringResource(R.string.ok),
                modifier = Modifier.offset(y = TimePickerButtonsBottomTrim),
                onClick = { onConfirm(timePickerState.hour, timePickerState.minute) },
            )
        },
        dismissButton = {
            DialogCancelButton(
                label = stringResource(R.string.cancel),
                onClick = onDismiss,
                modifier = Modifier.offset(y = TimePickerButtonsBottomTrim),
            )
        },
    ) {
        // Blurs the app behind the picker and lightens the dim, like the app's alert dialogs.
        DialogWindowGlass()
        TimePicker(state = timePickerState)
    }
}

// Material's time picker keeps its buttons 28dp from the dialog's bottom but 24dp from its right
// edge; moving them down 4dp makes both 24dp, as in every other dialog.
private val TimePickerButtonsBottomTrim = 4.dp
