package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.features.tasks.presentation.components.buttons.ClearCompletedButton
import com.mandrecode.tempo.features.tasks.presentation.components.buttons.SortButton

private val TASK_ACTIONS_TO_NAV_OFFSET = 126.dp
private val TASK_ACTIONS_WITH_CLEAR_TO_NAV_OFFSET = 153.dp
private val TASK_ACTIONS_LANDSCAPE_TO_NAV_OFFSET = 88.dp
private val TASK_ACTIONS_CENTERING_OFFSET = 29.dp
private val TASK_ACTIONS_WITH_CLEAR_CENTERING_OFFSET = 58.dp
private val TASK_ACTIONS_BUTTON_SIZE = 48.dp
private val TASK_ACTIONS_BUTTON_SPACING = 6.dp
private val TASK_ACTIONS_WIDTH = TASK_ACTIONS_BUTTON_SIZE * 2 + TASK_ACTIONS_BUTTON_SPACING
private val TASK_ACTIONS_HEIGHT = TASK_ACTIONS_BUTTON_SIZE * 2 + TASK_ACTIONS_BUTTON_SPACING
private val TASK_SORT_WITH_CLEAR_OFFSET = (TASK_ACTIONS_BUTTON_SIZE + TASK_ACTIONS_BUTTON_SPACING) / 2

internal fun floatingControlsMotionSpec() =
    spring<Dp>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

@Composable
internal fun PersistentFloatingBar(
    currentDestination: NavDestination?,
    navController: NavHostController,
    navigationPreferencesRepository: NavigationPreferencesRepository,
    routinesState: RoutinesFloatingBarState,
    tasksState: TasksFloatingBarState,
    onRouteChange: (String) -> Unit,
) {
    val isRailLayout = isFloatingNavigationRailLayout()
    val isSingleTabMode = rememberIsSingleTabMode(navigationPreferencesRepository)
    val isTasksRoute = currentDestination?.hasRoute(TasksRoute::class) == true
    val visible =
        when {
            currentDestination?.hasRoute(RoutinesRoute::class) == true -> routinesState.visible
            isTasksRoute -> tasksState.visible
            else -> false
        }

    if (!visible) return

    val navigationContent: @Composable () -> Unit = {
        TempoBottomNavigation(
            navController = navController,
            navigationPreferencesRepository = navigationPreferencesRepository,
            onRouteChange = onRouteChange,
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment =
            if (isRailLayout) {
                Alignment.CenterStart
            } else {
                Alignment.BottomCenter
            },
    ) {
        if (isRailLayout) {
            PersistentLandscapeFloatingBar(
                isTasksRoute = isTasksRoute,
                navigationContent = navigationContent,
                routinesState = routinesState,
                tasksState = tasksState,
                isSingleTabMode = isSingleTabMode,
            )
        } else {
            PersistentPortraitFloatingBar(
                isTasksRoute = isTasksRoute,
                navigationContent = navigationContent,
                routinesState = routinesState,
                tasksState = tasksState,
                isSingleTabMode = isSingleTabMode,
            )
        }
    }
}

@Composable
private fun PersistentLandscapeFloatingBar(
    isTasksRoute: Boolean,
    navigationContent: @Composable () -> Unit,
    routinesState: RoutinesFloatingBarState,
    tasksState: TasksFloatingBarState,
    isSingleTabMode: Boolean,
) {
    val addAction = rememberAddAction(isTasksRoute, routinesState, tasksState)
    if (isSingleTabMode) {
        PersistentSingleTabPortraitFloatingBar(
            isTasksRoute = isTasksRoute,
            addAction = addAction,
            tasksState = tasksState,
        )
        return
    }

    val (barOffset, actionOffset) =
        taskFloatingOffsets(
            showTaskActions = isTasksRoute,
            hasCompletedTasks = tasksState.hasCompletedTasks,
        )

    Box(
        modifier =
            Modifier
                .fillMaxHeight()
                .navigationBarsPadding()
                .padding(start = 56.dp, top = 16.dp, bottom = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        FloatingBarMainControls(
            isSingleTabMode = isSingleTabMode,
            addAction = addAction,
            navigationContent = navigationContent,
            orientation = FloatingBarOrientation.Vertical,
            modifier = Modifier.offset(y = barOffset),
        )

        Box(
            modifier = Modifier.offset(y = barOffset - actionOffset),
            contentAlignment = Alignment.Center,
        ) {
            VerticalTaskActionButtons(
                tasksState = tasksState,
                showActions = isTasksRoute,
            )
        }
    }
}

@Composable
private fun PersistentSingleTabPortraitFloatingBar(
    isTasksRoute: Boolean,
    addAction: AddAction,
    tasksState: TasksFloatingBarState,
) {
    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        val visibleTaskActionsWidth =
            if (tasksState.hasCompletedTasks) {
                TASK_ACTIONS_WIDTH
            } else {
                TASK_ACTIONS_BUTTON_SIZE
            }
        val layout =
            rememberSingleTabFloatingBarLayout(
                maxWidth = maxWidth,
                isTasksRoute = isTasksRoute,
                compact = addAction.compact,
                visibleTaskActionsWidth = visibleTaskActionsWidth,
            )

        TempoSoloActionButton(
            iconRes = R.drawable.ic_add,
            label = addAction.label,
            expanded = !addAction.compact,
            onClick = addAction.onClick,
            modifier =
                Modifier
                    .offset(x = layout.addOffset)
                    .onSizeChanged { size ->
                        if (!addAction.compact) {
                            layout.onExpandedAddButtonMeasured(size.width)
                        }
                    },
        )

        Box(
            modifier = Modifier.offset(x = layout.actionOffset),
            contentAlignment = Alignment.Center,
        ) {
            TaskActionButtons(
                tasksState = tasksState,
                showActions = isTasksRoute,
            )
        }
    }
}

@Composable
private fun PersistentPortraitFloatingBar(
    isTasksRoute: Boolean,
    navigationContent: @Composable () -> Unit,
    routinesState: RoutinesFloatingBarState,
    tasksState: TasksFloatingBarState,
    isSingleTabMode: Boolean,
) {
    val addAction = rememberAddAction(isTasksRoute, routinesState, tasksState)
    if (isSingleTabMode) {
        PersistentSingleTabPortraitFloatingBar(
            isTasksRoute = isTasksRoute,
            addAction = addAction,
            tasksState = tasksState,
        )
        return
    }

    val (barOffset, actionOffset) =
        taskFloatingOffsets(
            showTaskActions = isTasksRoute,
            hasCompletedTasks = tasksState.hasCompletedTasks,
        )

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        FloatingBarMainControls(
            isSingleTabMode = isSingleTabMode,
            addAction = addAction,
            navigationContent = navigationContent,
            orientation = FloatingBarOrientation.Horizontal,
            modifier = Modifier.offset(x = barOffset),
        )

        Box(
            modifier = Modifier.offset(x = barOffset - actionOffset),
            contentAlignment = Alignment.Center,
        ) {
            TaskActionButtons(
                tasksState = tasksState,
                showActions = isTasksRoute,
            )
        }
    }
}

