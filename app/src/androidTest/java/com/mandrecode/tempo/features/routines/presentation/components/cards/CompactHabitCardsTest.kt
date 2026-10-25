package com.mandrecode.tempo.features.routines.presentation.components.cards

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Rule
import org.junit.Test

/** Habit rows are compact and a chain always lists its habits (#454). */
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
    fun habitCard_isShorterThanATaskRow() {
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

        // 40dp checkbox + 2 * 8dp padding; the old card was 88dp.
        val height = composeTestRule.onNodeWithTag(ROOT_TAG).getUnclippedBoundsInRoot().height
        assertThat(height).isAtMost(56.dp)
    }

    @Test
    fun habitChainCard_listsItsHabitsWithoutExpanding() {
        composeTestRule.setContent {
            TempoTheme {
                HabitChainCard(
                    habitChain =
                        HabitChain(
                            id = 1,
                            title = "Morning routine",
                            habitIds = listOf(1, 2),
                            createdDate = createdDate,
                        ),
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

    private companion object {
        const val ROOT_TAG = "card_root"
    }
}
