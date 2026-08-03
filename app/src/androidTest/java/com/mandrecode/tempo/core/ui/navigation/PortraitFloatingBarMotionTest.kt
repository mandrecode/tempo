package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs

/**
 * The bar's controls join and leave as the tab changes, and the centred group has to follow them
 * continuously. It used to land, hold for a few hundred milliseconds, and then shift again by half
 * the spacing of whichever control had just been disposed — see issue #344.
 */
class PortraitFloatingBarMotionTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private companion object {
        const val PILL_TAG = "nav_pill"

        /**
         * Narrow enough that the bar still fits the narrowest screen Android ships.
         *
         * Centring is only a property the layout can have when there is room to centre in. At the
         * 200dp this used to be, the row comes to 344dp with both flanks and their gaps, which
         * overflows a 320dp screen: the row fills the bar, centring stops happening, and the pill
         * sits at the padding edge instead. Every test here would then be measuring overflow
         * rather than the centred group — silently, since "it stays put once it stops" is trivially
         * true when nothing can move. It is also what made one of these fail by exactly 12dp on
         * CI, whose emulator is 320dp wide, while passing on every local device.
         */
        val PillWidth = 120.dp

        /** One frame at 60 Hz, in milliseconds. */
        const val FRAME_MS = 16L

        /**
         * Below this, a change is the spring's own tail or pixel rounding rather than a step.
         * Positions are pixel-quantised, so this has to clear one device pixel on any density.
         */
        val Settled = 1.dp

        /**
         * How far the resting position may sit from where the motion actually stopped.
         *
         * The defect this guards against is half of [FloatingToolbarItemSpacing] — measured at
         * 4.19dp — so anything comfortably under that still catches it. It has to stay well above
         * rounding noise too: a first run at 1dp failed on CI by exactly 1.0dp on a density this
         * emulator does not have.
         */
        val Tolerance = 2.5.dp