@Composable
private fun FloatingBarMainControls(
    isSingleTabMode: Boolean,
    addAction: AddAction,
    navigationContent: @Composable () -> Unit,
    orientation: FloatingBarOrientation,
    modifier: Modifier = Modifier,
) {
    val stableNavigationContent = remember(navigationContent) { movableContentOf(navigationContent) }

    if (orientation == FloatingBarOrientation.Vertical) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(FloatingToolbarItemSpacing),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (isSingleTabMode) {
                TempoSoloActionButton(
                    iconRes = R.drawable.ic_add,
                    label = addAction.label,
                    expanded = !addAction.compact,
                    onClick = addAction.onClick,
                )
            } else {
                stableNavigationContent()
                AddActionButton(addAction)
            }
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(FloatingToolbarItemSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isSingleTabMode) {
                TempoSoloActionButton(
                    iconRes = R.drawable.ic_add,
                    label = addAction.label,
                    expanded = !addAction.compact,
                    onClick = addAction.onClick,
                )
            } else {
                stableNavigationContent()
                AddActionButton(addAction)
            }
        }
    }
}

private enum class FloatingBarOrientation {
    Horizontal,
    Vertical,
}

@Composable
private fun AddActionButton(addAction: AddAction) {
    TempoBottomRailActionButton(
        iconRes = R.drawable.ic_add,
        contentDescription = addAction.label,
        onClick = addAction.onClick,
    )
}

@Composable
private fun rememberAddAction(
    isTasksRoute: Boolean,
    routinesState: RoutinesFloatingBarState,
    tasksState: TasksFloatingBarState,
): AddAction =
    if (isTasksRoute) {
        AddAction(
            label = stringResource(R.string.add_task),
            compact = tasksState.compactSoloAction,
            onClick = tasksState.onAddTask,
        )
    } else {
        AddAction(
            label = stringResource(R.string.add_habit),
            compact = routinesState.compactSoloAction,
            onClick = routinesState.onAddHabit,
        )
    }

private data class AddAction(
    val label: String,
    val compact: Boolean,
    val onClick: () -> Unit,
)

@Composable
private fun taskFloatingOffsets(
    showTaskActions: Boolean,
    hasCompletedTasks: Boolean,
): Pair<Dp, Dp> {
    val targetActionOffset =
        when {
            !showTaskActions -> 0.dp
            hasCompletedTasks -> TASK_ACTIONS_WITH_CLEAR_TO_NAV_OFFSET
            else -> TASK_ACTIONS_TO_NAV_OFFSET
        }
    val actionOffset by animateDpAsState(
        targetValue = targetActionOffset,
        animationSpec = floatingControlsMotionSpec(),
        label = "shell_task_action_buttons_offset",
    )
    val barOffset by animateDpAsState(
        targetValue =
            when {
                !showTaskActions -> 0.dp
                hasCompletedTasks -> TASK_ACTIONS_WITH_CLEAR_CENTERING_OFFSET
                else -> TASK_ACTIONS_CENTERING_OFFSET
            },
        animationSpec = floatingControlsMotionSpec(),
        label = "shell_floating_bar_centering_offset",
    )
    return barOffset to actionOffset
}

@Composable
private fun TaskActionButtons(
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
            SortButton(
                sortOption = tasksState.sortOption,
                onClick = tasksState.onSort,
            )
        }
    }
}

@Composable
private fun VerticalTaskActionButtons(
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
        label = "shell_landscape_tasks_sort_button_slot_offset",
    )

    Box(
        modifier = Modifier.size(width = TASK_ACTIONS_BUTTON_SIZE, height = TASK_ACTIONS_HEIGHT),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = showActions && tasksState.hasCompletedTasks,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
        ) {
            ClearCompletedButton(
                onClick = tasksState.onClearCompleted,
            )
        }
        AnimatedVisibility(
            visible = showActions,
            modifier = Modifier.offset(y = sortOffset),
            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
        ) {
            SortButton(
                sortOption = tasksState.sortOption,
                onClick = tasksState.onSort,
            )
        }
    }
}
