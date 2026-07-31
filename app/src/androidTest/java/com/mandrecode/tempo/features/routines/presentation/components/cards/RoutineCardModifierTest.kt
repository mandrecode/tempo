package com.mandrecode.tempo.features.routines.presentation.components.cards

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Rule
import org.junit.Test

/**
 * Both cards took a `modifier` and dropped it, so a list handing one an item animation got nothing
 * back — which is why an expanding card in Focus teleported the rows below it instead of pushing
 * them. The parameter has to reach the root for the caller to be able to say anything at all.
 */
class RoutineCardModifierTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val createdDate = LocalDateTime(2024, 1, 1, 0, 0)
    private val selectedDate = LocalDate(2024, 6, 15)

    private companion object {
        const val ROOT_TAG = "card_root"
    }

    private fun habit(
        id: Long,
        title: String,
    ) = Habit(id = id, title = title, description = "", createdDate = createdDate)

    @Test
    fun habitCard_appliesTheCallersModifier() {
        composeTestRule.setContent {
            TempoTheme {
                HabitCard(
                    habit = habit(1, "Morning Run"),
                    selectedDate = selectedDate,
                    onEdit = {},
                    onDelete = {},
                    modifier = Modifier.testTag(ROOT_TAG),
                )
            }
        }

        composeTestRule.onNodeWithTag(ROOT_TAG).assertIsDisplayed()
    }

    @Test
    fun habitChainCard_appliesTheCallersModifier() {
        composeTestRule.setContent {
            TempoTheme {
                HabitChainCard(
                    habitChain =
                        HabitChain(
                            id = 1,
                            title = "Morning routine",
                            habitIds = listOf(1),
                            createdDate = createdDate,
                        ),
                    chainHabits = listOf(habit(1, "Stretch")),
                    selectedDate = selectedDate,
                    isExpanded = false,
                    onEdit = {},
                    onToggleExpansion = {},
                    modifier = Modifier.testTag(ROOT_TAG),
                )
            }
        }

        composeTestRule.onNodeWithTag(ROOT_TAG).assertIsDisplayed()
    }
}
