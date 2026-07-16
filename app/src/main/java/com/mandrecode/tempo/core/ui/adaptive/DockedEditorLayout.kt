package com.mandrecode.tempo.core.ui.adaptive

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.TempoMotionTokens

internal val DockedEditorWidth = 412.dp
internal val DockedEditorPadding = 12.dp

/**
 * Grows the docked supporting pane in from its trailing edge. Uses expandHorizontally rather than
 * slideInHorizontally so the reserved width animates too — the sibling weighted content resizes in
 * lockstep instead of snapping to its final width on the first frame while the pane slides over it.
 */
internal fun dockedEditorEnterTransition(): EnterTransition =
    expandHorizontally(
        expandFrom = Alignment.End,
        animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS),
    ) + fadeIn(animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS))

internal fun dockedEditorExitTransition(): ExitTransition =
    shrinkHorizontally(
        shrinkTowards = Alignment.End,
        animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS),
    ) + fadeOut(animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS))
