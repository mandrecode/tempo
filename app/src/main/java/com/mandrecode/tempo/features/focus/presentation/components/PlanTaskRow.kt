package com.mandrecode.tempo.features.focus.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.ExpressiveChip
import com.mandrecode.tempo.core.ui.components.SquaredSelection
import com.mandrecode.tempo.core.ui.theme.TempoSpacing.cardContentPadding
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.model.UndatedTask
import com.mandrecode.tempo.features.tasks.presentation.components.cards.TaskItem
import com.mandrecode.tempo.util.DateTimeFormatter
import kotlinx.datetime.LocalDate

/**
 * Smaller than the app's default chip. These repeat on every row, so three of them at full size
 * carried as much weight as the task title they belong to — and the title is the thing you are
 * deciding about.
 */
private val QuickPlanChipHeight = 36.dp

/**
 * The row width at which the chips move out from under the card's text and sit beside it.
 *
 * Measured against the row itself rather than the window: the sheet caps at 640dp and the grid may
 * one day give it more than one column, so what decides this is how much room *this card* has, not
 * how big the screen is. Below it the text column still wants the full width; above it the column
 * has stopped growing and the chips are stacking height onto space already going spare.
 */
private val ChipsBesideMinWidth = 480.dp

/**
 * The corner where the tray meets the rest of the card — the only one it draws for itself. The card
 * clips the outer ones, so those follow whatever radius it is currently animating through and the
 * tray never has to know about it.
 */
private val ChipTrayCorner = 20.dp

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
    tomorrow: LocalDate,
    onPlan: (Long, LocalDate?) -> Unit,
    onUnplan: (Long) -> Unit,
    onEdit: (Task) -> Unit,
    onToggleCompletion: (Task) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Folded is the resting state here. The count badge is the part that matters when you are
    // choosing a day — how much is left in this task — and unfolding every step of every row would
    // bury the question the sheet is asking. The chevron is still there for anyone who wants to look.
    var isSubtasksExpanded by remember(row.task.id) { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier) {
        val roomToSit = maxWidth >= ChipsBesideMinWidth

        // The card's bottom band: it reaches both of the card's sides and rounds only along its top.
        val chipBand: @Composable () -> Unit = {
            ChipTray(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = ChipTrayCorner, topEnd = ChipTrayCorner),
            ) {
                QuickPlanChips(
                    plannedFor = row.task.reminderDate?.date,
                    today = today,
                    tomorrow = tomorrow,
                    isBesideText = false,
                    onPlan = { date -> onPlan(row.task.id, date) },
                    onUnplan = { onUnplan(row.task.id) },
                )
            }
        }

        // The card's right-hand column: it reaches the card's full height and rounds only where it
        // meets the left-hand one. Offered rather than chosen — the card takes it only while it is
        // folded, and falls back to the band above once it is not.
        val chipColumn: @Composable () -> Unit = {
            ChipTray(
                modifier = Modifier.fillMaxHeight(),
                shape = RoundedCornerShape(topStart = ChipTrayCorner, bottomStart = ChipTrayCorner),
            ) {
                QuickPlanChips(
                    plannedFor = row.task.reminderDate?.date,
                    today = today,
                    tomorrow = tomorrow,
                    isBesideText = true,
                    onPlan = { date -> onPlan(row.task.id, date) },
                    onUnplan = { onUnplan(row.task.id) },
                )
            }
        }

        TaskItem(
            task = row.task,
            onToggleCompletion = onToggleCompletion,
            onEdit = onEdit,
            category = row.category,
            subtasks = row.subtasks,
            isSubtasksExpanded = isSubtasksExpanded,
            onToggleSubtasksExpansion = { isSubtasksExpanded = it },
            // The sheet asks one question of every row — which day? — and an "add a step" button
            // beside the answer is a different job offered in the middle of this one. The full
            // editor is still a tap on the card away for anyone who wants it.
            showAddSubtaskAction = false,
            footer = chipBand,
            trailingContent = if (roomToSit) chipColumn else null,
            // Beside, the card is two columns of similar height and the three parts read as one
            // line of the list, so they centre on each other. In the band the chips sit under the
            // text and the checkbox belongs against the title, as it does everywhere else — which
            // the card falls back to on its own whenever it is not using the column.
            verticalAlignment = Alignment.CenterVertically,
        )
    }
}

/**
 * The half of the card that changes the task, divided off from the half that describes it.
 *
 * Not a panel laid on the card but a piece of it: the surface runs out to the card's own edges and
 * rounds only where the two halves meet, which is how the app's screens already split a page into
 * its two tones. The card clips the rest.
 *
 * The card is drawn on `background` and this on `surfaceContainerHighest`, so the step between them
 * is read off that same ramp — darker than the card in the light theme, lighter in the dark one,
 * without either being named here.
 */
