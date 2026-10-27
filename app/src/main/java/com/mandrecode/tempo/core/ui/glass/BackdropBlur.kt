package com.mandrecode.tempo.core.ui.glass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import com.mandrecode.tempo.core.ui.theme.LocalTempoGlass
import kotlinx.coroutines.flow.collectLatest

/**
 * How far each open modal wants the app behind it blurred, from 0 (not at all) to 1 (fully), and
 * how far it is blurred right now ([shown]), eased toward the strongest request.
 */
@Stable
class BackdropBlur {
    internal val levels = mutableStateMapOf<Any, Float>()
    internal val shown = Animatable(0f)
}

internal val LocalBackdropBlur = staticCompositionLocalOf<BackdropBlur?> { null }

/**
 * The app's own content, blurred while a modal sheet or an alert is open over it.
 *
 * Done here, on the app's root layer, rather than with the platform's cross-window blur: that one is
 * composited from the window behind, and on some devices it only appeared once that window drew
 * again (a scroll, a tick), so a sheet could open over a sharp app and blur it late. A layer of our
 * own blurs on the frame the sheet asks for it, and follows the sheet as it slides in, is dragged
 * and slides away, so the blur never switches on or off at once.
 *
 * Wrap the app's content in it once; [rememberBackdropBlur] is how a modal asks for it.
 */
@Composable
fun BackdropBlurHost(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val blur = remember { BackdropBlur() }
    val glass = LocalTempoGlass.current
    // Springy enough to keep up with a sheet under the finger; it is what eases a dialog's blur in
    // and out, since a dialog has no slide to follow. Driven here and read only in the layer below,
    // so a sliding sheet redraws the blur without recomposing the app.
    LaunchedEffect(blur, glass.blurEnabled) {
        snapshotFlow { if (glass.blurEnabled) blur.levels.values.maxOrNull() ?: 0f else 0f }
            .collectLatest { target -> blur.shown.animateTo(target, BlurLevelSpring) }
    }
    CompositionLocalProvider(LocalBackdropBlur provides blur) {
        Box(
            modifier =
                modifier.graphicsLayer {
                    val px = glass.windowBlurRadius.toPx() * blur.shown.value
                    renderEffect = if (px > 0f) BlurEffect(px, px, TileMode.Clamp) else null
                },
        ) {
            content()
        }
    }
}

/**
 * Asks the [BackdropBlurHost] to blur the app behind the modal this is called from, as far as
 * [progress] says: a sheet passes how far it is open, so the blur grows as it slides in, eases off
 * as it is dragged down and is gone once it has slid away.
 *
 * Returns whether the blur can show at all, so the caller can thin its scrim and lighten its
 * surface. It cannot below API 31, on low-RAM devices, in previews, or with no host above.
 */
@Composable
fun rememberBackdropBlur(progress: () -> Float = { 1f }): Boolean {
    val blur = LocalBackdropBlur.current
    val glass = LocalTempoGlass.current
    if (blur == null || !glass.blurEnabled) return false
    val key = remember { Any() }
    val currentProgress by rememberUpdatedState(progress)
    LaunchedEffect(blur, key) {
        snapshotFlow { currentProgress().coerceIn(0f, 1f) }.collect { blur.levels[key] = it }
    }
    DisposableEffect(blur, key) {
        onDispose { blur.levels.remove(key) }
    }
    return true
}

/**
 * Glass for an alert dialog: blurs the app behind it and lightens the platform dim as the blur
 * comes in, so the dialog floats over a frosted, softly dimmed app rather than a dark sheet, and is
 * never over a sharp, barely dimmed one while the blur is still easing in. Call it from inside the
 * dialog's content (any slot); it draws nothing.
 */
@Composable
fun DialogWindowGlass() {
    val window = (LocalView.current.parent as? DialogWindowProvider)?.window
    val blurred = rememberBackdropBlur()
    val blur = LocalBackdropBlur.current
    val defaultDim = remember(window) { window?.attributes?.dimAmount }
    LaunchedEffect(window, blur, blurred, defaultDim) {
        if (window == null || defaultDim == null) return@LaunchedEffect
        if (!blurred || blur == null) {
            window.setDimAmount(defaultDim)
            return@LaunchedEffect
        }
        snapshotFlow { blur.shown.value }.collect { level ->
            window.setDimAmount(defaultDim * (1f - (1f - BLURRED_DIALOG_DIM_FRACTION) * level))
        }
    }
}

private const val BLURRED_DIALOG_DIM_FRACTION = 0.55f

private val BlurLevelSpring =
    spring<Float>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
