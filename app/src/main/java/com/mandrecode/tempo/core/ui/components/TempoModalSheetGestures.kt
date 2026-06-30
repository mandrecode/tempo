package com.mandrecode.tempo.core.ui.components

import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import kotlin.math.abs

internal suspend fun PointerInputScope.detectSheetVerticalDragGestures(
    direction: TempoModalSheetDirection,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onVerticalDrag: (dragAmount: Float) -> Unit,
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
                        onVerticalDrag(change.verticalDelta)
                    }
                    else -> {
                        accumulatedDrag += change.verticalDelta
                        if (
                            abs(accumulatedDrag) > viewConfiguration.touchSlop &&
                            direction.isDismissDrag(accumulatedDrag)
                        ) {
                            pastTouchSlop = true
                            change.consume()
                            onVerticalDrag(accumulatedDrag)
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

private val PointerInputChange.verticalDelta: Float
    get() = position.y - previousPosition.y

private fun TempoModalSheetDirection.isDismissDrag(dragAmount: Float): Boolean =
    when (this) {
        TempoModalSheetDirection.Top -> dragAmount < 0f
        TempoModalSheetDirection.Bottom -> dragAmount > 0f
    }
