package com.mandrecode.tempo.core.ui.glass

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import com.mandrecode.tempo.core.ui.theme.LocalTempoGlass
import com.mandrecode.tempo.core.ui.theme.TempoMotionTokens

/** How many open modals are asking for the app behind them to be blurred. */
@Stable
class BackdropBlur {
    internal var requests by mutableIntStateOf(0)
}

internal val LocalBackdropBlur = staticCompositionLocalOf<BackdropBlur?> { null }

/**
 * The app's own content, blurred while a modal sheet or an alert is open over it.
 *
 * Done here, on the app's root layer, rather than with the platform's cross-window blur: that one is
 * composited from the window behind, and on some devices it only appeared once that window drew
 * again (a scroll, a tick), so a sheet could open over a sharp app and blur it late. A layer of our
 * own blurs on the frame the sheet asks for it, and eases in and out with the sheet.
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
    val radius by animateDpAsState(
        targetValue = if (glass.blurEnabled && blur.requests > 0) glass.windowBlurRadius else 0.dp,
        animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS),
        label = "backdropBlur",
    )
    CompositionLocalProvider(LocalBackdropBlur provides blur) {
        Box(
            modifier =
                modifier.graphicsLayer {
                    val px = radius.toPx()
                    renderEffect = if (px > 0f) BlurEffect(px, px, TileMode.Clamp) else null
                },
        ) {
            content()
        }
    }
}

/**
 * Asks the [BackdropBlurHost] to blur the app behind the modal this is called from, for as long as
 * [visible] (a dismissing sheet passes false, so the app comes back into focus as it slides away).
 *
 * Returns whether the blur is showing, so the caller can thin its scrim and lighten its surface
 * only then. It is not below API 31, on low-RAM devices, in previews, or with no host above.
 */
@Composable
fun rememberBackdropBlur(visible: Boolean = true): Boolean {
    val blur = LocalBackdropBlur.current
    val glass = LocalTempoGlass.current
    if (blur == null || !glass.blurEnabled) return false
    DisposableEffect(blur, visible) {
        if (visible) blur.requests++
        onDispose { if (visible) blur.requests-- }
    }
    return visible
}

/**
 * Glass for an alert dialog: blurs the app behind it and lightens the platform dim while the blur
 * shows, so the dialog floats over a frosted, softly dimmed app rather than a dark sheet. Call it
 * from inside the dialog's content (any slot); it draws nothing.
 */
@Composable
fun DialogWindowGlass() {
    val window = (LocalView.current.parent as? DialogWindowProvider)?.window
    val blurred = rememberBackdropBlur()
    val defaultDim = remember(window) { window?.attributes?.dimAmount }
    SideEffect {
        if (window != null && defaultDim != null) {
            window.setDimAmount(if (blurred) defaultDim * BLURRED_DIALOG_DIM_FRACTION else defaultDim)
        }
    }
}

private const val BLURRED_DIALOG_DIM_FRACTION = 0.55f
