package com.mandrecode.tempo.core.ui.navigation

import androidx.activity.ExperimentalActivityApi
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntOffset
import com.mandrecode.tempo.core.ui.theme.TempoMotionTokens
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Slides [content] in from the right edge and back out, driven by a plain [Animatable] offset
 * rather than [androidx.navigation3.ui.NavDisplay]'s scene transitions. The Nav3 scene transition
 * for this route was found to complete with zero visible duration regardless of the configured
 * animation spec (root-caused to the decorated entries list losing referential stability across
 * recompositions, which restarts the underlying SeekableTransitionState animation before it can
 * render a frame) — this overlay sidesteps that by keeping the destination out of the NavDisplay
 * entries entirely and animating it here instead, the same proven pattern [TempoModalSheet] uses.
 */
@OptIn(ExperimentalActivityApi::class)
@Composable
internal fun SettingsSlideOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val scope = rememberCoroutineScope()
    val widthPx =
        with(LocalDensity.current) {
            LocalWindowInfo.current.containerSize.width
                .toFloat()
        }
    val offsetX = remember { Animatable(widthPx) }
    var isComposed by remember { mutableStateOf(visible) }

    LaunchedEffect(visible, widthPx) {
        if (visible) {
            isComposed = true
            offsetX.animateTo(0f, animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS))
        } else if (isComposed) {
            offsetX.animateTo(widthPx, animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS))
            isComposed = false
        } else {
            // Already hidden: keep the off-screen position in sync with the current width so a
            // later open still slides in from fully off-screen after a resize/rotation.
            offsetX.snapTo(widthPx)
        }
    }

    if (!isComposed) return

    if (visible) {
        PredictiveBackHandler { progress ->
            try {
                progress.collect { backEvent -> offsetX.snapTo(backEvent.progress * widthPx) }
                currentOnDismiss()
            } catch (cancellation: CancellationException) {
                // Restore on a separate scope: this coroutine is already cancelled at this point,
                // so animateTo here would itself be cancelled before completing.
                scope.launch {
                    offsetX.animateTo(0f, animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS))
                }
                throw cancellation
            }
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .offset { IntOffset(x = offsetX.value.roundToInt(), y = 0) },
    ) {
        content()
    }
}
