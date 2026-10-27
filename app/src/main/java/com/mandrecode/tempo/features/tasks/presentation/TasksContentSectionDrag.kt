package com.mandrecode.tempo.features.tasks.presentation

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LocalPinnableContainer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mandrecode.tempo.features.tasks.presentation.model.DropEntry
import com.mandrecode.tempo.features.tasks.presentation.model.DropTarget
import com.mandrecode.tempo.features.tasks.presentation.model.SectionDropAction
import com.mandrecode.tempo.features.tasks.presentation.model.autoScrollStep
import com.mandrecode.tempo.features.tasks.presentation.model.dropActionFor
import com.mandrecode.tempo.features.tasks.presentation.model.resolveDropTarget
import kotlinx.coroutines.isActive

private const val DRAGGED_CARD_ALPHA = 0.85f
private const val TARGET_CARD_ALPHA = 0.5f

private fun cardEntryKey(taskId: Long) = "card_$taskId"

/**
 * Dragging a task between sections in Manual sort (#61). Every header, card and empty-section
 * placeholder on screen reports where it is; the pointer's position over them is the drop target.
 * Bounds rather than an estimated row height, so it works however tall cards are.
 */
@Stable
internal class SectionDragState {
    val entries = mutableStateMapOf<String, DropEntry>()
    var draggedTaskId by mutableStateOf<Long?>(null)
        private set

    /** Where the dragged card is drawn, relative to where the list laid it out. */
    var offsetY by mutableFloatStateOf(0f)
        private set
    private var fromSectionId: Long? = null
    private var fromIndex = 0
    private var startCenterY = 0f

    // The finger's travel, and how far the list scrolled under it since the drag began. The card
    // follows both; the drop target follows only the finger, since entries report current bounds.
    private var dragTotal = 0f
    private var scrolled = 0f
    private var viewport: Rect? = null

    /** The visible extent of the scrolling list, in root coordinates; auto-scroll works off it. */
    fun setViewport(bounds: Rect) {
        viewport = bounds
    }

    /** Where the dragged task would land now; its own card is left out so it never targets itself. */
    val target: DropTarget? by derivedStateOf {
        val taskId = draggedTaskId ?: return@derivedStateOf null
        val others = entries.filterKeys { it != cardEntryKey(taskId) }.values
        resolveDropTarget(startCenterY + dragTotal, others)
    }

    fun start(
        taskId: Long,
        sectionId: Long?,
        index: Int,
    ) {
        val own = entries[cardEntryKey(taskId)] ?: return
        draggedTaskId = taskId
        fromSectionId = sectionId
        fromIndex = index
        startCenterY = (own.top + own.bottom) / 2
        dragTotal = 0f
        scrolled = 0f
        offsetY = 0f
    }

    fun drag(deltaY: Float) {
        dragTotal += deltaY
        offsetY = dragTotal + scrolled
    }

    /** How far to scroll the list this frame: negative up, positive down, 0 away from the edges. */
    fun autoScrollStep(
        edgePx: Float,
        maxStepPx: Float,
    ): Float {
        val bounds = viewport
        return if (bounds == null || draggedTaskId == null) {
            0f
        } else {
            autoScrollStep(startCenterY + dragTotal, bounds.top, bounds.bottom, edgePx, maxStepPx)
        }
    }

    /** The list moved by [consumed] px under the finger; keeps the card under it. */
    fun onScrolled(consumed: Float) {
        scrolled += consumed
        offsetY = dragTotal + scrolled
    }

    /** Ends the drag and reports what it means, if anything. */
    fun finish(): SectionDropAction? {
        val action = target?.let { dropActionFor(fromSectionId, fromIndex, it) }
        cancel()
        return action
    }

    fun cancel() {
        draggedTaskId = null
        dragTotal = 0f
        scrolled = 0f
        offsetY = 0f
    }

    fun isTargeting(
        kind: DropEntry.Kind,
        sectionId: Long?,
        index: Int = 0,
    ): Boolean {
        val entry = target?.entry ?: return false
        val sameIndex = kind != DropEntry.Kind.Card || entry.index == index
        return entry.kind == kind && entry.sectionId == sectionId && sameIndex
    }
}

/** Keeps [state] told where this element is, as a drop target of [kind]. */
@Composable
internal fun Modifier.dropEntry(
    state: SectionDragState,
    key: String,
    kind: DropEntry.Kind,
    sectionId: Long?,
    index: Int = 0,
    taskCount: Int = 0,
): Modifier {
    DisposableEffect(state, key) { onDispose { state.entries.remove(key) } }
    return onGloballyPositioned { coordinates ->
        val bounds = coordinates.boundsInRoot()
        state.entries[key] = DropEntry(kind, sectionId, bounds.top, bounds.bottom, index, taskCount)
    }
}

/**
 * A task card that drags (after a long press) anywhere in its category's sections, reporting the
 * drop through [onDrop]. Its own bounds are registered so the drag knows where it started.
 */
@Composable
internal fun Modifier.sectionDragGestures(
    state: SectionDragState,
    taskId: Long,
    sectionId: Long?,
    index: Int,
    onDrop: (SectionDropAction) -> Unit,
): Modifier {
    val haptic = LocalHapticFeedback.current
    val isDragged = state.draggedTaskId == taskId
    val currentSectionId by rememberUpdatedState(sectionId)
    val currentIndex by rememberUpdatedState(index)
    val currentOnDrop by rememberUpdatedState(onDrop)
    // Auto-scroll can carry the card out of the viewport; pinned, it stays composed and keeps
    // receiving the finger's moves.
    val pinnable = LocalPinnableContainer.current
    DisposableEffect(isDragged, pinnable) {
        val handle = if (isDragged) pinnable?.pin() else null
        onDispose { handle?.release() }
    }
    val isTarget = !isDragged && state.isTargeting(DropEntry.Kind.Card, sectionId, index)
    return this
        .dropEntry(state, cardEntryKey(taskId), DropEntry.Kind.Card, sectionId, index)
        .zIndex(if (isDragged) 1f else 0f)
        .graphicsLayer {
            if (isDragged) {
                translationY = state.offsetY
                alpha = DRAGGED_CARD_ALPHA
            } else if (isTarget) {
                alpha = TARGET_CARD_ALPHA
            }
        }.pointerInput(taskId) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    state.start(taskId, currentSectionId, currentIndex)
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    val before = state.target
                    state.drag(dragAmount.y)
                    if (state.target != before) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                onDragEnd = { state.finish()?.let(currentOnDrop) },
                onDragCancel = { state.cancel() },
            )
        }
}

private val AutoScrollEdge = 72.dp
private val AutoScrollMaxStep = 24.dp

/**
 * While a task is being dragged, scrolls the list through [scrollBy] when the finger is near its
 * top or bottom edge, so a section off-screen can still be reached.
 */
@Composable
internal fun SectionDragAutoScroll(
    state: SectionDragState,
    scrollBy: suspend (Float) -> Float,
) {
    val density = LocalDensity.current
    val edgePx = with(density) { AutoScrollEdge.toPx() }
    val maxStepPx = with(density) { AutoScrollMaxStep.toPx() }
    val currentScrollBy by rememberUpdatedState(scrollBy)
    LaunchedEffect(state.draggedTaskId) {
        if (state.draggedTaskId == null) return@LaunchedEffect
        while (isActive) {
            withFrameNanos { }
            val step = state.autoScrollStep(edgePx, maxStepPx)
            if (step != 0f) state.onScrolled(currentScrollBy(step))
        }
    }
}
