package com.mandrecode.tempo.core.ui.components

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import org.junit.Rule
import org.junit.Test

class TempoTopBarTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersTitleWithoutBadge_whenNoBadgeIsGiven() {
        composeTestRule.setContent {
            TempoTheme {
                TempoTopBar(title = "Routines")
            }
        }

        composeTestRule.onNodeWithText("Routines").assertIsDisplayed()
        composeTestRule.onNodeWithText("Paused").assertDoesNotExist()
    }

    @Test
    fun rendersBadgeNextToTitle_whenBadgeIsGiven() {
        composeTestRule.setContent {
            TempoTheme {
                TempoTopBar(
                    title = "Routines",
                    titleBadge = { Text(text = "Paused") },
                )
            }
        }

        composeTestRule.onNodeWithText("Routines").assertIsDisplayed()
        composeTestRule.onNodeWithText("Paused").assertIsDisplayed()
    }
}
