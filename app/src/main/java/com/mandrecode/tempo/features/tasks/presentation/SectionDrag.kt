package com.mandrecode.tempo.features.tasks.presentation

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.zIndex
import com.mandrecode.tempo.features.tasks.presentation.model.DropEntry
import com.mandrecode.tempo.features.tasks.presentation.model.DropTarget
import com.mandrecode.tempo.features.tasks.presentation.model.SectionDropAction
import com.mandrecode.tempo.features.tasks.presentation.model.dropActionFor
import com.mandrecode.tempo.features.tasks.presentation.model.resolveDropTarget

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
    var offsetY by mutableFloatStateOf(0f)
        private set
    private var fromSectionId: Long? = null
    private var fromIndex = 0
    private var startCenterY = 0f

    /** Where the dragged task would land now; its own card is left out so it never targets itself. */
    val target: DropTarget? by derivedStateOf {
        val taskId = draggedTaskId ?: return@derivedStateOf null
        val others = entries.filterKeys { it != cardEntryKey(taskId) }.values
        resolveDropTarget(startCenterY + offsetY, others)
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
        offsetY = 0f
    }

    fun drag(deltaY: Float) {
        offsetY += deltaY
    }

    /** Ends the drag and reports what it means, if anything. */
    fun finish(): SectionDropAction? {
        val action = target?.let { dropActionFor(fromSectionId, fromIndex, it) }
        cancel()
        return action
    }

    fun cancel() {
        draggedTaskId = null
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
        }.pointerInput(taskId, sectionId, index) {
            detectDragGesturesAfterLongPress(
                onDragStart = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    state.start(taskId, sectionId, index)
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    val before = state.target
                    state.drag(dragAmount.y)
                    if (state.target != before) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
                onDragEnd = { state.finish()?.let(onDrop) },
                onDragCancel = { state.cancel() },
            )
        }
}