@Composable
private fun ChipTray(
    shape: Shape,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = cardContentPadding, vertical = 12.dp),
            // Only does anything on the side panel, which is as tall as the card rather than as
            // tall as its chips.
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

/**
 * The whole point of the sheet: a day, in one tap.
 *
 * Each chip is rounded on both ends rather than segment-joined: a connected group that wraps mid-
 * run reads as broken, and these are three separate offers, not one control with three positions.
 *
 * Every one of them toggles. Pressing the chip that is already lit takes the date back off and
 * returns the task to Unplanned — planning a list at speed means mis-tapping some of it, and a
 * choice you cannot take back is one people stop making quickly.
 */
@Composable
private fun QuickPlanChips(
    plannedFor: LocalDate?,
    today: LocalDate,
    tomorrow: LocalDate,
    isBesideText: Boolean,
    onPlan: (LocalDate?) -> Unit,
    onUnplan: () -> Unit,
) {
    val days: @Composable () -> Unit = {
        QuickPlanChip(
            label = stringResource(R.string.today),
            isSelected = plannedFor == today,
            onPlan = { onPlan(today) },
            onUnplan = onUnplan,
        )
        QuickPlanChip(
            label = stringResource(R.string.tomorrow),
            isSelected = plannedFor == tomorrow,
            onPlan = { onPlan(tomorrow) },
            onUnplan = onUnplan,
        )
    }

    // Lit when the task carries a day the other two do not name — the only state in which this chip
    // stands for a value rather than for opening the picker. It says that value, too: a lit chip
    // still reading "Pick a date" would be the one chip on the row not telling you what it chose.
    val chosenDate = plannedFor?.takeIf { it != today && it != tomorrow }
    val picker: @Composable (Modifier) -> Unit = { pickerModifier ->
        QuickPlanChip(
            label =
                chosenDate?.let { remember(it) { DateTimeFormatter.formatDate(it) } }
                    ?: stringResource(R.string.plan_tasks_pick_date),
            isSelected = chosenDate != null,
            onPlan = { onPlan(null) },
            onUnplan = onUnplan,
            // Only while it stands for an action. Two of these chips are values and this one opens
            // a picker, which nothing distinguished until it had been used; once it carries a date
            // it is a value like the others and the glyph would be describing the wrong thing.
            icon =
                if (chosenDate == null) {
                    {
                        Icon(
                            painter = painterResource(R.drawable.ic_calendar),
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                } else {
                    null
                },
            modifier = pickerModifier,
        )
    }

    if (isBesideText) {
        // The two days set the block's width — [IntrinsicSize.Max] asks the Row below what it wants
        // and hands the Column exactly that — and the picker then fills it. Left to size itself it
        // ended a ragged distance short of Tomorrow, and shrank again the moment it was chosen and
        // its label became a date. Filling gives the group one edge, and gives the one chip here
        // that opens a dialog rather than answering outright the target to match.
        Column(
            modifier = Modifier.width(IntrinsicSize.Max),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { days() }
            picker(Modifier.fillMaxWidth())
        }
    } else {
        // Under the text there is no column to line up with, and chips are text — as wide as the
        // language they are in. A [FlowRow] answers that at every width and in every translation,
        // where a fixed arrangement would only answer it in English.
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            days()
            picker(Modifier)
        }
    }
}

@Composable
private fun QuickPlanChip(
    label: String,
    isSelected: Boolean,
    onPlan: () -> Unit,
    onUnplan: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
) {
    val unplanLabel = stringResource(R.string.plan_tasks_unplan_action, label)

    ExpressiveChip(
        label = label,
        isSelected = isSelected,
        onClick = if (isSelected) onUnplan else onPlan,
        isFirst = true,
        isLast = true,
        icon = icon,
        height = QuickPlanChipHeight,
        horizontalPadding = 12.dp,
        // Outline only when unchosen. Filled containers on every chip of every row made the row's
        // controls compete with its title; an outline still reads as tappable and lets the one that
        // has been chosen be the only filled thing on the card.
        unselectedContainerColor = Color.Transparent,
        // These stand alone with gaps between them rather than joined into a run, so the chosen one
        // squares off instead of rounding out.
        selectedCornerRadius = SquaredSelection,
        // A lit chip and an unlit one do the opposite things, and a screen reader reading the same
        // word for both would be describing only half of the control.
        modifier =
            if (isSelected) {
                modifier.semantics { contentDescription = unplanLabel }
            } else {
                modifier
            },
    )
}
