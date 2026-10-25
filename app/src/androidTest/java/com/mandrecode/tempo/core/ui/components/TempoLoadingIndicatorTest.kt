package com.mandrecode.tempo.core.ui.components

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.StartupContentGate
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

    /** The first content fades in over the empty loading state instead of appearing at once. */
    @Test
    fun firstContent_fadesIn_ratherThanAppearingAtOnce() {
        var isLoading by mutableStateOf(true)
        composeTestRule.setContent {
            TempoTheme {
                FirstLoadCrossfade(
                    isLoading = isLoading,
                    loading = {
                        TempoLoadingIndicator(message = "Loading tasks", modifier = Modifier.testTag("loading"))
                    },
                ) {
                    Text("Pay rent", modifier = Modifier.testTag("content"))
                }
            }
        }
        composeTestRule.mainClock.advanceTimeByFrame()

        isLoading = false
        composeTestRule.mainClock.advanceTimeByFrame()
        composeTestRule.mainClock.advanceTimeBy(80)
        // Midway through the fade the content is already coming in, and the outgoing loading state
        // is empty: the load is over, so it has nothing left to show.
        composeTestRule.onNodeWithTag("content").assertExists()
        composeTestRule.onNodeWithTag("loading").assertDoesNotExist()

        composeTestRule.mainClock.advanceTimeBy(1_000)
        composeTestRule.onNodeWithTag("content").assertIsDisplayed()
        composeTestRule.onNodeWithTag("loading").assertDoesNotExist()
    }

    /** Content that is ready on first composition (tests, previews, a warm tab) shows at once. */
    @Test
    fun contentReadyFromTheStart_showsWithoutFading() {
        composeTestRule.setContent {
            TempoTheme {
                FirstLoadCrossfade(
                    isLoading = false,
                    loading = { TempoLoadingIndicator(message = "Loading tasks") },
                ) {
                    Text("Pay rent", modifier = Modifier.testTag("content"))
                }
            }
        }
        composeTestRule.mainClock.advanceTimeByFrame()

        composeTestRule.onNodeWithTag("content").assertIsDisplayed()
    }

    /**
     * A load that ends just before the indicator's reveal delay: the fade-out that follows must not
     * carry the indicator past its deadline and show it over the loaded content.
     */
    @Test
    fun aLoadEndingJustBeforeTheRevealDelay_neverShowsTheIndicator() {
        var isLoading by mutableStateOf(true)
        composeTestRule.setContent {
            TempoTheme {
                FirstLoadCrossfade(
                    isLoading = isLoading,
                    loading = { TempoLoadingIndicator(message = "Loading tasks") },
                ) {
                    Text("Pay rent", modifier = Modifier.testTag("content"))
                }
            }
        }
        composeTestRule.mainClock.advanceTimeBy(LOADING_INDICATOR_REVEAL_DELAY_MILLIS - 50)

        isLoading = false
        // Through the reveal deadline and the rest of the fade, frame by frame.
        repeat(30) {
            composeTestRule.mainClock.advanceTimeByFrame()
            composeTestRule.onNodeWithContentDescription("Loading tasks").assertDoesNotExist()
        }
        composeTestRule.onNodeWithTag("content").assertIsDisplayed()
    }

    /**
     * Renders [FirstLoadCrossfade] as a 100 dp white square whose content is solid black, so the
     * colour at its centre says exactly how opaque the content is.
     */
    private fun setOpacityProbe(
        isLoading: () -> Boolean,
        activity: Activity? = null,
    ) {
        composeTestRule.setContent {
            val host = activity ?: LocalActivity.current
            CompositionLocalProvider(LocalActivity provides host) {
                Box(modifier = Modifier.size(100.dp).background(Color.White).testTag("probe")) {
                    FirstLoadCrossfade(isLoading = isLoading(), loading = {}) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black))
                    }
                }
            }
        }
    }

    /** Red channel at the probe's centre: 1 is the white background alone, 0 is opaque content. */
    private fun probeRed(): Float {
        val image = composeTestRule.onNodeWithTag("probe").captureToImage()
        return image.toPixelMap()[image.width / 2, image.height / 2].red
    }

    @Test
    fun firstContent_isPartlyTransparentMidFade_andOpaqueAfterIt() {
        var isLoading by mutableStateOf(true)
        setOpacityProbe(isLoading = { isLoading })
        composeTestRule.mainClock.advanceTimeByFrame()
        assertThat(probeRed()).isWithin(0.02f).of(1f)

        isLoading = false
        composeTestRule.mainClock.advanceTimeByFrame()
        composeTestRule.mainClock.advanceTimeBy(100)
        val midway = probeRed()
        assertThat(midway).isGreaterThan(0.05f)
        assertThat(midway).isLessThan(0.95f)

        composeTestRule.mainClock.advanceTimeBy(1_000)
        assertThat(probeRed()).isWithin(0.02f).of(0f)
    }

    /**
     * Under the launch splash the content arrives fully opaque in its very first frame: the splash
     * lets go in that frame, and anything less showed as an empty screen.
     */
    @Test
    fun underTheSplash_firstContentIsOpaqueInItsFirstFrame() {
        // An Activity can only be constructed on the main thread.
        val host = composeTestRule.runOnUiThread { SplashHost() }
        var isLoading by mutableStateOf(true)
        setOpacityProbe(isLoading = { isLoading }, activity = host)
        composeTestRule.mainClock.advanceTimeByFrame()
        assertThat(host.startupContentGate.isHolding).isTrue()

        isLoading = false
        composeTestRule.mainClock.advanceTimeByFrame()

        assertThat(probeRed()).isWithin(0.02f).of(0f)
    }

    /** An activity standing in for MainActivity: only its gate is read. */
    private class SplashHost :
        ComponentActivity(),
        StartupContentGateHost {
        override val startupContentGate = StartupContentGate()
    }
}
