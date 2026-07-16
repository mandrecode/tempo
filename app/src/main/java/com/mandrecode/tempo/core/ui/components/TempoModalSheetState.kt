package com.mandrecode.tempo.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.MutableState
import androidx.compose.ui.unit.Dp
import com.mandrecode.tempo.core.ui.theme.TempoMotionTokens

internal class TempoModalSheetState(
    val direction: TempoModalSheetDirection,
    val maxSheetHeight: Dp,
    val dismissAxisSizePx: Float,
    val offset: Animatable<Float, *>,
    val dismissing: MutableState<Boolean>,
    val showDiscardDialog: MutableState<Boolean>,
    val forceDismiss: MutableState<Boolean>,
    val isExpandedToStatusBar: MutableState<Boolean>,
    val animateDismiss: () -> Unit,
    val onRequestDismiss: () -> Unit,
) {
    suspend fun restore() {
        offset.animateTo(
            0f,
            animationSpec = tween(TempoMotionTokens.DURATION_SHEET_MILLIS),
        )
    }

    suspend fun handlePredictiveBackProgress(progress: Float) {
        offset.snapTo(
            direction.dismissOffsetForProgress(
                axisSizePx = dismissAxisSizePx,
                progress = progress,
            ),
        )
    }
}
