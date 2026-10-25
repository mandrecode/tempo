package com.mandrecode.tempo.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonColors
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.DayOfWeek

/** One day of the week: its full name (for accessibility) and its short name (for the letter). */
private data class WeekdayLabel(
    val day: DayOfWeek,
    @StringRes val nameRes: Int,
    @StringRes val shortRes: Int,
)

/** Monday-first. */
private val WeekdayLabels =
    listOf(
        WeekdayLabel(DayOfWeek.MONDAY, R.string.monday, R.string.mon),
        WeekdayLabel(DayOfWeek.TUESDAY, R.string.tuesday, R.string.tue),
        WeekdayLabel(DayOfWeek.WEDNESDAY, R.string.wednesday, R.string.wed),
        WeekdayLabel(DayOfWeek.THURSDAY, R.string.thursday, R.string.thu),
        WeekdayLabel(DayOfWeek.FRIDAY, R.string.friday, R.string.fri),
        WeekdayLabel(DayOfWeek.SATURDAY, R.string.saturday, R.string.sat),
        WeekdayLabel(DayOfWeek.SUNDAY, R.string.sunday, R.string.sun),
    )

private val WeekdayButtonHeight = 48.dp

/**
 * The narrowest a day may be in the equal-share group. Beside the icon column a 412 dp phone gives
 * each about 43 dp; below this the days scroll at full size instead.
 */
private val MinWeekdayWidth = 40.dp

/**
 * Picks repeat days the way an alarm clock does (#115): seven day initials in an expressive
 * [ButtonGroup], each a toggle that rounds into a square when on and widens while pressed.
 *
 * `null` [selectedDays] is the app's "no particular days". With [nullMeansEveryDay] (habits and
 * chains) that is every day, shown with all seven on, and the last day left on cannot be turned off
 * — an empty week would read as "every day" again. Without it (a weekly task, whose days otherwise
 * default from its reminder), `null` shows as nothing picked.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DayOfWeekSelector(
    selectedDays: Set<DayOfWeek>?,
    onDaysChange: (Set<DayOfWeek>?) -> Unit,
    modifier: Modifier = Modifier,
    nullMeansEveryDay: Boolean = true,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    enabled: Boolean = true,
) {
    val colors =
        ToggleButtonDefaults.colors(
            // The sheet sits on a container tone already; an off day needs to stand out from it.
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            checkedContainerColor = selectedContainerColor,
            checkedContentColor = selectedContentColor,
        )
    val day: @Composable (WeekdayLabel, Modifier) -> Unit = { label, dayModifier ->
        WeekdayToggle(label, selectedDays, onDaysChange, nullMeansEveryDay, enabled, colors, dayModifier)
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        val spacing = ButtonGroupDefaults.ConnectedSpaceBetween
        val share = (maxWidth - spacing * (WeekdayLabels.size - 1)) / WeekdayLabels.size
        if (share >= MinWeekdayWidth) {
            ButtonGroup(
                overflowIndicator = {},
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing),
            ) {
                WeekdayLabels.forEach { label ->
                    customItem(
                        buttonGroupContent = {
                            val interactionSource = remember { MutableInteractionSource() }
                            day(label, Modifier.weight(1f).animateWidth(interactionSource))
                        },
                        // Seven fixed days; nothing ever overflows.
                        menuContent = {},
                    )
                }
            }
        } else {
            // Too narrow for seven comfortable targets beside the icon column (a compact phone, a
            // large display size): full-size days in a row that scrolls, rather than squeezed ones.
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(spacing),
            ) {
                WeekdayLabels.forEach { label -> day(label, Modifier.width(WeekdayButtonHeight)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WeekdayToggle(
    label: WeekdayLabel,
    selectedDays: Set<DayOfWeek>?,
    onDaysChange: (Set<DayOfWeek>?) -> Unit,
    nullMeansEveryDay: Boolean,
    enabled: Boolean,
    colors: ToggleButtonColors,
    modifier: Modifier = Modifier,
) {
    val name = stringResource(label.nameRes)
    val initial = stringResource(label.shortRes).take(1).uppercase()
    ToggleButton(
        checked = label.day in displayedRepeatDays(selectedDays, nullMeansEveryDay),
        onCheckedChange = {
            val toggle = toggledRepeatDays(selectedDays, label.day, nullMeansEveryDay)
            if (toggle is RepeatDaysToggle.Changed) onDaysChange(toggle.days)
        },
        enabled = enabled,
        colors = colors,
        shapes = ToggleButtonDefaults.shapesFor(WeekdayButtonHeight),
        // A single letter in a narrow button: the default side padding would clip it.
        contentPadding = PaddingValues(0.dp),
        modifier =
            modifier
                .height(WeekdayButtonHeight)
                .semantics {
                    role = Role.Checkbox
                    contentDescription = name
                },
    ) {
        Text(text = initial, style = MaterialTheme.typography.labelLarge)
    }
}

/** The days drawn as on: `null` is every day only where it means that. */
internal fun displayedRepeatDays(
    selectedDays: Set<DayOfWeek>?,
    nullMeansEveryDay: Boolean,
): Set<DayOfWeek> =
    when {
        selectedDays == null -> if (nullMeansEveryDay) DayOfWeek.ALL_DAYS else emptySet()
        selectedDays.isEmpty() && nullMeansEveryDay -> DayOfWeek.ALL_DAYS
        else -> selectedDays
    }

/** What tapping a day does: store [days], or nothing at all. */
internal sealed interface RepeatDaysToggle {
    data class Changed(
        val days: Set<DayOfWeek>?,
    ) : RepeatDaysToggle

    /** Turning off the last day of an every-day habit, which would only mean every day again. */
    data object Refused : RepeatDaysToggle
}

/** A full or empty week is stored as `null`, as it always has been. */
internal fun toggledRepeatDays(
    selectedDays: Set<DayOfWeek>?,
    day: DayOfWeek,
    nullMeansEveryDay: Boolean,
): RepeatDaysToggle {
    val current = displayedRepeatDays(selectedDays, nullMeansEveryDay)
    val next = if (day in current) current - day else current + day
    return when {
        next.isEmpty() && nullMeansEveryDay -> RepeatDaysToggle.Refused
        next.isEmpty() || next.size == DayOfWeek.ALL_DAYS.size -> RepeatDaysToggle.Changed(null)
        else -> RepeatDaysToggle.Changed(next)
    }
}
