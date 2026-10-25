package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

/**
 * Crossing a breakpoint, or docking an editor, changes how far content clears the rail. The
 * content has to travel there rather than jump (#163).
 */
class AnimatedRailClearanceTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun aNewClearance_isReachedGradually_andThenHeld() {
        var target by mutableStateOf(0.dp)
        var current = 0.dp
        composeTestRule.setContent { current = animatedRailClearance(target) }
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.waitForIdle()

        target = 120.dp
        composeTestRule.mainClock.advanceTimeByFrame()
        composeTestRule.mainClock.advanceTimeBy(48)
        val midway: Dp = current

        composeTestRule.mainClock.advanceTimeBy(2_000)

        assertThat(midway.value).isGreaterThan(0f)
        assertThat(midway.value).isLessThan(120f)
        assertThat(current.value).isWithin(0.5f).of(120f)
    }
}
