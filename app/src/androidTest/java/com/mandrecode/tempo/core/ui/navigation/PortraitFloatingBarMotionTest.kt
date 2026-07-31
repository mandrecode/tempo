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
        val PillWidth = 200.dp

        /** One frame at 60 Hz, in milliseconds. */
        const val FRAME_MS = 16L

        /** Below this, a change is the spring's own tail rather than a step. */
        val Settled = 0.5.dp

        /** How far the resting position may sit from where the motion actually stopped. */
        val Tolerance = 1.dp

        const val SORT_DESCRIPTION = "Sort tasks by"
        const val CLEAR_DESCRIPTION = "Delete all completed"
        const val ADD_DESCRIPTION = "Add Task"
    }

    private var isTasksRoute by mutableStateOf(true)

    private fun setBar() {
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
                    tasksState = TasksFloatingBarState(hasCompletedTasks = true),
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
     * Where the motion first came to rest — the first position it held for [HOLD_FRAMES] frames,
     * which is what a watching eye reads as the transition being over.
     */
    private fun List<Dp>.whereItFirstRested(): Dp {
        for (index in 0..size - HOLD_FRAMES) {
            val held =
                (index until index + HOLD_FRAMES).all {
                    abs((this[it] - this[index]).value) < Settled.value
                }
            if (held && index > 0) return this[index]
        }
        return last()
    }

    /**
     * The transition must end where it stopped. It used to stop, hold, and then take one more step
     * of half a spacing as the leaving control's node was disposed.
     */
    private fun assertNoLateStep(positions: List<Dp>) {
        assertThat(abs((positions.whereItFirstRested() - positions.last()).value))
            .isLessThan(Tolerance.value)
    }

    /** Guards the sampling window itself: a run that never finished proves nothing. */
    private fun assertMotionFinished(positions: List<Dp>) {
        val tail = positions.takeLast(STILL_FRAMES)
        assertThat(tail.all { abs((it - positions.last()).value) < Settled.value }).isTrue()
    }

    @Test
    fun leavingTasks_theGroupSettlesWhereItStops() {
        setBar()
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.waitForIdle()

        isTasksRoute = false
        composeTestRule.mainClock.advanceTimeByFrame()

        val positions = samplePositions()
        assertMotionFinished(positions)
        assertNoLateStep(positions)
    }

    @Test
    fun arrivingOnTasks_theGroupSettlesWhereItStops() {
        isTasksRoute = false
        setBar()
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.waitForIdle()

        isTasksRoute = true
        composeTestRule.mainClock.advanceTimeByFrame()

        val positions = samplePositions()
        assertMotionFinished(positions)
        assertNoLateStep(positions)
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

        assertThat((sort.getUnclippedBoundsInRoot().left - clear.getUnclippedBoundsInRoot().right).value)
            .isWithin(1f)
            .of(TASK_ACTIONS_BUTTON_SPACING.value)
        assertThat((pill.left - sort.getUnclippedBoundsInRoot().right).value)
            .isWithin(1f)
            .of(FloatingToolbarItemSpacing.value)
        assertThat((add.getUnclippedBoundsInRoot().left - pill.right).value)
            .isWithin(1f)
            .of(FloatingToolbarItemSpacing.value)
    }
}

/** How many trailing frames must be still for the sample window to have covered the motion. */
private const val STILL_FRAMES = 8

/** How long a position has to hold before it reads as the motion having finished. */
private const val HOLD_FRAMES = 6

/** Roughly two seconds at 60 Hz — long enough to include a step taken after the spring ran out. */
private const val SAMPLE_FRAMES = 120
