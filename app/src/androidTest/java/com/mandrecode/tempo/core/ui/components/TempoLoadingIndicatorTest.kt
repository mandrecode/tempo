package com.mandrecode.tempo.core.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TempoLoadingIndicatorTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun pauseClock() {
        composeTestRule.mainClock.autoAdvance = false
    }

    /** A load that finishes inside the grace period must leave nothing behind to flash (#438). */
    @Test
    fun staysHidden_untilTheRevealDelayHasPassed() {
        composeTestRule.setContent {
            TempoTheme { TempoLoadingIndicator(message = "Loading tasks") }
        }

        composeTestRule.mainClock.advanceTimeBy(LOADING_INDICATOR_REVEAL_DELAY_MILLIS - 100)
        composeTestRule.onNodeWithContentDescription("Loading tasks").assertDoesNotExist()
    }

    @Test
    fun appears_onceTheLoadOutlastsTheRevealDelay() {
        composeTestRule.setContent {
            TempoTheme { TempoLoadingIndicator(message = "Loading tasks") }
        }

        composeTestRule.mainClock.advanceTimeBy(LOADING_INDICATOR_REVEAL_DELAY_MILLIS + 500)
        composeTestRule.onNodeWithContentDescription("Loading tasks").assertIsDisplayed()
    }

    @Test
    fun appearsImmediately_whenNoDelayIsAsked() {
        composeTestRule.setContent {
            TempoTheme { TempoLoadingIndicator(message = "Loading tasks", revealDelayMillis = 0L) }
        }

        composeTestRule.mainClock.advanceTimeByFrame()
        composeTestRule.onNodeWithContentDescription("Loading tasks").assertIsDisplayed()
    }
}
