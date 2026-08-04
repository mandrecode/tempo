package com.mandrecode.tempo.features.focus.presentation.components

import com.mandrecode.tempo.features.focus.presentation.FocusContract
import kotlinx.datetime.LocalDate

/** A chip press waiting on permissions. A null [date] means "let the user pick one". */
internal data class PendingPlan(
    val taskId: Long,
    val date: LocalDate?,
)

internal fun PendingPlan.carryOut(
    onEvent: (FocusContract.UiEvent) -> Unit,
    openDatePicker: (Long) -> Unit,
) {
    if (date == null) openDatePicker(taskId) else onEvent(FocusContract.UiEvent.PlanTask(taskId, date))
}
