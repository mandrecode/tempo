package com.mandrecode.tempo.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.TempoMotionTokens
import kotlinx.coroutines.launch

private val SIDE_SHEET_WIDTH = 412.dp

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

@Composable
internal fun rememberTempoModalSheetState(
    direction: TempoModalSheetDirection,
    onDismissRequest: () -> Unit,
    hasUnsavedChanges: Boolean,
): TempoModalSheetState {
    val currentHasUnsavedChanges by rememberUpdatedState(hasUnsavedChanges)
    val currentOnDismiss by rememberUpdatedState(onDismissRequest)
    val scope = rememberCoroutineScope()
    val currentAxisSizePx by rememberUpdatedState(rememberDismissAxisSizePx(direction))
    val maxSheetHeight = rememberTempoModalSheetMaxHeight(direction, rememberWindowHeightDp())
    val offset = remember { Animatable(direction.hiddenOffset(currentAxisSizePx)) }
    val dismissing = remember { mutableStateOf(false) }
    val showDiscardDialog = remember { mutableStateOf(false) }
    val forceDismiss = remember { mutableStateOf(false) }
    val isExpandedToStatusBar = remember { mutableStateOf(false) }

    lateinit var state: TempoModalSheetState
    val animateRestore: () -> Unit = {
        scope.launch { state.restore() }
    }
    val animateDismiss: () -> Unit = {
        if (!dismissing.value) {
            dismissing.value = true
            scope.launch {
                try {
                    offset.animateTo(
                        direction.hiddenOffset(currentAxisSizePx),
                        animationSpec = tween(TempoMotionTokens.DURATION_SHEET_MILLIS),
                    )
                } finally {
                    currentOnDismiss()
                }
            }
        }
    }
    val onRequestDismiss: () -> Unit = {
        if (currentHasUnsavedChanges && !forceDismiss.value) {
            animateRestore()
            showDiscardDialog.value = true
        } else {
            animateDismiss()
        }
    }

    state =
        TempoModalSheetState(
            direction = direction,
            maxSheetHeight = maxSheetHeight,
            dismissAxisSizePx = currentAxisSizePx,
            offset = offset,
            dismissing = dismissing,
            showDiscardDialog = showDiscardDialog,
            forceDismiss = forceDismiss,
            isExpandedToStatusBar = isExpandedToStatusBar,
            animateDismiss = animateDismiss,
            onRequestDismiss = onRequestDismiss,
        )
    return state
}

@Composable
private fun rememberTempoModalSheetMaxHeight(
    direction: TempoModalSheetDirection,
    screenHeightDp: Dp,
): Dp {
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    return remember(direction, screenHeightDp, statusBarTopPadding) {
        direction.maxHeight(
            screenHeight = screenHeightDp,
            topInset = statusBarTopPadding,
        )
    }
}

/**
 * The dismiss axis is vertical for top/bottom sheets (the window height guarantees the sheet
 * fully exits) and horizontal for side sheets (the sheet's own width is enough to hide it).
 */
@Composable
private fun rememberDismissAxisSizePx(direction: TempoModalSheetDirection): Float {
    val windowSize = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    return if (direction.isHorizontal) {
        with(density) { minOf(SIDE_SHEET_WIDTH.toPx(), windowSize.width.toFloat()) }
    } else {
        windowSize.height.toFloat()
    }
}

@Composable
private fun rememberWindowHeightDp(): Dp =
    with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.height
            .toFloat()
            .toDp()
    }
