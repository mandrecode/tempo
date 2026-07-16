package com.mandrecode.tempo.core.ui.adaptive

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.TempoMotionTokens

internal val DockedEditorWidth = 412.dp
internal val DockedEditorPadding = 12.dp

/** Slides the docked supporting pane in from its trailing edge as it takes the space it reserves. */
internal fun dockedEditorEnterTransition(): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS),
    ) + fadeIn(animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS))

internal fun dockedEditorExitTransition(): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS),
    ) + fadeOut(animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS))
