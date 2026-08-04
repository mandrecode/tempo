package com.mandrecode.tempo.features.focus.presentation.components

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.focus.presentation.FocusContract
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.model.UndatedTask
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.time.Clock
import kotlin.time.Instant

private val today = LocalDate(2026, 8, 3)

/** A clock pinned to [today] so the Today/Tomorrow chips read the same in every rendering. */
private val previewClock =
    object : Clock {
        override fun now(): Instant = Instant.parse("2026-08-03T08:15:00Z")
    }

// Colors are `ColorOption.labelKey` values, because that is what `resolveColor()` matches on.
// Bare names like "blue" resolve to nothing and fall back silently to no color at all.
private val work = Category(id = 1, name = "Work", icon = "work", color = "color_m3_blue")
private val home = Category(id = 2, name = "Home", icon = "home", color = "color_m3_green")
private val errands = Category(id = 3, name = "Errands", icon = null, color = null)

private fun row(
    id: Long,
    title: String,
    category: Category,
    description: String = "",
    priority: Priority? = null,
    plannedFor: LocalDate? = null,
) = UndatedTask(
    task =
        Task(
            id = id,
            title = title,
            description = description,
            categoryId = category.id,
            priority = priority,
            reminderDate = plannedFor?.let { LocalDateTime(it, LocalTime(9, 0)) },
        ),
    category = category,
)

private fun sheetOf(vararg rows: UndatedTask) =
    FocusContract.PlanSheetState(
        rows = rows.toList().toPersistentList(),
        originalReminders = rows.associate { it.task.id to null }.toPersistentMap(),
        isLoading = false,
    )

private val nothingPlanned =
    sheetOf(
        row(1, "Renew the passport", work, description = "Book the appointment first"),
        row(2, "Fix the shelf", home, priority = Priority.LOW),
        row(3, "Return the parcel", errands),
    )

private val partlyPlanned =
    sheetOf(
        row(1, "Renew the passport", work, description = "Book the appointment first"),
        row(2, "Fix the shelf", home, priority = Priority.LOW, plannedFor = today),
        row(3, "Return the parcel", errands),
    )

private val everythingPlanned =
    sheetOf(
        row(1, "Renew the passport", work, plannedFor = today),
        row(2, "Fix the shelf", home, plannedFor = today),
        row(3, "Return the parcel", errands, plannedFor = today),
    )

private val loading =
    FocusContract.PlanSheetState(originalReminders = persistentMapOf(), isLoading = true)

@Preview(name = "Plan - nothing planned yet", showBackground = true, device = "id:pixel_9")
@Composable
private fun PlanTasksSheetUnplannedPreview() {
    TempoTheme { PlanTasksSheet(state = nothingPlanned, onEvent = {}, clock = previewClock) }
}

@Preview(
    name = "Plan - nothing planned yet, dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PlanTasksSheetUnplannedDarkPreview() {
    TempoTheme(darkTheme = true) {
        PlanTasksSheet(state = nothingPlanned, onEvent = {}, clock = previewClock)
    }
}

/** The split the sheet exists to show: one task moved, the rest still waiting. */
@Preview(name = "Plan - partly planned", showBackground = true, device = "id:pixel_9")
@Composable
private fun PlanTasksSheetPartlyPlannedPreview() {
    TempoTheme { PlanTasksSheet(state = partlyPlanned, onEvent = {}, clock = previewClock) }
}

@Preview(
    name = "Plan - partly planned, dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PlanTasksSheetPartlyPlannedDarkPreview() {
    TempoTheme(darkTheme = true) {
        PlanTasksSheet(state = partlyPlanned, onEvent = {}, clock = previewClock)
    }
}

@Preview(name = "Plan - everything planned", showBackground = true, device = "id:pixel_9")
@Composable
private fun PlanTasksSheetAllPlannedPreview() {
    TempoTheme { PlanTasksSheet(state = everythingPlanned, onEvent = {}, clock = previewClock) }
}

@Preview(name = "Plan - loading", showBackground = true, device = "id:pixel_9")
@Composable
private fun PlanTasksSheetLoadingPreview() {
    TempoTheme { PlanTasksSheet(state = loading, onEvent = {}, clock = previewClock) }
}

/** The narrow case the category badge and the chip row have to survive. */
@Preview(name = "Plan - 360dp", showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun PlanTasksSheetCompactPreview() {
    TempoTheme { PlanTasksSheet(state = partlyPlanned, onEvent = {}, clock = previewClock) }
}

/** Wide enough for the docked pane, where the sheet stops being a bottom sheet. */
@Preview(name = "Plan - expanded", showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
private fun PlanTasksSheetExpandedPreview() {
    TempoTheme { PlanTasksSheet(state = partlyPlanned, onEvent = {}, clock = previewClock) }
}
