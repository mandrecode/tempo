package com.mandrecode.tempo.features.focus.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.HandleReminderPermissions
import com.mandrecode.tempo.core.ui.components.TempoDatePickerDialog
import com.mandrecode.tempo.core.ui.components.TempoLoadingIndicator
import com.mandrecode.tempo.core.ui.components.TempoModalBottomSheet
import com.mandrecode.tempo.core.ui.components.WavyDivider
import com.mandrecode.tempo.core.ui.theme.dialogAction
import com.mandrecode.tempo.core.ui.theme.groupLabel
import com.mandrecode.tempo.core.ui.theme.sheetTitle
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation
import com.mandrecode.tempo.features.focus.presentation.FocusContract
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.model.UndatedTask
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.todayIn
import kotlin.time.Clock

internal const val PLAN_SHEET_TAG = "plan_tasks_sheet"
internal const val PLAN_SHEET_DONE_TAG = "plan_tasks_done"
internal const val PLAN_SHEET_PLANNED_HEADER_TAG = "plan_sheet_planned_header"
internal const val PLAN_SHEET_UNPLANNED_HEADER_TAG = "plan_sheet_unplanned_header"

/**
 * The width a task card actually gets in the Tasks list on a 360dp phone.
 *
 * The column count is derived against it rather than fixed, so the sheet answers "how many fit"
 * from the window it is in. At today's sheet widths — 640dp for the bottom sheet, 412dp for the
 * docked pane — that answer is one everywhere, but it is an answer rather than an assumption.
 */
private val PlanRowMinWidth = 328.dp

/**
 * Somewhere to give undated work a day, without leaving the day you were looking at.
 *
 * Every change here lands the moment it is made — there is nothing to save, and so nothing to lose
 * by closing. What the sheet offers instead is the way back: confirming hands the whole batch to an
 * undo, which is the only honest thing a second button can mean when the first one has already
 * written everything.
 */
@Composable
internal fun PlanTasksSheet(
    state: FocusContract.PlanSheetState,
    onEvent: (FocusContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
    clock: Clock = Clock.System,
) {
    val close = { onEvent(FocusContract.UiEvent.ClosePlanSheet) }
    val today = clock.todayIn(TimeZone.currentSystemDefault())

    // Held rather than acted on: the first plan of a session has to get past the permission
    // education first, and the chip the user pressed is what should happen once it does.
    var pendingPlan by remember { mutableStateOf<PendingPlan?>(null) }
    var permissionsSettled by remember { mutableStateOf(false) }
    var datePickerTaskId by remember { mutableStateOf<Long?>(null) }

    val openDatePicker: (Long) -> Unit = { datePickerTaskId = it }
    val requestPlan: (Long, LocalDate?) -> Unit = { taskId, date ->
        val plan = PendingPlan(taskId, date)
        if (permissionsSettled) plan.carryOut(onEvent, openDatePicker) else pendingPlan = plan
    }

    TempoModalBottomSheet(
        // Every way out is the same way out. The handle, back, the scrim, Escape and the button all
        // leave the planning in place and offer it back, so none of them is the one that loses work.
        onDismissRequest = close,
        modifier =
            modifier.testTag(PLAN_SHEET_TAG).onPreviewKeyEvent { event ->
                // Large windows come with keyboards, and Escape is what a keyboard expects to close
                // a modal with.
                val isEscape = event.type == KeyEventType.KeyUp && event.key == Key.Escape
                if (isEscape) close()
                isEscape
            },
        adaptivePlacement = true,
    ) { _ ->
        PlanSheetBody(
            state = state,
            today = today,
            onPlan = requestPlan,
            onEvent = onEvent,
        )
    }

    PlanSheetDialogs(
        pendingPlan = pendingPlan,
        datePickerTaskId = datePickerTaskId,
        today = today,
        onGrantPermissions = {
            permissionsSettled = true
            pendingPlan?.carryOut(onEvent, openDatePicker)
            pendingPlan = null
        },
        // Declined: the task keeps no date rather than gaining one whose reminder would never
        // arrive, and the next chip asks again.
        onDeclinePermissions = { pendingPlan = null },
        onChooseDate = { taskId, date ->
            datePickerTaskId = null
            onEvent(FocusContract.UiEvent.PlanTask(taskId, date))
        },
        onDismissDatePicker = { datePickerTaskId = null },
    )
}

/** Title, list and footer — everything inside the sheet's own surface. */
@Composable
private fun ColumnScope.PlanSheetBody(
    state: FocusContract.PlanSheetState,
    today: LocalDate,
    onPlan: (Long, LocalDate?) -> Unit,
    onEvent: (FocusContract.UiEvent) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                // The sheet is only as tall as this column, so every row that changes section
                // resizes the whole surface. Left to itself that is a snap — the sheet jumps, and
                // at the top of its range the corners and status-bar inset change with it. Animated
                // on the same spring the rows move on, the sheet settles with them instead.
                .animateContentSize(),
    ) {
        PlanSheetHeader(remainingCount = state.unplanned.size, isLoading = state.isLoading)

        if (state.isLoading) {
            TempoLoadingIndicator(
                message = stringResource(R.string.loading_tasks),
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
            )
        } else {
            PlanSheetRows(
                state = state,
                today = today,
                onPlan = onPlan,
                onUnplan = { taskId -> onEvent(FocusContract.UiEvent.UnplanTask(taskId)) },
                onEdit = { task -> onEvent(FocusContract.UiEvent.EditTask(task)) },
                onToggleCompletion = { task ->
                    onEvent(FocusContract.UiEvent.ToggleTaskCompletion(task))
                },
                modifier = Modifier.weight(1f, fill = false),
            )
        }

        PlanSheetDoneButton(onClick = { onEvent(FocusContract.UiEvent.ClosePlanSheet) })
    }
}

