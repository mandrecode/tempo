package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import com.google.common.truth.Truth.assertThat
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

    /**
     * The plan sheet wraps its height around the indicator; if the space only arrived with the
     * indicator, the sheet would jump taller mid-load.
     */
    @Test
    fun holdsItsSpace_whileHidden() {
        composeTestRule.setContent {
            TempoTheme {
                TempoLoadingIndicator(
                    message = "Loading tasks",
                    modifier = Modifier.fillMaxWidth().testTag("indicator"),
                )
            }
        }

        composeTestRule.mainClock.advanceTimeByFrame()
        val hiddenHeight = composeTestRule.onNodeWithTag("indicator").getUnclippedBoundsInRoot().height
        composeTestRule.mainClock.advanceTimeBy(LOADING_INDICATOR_REVEAL_DELAY_MILLIS + 500)
        val shownHeight = composeTestRule.onNodeWithTag("indicator").getUnclippedBoundsInRoot().height

        assertThat(hiddenHeight.value).isAtLeast(96.dp.value)
        assertThat(hiddenHeight).isEqualTo(shownHeight)
    }
}
