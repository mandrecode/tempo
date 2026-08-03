package com.mandrecode.tempo.features.focus.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs

/** What the day's rows do when tapped, and how Focus hands off to Tasks. */
class FocusContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val today = LocalDate(2024, 6, 15)
    private val createdDate = LocalDateTime(2024, 1, 1, 0, 0)

    private fun habit(
        id: Long,
        title: String,
    ) = Habit(id = id, title = title, description = "", createdDate = createdDate)

    private fun chainEntry(
        chainId: Long = 1,
        title: String = "Morning routine",
        habits: List<Habit> = listOf(habit(10, "Stretch")),
    ) = FocusAgendaItem.ChainEntry(
        chain =
            HabitChain(
                id = chainId,
                title = title,
                habitIds = habits.map { it.id },
                createdDate = createdDate,
            ),
        habits = habits,
        isCompleted = false,
    )

    private fun taskEntry(
        id: Long,
        title: String,
        subtasks: List<Task> = emptyList(),
        due: LocalDateTime? = null,
        parentTaskId: Long? = null,
    ) = FocusAgendaItem.TaskEntry(
        task =
            Task(
                id = id,
                title = title,
                description = "",
                reminderDate = due,
                parentTaskId = parentTaskId,
            ),
        subtasks = subtasks,
    )

    private fun stateWith(
        items: List<FocusAgendaItem> = emptyList(),
        overdueItems: List<FocusAgendaItem> = emptyList(),
        upNext: List<FocusAgendaItem.TaskEntry> = emptyList(),
        undatedTaskCount: Int = 0,
    ) = FocusContract.UiState(
        isLoading = false,
        today = today,
        todayItems = items.toPersistentList(),
        overdue = overdueItems.toPersistentList(),
        upNext = upNext.toPersistentList(),
        undatedTaskCount = undatedTaskCount,
    )

    private fun setContent(
        uiState: FocusContract.UiState,
        onEvent: (FocusContract.UiEvent) -> Unit,
    ) {
        composeTestRule.setContent {
            TempoTheme {
                FocusContent(uiState = uiState, onEvent = onEvent)
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun tappingChainCard_opensTheChain() {
        val events = mutableListOf<FocusContract.UiEvent>()
        setContent(stateWith(items = listOf(chainEntry()))) { events += it }

        composeTestRule.onNodeWithText("Morning routine").performClick()
        composeTestRule.waitForIdle()

        assertThat(events.filterIsInstance<FocusContract.UiEvent.EditChain>().map { it.chain.id })
            .containsExactly(1L)
    }

    @Test
    fun tappingChevron_expandsWithoutOpeningTheChain() {
        val events = mutableListOf<FocusContract.UiEvent>()
        setContent(stateWith(items = listOf(chainEntry()))) { events += it }

        composeTestRule.onNodeWithContentDescription("Expand chain", substring = true).performClick()
        composeTestRule.waitForIdle()

        assertThat(events.filterIsInstance<FocusContract.UiEvent.ToggleChainExpanded>().map { it.chainId })
            .containsExactly(1L)
        assertThat(events.filterIsInstance<FocusContract.UiEvent.EditChain>()).isEmpty()
    }

    /**
     * Today above Overdue. Both are the day, but only one of them is the day you are in, and
     * opening Focus onto last week's leftovers put the work you came for below the fold.
     */
    @Test
    fun todaySection_readsAboveOverdue() {
        val state =
            stateWith(
                items = listOf(taskEntry(1, "Write the report")),
                overdueItems = listOf(taskEntry(2, "Chase the invoice")),
            )
        setContent(state) { }

        val todayHeader = composeTestRule.onNodeWithText("Today · 1").getUnclippedBoundsInRoot().top
        val overdueHeader = composeTestRule.onNodeWithText("Overdue · 1").getUnclippedBoundsInRoot().top

        assertThat(todayHeader.value).isLessThan(overdueHeader.value)
        assertThat(rowTop("Write the report").value).isLessThan(rowTop("Chase the invoice").value)
    }

    /**
     * The row mixes today's work with what came before it, so a bare "8:00 AM" on last week's task
     * read as something due this morning — the one thing the time was there to tell you.
     */
    @Test
    fun upNextCard_saysOverdueInsteadOfShowingLastWeeksTime() {
        // The card is the only thing on screen carrying a time, so "8:00" can only come from it.
        val stale = taskEntry(1, "Chase the invoice", due = LocalDateTime(2024, 6, 8, 8, 0))
        val filler = taskEntry(2, "Something else today")
        setContent(stateWith(items = listOf(filler), upNext = listOf(stale))) { }

        composeTestRule.onNodeWithText("OVERDUE", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("8:00", substring = true).assertDoesNotExist()
    }

    @Test
    fun upNextCard_keepsTheTimeForWorkDueToday() {
        val dueToday = taskEntry(1, "Write the report", due = LocalDateTime(2024, 6, 15, 9, 0))
        val filler = taskEntry(2, "Something else today")
        setContent(stateWith(items = listOf(filler), upNext = listOf(dueToday))) { }

        composeTestRule.onNodeWithText("9:00", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("OVERDUE", substring = true).assertDoesNotExist()
    }

    /**
     * A step with a time on it under a parent that has none is a row like any other here — no
     * breadcrumb, no indentation, and something a session can be started on.
     */
    @Test
    fun promotedSubtask_rendersAsAnOrdinaryRow() {
        setContent(stateWith(items = listOf(taskEntry(2, "Call the bank")))) { }

        composeTestRule.onNodeWithText("Call the bank").assertIsDisplayed()
    }

    /**
     * The one thing a promoted subtask's card must not offer. Tasks nests exactly one level, so a
     * task added beneath a subtask exists with nowhere in Tasks to show it — reachable only from
     * Focus, and only while its grandparent stays off the day.
     */
    @Test
    fun promotedSubtask_doesNotOfferToAddASubtaskBeneathItself() {
        val promoted = taskEntry(2, "Call the bank", parentTaskId = 1)
        setContent(stateWith(items = listOf(promoted))) { }

        // The row has to be there for its missing button to mean anything — without this the
        // assertion below would pass just as well on a row that never rendered.
        composeTestRule.onNodeWithText("Call the bank").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add Subtask").assertDoesNotExist()
    }

    @Test
    fun topLevelRow_stillOffersToAddASubtask() {
        setContent(stateWith(items = listOf(taskEntry(1, "Write the report")))) { }

        composeTestRule.onNodeWithContentDescription("Add Subtask").assertIsDisplayed()
    }

    @Test
    fun undatedFooter_namesItsDestinationAndHandsOff() {
        val events = mutableListOf<FocusContract.UiEvent>()
        setContent(stateWith(items = listOf(chainEntry()), undatedTaskCount = 3)) { events += it }

        composeTestRule.onNodeWithText("3 tasks without a date").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 tasks without a date").performClick()
        composeTestRule.waitForIdle()

        assertThat(events).contains(FocusContract.UiEvent.UndatedTasksClicked)
    }

    @Test
    fun undatedFooter_isAbsentWhenNothingIsUndated() {
        setContent(stateWith(items = listOf(chainEntry()), undatedTaskCount = 0)) { }

        composeTestRule.onNodeWithText("without a date", substring = true).assertDoesNotExist()
    }

    /** The chain's steps read down in the order the chain runs them, as they do in Routines. */
    @Test
    fun expandedChain_listsItsHabitsInTheChainsOrder() {
        val habits = listOf(habit(3, "Shower"), habit(1, "Stretch"), habit(2, "Water"))
        val entry = chainEntry(habits = habits)
        val state =
            stateWith(items = listOf(entry)).copy(
                expandedChainIds = persistentListOf(entry.chain.id),
            )
        setContent(state) { }

        val tops =
            habits.map { habit ->
                composeTestRule
                    .onNodeWithText(habit.title)
                    .getUnclippedBoundsInRoot()
                    .top
            }

        assertThat(tops).isInOrder()
    }

    /**
     * A card growing has to push the ones below it, not teleport them. Routines and Tasks both
     * animate their rows into place; the Focus agenda did not, so expanding a chain relocated
     * everything under it in a single frame.
     */
    @Test
    fun expandingAChain_slidesTheRowsBelowItRatherThanJumpingThem() {
        val chain = chainEntry(habits = listOf(habit(1, "Stretch"), habit(2, "Water")))
        val below =
            FocusAgendaItem.HabitEntry(habit = habit(9, "Zulu"), isCompleted = false)
        var expanded by mutableStateOf(persistentListOf<Long>())

        composeTestRule.setContent {
            TempoTheme {
                FocusContent(
                    uiState =
                        stateWith(items = listOf(chain, below)).copy(expandedChainIds = expanded),
                    onEvent = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.mainClock.autoAdvance = false
        val start = rowTop("Zulu")
        expanded = persistentListOf(chain.chain.id)
        composeTestRule.mainClock.advanceTimeByFrame()

        var previous = start
        var largestStep = 0f
        var steps = 0
        repeat(EXPAND_FRAMES) {
            composeTestRule.mainClock.advanceTimeBy(FRAME_MS)
            val here = rowTop("Zulu")
            val step = abs((here - previous).value)
            if (step > 0.5f) steps++
            largestStep = maxOf(largestStep, step)
            previous = here
        }

        val travelled = abs((previous - start).value)
        // It has to have actually moved, or the assertion below would pass on a still list.
        assertThat(travelled).isGreaterThan(MIN_TRAVEL_DP)
        // Spread over many frames rather than taken in one or two.
        assertThat(steps).isGreaterThan(MIN_STEPS)
        // No single frame carries an outsized share of the distance.
        assertThat(largestStep).isLessThan(travelled * MAX_STEP_FRACTION)
    }

    private fun rowTop(text: String) = composeTestRule.onNodeWithText(text).getUnclippedBoundsInRoot().top

    /**
     * The empty day sits above the middle, as Tasks' and Routines' empty states do — dead centre
     * was the one thing marking Focus out as a different screen.
     */
    @Test
    fun emptyDay_sitsAboveTheMiddleLikeTheOtherTabs() {
        setContent(stateWith(items = emptyList())) { }

        val root = composeTestRule.onRoot().getUnclippedBoundsInRoot()
        val title = composeTestRule.onNodeWithText("A clear day", substring = true).getUnclippedBoundsInRoot()
        val titleCentre = ((title.top + title.bottom) / 2f).value

        assertThat(titleCentre).isLessThan(((root.bottom - root.top) / 2f).value)
    }

    /**
     * The invitation is one line. Two lines under a one-line headline read as a paragraph, which
     * is not what the other tabs' empty states look like.
     *
     * Measured inside a fixed-width box rather than at whatever the running device happens to be:
     * a first version asserted against the device's own width, passed on a 411dp phone, and failed
     * on CI's narrower emulator. [SmallPhoneWidth] is the width this has to survive.
     */
    @Test
    fun emptyDay_invitationFitsOnOneLineOnASmallPhone() {
        composeTestRule.setContent {
            TempoTheme {
                Box(modifier = Modifier.width(SmallPhoneWidth)) {
                    FocusContent(uiState = stateWith(items = emptyList()), onEvent = {})
                }
            }
        }
        composeTestRule.waitForIdle()

        val message =
            composeTestRule
                .onNodeWithText("Plan a task or routine", substring = true)
                .getUnclippedBoundsInRoot()

        assertThat((message.bottom - message.top).value).isLessThan(SINGLE_LINE_MAX_DP)
    }

    @Test
    fun emptyDay_stillOffersTheHandOff() {
        val events = mutableListOf<FocusContract.UiEvent>()
        setContent(stateWith(items = emptyList(), undatedTaskCount = 1)) { events += it }

        composeTestRule.onNodeWithText("1 task without a date").performClick()
        composeTestRule.waitForIdle()

        assertThat(events).contains(FocusContract.UiEvent.UndatedTasksClicked)
    }
}

/** One frame at 60 Hz, in milliseconds. */
private const val FRAME_MS = 16L

/** Long enough for the expansion spring to run out. */
private const val EXPAND_FRAMES = 60

/** Below this the row never really moved, and the spread assertions would be vacuous. */
private const val MIN_TRAVEL_DP = 40f

/** A single-frame relocation shows up as one step; a spring takes many. */
private const val MIN_STEPS = 4

/** No frame may carry this much of the journey. A teleport carries all of it. */
private const val MAX_STEP_FRACTION = 0.6f

/** One line of bodyMedium is about 20dp; two lines clear 40dp. */
private const val SINGLE_LINE_MAX_DP = 30f

/** The narrowest phone worth supporting; CI's emulator sits around here. */
private val SmallPhoneWidth = 320.dp
