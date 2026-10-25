package com.mandrecode.tempo.features.routines.presentation.components.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Rule
import org.junit.Test

/** Habit rows are more compact, a chain always lists its habits, and both rows match (#454). */
class CompactHabitCardsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val createdDate = LocalDateTime(2024, 1, 1, 0, 0)
    private val selectedDate = LocalDate(2024, 6, 15)

    private fun habit(
        id: Long,
        title: String,
    ) = Habit(id = id, title = title, description = "", createdDate = createdDate)

    @Test
    fun habitCard_isShorterThanBefore() {
        composeTestRule.setContent {
            TempoTheme {
                HabitCard(
                    habit = habit(1, "Drink water"),
                    selectedDate = selectedDate,
                    onEdit = {},
                    onDelete = {},
                    showTimeline = false,
                    modifier = Modifier.testTag(ROOT_TAG),
                )
            }
        }

        // 48dp checkbox + 2 * 12dp padding = 72dp, plus up to 1dp of pixel rounding; the card
        // used to be 88dp.
        val height = composeTestRule.onNodeWithTag(ROOT_TAG).getUnclippedBoundsInRoot().height
        assertThat(height).isAtMost(73.dp)
    }

    @Test
    fun habitChainCard_listsItsHabitsWithoutExpanding() {
        composeTestRule.setContent {
            TempoTheme {
                HabitChainCard(
                    habitChain = chain(),
                    chainHabits = listOf(habit(1, "Stretch"), habit(2, "Meditate")),
                    selectedDate = selectedDate,
                    onEdit = {},
                    showTimeline = false,
                )
            }
        }

        composeTestRule.onNodeWithText("Morning routine").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stretch").assertIsDisplayed()
        composeTestRule.onNodeWithText("Meditate").assertIsDisplayed()
    }

    @Test
    fun habitInsideChain_isAsTallAsAStandaloneHabit() {
        composeTestRule.setContent {
            TempoTheme {
                Column {
                    HabitCard(
                        habit = habit(1, "Drink water"),
                        selectedDate = selectedDate,
                        onEdit = {},
                        onDelete = {},
                        showTimeline = false,
                    )
                    HabitChainCard(
                        habitChain = chain(),
                        chainHabits = listOf(habit(2, "Stretch")),
                        selectedDate = selectedDate,
                        onEdit = {},
                        showTimeline = false,
                    )
                }
            }
        }

        // Each row is clickable, so its text resolves to the whole row (or card).
        val standalone = composeTestRule.onNodeWithText("Drink water").getUnclippedBoundsInRoot().height
        val inChain = composeTestRule.onNodeWithText("Stretch").getUnclippedBoundsInRoot().height
        assertThat(inChain).isEqualTo(standalone)
    }

    @Test
    fun habitChainCard_addRowAddsAHabit() {
        var added = 0
        composeTestRule.setContent {
            TempoTheme {
                HabitChainCard(
                    habitChain = chain(),
                    chainHabits = listOf(habit(1, "Stretch")),
                    selectedDate = selectedDate,
                    onEdit = {},
                    onAddHabit = { added++ },
                    showTimeline = false,
                )
            }
        }

        composeTestRule.onNodeWithText(string(R.string.add_habit)).performClick()

        composeTestRule.runOnIdle { assertThat(added).isEqualTo(1) }
    }

    @Test
    fun habitChainCard_showsAllDoneOnceEveryHabitIsDone() {
        val done = { id: Long, title: String -> habit(id, title).copy(completionHistory = selectedDate.toString()) }
        composeTestRule.setContent {
            TempoTheme {
                HabitChainCard(
                    habitChain = chain(),
                    chainHabits = listOf(done(1, "Stretch"), done(2, "Meditate")),
                    selectedDate = selectedDate,
                    onEdit = {},
                    showTimeline = false,
                )
            }
        }

        composeTestRule.onNodeWithText(string(R.string.habit_chain_card_all_done, 2)).assertIsDisplayed()
    }

    private fun string(
        id: Int,
        vararg args: Any,
    ): String = InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)

    private fun chain() =
        HabitChain(
            id = 1,
            title = "Morning routine",
            habitIds = listOf(1, 2),
            createdDate = createdDate,
        )

    private companion object {
        const val ROOT_TAG = "card_root"
    }
}
