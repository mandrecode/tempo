package com.mandrecode.tempo.screenshots

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.mandrecode.tempo.core.domain.model.DailyFocusActivity
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import com.mandrecode.tempo.features.focus.presentation.FocusContent
import com.mandrecode.tempo.features.focus.presentation.FocusContract
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus

// Fixtures are deliberately duplicated from `src/debug`'s FocusContentPreviews rather than
// shared with it: those previews are a scratchpad developers retune freely, and every tweak
// there would rewrite committed reference images. A screenshot test's input has to be pinned.
private val today = LocalDate(2026, 7, 29)

private fun task(
    id: Long,
    title: String,
    hour: Int?,
    priority: Priority? = null,
    isCompleted: Boolean = false,
    date: LocalDate = today,
) = Task(
    id = id,
    title = title,
    description = "",
    isCompleted = isCompleted,
    priority = priority,
    reminderDate = hour?.let { LocalDateTime(date, LocalTime(it, 0)) },
)

private fun habit(
    id: Long,
    title: String,
) = Habit(
    id = id,
    title = title,
    description = "",
    createdDate = LocalDateTime(today, LocalTime(0, 0)),
)

private fun history(vararg pairs: Pair<Int, Int>) =
    pairs
        .mapIndexed { index, (scheduled, completed) ->
            DailyFocusActivity(
                date = today.minus(pairs.size - 1 - index, DateTimeUnit.DAY),
                scheduledCount = scheduled,
                completedCount = completed,
            )
        }.let { persistentListOf(*it.toTypedArray()) }

private val midDayState =
    FocusContract.UiState(
        isLoading = false,
        today = today,
        streakDays = 14,
        history = history(3 to 3, 2 to 1, 0 to 0, 4 to 4, 2 to 2, 3 to 1, 9 to 5),
        scheduledCount = 9,
        completedCount = 5,
        upNext =
            persistentListOf(
                FocusAgendaItem.TaskEntry(
                    task(1, "Finish Q3 budget report", hour = 9, priority = Priority.HIGH),
                ),
                FocusAgendaItem.TaskEntry(
                    task(2, "Reply to the design review", hour = 11, priority = Priority.MEDIUM),
                ),
            ),
        overdue =
            persistentListOf(
                FocusAgendaItem.TaskEntry(
                    task(2, "Renew gym membership", hour = 10, date = today.minus(2, DateTimeUnit.DAY)),
                ),
            ),
        todayItems =
            persistentListOf(
                FocusAgendaItem.TaskEntry(
                    task(1, "Finish Q3 budget report", hour = 9, priority = Priority.HIGH),
                ),
                FocusAgendaItem.TaskEntry(task(3, "Reply to client emails", hour = 12)),
                FocusAgendaItem.HabitEntry(habit(1, "Drink Water"), isCompleted = true),
            ),
        undatedTaskCount = 12,
    )

private val emptyDayState =
    FocusContract.UiState(
        isLoading = false,
        today = today,
        streakDays = 0,
        history = history(0 to 0, 0 to 0, 0 to 0, 0 to 0, 0 to 0, 0 to 0, 0 to 0),
        undatedTaskCount = 3,
    )

/**
 * A populated Focus day. Covers the hero, the streak strip, and both agenda sections, and pins
 * the bottom clearance `FocusContent` asks `floatingNavigationBottomClearancePadding()` for —
 * which is itself window-width dependent, so it differs across the three specs.
 */
@PreviewTest
@PreviewAdaptiveFormFactors
@Composable
private fun FocusContentMidDay() {
    ScreenshotTheme { FocusContent(uiState = midDayState, onEvent = {}) }
}

/** Nothing scheduled. The empty state centres itself, so it is the width-sensitive one. */
@PreviewTest
@PreviewAdaptiveFormFactors
@Composable
private fun FocusContentEmptyDay() {
    ScreenshotTheme { FocusContent(uiState = emptyDayState, onEvent = {}) }
}
