package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val BOTTOM_SHEET_TOP_AIR = 48.dp

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

internal fun TempoModalSheetDirection.maxHeight(
    screenHeight: Dp,
    topInset: Dp = 0.dp,
): Dp =
    when (this) {
        TempoModalSheetDirection.Top -> screenHeight
        TempoModalSheetDirection.Bottom -> screenHeight - maxOf(BOTTOM_SHEET_TOP_AIR, topInset)
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
