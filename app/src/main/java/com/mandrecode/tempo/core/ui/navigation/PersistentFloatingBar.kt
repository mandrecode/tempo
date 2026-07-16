package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.ui.theme.topBarTitle

private val TASK_ACTIONS_TO_NAV_OFFSET = 126.dp
private val TASK_ACTIONS_WITH_CLEAR_TO_NAV_OFFSET = 153.dp
private val TASK_ACTIONS_CENTERING_OFFSET = 29.dp
private val TASK_ACTIONS_WITH_CLEAR_CENTERING_OFFSET = 58.dp

internal fun floatingControlsMotionSpec() =
    spring<Dp>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
    )

@Composable
internal fun PersistentFloatingBar(
    currentRoute: NavKey,
    topLevelRoute: NavKey,
    navigationPreferencesRepository: NavigationPreferencesRepository,
    routinesState: RoutinesFloatingBarState,
    tasksState: TasksFloatingBarState,
    onNavigateToTopLevel: (NavKey) -> Unit,
    onOpenSettings: () -> Unit,
    onRouteChange: (String) -> Unit,
) {
    val isRailLayout = isFloatingNavigationRailLayout()
    val isSingleTabMode = rememberIsSingleTabMode(navigationPreferencesRepository)
    val isTasksRoute = topLevelRoute == TasksRoute
    val isRailSettingsDestination = currentRoute == SettingsRoute && isRailLayout
    val visible =
        when {
            currentRoute == RoutinesRoute -> routinesState.visible
            currentRoute == TasksRoute -> tasksState.visible
            isRailSettingsDestination -> true
            else -> false
        }

    if (!visible) return

    val navigationContent: @Composable () -> Unit = {
        TempoBottomNavigation(
            currentRoute = currentRoute,
            navigationPreferencesRepository = navigationPreferencesRepository,
            onNavigateToTopLevel = onNavigateToTopLevel,
            onRouteChange = onRouteChange,
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment =
            if (isRailLayout) {
                Alignment.TopStart
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
                onOpenSettings = onOpenSettings,
                settingsSelected = currentRoute == SettingsRoute,
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
    onOpenSettings: () -> Unit,
    settingsSelected: Boolean,
) {
    val addAction = rememberAddAction(isTasksRoute, routinesState, tasksState)
    val isExpandedRail = isExpandedFloatingRailLayout()

    // Rail hierarchy: app identity first on expanded rails, then the primary add action,
    // navigation tabs, contextual secondary actions (sort, clear completed), and finally
    // settings pinned to the bottom.
    Column(
        modifier =
            Modifier
                .fillMaxHeight()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = FloatingRailStartPadding, top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(FloatingToolbarItemSpacing),
        horizontalAlignment = if (isExpandedRail) Alignment.Start else Alignment.CenterHorizontally,
    ) {
        if (isExpandedRail) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.topBarTitle,
                color = MaterialTheme.colorScheme.primary,
                modifier =
                    Modifier.padding(
                        start = FloatingToolbarRailSurfacePadding,
                        bottom = FloatingToolbarItemSpacing,
                    ),
            )
            if (!settingsSelected) {
                TempoSoloActionButton(
                    iconRes = R.drawable.ic_add,
                    label = addAction.label,
                    expanded = true,
                    onClick = addAction.onClick,
                )
            }
        } else if (!settingsSelected) {
            AddActionButton(addAction)
        }

        navigationContent()

        VerticalTaskActionButtons(
            tasksState = tasksState,
            showActions = isTasksRoute && !settingsSelected,
            modifier = Modifier.padding(start = if (isExpandedRail) FloatingToolbarRailSurfacePadding else 0.dp),
            expanded = isExpandedRail,
        )

        Spacer(modifier = Modifier.weight(1f))
        SettingsRailButton(
            selected = settingsSelected,
            expanded = isExpandedRail,
            onClick = onOpenSettings,
        )
    }
}

@Composable
private fun SettingsRailButton(
    selected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    if (!expanded) {
        CompactSettingsRailButton(selected = selected, onClick = onClick)
        return
    }

    Surface(
        onClick = { if (!selected) onClick() },
        shape = CircleShape,
        color =
            if (selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
        contentColor =
            if (selected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        modifier =
            Modifier
                .width(FloatingRailExpandedSurfaceWidth)
                .height(FloatingToolbarItemSize),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = null,
            )
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun CompactSettingsRailButton(
    selected: Boolean,
    onClick: () -> Unit,
) {
    if (selected) {
        FilledIconButton(
            onClick = {},
            modifier = Modifier.size(FloatingToolbarItemSize),
            shape = CircleShape,
            colors =
                IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = stringResource(R.string.settings),
            )
        }
    } else {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(FloatingToolbarItemSize),
            colors =
                IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_settings),
                contentDescription = stringResource(R.string.settings),
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
    modifier: Modifier = Modifier,
) {
    val stableNavigationContent = remember(navigationContent) { movableContentOf(navigationContent) }

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
