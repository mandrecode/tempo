package com.mandrecode.tempo.features.focus.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.ExpressiveChip
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.model.UndatedTask
import com.mandrecode.tempo.features.tasks.presentation.components.cards.TaskItem
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

private val QuickPlanChipHeight = 40.dp

/**
 * One task, as the Tasks list itself draws it, with the day it is missing offered underneath.
 *
 * Pulled out of the grid so it can be measured on its own: this is the part that has to survive a
 * 328dp column — the card, the category badge and three chips of translated text — and the grid
 * around it cannot be constrained in a test the way a single row can.
 */
@Composable
internal fun PlanTaskRow(
    row: UndatedTask,
    today: LocalDate,
    onPlan: (Long, LocalDate?) -> Unit,
    onEdit: (Task) -> Unit,
    onToggleCompletion: (Task) -> Unit,
    modifier: Modifier = Modifier,
) {
    TaskItem(
        task = row.task,
        onToggleCompletion = onToggleCompletion,
        onEdit = onEdit,
        modifier = modifier,
        category = row.category,
        footer = {
            QuickPlanChips(
                plannedFor = row.task.reminderDate?.date,
                today = today,
                onPlan = { date -> onPlan(row.task.id, date) },
            )
        },
    )
}

/**
 * The whole point of the sheet: a day, in one tap.
 *
 * Wrapped rather than laid out per size class — chips are text, and text is as wide as the
 * language it is in. A [FlowRow] answers that at every width, where a breakpoint would only answer
 * it in English.
 *
 * Each chip is rounded on both ends rather than segment-joined: a connected group that wraps mid-
 * run reads as broken, and these are three separate offers, not one control with three positions.
 */
@Composable
private fun QuickPlanChips(
    plannedFor: LocalDate?,
    today: LocalDate,
    onPlan: (LocalDate?) -> Unit,
) {
    val tomorrow = remember(today) { today.plus(1, DateTimeUnit.DAY) }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        QuickPlanChip(
            label = stringResource(R.string.today),
            isSelected = plannedFor == today,
            onClick = { onPlan(today) },
        )
        QuickPlanChip(
            label = stringResource(R.string.tomorrow),
            isSelected = plannedFor == tomorrow,
            onClick = { onPlan(tomorrow) },
        )
        QuickPlanChip(
            label = stringResource(R.string.plan_tasks_pick_date),
            isSelected = plannedFor != null && plannedFor != today && plannedFor != tomorrow,
            onClick = { onPlan(null) },
        )
    }
}

@Composable
private fun QuickPlanChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    ExpressiveChip(
        label = label,
        isSelected = isSelected,
        onClick = onClick,
        isFirst = true,
        isLast = true,
        height = QuickPlanChipHeight,
        horizontalPadding = 14.dp,
    )
}
