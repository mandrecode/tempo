package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.util.getIconForSortOption
import com.mandrecode.tempo.features.tasks.presentation.components.buttons.SortButton
import com.mandrecode.tempo.features.tasks.presentation.model.SortOption

internal val TASK_ACTIONS_BUTTON_SIZE = 48.dp

@Composable
internal fun TaskActionButtons(
    tasksState: TasksFloatingBarState,
    showActions: Boolean,
    modifier: Modifier = Modifier,
) {
    // Sort sits right against the navigation pill. Clearing completed tasks lives in the list's
    // "Completed" separator, next to the tasks it acts on, so this is the bar's only task action.
    AnimatedVisibility(
        visible = showActions,
        modifier = modifier,
        enter = expandHorizontally(expandFrom = Alignment.End) + fadeIn(),
        exit = shrinkHorizontally(shrinkTowards = Alignment.End) + fadeOut(),
    ) {
        SortButton(
            sortOption = tasksState.sortOption,
            onClick = tasksState.onSort,
        )
    }
}

@Composable
internal fun VerticalTaskActionButtons(
    tasksState: TasksFloatingBarState,
    showActions: Boolean,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
) {
    AnimatedVisibility(
        visible = showActions,
        modifier = modifier,
        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
    ) {
        SortMenuAnchor(tasksState = tasksState, expanded = expanded)
    }
}

@Composable
private fun SortMenuAnchor(
    tasksState: TasksFloatingBarState,
    expanded: Boolean,
) {
    Box {
        SortButton(
            sortOption = tasksState.sortOption,
            onClick = tasksState.onSort,
            expanded = expanded,
        )
        DropdownMenu(
            expanded = tasksState.sortMenuExpanded,
            onDismissRequest = tasksState.onDismissSort,
            shape = RoundedCornerShape(16.dp),
        ) {
            SortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(option.labelResId)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(getIconForSortOption(option)),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    onClick = {
                        tasksState.onSelectSortOption(option)
                        tasksState.onDismissSort()
                    },
                )
            }
        }
    }
}
