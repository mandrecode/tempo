package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.features.tasks.presentation.components.buttons.ClearCompletedButton
import com.mandrecode.tempo.features.tasks.presentation.components.buttons.SortButton
import com.mandrecode.tempo.features.tasks.presentation.components.sections.SortDropdownMenu

internal val TASK_ACTIONS_BUTTON_SIZE = 48.dp
internal val TASK_ACTIONS_BUTTON_SPACING = 6.dp
internal val TASK_ACTIONS_WIDTH = TASK_ACTIONS_BUTTON_SIZE * 2 + TASK_ACTIONS_BUTTON_SPACING
internal val TASK_SORT_WITH_CLEAR_OFFSET = (TASK_ACTIONS_BUTTON_SIZE + TASK_ACTIONS_BUTTON_SPACING) / 2

@Composable
internal fun TaskActionButtons(
    tasksState: TasksFloatingBarState,
    showActions: Boolean,
) {
    val sortOffset by animateDpAsState(
        targetValue =
            if (tasksState.hasCompletedTasks) {
                TASK_SORT_WITH_CLEAR_OFFSET
            } else {
                0.dp
            },
        animationSpec = floatingControlsMotionSpec(),
        label = "shell_tasks_sort_button_slot_offset",
    )

    Box(
        modifier = Modifier.size(width = TASK_ACTIONS_WIDTH, height = TASK_ACTIONS_BUTTON_SIZE),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = showActions && tasksState.hasCompletedTasks,
            modifier = Modifier.align(Alignment.CenterStart),
            enter = expandHorizontally(expandFrom = Alignment.End) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut(),
        ) {
            ClearCompletedButton(
                onClick = tasksState.onClearCompleted,
            )
        }
        AnimatedVisibility(
            visible = showActions,
            modifier = Modifier.offset(x = sortOffset),
            enter = expandHorizontally(expandFrom = Alignment.End) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut(),
        ) {
            SortButtonWithMenu(tasksState = tasksState)
        }
    }
}

@Composable
internal fun VerticalTaskActionButtons(
    tasksState: TasksFloatingBarState,
    showActions: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(TASK_ACTIONS_BUTTON_SPACING),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(
            visible = showActions,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        ) {
            SortButtonWithMenu(tasksState = tasksState)
        }
        AnimatedVisibility(
            visible = showActions && tasksState.hasCompletedTasks,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        ) {
            ClearCompletedButton(
                onClick = tasksState.onClearCompleted,
            )
        }
    }
}

@Composable
internal fun SortButtonWithMenu(tasksState: TasksFloatingBarState) {
    Box {
        SortButton(
            sortOption = tasksState.sortOption,
            onClick = tasksState.onSort,
        )
        SortDropdownMenu(
            currentSortOption = tasksState.sortOption,
            expanded = tasksState.isSortMenuVisible,
            onDismiss = tasksState.onDismissSortMenu,
            onSelectSortOption = tasksState.onSelectSortOption,
        )
    }
}
