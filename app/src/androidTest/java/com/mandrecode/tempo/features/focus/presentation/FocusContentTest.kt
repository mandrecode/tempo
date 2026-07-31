package com.mandrecode.tempo.features.focus.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
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

    private fun stateWith(
        items: List<FocusAgendaItem> = emptyList(),
        undatedTaskCount: Int = 0,
    ) = FocusContract.UiState(
        isLoading = false,
        today = today,
        todayItems = items.toPersistentList(),
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
     */
    @Test
    fun emptyDay_invitationFitsOnOneLine() {
        setContent(stateWith(items = emptyList())) { }

        val message =
            composeTestRule
                .onNodeWithText("Plan a task or routine", substring = true)
                .getUnclippedBoundsInRoot()

        assertThat((message.bottom - message.top).value).isLessThan(SINGLE_LINE_MAX_DP)
    }

    /**
     * The icon is what tells a sighted user the footer leaves for Tasks, and TalkBack does not see
     * icons. Without a description of its own the button announced only the count.
     */
    @Test
    fun undatedFooter_tellsAScreenReaderWhereItGoes() {
        setContent(stateWith(items = listOf(chainEntry()), undatedTaskCount = 3)) { }

        // The label still reads as the count, and the destination is spoken alongside it.
        composeTestRule.onNodeWithText("3 tasks without a date").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Open in Tasks").assertExists()
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
