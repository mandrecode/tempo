package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

private val BOTTOM_SHEET_TOP_AIR = 48.dp

/**
 * Whether the dismiss axis runs horizontally (side sheets) instead of vertically.
 */
internal val TempoModalSheetDirection.isHorizontal: Boolean
    get() = this == TempoModalSheetDirection.End

internal val TempoModalSheetDirection.alignment: Alignment
    get() =
        when (this) {
            TempoModalSheetDirection.Top -> Alignment.TopCenter
            TempoModalSheetDirection.Bottom -> Alignment.BottomCenter
            TempoModalSheetDirection.End -> Alignment.CenterEnd
        }

internal val TempoModalSheetDirection.shape: RoundedCornerShape
    get() =
        when (this) {
            TempoModalSheetDirection.Top -> RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            TempoModalSheetDirection.Bottom -> RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            TempoModalSheetDirection.End -> RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp)
        }

internal fun TempoModalSheetDirection.maxHeight(
    screenHeight: Dp,
    topInset: Dp = 0.dp,
): Dp =
    when (this) {
        TempoModalSheetDirection.Top -> screenHeight
        TempoModalSheetDirection.Bottom -> screenHeight - maxOf(BOTTOM_SHEET_TOP_AIR, topInset)
        TempoModalSheetDirection.End -> screenHeight
    }

/**
 * Offset that fully hides the sheet. [axisSizePx] is the dismiss-axis extent: the window height
 * for vertical sheets, the sheet width for side sheets.
 */
internal fun TempoModalSheetDirection.hiddenOffset(axisSizePx: Float): Float =
    when (this) {
        TempoModalSheetDirection.Top -> -axisSizePx
        TempoModalSheetDirection.Bottom -> axisSizePx
        TempoModalSheetDirection.End -> axisSizePx
    }

internal fun TempoModalSheetDirection.dismissOffsetForProgress(
    axisSizePx: Float,
    progress: Float,
): Float =
    when (this) {
        TempoModalSheetDirection.Top -> -axisSizePx * progress
        TempoModalSheetDirection.Bottom -> axisSizePx * progress
        TempoModalSheetDirection.End -> axisSizePx * progress
    }

internal fun TempoModalSheetDirection.coerceDragOffset(
    offset: Float,
    axisSizePx: Float,
): Float =
    when (this) {
        TempoModalSheetDirection.Top -> offset.coerceIn(-axisSizePx, 0f)
        TempoModalSheetDirection.Bottom -> offset.coerceIn(0f, axisSizePx)
        TempoModalSheetDirection.End -> offset.coerceIn(0f, axisSizePx)
    }

internal fun TempoModalSheetDirection.shouldDismiss(
    offset: Float,
    axisSizePx: Float,
): Boolean =
    when (this) {
        TempoModalSheetDirection.Top -> offset < -axisSizePx * DISMISS_THRESHOLD_FRACTION
        TempoModalSheetDirection.Bottom -> offset > axisSizePx * DISMISS_THRESHOLD_FRACTION
        TempoModalSheetDirection.End -> offset > axisSizePx * DISMISS_THRESHOLD_FRACTION
    }

/**
 * Maps a dismiss-axis offset to a translation. [layoutFactor] is 1 for LTR and -1 for RTL so
 * side sheets translate toward the physical end edge.
 */
internal fun TempoModalSheetDirection.translationOffset(
    offsetPx: Int,
    layoutFactor: Int,
): IntOffset =
    when (this) {
        TempoModalSheetDirection.Top,
        TempoModalSheetDirection.Bottom,
        -> IntOffset(0, offsetPx)

        TempoModalSheetDirection.End -> IntOffset(offsetPx * layoutFactor, 0)
    }
