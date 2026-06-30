package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val NO_SCALE = 1f
private const val PIVOT_CENTER = 0.5f
private const val PIVOT_TOP = 0f
private val BOTTOM_SHEET_TOP_AIR = 72.dp
private val PREDICTIVE_BACK_MAX_SCALE_X_DISTANCE = 48.dp
private val PREDICTIVE_BACK_MAX_SCALE_Y_DISTANCE = 24.dp

internal fun Modifier.tempoSheetPredictiveBackScaling(
    direction: TempoModalSheetDirection,
    predictiveBackProgress: Float,
    offsetY: Float,
) = graphicsLayer {
    val sheetWidth = size.width
    val sheetHeight = size.height
    if (sheetWidth != 0f && sheetHeight != 0f) {
        val scaleXDistance = minOf(PREDICTIVE_BACK_MAX_SCALE_X_DISTANCE.toPx(), sheetWidth)
        val scaleYDistance = minOf(PREDICTIVE_BACK_MAX_SCALE_Y_DISTANCE.toPx(), sheetHeight)
        scaleX = NO_SCALE - scaleXDistance * predictiveBackProgress / sheetWidth
        scaleY = NO_SCALE - scaleYDistance * predictiveBackProgress / sheetHeight
        transformOrigin = direction.transformOrigin(offsetY, sheetHeight)
    }
}

internal fun Modifier.tempoSheetContentPredictiveBackScaling(predictiveBackProgress: Float) =
    graphicsLayer {
        val sheetWidth = size.width
        val sheetHeight = size.height
        if (sheetWidth != 0f && sheetHeight != 0f) {
            val scaleXDistance = minOf(PREDICTIVE_BACK_MAX_SCALE_X_DISTANCE.toPx(), sheetWidth)
            val scaleYDistance = minOf(PREDICTIVE_BACK_MAX_SCALE_Y_DISTANCE.toPx(), sheetHeight)
            val scaleX = NO_SCALE - scaleXDistance * predictiveBackProgress / sheetWidth
            val scaleY = NO_SCALE - scaleYDistance * predictiveBackProgress / sheetHeight
            this.scaleY = if (scaleY != 0f) scaleX / scaleY else NO_SCALE
            transformOrigin = TransformOrigin(PIVOT_CENTER, PIVOT_TOP)
        }
    }

internal val TempoModalSheetDirection.alignment: Alignment
    get() =
        when (this) {
            TempoModalSheetDirection.Top -> Alignment.TopCenter
            TempoModalSheetDirection.Bottom -> Alignment.BottomCenter
        }

internal val TempoModalSheetDirection.shape: RoundedCornerShape
    get() =
        when (this) {
            TempoModalSheetDirection.Top -> RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            TempoModalSheetDirection.Bottom -> RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        }

internal fun TempoModalSheetDirection.maxHeight(screenHeight: Dp): Dp =
    when (this) {
        TempoModalSheetDirection.Top -> screenHeight
        TempoModalSheetDirection.Bottom -> screenHeight - BOTTOM_SHEET_TOP_AIR
    }

internal fun TempoModalSheetDirection.hiddenOffset(screenHeightPx: Float): Float =
    when (this) {
        TempoModalSheetDirection.Top -> -screenHeightPx
        TempoModalSheetDirection.Bottom -> screenHeightPx
    }

internal fun TempoModalSheetDirection.dismissOffsetForProgress(
    screenHeightPx: Float,
    progress: Float,
): Float =
    when (this) {
        TempoModalSheetDirection.Top -> -screenHeightPx * progress
        TempoModalSheetDirection.Bottom -> screenHeightPx * progress
    }

internal fun TempoModalSheetDirection.coerceDragOffset(
    offset: Float,
    screenHeightPx: Float,
): Float =
    when (this) {
        TempoModalSheetDirection.Top -> offset.coerceIn(-screenHeightPx, 0f)
        TempoModalSheetDirection.Bottom -> offset.coerceIn(0f, screenHeightPx)
    }

internal fun TempoModalSheetDirection.shouldDismiss(
    offset: Float,
    screenHeightPx: Float,
): Boolean =
    when (this) {
        TempoModalSheetDirection.Top -> offset < -screenHeightPx * DISMISS_THRESHOLD_FRACTION
        TempoModalSheetDirection.Bottom -> offset > screenHeightPx * DISMISS_THRESHOLD_FRACTION
    }

private fun TempoModalSheetDirection.transformOrigin(
    offsetY: Float,
    sheetHeight: Float,
): TransformOrigin =
    when (this) {
        TempoModalSheetDirection.Top -> TransformOrigin(PIVOT_CENTER, -offsetY / sheetHeight)
        TempoModalSheetDirection.Bottom -> TransformOrigin(PIVOT_CENTER, (offsetY + sheetHeight) / sheetHeight)
    }
