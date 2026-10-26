package com.mandrecode.tempo.core.ui.adaptive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.TempoMotionTokens

/** Width of the docked editor's surface itself, excluding the gutter and end margin around it. */
internal val DockedEditorWidth = 400.dp

/**
 * The gutter between the screen's content block and the docked editor, also used as the pane's
 * margin from the window's end edge. Matches the gap between the floating rail and the content,
 * so the content block is framed by the same space on both sides.
 */
internal val DockedEditorPadding = 16.dp

/**
 * The docked editor column at the end of a screen's [androidx.compose.foundation.layout.Row],
 * beside the weighted live content.
 *
 * [contentBlockTop] is how far below the status bar the screen's rounded content block starts
 * (the height of the header row above it). The pane starts its surface there too, and like the
 * content block runs to the bottom edge, so the two white surfaces read as one plane split by the
 * gutter rather than a block and a floating card at staggered heights. The gutter is part of the
 * animated width, so it grows in with the pane instead of appearing on the first frame.
 */
@Composable
internal fun RowScope.DockedEditorPane(
    visible: Boolean,
    contentBlockTop: Dp,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = dockedEditorEnterTransition(),
        exit = dockedEditorExitTransition(),
    ) {
        Box(
            modifier =
                Modifier
                    .width(DockedEditorWidth + DockedEditorPadding * 2)
                    .fillMaxHeight()
                    .padding(
                        start = DockedEditorPadding,
                        end = DockedEditorPadding,
                        top = contentBlockTop,
                    ),
        ) {
            content()
        }
    }
}

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
