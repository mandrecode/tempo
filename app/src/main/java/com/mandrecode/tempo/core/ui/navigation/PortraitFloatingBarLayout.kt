package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey

@Composable
internal fun PersistentPortraitFloatingBar(
    isTasksRoute: Boolean,
    topLevelRoute: NavKey,
    navigationContent: @Composable () -> Unit,
    routinesState: RoutinesFloatingBarState,
    tasksState: TasksFloatingBarState,
    isSingleTabMode: Boolean,
) {
    val addAction = rememberAddAction(topLevelRoute, routinesState, tasksState)
    // Single tab and no add action leaves nothing to draw — don't float an empty surface.
    if (isSingleTabMode && addAction == null) return
    if (isSingleTabMode && addAction != null) {
        PersistentSingleTabPortraitFloatingBar(
            isTasksRoute = isTasksRoute,
            addAction = addAction,
            tasksState = tasksState,
        )
        return
    }

    // The nav pill is the fixed point: it sits on the screen's centre line on every tab, and
    // everything else hangs off it — the tab's contextual actions to its leading edge, the add
    // button to its trailing one. Anchoring the pill rather than centring the whole row is what
    // stops the bar shifting each time a button appears, which is what made the previous layout
    // feel unsettled when switching tabs.
    var pillWidth by remember { mutableIntStateOf(0) }
    var addWidth by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val halfPill = with(density) { pillWidth.toDp() } / 2

    val actionsOffset by animateDpAsState(
        targetValue =
            if (isTasksRoute) {
                -(halfPill + TASK_ACTIONS_WIDTH / 2 + FloatingToolbarItemSpacing)
            } else {
                0.dp
            },
        animationSpec = floatingControlsMotionSpec(),
        label = "shell_task_actions_offset",
    )
    val addOffset by animateDpAsState(
        targetValue = halfPill + with(density) { addWidth.toDp() } / 2 + FloatingToolbarItemSpacing,
        animationSpec = floatingControlsMotionSpec(),
        label = "shell_add_button_offset",
    )

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.offset(x = actionsOffset),
            contentAlignment = Alignment.Center,
        ) {
            TaskActionButtons(tasksState = tasksState, showActions = isTasksRoute)
        }

        Box(
            modifier = Modifier.onSizeChanged { pillWidth = it.width },
            contentAlignment = Alignment.Center,
        ) {
            navigationContent()
        }

        AnchoredAddButton(
            addAction = addAction,
            modifier =
                Modifier
                    .offset(x = addOffset)
                    .onSizeChanged { addWidth = it.width },
        )
    }
}

/** The add action, trailing the nav pill and animating in and out like the contextual buttons. */
@Composable
private fun AnchoredAddButton(
    addAction: AddAction?,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AnimatedVisibility(
            visible = addAction != null,
            enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut(),
        ) {
            addAction?.let { AddActionButton(it) }
        }
    }
}
