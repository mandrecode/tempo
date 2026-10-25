package com.mandrecode.tempo.core.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.TempoSpacing
import kotlinx.coroutines.delay

/**
 * How long a load has to last before the indicator shows at all. Local data almost always arrives
 * well inside this window, and an indicator that appears only to vanish a frame or two later reads
 * as a flash rather than as progress (#438).
 */
const val LOADING_INDICATOR_REVEAL_DELAY_MILLIS = 400L

private const val LOADING_INDICATOR_FADE_IN_MILLIS = 200

/**
 * Full-size loading state shared by the tasks and routines list screens, biased slightly above
 * center: a Material 3 Expressive loading indicator. `message` should already be the resolved,
 * screen-specific string (e.g. "Loading tasks…", "Loading habits…") and is exposed only as an
 * accessibility label.
 *
 * The indicator stays hidden for [revealDelayMillis] and then fades in, so a load that finishes
 * quickly leaves the space empty instead of flashing the indicator. It is measured and laid out
 * from the first frame and only its alpha changes, so a caller that wraps its own size around it
 * (the plan sheet does) does not grow when it appears. Until it shows it is also kept out of the
 * semantics tree, so accessibility services do not announce a load nobody can see.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TempoLoadingIndicator(
    message: String,
    modifier: Modifier = Modifier.fillMaxSize(),
    revealDelayMillis: Long = LOADING_INDICATOR_REVEAL_DELAY_MILLIS,
) {
    var revealed by remember { mutableStateOf(revealDelayMillis <= 0L) }
    if (!revealed) {
        LaunchedEffect(Unit) {
            delay(revealDelayMillis)
            revealed = true
        }
    }

    Box(
        modifier = modifier,
        contentAlignment =
            BiasAlignment(
                horizontalBias = 0f,
                verticalBias = TempoSpacing.CENTERED_CONTENT_VERTICAL_BIAS,
            ),
    ) {
        val alpha by animateFloatAsState(
            targetValue = if (revealed) 1f else 0f,
            animationSpec = tween(LOADING_INDICATOR_FADE_IN_MILLIS),
            label = "loadingIndicatorReveal",
        )
        LoadingIndicator(
            modifier =
                Modifier
                    .size(96.dp)
                    .alpha(alpha)
                    .then(if (revealed) Modifier.semantics { contentDescription = message } else Modifier),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

private const val FIRST_CONTENT_FADE_MILLIS = 250

/**
 * Shows [loading] until the screen's first content is ready, then fades the content in over it
 * instead of swapping it in on one frame (#438). Without the fade, a tab opened for the first time
 * showed its bare background and then the whole screen at once on top — a flash, however short
 * the load. A screen that is already loaded when it first composes shows its content straight
 * away; only the change from loading to loaded animates.
 */
@Composable
fun FirstLoadCrossfade(
    isLoading: Boolean,
    loading: @Composable () -> Unit,
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable () -> Unit,
) {
    Crossfade(
        targetState = isLoading,
        modifier = modifier,
        animationSpec = tween(FIRST_CONTENT_FADE_MILLIS),
        label = "firstLoad",
    ) { stillLoading ->
        if (stillLoading) loading() else content()
    }
}
