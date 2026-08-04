package com.mandrecode.tempo.core.ui.util

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

/**
 * The one behaviour worth pinning: a row moving to another section must not take the view with it.
 *
 * Written as a pair, because the interesting half is the second one — without the hold the list
 * really does chase the row, and a test that only asserted the good case would still pass if the
 * hold were deleted and lazy lists happened to change their anchoring.
 */
class ViewportHoldTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val rowHeight = 50.dp
    private val viewportHeight = 200.dp

    /**
     * Two sections of a list, and the move between them. Rows keep the same key on the way across —
     * which is the whole reason a list would follow one — and the section a row is in is the only
     * thing that changes.
     */
    @Composable
    private fun TwoSectionList(
        active: SnapshotStateList<Int>,
        completed: SnapshotStateList<Int>,
        state: LazyListState,
    ) {
        LazyColumn(state = state, modifier = Modifier.height(viewportHeight)) {
            items(active, key = { it }) { FixedRow() }
            item(key = "separator") { FixedRow() }
            items(completed, key = { it }) { FixedRow() }
        }
    }

    @Composable
    private fun FixedRow() {
        Box(modifier = Modifier.fillMaxWidth().height(rowHeight))
    }

    private fun setList(holding: Boolean): Pair<LazyListState, () -> Unit> {
        lateinit var state: LazyListState
        lateinit var settleTopRow: () -> Unit

        composeTestRule.setContent {
            val active = remember { mutableStateListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9) }
            val completed = remember { mutableStateListOf<Int>() }
            state = rememberLazyListState()
            val holdViewport = rememberViewportHold(state, active.size, completed.size)

            settleTopRow = {
                if (holding) holdViewport()
                val top = active.removeAt(0)
                completed.add(top)
            }

            TwoSectionList(active = active, completed = completed, state = state)
        }

        composeTestRule.waitForIdle()
        return state to settleTopRow
    }

    @Test
    fun settlingTheTopRow_leavesTheViewWhereItWas() {
        val (state, settleTopRow) = setList(holding = true)
        assertThat(state.firstVisibleItemIndex).isEqualTo(0)

        composeTestRule.runOnUiThread { settleTopRow() }
        composeTestRule.waitForIdle()

        // Still the top of the list — the row left, and the one under it moved up into its place.
        assertThat(state.firstVisibleItemIndex).isEqualTo(0)
        assertThat(state.firstVisibleItemScrollOffset).isEqualTo(0)
    }

    /** What the hold is for: left alone, the list goes after the row it just lost. */
    @Test
    fun withoutTheHold_theListFollowsTheRowToItsNewSection() {
        val (state, settleTopRow) = setList(holding = false)

        composeTestRule.runOnUiThread { settleTopRow() }
        composeTestRule.waitForIdle()

        assertThat(state.firstVisibleItemIndex).isGreaterThan(0)
    }
}
