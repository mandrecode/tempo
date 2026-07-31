package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    // The button that is leaving has to keep drawing itself. The action goes null the instant the
    // route changes, so rendering straight from it left AnimatedVisibility shrinking an empty box:
    // the button blinked out and the gap closed after it. Holding the last one lets the exit
    // animate the button itself, which is what arriving on Focus was missing and leaving it had.
    val exitingAddAction = rememberLastNonNull(addAction)
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

    // The pill and its auxiliary buttons are one group, centred as a whole: when a button joins or
    // leaves, the group re-centres and everything slides together rather than some parts holding
    // still while others move. The buttons collapse to zero width when hidden, so the centring is
    // the layout's own and needs no measured offsets.
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        // No `spacedBy` here, deliberately: it spaces *children*, and a child that has shrunk to
        // zero width is still a child. The gap outlived the button all the way to the frame the
        // node was disposed on, then went in one step — half of it as a jump of the centred group,
        // which is the twitch the transition ended on. Each button carries its own gap instead, so
        // the gap shrinks with the button it belongs to and there is nothing left to drop.
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnimatedVisibility(
                visible = isTasksRoute,
                enter = expandHorizontally(expandFrom = Alignment.End) + fadeIn(),
                exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut(),
            ) {
                TaskActionButtons(
                    tasksState = tasksState,
                    showActions = isTasksRoute,
                    modifier = Modifier.padding(end = FloatingToolbarItemSpacing),
                )
            }

            navigationContent()

            AnimatedVisibility(
                visible = addAction != null,
                enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut(),
            ) {
                exitingAddAction?.let {
                    Box(modifier = Modifier.padding(start = FloatingToolbarItemSpacing)) {
                        AddActionButton(it)
                    }
                }
            }
        }
    }
}