        const val SORT_DESCRIPTION = "Sort tasks by"
        const val CLEAR_DESCRIPTION = "Delete all completed"
        const val ADD_DESCRIPTION = "Add Task"
    }

    private var isTasksRoute by mutableStateOf(true)

    private fun setBar(hasCompletedTasks: Boolean = true) {
        composeTestRule.setContent {
            TempoTheme {
                val route: NavKey = if (isTasksRoute) TasksRoute else FocusRoute
                PersistentPortraitFloatingBar(
                    isTasksRoute = isTasksRoute,
                    topLevelRoute = route,
                    navigationContent = {
                        Box(modifier = Modifier.size(PillWidth).testTag(PILL_TAG))
                    },
                    routinesState = RoutinesFloatingBarState(),
                    tasksState = TasksFloatingBarState(hasCompletedTasks = hasCompletedTasks),
                    isSingleTabMode = false,
                )
            }
        }
    }

    private fun pillLeft(): Dp = composeTestRule.onNodeWithTag(PILL_TAG).getUnclippedBoundsInRoot().left

    /**
     * Steps the transition a frame at a time and returns where the pill sat on each one.
     *
     * Runs the whole window rather than stopping at the first stretch of stillness: stopping there
     * is exactly what hides the defect, which is a step taken *after* the motion looks finished.
     */
    private fun samplePositions(frames: Int = SAMPLE_FRAMES): List<Dp> {
        val positions = mutableListOf(pillLeft())
        repeat(frames) {
            composeTestRule.mainClock.advanceTimeBy(FRAME_MS)
            positions += pillLeft()
        }
        return positions
    }

    /**
     * The index of the first position the group held for [HOLD_FRAMES] frames — the point a
     * watching eye reads as the transition being over.
     */
    private fun List<Dp>.firstRestIndex(): Int {
        for (index in 1..size - HOLD_FRAMES) {
            val held =
                (index until index + HOLD_FRAMES).all {
                    abs((this[it] - this[index]).value) < Settled.value
                }
            if (held) return index
        }
        return size - 1
    }

    /**
     * Once the group stops, it has to stay stopped.
     *
     * Not "it must end where it stopped": the defect was a round trip. The group came to rest,
     * stepped half a spacing away as one leaving control's node was disposed, held there for a few
     * hundred milliseconds, and stepped back as the second one went — finishing exactly where it
     * had first landed. Comparing the rest to the final position sees nothing at all.
     */
    private fun assertStaysPutOnceItStops(positions: List<Dp>) {
        val restIndex = positions.firstRestIndex()
        val rest = positions[restIndex]
        val worst = positions.drop(restIndex).maxOf { abs((it - rest).value) }
        assertThat(worst).isLessThan(Tolerance.value)
    }

    /** Guards the sampling window itself: a run that never finished proves nothing. */
    private fun assertMotionFinished(positions: List<Dp>) {
        val tail = positions.takeLast(STILL_FRAMES)
        assertThat(tail.all { abs((it - positions.last()).value) < Settled.value }).isTrue()
    }

    @Test
    fun leavingTasks_theGroupStaysPutOnceItStops() {
        setBar()
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.waitForIdle()

        isTasksRoute = false
        composeTestRule.mainClock.advanceTimeByFrame()

        val positions = samplePositions()
        assertMotionFinished(positions)
        assertStaysPutOnceItStops(positions)
    }

    @Test
    fun arrivingOnTasks_theGroupStaysPutOnceItStops() {
        isTasksRoute = false
        setBar()
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.waitForIdle()

        isTasksRoute = true
        composeTestRule.mainClock.advanceTimeByFrame()

        val positions = samplePositions()
        assertMotionFinished(positions)
        assertStaysPutOnceItStops(positions)
    }

    /**
     * With nothing to clear, Tasks flanks the pill with one button on each side, and the two are
     * the same size on the same spring — so the pill sits where Focus, which flanks it with none,
     * had it, and it stays there for every frame in between rather than only at the two ends.
     *
     * At rest the add button used to be 4dp wider than sort, and centring the group put half of
     * that on the pill — see issue #355.
     *
     * The clock is stepped by hand rather than left to `waitForIdle`, which means "no pending
     * work", not "the springs have stopped".
     */
    @Test
    fun withNothingToClear_thePillStaysWhereFocusHadItThroughout() {
        isTasksRoute = false
        setBar(hasCompletedTasks = false)
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.waitForIdle()
        val onFocus = pillLeft()

        isTasksRoute = true
        composeTestRule.mainClock.advanceTimeByFrame()

        val positions = samplePositions()
        assertMotionFinished(positions)
        // Nothing on the way may lean either side of where Focus had it...
        assertThat(positions.maxOf { abs((it - onFocus).value) }).isLessThan(Tolerance.value)
        // ...and the rest has to land back on it, bar pixel rounding.
        // Inclusive: on mdpi one device pixel *is* 1dp, so two adjacent quantised positions differ
        // by exactly [Settled] without either having moved. The 2dp this guards still fails.
        assertThat(abs((positions.last() - onFocus).value)).isAtMost(Settled.value)
    }

    /**
     * With something to clear the pill does move — the left side ends up heavier — but it only ever
     * moves that one way.
     *
     * Guarding the direction, not a known defect: a transition that sets off the opposite way
     * before turning around reads as a bounce, however it comes about.
     */
    @Test
    fun arrivingOnTasks_withSomethingToClear_thePillOnlyEverMovesOneWay() {
        isTasksRoute = false
        setBar(hasCompletedTasks = true)
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.waitForIdle()
        val start = pillLeft()

        isTasksRoute = true
        composeTestRule.mainClock.advanceTimeByFrame()

        val positions = samplePositions()
        assertMotionFinished(positions)
        // It settles to the right of where it started, so nothing on the way may sit to the left.
        assertThat((positions.last() - start).value).isGreaterThan(Tolerance.value)
        assertThat(positions.minOf { (it - start).value }).isGreaterThan(-Tolerance.value)
    }

    /**
     * The gaps moved from the row into the buttons; they must not have changed size on the way.
     */
    @Test
    fun restingGaps_areUnchangedWhenEveryControlIsShown() {
        setBar()
        composeTestRule.waitForIdle()

        val clear = composeTestRule.onNodeWithContentDescription(CLEAR_DESCRIPTION, substring = true)
        val sort = composeTestRule.onNodeWithContentDescription(SORT_DESCRIPTION, substring = true)
        val add = composeTestRule.onNodeWithContentDescription(ADD_DESCRIPTION, substring = true)
        val pill = composeTestRule.onNodeWithTag(PILL_TAG).getUnclippedBoundsInRoot()

        // A dp of slack for pixel rounding; the gaps being guarded are 6dp and 8dp, so a genuine
        // change to either would still be caught.
        assertThat((sort.getUnclippedBoundsInRoot().left - clear.getUnclippedBoundsInRoot().right).value)
            .isWithin(GAP_TOLERANCE_DP)
            .of(TASK_ACTIONS_BUTTON_SPACING.value)
        assertThat((pill.left - sort.getUnclippedBoundsInRoot().right).value)
            .isWithin(GAP_TOLERANCE_DP)
            .of(FloatingToolbarItemSpacing.value)
        assertThat((add.getUnclippedBoundsInRoot().left - pill.right).value)
            .isWithin(GAP_TOLERANCE_DP)
            .of(FloatingToolbarItemSpacing.value)
    }
}

/** How many trailing frames must be still for the sample window to have covered the motion. */
private const val STILL_FRAMES = 8

/** How long a position has to hold before it reads as the motion having finished. */
private const val HOLD_FRAMES = 6

/** Roughly two seconds at 60 Hz — long enough to include a step taken after the spring ran out. */
private const val SAMPLE_FRAMES = 120

/** Slack for pixel rounding when comparing a measured gap against its dp constant. */
private const val GAP_TOLERANCE_DP = 1.5f
