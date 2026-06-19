package com.mandrecode.tempo.features.routines.presentation.components.sections

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.routines.domain.model.HabitType
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

private val PREVIEW_TODAY = LocalDate(2026, 3, 26)
private val PREVIEW_FULL_WINDOW_CREATED_DATE = PREVIEW_TODAY.minus(DatePeriod(days = 20))

// region Full History

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitHistoryView_FullHistory() {
    TempoTheme {
        HabitHistoryView(
            completionHistory =
                "2026-03-01,2026-03-02,2026-03-05,2026-03-10,2026-03-15," +
                    "2026-03-20,2026-03-22,2026-03-24,2026-03-25,2026-03-26",
            createdDate = LocalDate(2026, 2, 1),
            modifier = Modifier.fillMaxWidth(),
            today = PREVIEW_TODAY,
        )
    }
}

// endregion

// region With Streak

@Preview(name = "Light – Streak", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark – Streak",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitHistoryView_WithStreak() {
    TempoTheme {
        HabitHistoryView(
            completionHistory = "2026-03-23,2026-03-24,2026-03-25,2026-03-26",
            createdDate = LocalDate(2026, 3, 20),
            modifier = Modifier.fillMaxWidth(),
            today = PREVIEW_TODAY,
        )
    }
}

// endregion

// region Created Today

@Preview(name = "Light – New Habit", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark – New Habit",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitHistoryView_CreatedToday() {
    TempoTheme {
        HabitHistoryView(
            completionHistory = "",
            createdDate = PREVIEW_TODAY,
            modifier = Modifier.fillMaxWidth(),
            today = PREVIEW_TODAY,
        )
    }
}

// endregion

// region Quit – With Streak

@Preview(name = "Light – Quit Streak", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark – Quit Streak",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitHistoryView_QuitWithStreak() {
    TempoTheme {
        HabitHistoryView(
            completionHistory = "2026-03-23,2026-03-24,2026-03-25,2026-03-26",
            createdDate = LocalDate(2026, 3, 20),
            modifier = Modifier.fillMaxWidth(),
            habitType = HabitType.QUIT,
            today = PREVIEW_TODAY,
        )
    }
}

// endregion

// region Quit – No Streak

@Preview(name = "Light – Quit No Streak", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark – Quit No Streak",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitHistoryView_QuitNoStreak() {
    TempoTheme {
        HabitHistoryView(
            completionHistory = "2026-03-10,2026-03-15",
            createdDate = LocalDate(2026, 3, 1),
            modifier = Modifier.fillMaxWidth(),
            habitType = HabitType.QUIT,
            today = PREVIEW_TODAY,
        )
    }
}

// endregion

// region Streak Tiers (#547)

/**
 * Helper to build a contiguous completion-history string ending on [PREVIEW_TODAY]
 * spanning [days] consecutive days.
 */
private fun buildStreak(days: Int): String =
    (0 until days)
        .map { LocalDate.fromEpochDays((PREVIEW_TODAY.toEpochDays() - it).toInt()) }
        .joinToString(",")

private fun buildFullWindowHistoryWithoutToday(): String = buildHistoryForDaysAgo(20 downTo 1)

private fun buildFullWindowHistoryWithToday(): String = buildStreak(21)

private fun buildHistoryForDaysAgo(daysAgoRange: IntProgression): String {
    val todayEpochDays = PREVIEW_TODAY.toEpochDays()
    return daysAgoRange.joinToString(",") { daysAgo ->
        LocalDate.fromEpochDays((todayEpochDays - daysAgo).toInt()).toString()
    }
}

@Preview(name = "Light – Tier Yellow (7d)", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark – Tier Yellow (7d)",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitHistoryView_TierYellow() {
    TempoTheme {
        HabitHistoryView(
            completionHistory = buildStreak(7),
            createdDate = PREVIEW_TODAY.minus(DatePeriod(days = 7)),
            modifier = Modifier.fillMaxWidth(),
            today = PREVIEW_TODAY,
        )
    }
}

@Preview(name = "Light – Tier Green (14d)", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark – Tier Green (14d)",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitHistoryView_TierGreen() {
    TempoTheme {
        HabitHistoryView(
            completionHistory = buildStreak(14),
            createdDate = PREVIEW_TODAY.minus(DatePeriod(days = 14)),
            modifier = Modifier.fillMaxWidth(),
            today = PREVIEW_TODAY,
        )
    }
}

