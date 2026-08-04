package com.mandrecode.tempo.core.ui.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember

/**
 * Keeps a list looking where it is when one of its rows moves to another section, rather than
 * following the row to where it went.
 *
 * A lazy list holds the *key* of its first visible row across a change, so that content arriving
 * above it does not shove the reader down the page. That is the right instinct almost everywhere,
 * and exactly wrong for a list you act on a row at a time: the row you check off, or give a day to,
 * is usually the first one visible, and the list follows it into whatever section it lands in — a
 * scroll back for every row settled, in the one loop these lists are for.
 *
 * Holding the index instead keeps the viewport still. The settled row slides away to where it now
 * belongs and the next one rises into the place it left, under the finger already there.
 *
 * The hold is a *request* rather than a scroll, made before the list has measured itself against
 * the new sections rather than after. That is the whole difference between the row sliding away and
 * the list jumping: `requestScrollToItem` forgets the anchor key, so the chase never happens and
 * there is nothing to undo, and it is answered by the next measure rather than forcing one of its
 * own — one layout for the change, which is what `animateItem` needs to animate across. Restored
 * afterwards instead, from a coroutine, the correction lands on whichever side of the frame's
 * layout the dispatcher happens to resume on: sometimes invisible, sometimes a frame at the chased
 * position and a snap back, and the row's slide re-targeted mid-flight either way.
 *
 * Call the returned lambda immediately *before* the event that can move the row — it has to read
 * the scroll position while the list still has its old contents.
 *
 * ```
 * val holdViewport = rememberViewportHold(listState, activeCount, completedCount)
 * ...
 * onToggleCompletion = { task ->
 *     holdViewport()
 *     onEvent(ToggleTaskCompletion(task))
 * }
 * ```
 *
 * @param sectionKey a value that changes when a row crosses between sections, and only then. How
 * many rows a section holds is the usual answer. It has to change *by value* — a collection that
 * mutates in place reads as the same key, the hold never fires, and the restore silently never
 * happens — and it should not change for anything else, or the hold fires for edits that moved
 * nothing. One is required rather than a bare vararg, because none at all is the same silent
 * nothing and there is no reason to let it compile.
 * @param moreSectionKeys the rest, where more than one section is in play — usually the count on
 * the other side of the divide.
 */
@Composable
fun rememberViewportHold(
    state: LazyListState,
    sectionKey: Any?,
    vararg moreSectionKeys: Any?,
): () -> Unit =
    rememberViewportHold(
        readIndex = { state.firstVisibleItemIndex },
        readOffset = { state.firstVisibleItemScrollOffset },
        sectionKeys = arrayOf(sectionKey, *moreSectionKeys),
        requestScrollToItem = { index, offset -> state.requestScrollToItem(index, offset) },
    )

/** As above, for a grid. The two state types share the behaviour but not an interface. */
@Composable
fun rememberViewportHold(
    state: LazyGridState,
    sectionKey: Any?,
    vararg moreSectionKeys: Any?,
): () -> Unit =
    rememberViewportHold(
        readIndex = { state.firstVisibleItemIndex },
        readOffset = { state.firstVisibleItemScrollOffset },
        sectionKeys = arrayOf(sectionKey, *moreSectionKeys),
        requestScrollToItem = { index, offset -> state.requestScrollToItem(index, offset) },
    )

@Composable
private fun rememberViewportHold(
    readIndex: () -> Int,
    readOffset: () -> Int,
    sectionKeys: Array<out Any?>,
    requestScrollToItem: (Int, Int) -> Unit,
): () -> Unit {
    // Deliberately not snapshot state. Nothing reads either of these during composition and nothing
    // should recompose because of them — they are notes passed from a click to the layout that
    // follows it, and making them observable would only invite a write-during-composition tangle.
    val hold = remember { ViewportHold() }

    // After the composition that brought the new sections in, and before the measure that would
    // otherwise chase — the one window where the answer is known and the list has not moved yet.
    // Every recomposition arrives here, so the keys are what decides whether anything crossed.
    SideEffect {
        if (hold.sectionKeys.contentEquals(sectionKeys)) return@SideEffect
        hold.sectionKeys = sectionKeys
        val (index, offset) = hold.restoreTo ?: return@SideEffect
        hold.restoreTo = null
        requestScrollToItem(index, offset)
    }

    return { hold.restoreTo = readIndex() to readOffset() }
}

/** The two notes the hold carries between a click and the layout that answers it. */
private class ViewportHold {
    /** Null until the first composition has run, so the first pass is never mistaken for a move. */
    var sectionKeys: Array<out Any?>? = null

    /** Where the list was looking when a row was last acted on, until a crossing spends it. */
    var restoreTo: Pair<Int, Int>? = null
}