/**
 * One button, and no way to lose anything by pressing it.
 *
 * A Cancel beside it would be lying: there is nothing staged to throw away, because every chip has
 * already written. Two buttons where one of them cannot do what its name says is worse than one
 * button — so the sheet has Done, always available, and closing by any other means does exactly the
 * same thing. What protects a mis-tap is the undo that follows, not a button that promises to.
 */
@Composable
private fun PlanSheetDoneButton(onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val (interactionSource, cornerRadius) = rememberPressableButtonAnimation()

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
            shape = RoundedCornerShape(cornerRadius.value),
            interactionSource = interactionSource,
            contentPadding = PaddingValues(horizontal = 24.dp),
            modifier = Modifier.height(48.dp).testTag(PLAN_SHEET_DONE_TAG),
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.plan_tasks_done),
                style = MaterialTheme.typography.dialogAction,
            )
        }
    }
}

/** The two things that can stand between a chip and a date: permission, and the picker itself. */
@Composable
private fun PlanSheetDialogs(
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

/** A chip press waiting on permissions. A null [date] means "let the user pick one". */
private data class PendingPlan(
    val taskId: Long,
    val date: LocalDate?,
)

private fun PendingPlan.carryOut(
    onEvent: (FocusContract.UiEvent) -> Unit,
    openDatePicker: (Long) -> Unit,
) {
    if (date == null) openDatePicker(taskId) else onEvent(FocusContract.UiEvent.PlanTask(taskId, date))
}

@Composable
private fun PlanSheetHeader(
    remainingCount: Int,
    isLoading: Boolean,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp)) {
        Text(
            text = stringResource(R.string.plan_tasks_title),
            style = MaterialTheme.typography.sheetTitle,
        )
        // Nothing is counted until the rows arrive. An empty list and a list that has not loaded
        // yet look identical from here, and only one of them means "every one of them has a day
        // now" — saying it before looking would be the sheet congratulating the user on work it
        // has not seen.
        if (!isLoading) {
            Text(
                text =
                    if (remainingCount == 0) {
                        stringResource(R.string.plan_tasks_subtitle_all_done)
                    } else {
                        pluralStringResource(R.plurals.plan_tasks_subtitle, remainingCount, remainingCount)
                    },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun PlanSheetRows(
    state: FocusContract.PlanSheetState,
    today: LocalDate,
    onPlan: (Long, LocalDate?) -> Unit,
    onUnplan: (Long) -> Unit,
    onEdit: (Task) -> Unit,
    onToggleCompletion: (Task) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = PlanRowMinWidth),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (state.showsSectionHeaders && state.unplanned.isNotEmpty()) {
            fullWidthItem(key = "unplanned_header") {
                PlanSectionHeader(
                    label = stringResource(R.string.plan_tasks_section_unplanned),
                    testTag = PLAN_SHEET_UNPLANNED_HEADER_TAG,
                    modifier = Modifier.animateItem(),
                )
            }
        }
        planRows(state.unplanned, today, onPlan, onUnplan, onEdit, onToggleCompletion)

        if (state.showsSectionHeaders) {
            fullWidthItem(key = "planned_header") {
                PlanSectionHeader(
                    label = stringResource(R.string.plan_tasks_section_planned),
                    testTag = PLAN_SHEET_PLANNED_HEADER_TAG,
                    modifier = Modifier.animateItem(),
                )
            }
        }
        planRows(state.planned, today, onPlan, onUnplan, onEdit, onToggleCompletion)
    }
}

private fun LazyGridScope.fullWidthItem(
    key: String,
    content: @Composable LazyGridItemScope.() -> Unit,
) = item(key = key, span = { GridItemSpan(maxLineSpan) }) { content() }

private fun LazyGridScope.planRows(
    rows: List<UndatedTask>,
    today: LocalDate,
    onPlan: (Long, LocalDate?) -> Unit,
    onUnplan: (Long) -> Unit,
    onEdit: (Task) -> Unit,
    onToggleCompletion: (Task) -> Unit,
) = items(count = rows.size, key = { index -> rows[index].task.id }) { index ->
    PlanTaskRow(
        row = rows[index],
        today = today,
        onPlan = onPlan,
        onUnplan = onUnplan,
        onEdit = onEdit,
        onToggleCompletion = onToggleCompletion,
        modifier = Modifier.animateItem(),
    )
}

/**
 * The Tasks list's own group header, borrowed rather than approximated: the same wave, the same
 * label style, the same muting. A second look for the same idea would read as a second idea.
 */
@Composable
private fun PlanSectionHeader(
    label: String,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp).testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WavyDivider(modifier = Modifier.weight(1f))
        Text(
            text = label,
            style = MaterialTheme.typography.groupLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp, end = 8.dp),
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
