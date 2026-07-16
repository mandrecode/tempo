package com.mandrecode.tempo.core.ui.components

import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import kotlin.math.abs

/**
 * Detects drags along the sheet's dismiss axis: vertical for top/bottom sheets, horizontal for
 * side sheets. [layoutFactor] normalizes horizontal deltas so positive always means "toward the
 * end edge" (1 for LTR, -1 for RTL); it is ignored on the vertical axis.
 */
internal suspend fun PointerInputScope.detectSheetDragGestures(
    direction: TempoModalSheetDirection,
    layoutFactor: Int,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onDrag: (dragAmount: Float) -> Unit,
) {
    awaitPointerEventScope {
        while (true) {
            val down = awaitPressedChange()
            var pointerId = down.id
            var pastTouchSlop = false
            var accumulatedDrag = 0f
            var pointerActive = true

            while (pointerActive) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == pointerId }
                when {
                    change == null -> {
                        pointerId = event.changes.firstOrNull { it.pressed }?.id ?: pointerId
                        if (event.changes.none { it.pressed }) {
                            onDragCancel()
                            pointerActive = false
                        }
                    }
                    !change.pressed -> {
                        if (pastTouchSlop) {
                            onDragEnd()
                        }
                        pointerActive = false
                    }
                    pastTouchSlop -> {
                        change.consume()
                        onDrag(change.axisDelta(direction, layoutFactor))
                    }
                    else -> {
                        accumulatedDrag += change.axisDelta(direction, layoutFactor)
                        if (
                            abs(accumulatedDrag) > viewConfiguration.touchSlop &&
                            direction.isDismissDrag(accumulatedDrag)
                        ) {
                            pastTouchSlop = true
                            change.consume()
                            onDrag(accumulatedDrag)
                        }
                    }
                }
            }
        }
    }
}

private suspend fun androidx.compose.ui.input.pointer.AwaitPointerEventScope.awaitPressedChange(): PointerInputChange {
    while (true) {
        val change =
            awaitPointerEvent(PointerEventPass.Initial)
                .changes
                .firstOrNull { it.pressed }
        if (change != null) {
            return change
        }
    }
}

private fun PointerInputChange.axisDelta(
    direction: TempoModalSheetDirection,
    layoutFactor: Int,
): Float =
    if (direction.isHorizontal) {
        (position.x - previousPosition.x) * layoutFactor
    } else {
        position.y - previousPosition.y
    }

private fun TempoModalSheetDirection.isDismissDrag(dragAmount: Float): Boolean =
    when (this) {
        TempoModalSheetDirection.Top -> dragAmount < 0f
        TempoModalSheetDirection.Bottom -> dragAmount > 0f
        TempoModalSheetDirection.End -> dragAmount > 0f
    }
