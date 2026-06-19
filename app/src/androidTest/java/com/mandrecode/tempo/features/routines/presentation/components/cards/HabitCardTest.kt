package com.mandrecode.tempo.features.routines.presentation.components.cards

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.routines.domain.model.Habit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Rule
import org.junit.Test

class HabitCardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val createdDate = LocalDateTime(2024, 1, 1, 0, 0)
    private val selectedDate = LocalDate(2024, 6, 15)

    @Test
    fun displaysHabitTitle() {
        val habit =
            Habit(
                id = 1,
                title = "Morning Run",
                description = "5km",
                createdDate = createdDate,
            )

        composeTestRule.setContent {
            TempoTheme {
                HabitCard(
                    habit = habit,
                    selectedDate = selectedDate,
                    onEdit = {},
                    onDelete = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Morning Run").assertIsDisplayed()
    }

    @Test
    fun displaysHabitDescription() {
        val habit =
            Habit(
                id = 1,
                title = "Read",
                description = "30 pages daily",
                createdDate = createdDate,
            )

        composeTestRule.setContent {
            TempoTheme {
                HabitCard(
                    habit = habit,
                    selectedDate = selectedDate,
                    onEdit = {},
                    onDelete = {},
                )
            }
        }

        composeTestRule.onNodeWithText("30 pages daily").assertIsDisplayed()
    }

    @Test
    fun displaysTimeLabel_whenProvided() {
        val habit =
            Habit(
                id = 1,
                title = "Meditate",
                description = "",
                createdDate = createdDate,
            )

        composeTestRule.setContent {
            TempoTheme {
                HabitCard(
                    habit = habit,
                    selectedDate = selectedDate,
                    onEdit = {},
                    onDelete = {},
                    timeLabel = "08:00",
                )
            }
        }

        composeTestRule.onNodeWithText("08:00").assertIsDisplayed()
    }

    @Test
    fun displaysHabitIcon_whenProvided() {
        val habit =
            Habit(
                id = 1,
                title = "Exercise",
                description = "",
                icon = "fitness",
                createdDate = createdDate,
            )

        composeTestRule.setContent {
            TempoTheme {
                HabitCard(
                    habit = habit,
                    selectedDate = selectedDate,
                    onEdit = {},
                    onDelete = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("habitIcon", useUnmergedTree = true).assertExists()
    }
}