@Preview(name = "Light – Tier Purple (21d)", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark – Tier Purple (21d)",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitHistoryView_TierPurple() {
    TempoTheme {
        HabitHistoryView(
            completionHistory = buildStreak(21),
            createdDate = PREVIEW_TODAY.minus(DatePeriod(days = 21)),
            modifier = Modifier.fillMaxWidth(),
            today = PREVIEW_TODAY,
        )
    }
}

// endregion

// region With Repeat Days

@Preview(name = "Light – Repeat Days", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark – Repeat Days",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitHistoryView_WithRepeatDays() {
    TempoTheme {
        HabitHistoryView(
            completionHistory = "2026-03-20,2026-03-23,2026-03-25",
            createdDate = LocalDate(2026, 3, 1),
            modifier = Modifier.fillMaxWidth(),
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            today = PREVIEW_TODAY,
        )
    }
}

@Preview(name = "Light – MWF Perfect Adherence", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark – MWF Perfect Adherence",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitHistoryView_MWFPerfectAdherence() {
    // PREVIEW_TODAY = 2026-03-26 is a Thursday. The Mondays/Wednesdays/Fridays in
    // the visible 21-day window are completed; Tue/Thu/Sat/Sun should render muted.
    TempoTheme {
        HabitHistoryView(
            completionHistory =
                "2026-03-09,2026-03-11,2026-03-13," +
                    "2026-03-16,2026-03-18,2026-03-20," +
                    "2026-03-23,2026-03-25",
            createdDate = LocalDate(2026, 3, 1),
            modifier = Modifier.fillMaxWidth(),
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            today = PREVIEW_TODAY,
        )
    }
}

@Preview(name = "Light – Weekly Mon", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark – Weekly Mon",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitHistoryView_WeeklyMonday() {
    // Mondays in the 21-day window ending 2026-03-26 (Thu): 09, 16, 23.
    TempoTheme {
        HabitHistoryView(
            completionHistory = "2026-03-09,2026-03-16,2026-03-23",
            createdDate = LocalDate(2026, 3, 1),
            modifier = Modifier.fillMaxWidth(),
            repeatDays = setOf(DayOfWeek.MONDAY),
            today = PREVIEW_TODAY,
        )
    }
}

// endregion

// region Bottom Sheet Row Context (#681)

/**
 * Reproduces the row layout used inside `HabitBottomSheet` (icon Box + weighted Column +
 * `HabitHistoryView`) so that the streak pill's horizontal end-alignment is easy to verify
 * visually. Regression guard for #681 — the pill must be flush with the row's right edge.
 */
@Preview(name = "Light – Sheet Row Context", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark – Sheet Row Context",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitHistoryView_BottomSheetRowContext() {
    BottomSheetRowHistoryPreview(
        completionHistory = buildStreak(7),
        createdDate = PREVIEW_TODAY.minus(DatePeriod(days = 7)),
    )
}

@Composable
private fun BottomSheetRowHistoryPreview(
    completionHistory: String,
    createdDate: LocalDate,
) {
    TempoTheme {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.width(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_calendar),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                HabitHistoryView(
                    completionHistory = completionHistory,
                    createdDate = createdDate,
                    modifier = Modifier.fillMaxWidth(),
                    today = PREVIEW_TODAY,
                )
            }
        }
    }
}

// endregion

// region Full-capacity toggle regression (#687)

/**
 * Regression guard for #687:
 * in a full 21-day window, toggling completion should not visually shift the
 * newest dot due to streak-label width transitions.
 */
@Preview(name = "Light – Full Capacity Before Toggle", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark – Full Capacity Before Toggle",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitHistoryView_FullCapacityBeforeToggle() {
    BottomSheetRowHistoryPreview(
        completionHistory = buildFullWindowHistoryWithoutToday(),
        createdDate = PREVIEW_FULL_WINDOW_CREATED_DATE,
    )
}

@Preview(name = "Light – Full Capacity After Toggle", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark – Full Capacity After Toggle",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitHistoryView_FullCapacityAfterToggle() {
    BottomSheetRowHistoryPreview(
        completionHistory = buildFullWindowHistoryWithToday(),
        createdDate = PREVIEW_FULL_WINDOW_CREATED_DATE,
    )
}

// endregion
