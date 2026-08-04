package com.mandrecode.tempo.features.focus.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.focus.presentation.FocusContract
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.model.UndatedTask
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Instant

/** Sub-pixel: these are laid-out edges, not values anyone typed, and they land a rounding off. */
private const val TOLERANCE = 0.5f

/** What the planning sheet shows, what its footer allows, and what a row survives when narrow. */
class PlanTasksSheetTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val today = LocalDate(2026, 8, 3)
    private val fixedClock =
        object : Clock {
            override fun now(): Instant = Instant.parse("2026-08-03T08:15:00Z")
        }

    private val work = Category(id = 1, name = "Work", icon = "work", color = "blue")
    private val home = Category(id = 2, name = "Home", icon = null, color = null)

    private fun row(
        id: Long,
        title: String,
        category: Category = work,
        plannedFor: LocalDate? = null,
    ) = UndatedTask(
        task =
            Task(
                id = id,
                title = title,
                description = "",
                categoryId = category.id,
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

    private fun setSheet(
        state: FocusContract.PlanSheetState,
        onEvent: (FocusContract.UiEvent) -> Unit = {},
    ) {
        composeTestRule.setContent {
            TempoTheme { PlanTasksSheet(state = state, onEvent = onEvent, clock = fixedClock) }
        }
    }

    /**
     * A single row, inline and pinned to [width].
     *
     * The sheet itself is a dialog and fills whatever window it is given, so a width constraint
     * around it would do nothing. The row is where the narrow-window risk actually lives.
     *
     * Required rather than merely asked for: a plain width is still coerced into the test device's
     * own, so anything wider than the phone this runs on would quietly be measured at phone width
     * and the wide layouts would never be reached.
     */
    private fun setRow(
        row: UndatedTask,
        width: Dp,
        onPlan: (Long, LocalDate?) -> Unit = { _, _ -> },
        onUnplan: (Long) -> Unit = {},
    ) {
        composeTestRule.setContent {
            TempoTheme {
                Box(modifier = Modifier.requiredWidth(width)) {
                    PlanTaskRow(
                        row = row,
                        today = today,
                        tomorrow = today.plus(1, DateTimeUnit.DAY),
                        onPlan = onPlan,
                        onUnplan = onUnplan,
                        onEdit = {},
                        onToggleCompletion = {},
                    )
                }
            }
        }
    }

    @Test
    fun sheet_listsEveryUndatedTaskWithItsCategory() {
        setSheet(sheetOf(row(1, "Renew the passport"), row(2, "Fix the shelf", home)))

        composeTestRule.onNodeWithText("Renew the passport").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fix the shelf").assertIsDisplayed()
        // The badge speaks as one label, so it is found by what it says, not by the name inside it.
        composeTestRule.onNodeWithContentDescription("Category: Work").assertIsDisplayed()
        // A category with no icon still names itself.
        composeTestRule.onNodeWithContentDescription("Category: Home").assertIsDisplayed()
    }

    @Test
    fun sheet_namesItselfAndCountsWhatIsLeft() {
        setSheet(sheetOf(row(1, "Renew the passport"), row(2, "Fix the shelf")))

        composeTestRule.onNodeWithText("Plan your tasks").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 still need a day").assertIsDisplayed()
    }

    /**
     * An empty list and a list that has not loaded look identical from the header, and only one of
     * them is worth congratulating anyone on.
     */
    @Test
    fun loadingSheet_claimsNothingAboutWorkItHasNotSeen() {
        setSheet(FocusContract.PlanSheetState(isLoading = true))

        composeTestRule.onNodeWithText("Plan your tasks").assertIsDisplayed()
        composeTestRule.onNodeWithText("Every one of them has a day now").assertDoesNotExist()
        composeTestRule.onNodeWithText("still need", substring = true).assertDoesNotExist()
    }

    @Test
    fun loadedEmptySheet_saysEverythingHasADay() {
        setSheet(sheetOf(row(1, "Renew the passport", plannedFor = today)))

        composeTestRule.onNodeWithText("Every one of them has a day now").assertIsDisplayed()
    }

    /** One question per row. Breaking a task down is a different job, and the editor still has it. */
    @Test
    fun sheet_doesNotOfferAddSubtask() {
        setSheet(sheetOf(row(1, "Renew the passport"), row(2, "Fix the shelf")))

        composeTestRule.onNodeWithContentDescription("Add Subtask").assertDoesNotExist()
    }

    @Test
    fun sheet_offersEveryQuickPlanChip() {
        setSheet(sheetOf(row(1, "Renew the passport")))

        composeTestRule.onNodeWithText("Today").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tomorrow").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pick a date").assertIsDisplayed()
    }

    @Test
    fun tappingToday_plansThatTaskForToday() {
        val planned = mutableListOf<Pair<Long, LocalDate?>>()
        setRow(row(7, "Renew the passport"), width = 360.dp, onPlan = { id, date -> planned += id to date })

        composeTestRule.onNodeWithText("Today").performClick()
        composeTestRule.waitForIdle()

        assertThat(planned).containsExactly(7L to today)
    }

    @Test
    fun tappingPickADate_asksForOneRatherThanChoosing() {
        val planned = mutableListOf<Pair<Long, LocalDate?>>()
        setRow(row(7, "Renew the passport"), width = 360.dp, onPlan = { id, date -> planned += id to date })

        composeTestRule.onNodeWithText("Pick a date").performClick()
        composeTestRule.waitForIdle()

        assertThat(planned).containsExactly(7L to null)
    }

    @Test
    fun noSectionHeaders_untilSomethingIsPlanned() {
        setSheet(sheetOf(row(1, "Renew the passport"), row(2, "Fix the shelf")))

        composeTestRule.onNodeWithText("Planned").assertDoesNotExist()
        composeTestRule.onNodeWithText("Unplanned").assertDoesNotExist()
    }

    @Test
    fun bothHeaders_onceOneTaskIsPlanned() {
        setSheet(sheetOf(row(1, "Renew the passport", plannedFor = today), row(2, "Fix the shelf")))

        composeTestRule.onNodeWithText("Planned").assertIsDisplayed()
        composeTestRule.onNodeWithText("Unplanned").assertIsDisplayed()
    }

    @Test
    fun onlyPlannedHeader_onceEverythingIsPlanned() {
        setSheet(
            sheetOf(
                row(1, "Renew the passport", plannedFor = today),
                row(2, "Fix the shelf", plannedFor = today),
            ),
        )

        composeTestRule.onNodeWithText("Planned").assertIsDisplayed()
        composeTestRule.onNodeWithText("Unplanned").assertDoesNotExist()
    }

    /**
     * One button, always available. A Cancel beside it would be lying — every chip has already
     * written — and there is no state in which the only way out is greyed out.
     */
    @Test
    fun done_isTheOnlyFooterActionAndIsAlwaysAvailable() {
        setSheet(sheetOf(row(1, "Renew the passport")))

        composeTestRule.onNodeWithText("Done").assertIsEnabled()
        composeTestRule.onNodeWithText("Close").assertDoesNotExist()
        composeTestRule.onNodeWithText("Cancel").assertDoesNotExist()
    }

    @Test
    fun done_isStillAvailableOnceSomethingIsPlanned() {
        setSheet(sheetOf(row(1, "Renew the passport", plannedFor = today)))

        composeTestRule.onNodeWithText("Done").assertIsEnabled()
    }

    @Test
    fun done_closesTheSheet() {
        val events = mutableListOf<FocusContract.UiEvent>()
        setSheet(sheetOf(row(1, "Renew the passport", plannedFor = today))) { events += it }

        composeTestRule.onNodeWithText("Done").performClick()
        composeTestRule.waitForIdle()

        assertThat(events).contains(FocusContract.UiEvent.ClosePlanSheet)
    }

    /** The chip that is already lit takes the date back off rather than setting it again. */
    @Test
    fun tappingTheSelectedChip_unplansTheTask() {
        val unplanned = mutableListOf<Long>()
        val planned = mutableListOf<Pair<Long, LocalDate?>>()
        setRow(
            row(7, "Renew the passport", plannedFor = today),
            width = 360.dp,
            onPlan = { id, date -> planned += id to date },
            onUnplan = { unplanned += it },
        )

        composeTestRule.onNodeWithText("Today").performClick()
        composeTestRule.waitForIdle()

        assertThat(unplanned).containsExactly(7L)
        assertThat(planned).isEmpty()
    }

    /** An unlit chip still plans — only the selected one reverses. */
    @Test
    fun tappingAnUnselectedChip_stillPlans() {
        val unplanned = mutableListOf<Long>()
        val planned = mutableListOf<Pair<Long, LocalDate?>>()
        setRow(
            row(7, "Renew the passport", plannedFor = today),
            width = 360.dp,
            onPlan = { id, date -> planned += id to date },
            onUnplan = { unplanned += it },
        )

        composeTestRule.onNodeWithText("Tomorrow").performClick()
        composeTestRule.waitForIdle()

        assertThat(planned).containsExactly(7L to today.plus(1, DateTimeUnit.DAY))
        assertThat(unplanned).isEmpty()
    }

    /**
     * Wide enough for the chips to sit beside the text, where they become a block of their own and
     * the picker spans the two days above it. Left to size itself it stopped a ragged distance
     * short of Tomorrow, and moved again the moment it was chosen and its label became a date.
     */
    @Test
    fun besideTheText_thePickerSpansBothDays() {
        setRow(row(1, "Renew the passport"), width = 640.dp)

        val today = composeTestRule.onNodeWithText("Today").getUnclippedBoundsInRoot()
        val tomorrow = composeTestRule.onNodeWithText("Tomorrow").getUnclippedBoundsInRoot()
        val picker = composeTestRule.onNodeWithText("Pick a date").getUnclippedBoundsInRoot()

        // Under the pair rather than beside them — the shape only holds if it wrapped.
        assertThat(picker.top.value).isAtLeast(today.bottom.value)
        assertThat(picker.left.value).isWithin(TOLERANCE).of(today.left.value)
        assertThat(picker.right.value).isWithin(TOLERANCE).of(tomorrow.right.value)
    }

    /**
     * The narrowest column the grid will ever hand a row, with a long title, a category badge and
     * a reminder badge all competing for it.
     */
    @Test
    fun row_survivesTheNarrowestColumn() {
        val width = 328.dp
        setRow(
            row(1, "Renew the passport before the September trip", plannedFor = today),
            width = width,
        )

        listOf("Today", "Tomorrow", "Pick a date").forEach { label ->
            val bounds = composeTestRule.onNodeWithText(label).getUnclippedBoundsInRoot()
            assertThat(bounds.left.value).isAtLeast(0f)
            assertThat(bounds.right.value).isAtMost(width.value)
        }

        val badge = composeTestRule.onNodeWithContentDescription("Category: Work").getUnclippedBoundsInRoot()
        assertThat(badge.left.value).isAtLeast(0f)
        assertThat(badge.right.value).isAtMost(width.value)
    }

    @Test
    fun row_survives360dpWithNoCategoryIcon() {
        setRow(row(1, "Fix the shelf in the spare room", category = home), width = 360.dp)

        val badge =
            composeTestRule.onNodeWithContentDescription("Category: Home").getUnclippedBoundsInRoot()
        assertThat(badge.right.value).isAtMost(360f)
        composeTestRule.onNodeWithText("Fix the shelf in the spare room").assertIsDisplayed()
    }
}
