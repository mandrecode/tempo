package com.mandrecode.tempo.features.focus.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Rule
import org.junit.Test

/** What the day's rows do when tapped, and how Focus hands off to Tasks. */
class FocusContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val today = LocalDate(2024, 6, 15)
    private val createdDate = LocalDateTime(2024, 1, 1, 0, 0)

    private fun chainEntry(
        chainId: Long = 1,
        title: String = "Morning routine",
    ): FocusAgendaItem.ChainEntry {
        val habit =
            Habit(
                id = 10,
                title = "Stretch",
                description = "",
                createdDate = createdDate,
            )
        return FocusAgendaItem.ChainEntry(
            chain =
                HabitChain(
                    id = chainId,
                    title = title,
                    habitIds = listOf(habit.id),
                    createdDate = createdDate,
                ),
            habits = listOf(habit),
            isCompleted = false,
        )
    }

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

    @Test
    fun emptyDay_stillOffersTheHandOff() {
        val events = mutableListOf<FocusContract.UiEvent>()
        setContent(stateWith(items = emptyList(), undatedTaskCount = 1)) { events += it }

        composeTestRule.onNodeWithText("1 task without a date").performClick()
        composeTestRule.waitForIdle()

        assertThat(events).contains(FocusContract.UiEvent.UndatedTasksClicked)
    }
}
