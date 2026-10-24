package com.mandrecode.tempo.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
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
 * quickly leaves the space empty instead of flashing the indicator. The space itself is laid out
 * from the start, so nothing around it moves when the indicator appears.
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
        AnimatedVisibility(
            visible = revealed,
            enter = fadeIn(tween(LOADING_INDICATOR_FADE_IN_MILLIS)),
        ) {
            LoadingIndicator(
                modifier =
                    Modifier
                        .size(96.dp)
                        .semantics { contentDescription = message },
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
