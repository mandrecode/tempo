package com.mandrecode.tempo.features.focus.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mandrecode.tempo.core.ui.components.HandleReminderPermissions
import com.mandrecode.tempo.core.ui.components.TempoDatePickerDialog
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

/** The two things that can stand between a chip and a date: permission, and the picker itself. */
@Composable
internal fun PlanSheetDialogs(
    pendingPlan: PendingPlan?,
    datePickerTaskId: Long?,
    today: LocalDate,
    onGrantPermissions: () -> Unit,
    onDeclinePermissions: () -> Unit,
    onChooseDate: (Long, LocalDate) -> Unit,
    onDismissDatePicker: () -> Unit,
) {
    HandleReminderPermissions(
        show = pendingPlan != null,
        onGrantPermissions = onGrantPermissions,
        onDismiss = onDeclinePermissions,
    )

    datePickerTaskId?.let { taskId ->
        PlanDatePickerDialog(
            today = today,
            onConfirm = { date -> onChooseDate(taskId, date) },
            onDismiss = onDismissDatePicker,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanDatePickerDialog(
    today: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    // Planning is about what is ahead. A date already gone would be a reminder that never fires,
    // which is the one thing the sheet exists to stop happening.
    val todayOrLater =
        remember(today) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis >= today.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            }
        }

    TempoDatePickerDialog(
        initialDate = today,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        selectableDates = todayOrLater,
    )
}
