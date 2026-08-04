package com.mandrecode.tempo.core.ui.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue

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
 * Call the returned lambda immediately *before* the event that can move the row — it has to read
 * the scroll position while the list still has its old contents.
 *
 * ```
 * val holdViewport = rememberViewportHold(listState, uiState.active, uiState.completed)
 * ...
 * onToggleCompletion = { task ->
 *     holdViewport()
 *     onEvent(ToggleTaskCompletion(task))
 * }
 * ```
 *
 * @param sectionKeys whatever changes when a row crosses between sections — the sections themselves
 * will do. The restore is keyed on these, so a row acted on without moving costs nothing.
 */
@Composable
fun rememberViewportHold(
    state: LazyListState,
    vararg sectionKeys: Any?,
): () -> Unit =
    rememberViewportHold(
        readIndex = { state.firstVisibleItemIndex },
        readOffset = { state.firstVisibleItemScrollOffset },
        sectionKeys = sectionKeys,
        scrollToItem = { index, offset -> state.scrollToItem(index, offset) },
    )

/** As above, for a grid. The two state types share the behaviour but not an interface. */
@Composable
fun rememberViewportHold(
    state: LazyGridState,
    vararg sectionKeys: Any?,
): () -> Unit =
    rememberViewportHold(
        readIndex = { state.firstVisibleItemIndex },
        readOffset = { state.firstVisibleItemScrollOffset },
        sectionKeys = sectionKeys,
        scrollToItem = { index, offset -> state.scrollToItem(index, offset) },
    )

@Composable
private fun rememberViewportHold(
    readIndex: () -> Int,
    readOffset: () -> Int,
    sectionKeys: Array<out Any?>,
    scrollToItem: suspend (Int, Int) -> Unit,
): () -> Unit {
    var restoreTo by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    // The effect restarts on the section keys, not on this, so it would otherwise go on calling
    // whichever list it was first composed with.
    val currentScrollToItem by rememberUpdatedState(scrollToItem)

    // Only once the sections have actually changed, which is the moment the list has had its chance
    // to chase. Nothing recorded means nothing to put back, so an unrelated change costs a return.
    LaunchedEffect(*sectionKeys) {
        val (index, offset) = restoreTo ?: return@LaunchedEffect
        currentScrollToItem(index, offset)
        restoreTo = null
    }

    return { restoreTo = readIndex() to readOffset() }
}
