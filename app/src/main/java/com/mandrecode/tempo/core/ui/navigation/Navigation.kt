package com.mandrecode.tempo.core.ui.navigation

import android.widget.Toast
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.ui.components.SettingsButton
import com.mandrecode.tempo.core.ui.components.TempoTopBar
import com.mandrecode.tempo.core.ui.theme.TempoMotionTokens
import com.mandrecode.tempo.core.ui.theme.spacing
import com.mandrecode.tempo.features.routines.presentation.RoutinesScreen
import com.mandrecode.tempo.features.settings.presentation.SettingsScreen
import com.mandrecode.tempo.features.tasks.presentation.TasksScreen
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
object RoutinesRoute

@Serializable
object TasksRoute

@Serializable
object SettingsRoute

sealed interface PendingNotificationAction {
    data class OpenTask(
        val taskId: Long,
        val originalReminderDate: LocalDateTime?,
    ) : PendingNotificationAction

    data class OpenHabit(
        val habitId: Long,
    ) : PendingNotificationAction

    data class OpenHabitChain(
        val chainId: Long,
        val scheduledDate: LocalDate? = null,
    ) : PendingNotificationAction
}

// Route name constants for persistence
const val ROUTINES_ROUTE_NAME = NavigationPreferencesRepository.DEFAULT_TAB_ROUTINES
const val TASKS_ROUTE_NAME = NavigationPreferencesRepository.DEFAULT_TAB_TASKS

private data class NavigationItem<T : Any>(
    val route: T,
    val titleRes: Int,
    val selectedIcon: Int,
    val unselectedIcon: Int,
)

private val navigationItems =
    listOf(
        NavigationItem(
            route = RoutinesRoute,
            titleRes = R.string.routines,
            selectedIcon = R.drawable.ic_routine,
            unselectedIcon = R.drawable.ic_routine_outlined,
        ),
        NavigationItem(
            route = TasksRoute,
            titleRes = R.string.tasks,
            selectedIcon = R.drawable.ic_tasks,
            unselectedIcon = R.drawable.ic_tasks_outlined,
        ),
    )

internal val FloatingToolbarItemSize = 48.dp
internal val FloatingToolbarActionButtonSize = 52.dp
internal val FloatingToolbarItemSpacing = 8.dp
private val FloatingToolbarShape = RoundedCornerShape(36.dp)

@Composable
fun TempoBottomNavigation(
    navController: NavHostController,
    navigationPreferencesRepository: NavigationPreferencesRepository,
    onRouteChange: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isRoutinesTabEnabled by navigationPreferencesRepository
        .isRoutinesTabEnabled()
        .collectAsStateWithLifecycle(initialValue = true)
    val isTasksTabEnabled by navigationPreferencesRepository
        .isTasksTabEnabled()
        .collectAsStateWithLifecycle(initialValue = true)

    val visibleNavigationItems =
        navigationItems.filter { item ->
            when (item.route) {
                RoutinesRoute -> isRoutinesTabEnabled
                TasksRoute -> isTasksTabEnabled
                else -> true
            }
        }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isRailLayout = isFloatingNavigationRailLayout()

    Surface(
        modifier = modifier,
        shape = FloatingToolbarShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        if (isRailLayout) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(FloatingToolbarItemSpacing),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                visibleNavigationItems.forEach { item ->
                    val selected = currentDestination?.hasRoute(item.route::class) == true
                    ToolbarNavigationButton(
                        item = item,
                        selected = selected,
                        onClick = { if (!selected) navigateTo(navController, item, onRouteChange) },
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(FloatingToolbarItemSpacing, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                visibleNavigationItems.forEach { item ->
                    val selected = currentDestination?.hasRoute(item.route::class) == true
                    ToolbarNavigationButton(
                        item = item,
                        selected = selected,
                        onClick = { if (!selected) navigateTo(navController, item, onRouteChange) },
                    )
                }
            }
        }
    }
}

private fun navigateTo(
    navController: NavHostController,
    item: NavigationItem<*>,
    onRouteChange: (String) -> Unit,
) {
    navController.navigate(item.route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
    val routeName =
        when (item.route) {
            RoutinesRoute -> ROUTINES_ROUTE_NAME
            TasksRoute -> TASKS_ROUTE_NAME
            else -> item.route::class.simpleName ?: ""
        }
    if (routeName.isNotEmpty()) {
        onRouteChange(routeName)
    }
}

@Composable
private fun ToolbarNavigationButton(
    item: NavigationItem<*>,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val iconRes =
        if (selected) {
            item.selectedIcon
        } else {
            item.unselectedIcon
        }

    if (selected) {
        FilledIconButton(
            onClick = onClick,
            modifier = Modifier.size(FloatingToolbarItemSize),
            shape = CircleShape,
            colors =
                IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = stringResource(item.titleRes),
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
                painter = painterResource(id = iconRes),
                contentDescription = stringResource(item.titleRes),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TempoNavHost(
    navigationPreferencesRepository: NavigationPreferencesRepository,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    routinesNavigationTrigger: Long = 0L,
    tasksNavigationTrigger: Long = 0L,
    pendingNotificationAction: PendingNotificationAction? = null,
    onConsumePendingNotificationAction: () -> Unit = {},
    startDestination: Any = RoutinesRoute,
    onRouteChange: (String) -> Unit = {},
) {
    var routinesFloatingBarState by remember { mutableStateOf(RoutinesFloatingBarState()) }
    var tasksFloatingBarState by remember { mutableStateOf(TasksFloatingBarState()) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NotificationNavigationEffects(
        navController = navController,
        routinesNavigationTrigger = routinesNavigationTrigger,
        tasksNavigationTrigger = tasksNavigationTrigger,
        onRouteChange = onRouteChange,
    )

    Box(modifier = modifier.fillMaxSize()) {
        TempoNavGraph(
            navController = navController,
            startDestination = startDestination,
            navigationPreferencesRepository = navigationPreferencesRepository,
            pendingNotificationAction = pendingNotificationAction,
            onConsumePendingNotificationAction = onConsumePendingNotificationAction,
            onRoutinesFloatingBarStateChange = { state ->
                if (routinesFloatingBarState != state) {
                    routinesFloatingBarState = state
                }
            },
            onTasksFloatingBarStateChange = { state ->
                if (tasksFloatingBarState != state) {
                    tasksFloatingBarState = state
                }
            },
        )

        PersistentFloatingBar(
            currentDestination = currentDestination,
            navController = navController,
            navigationPreferencesRepository = navigationPreferencesRepository,
            routinesState = routinesFloatingBarState,
            tasksState = tasksFloatingBarState,
            onRouteChange = onRouteChange,
        )
    }
}

@Composable
private fun TempoNavGraph(
    navController: NavHostController,
    startDestination: Any,
    navigationPreferencesRepository: NavigationPreferencesRepository,
    pendingNotificationAction: PendingNotificationAction?,
    onConsumePendingNotificationAction: () -> Unit,
    onRoutinesFloatingBarStateChange: (RoutinesFloatingBarState) -> Unit,
    onTasksFloatingBarStateChange: (TasksFloatingBarState) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize(),
        enterTransition = { fadeIn(animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS)) },
        exitTransition = { fadeOut(animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS)) },
        popEnterTransition = { fadeIn(animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS)) },
        popExitTransition = { fadeOut(animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS)) },
    ) {
        composable<RoutinesRoute> {
            RoutinesDestination(
                navController = navController,
                navigationPreferencesRepository = navigationPreferencesRepository,
                pendingNotificationAction = pendingNotificationAction,
                onConsumePendingNotificationAction = onConsumePendingNotificationAction,
                onFloatingBarStateChange = onRoutinesFloatingBarStateChange,
            )
        }

        composable<TasksRoute> {
            TasksDestination(
                navController = navController,
                navigationPreferencesRepository = navigationPreferencesRepository,
                pendingNotificationAction = pendingNotificationAction,
                onConsumePendingNotificationAction = onConsumePendingNotificationAction,
                onFloatingBarStateChange = onTasksFloatingBarStateChange,
            )
        }

        composable<SettingsRoute>(
            enterTransition = { settingsEnterTransition() },
            exitTransition = { settingsExitTransition() },
            popEnterTransition = { settingsPopEnterTransition() },
            popExitTransition = { settingsExitTransition() },
        ) {
            SettingsDestination(navController = navController)
        }
    }
}

@Composable
private fun NotificationNavigationEffects(
    navController: NavHostController,
    routinesNavigationTrigger: Long,
    tasksNavigationTrigger: Long,
    onRouteChange: (String) -> Unit,
) {
    val currentOnRouteChange by rememberUpdatedState(onRouteChange)

    LaunchedEffect(routinesNavigationTrigger) {
        if (routinesNavigationTrigger > 0) {
            navController.navigate(RoutinesRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
            currentOnRouteChange(ROUTINES_ROUTE_NAME)
        }
    }

    LaunchedEffect(tasksNavigationTrigger) {
        if (tasksNavigationTrigger > 0) {
            navController.navigate(TasksRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
            currentOnRouteChange(TASKS_ROUTE_NAME)
        }
    }
}

@Composable
private fun RoutinesDestination(
    navController: NavHostController,
    navigationPreferencesRepository: NavigationPreferencesRepository,
    pendingNotificationAction: PendingNotificationAction?,
    onConsumePendingNotificationAction: () -> Unit,
    onFloatingBarStateChange: (RoutinesFloatingBarState) -> Unit,
) {
    val isSingleTabMode = rememberIsSingleTabMode(navigationPreferencesRepository)
    RoutinesScreen(
        isSingleTabMode = isSingleTabMode,
        topBar = {
            RouteTopBar(
                title = stringResource(R.string.routines),
                navController = navController,
            )
        },
        showAddHabitRailButton = true,
        onFloatingBarStateChange = onFloatingBarStateChange,
        pendingNotificationAction = pendingNotificationAction,
        onConsumePendingNotificationAction = onConsumePendingNotificationAction,
    )
}

@Composable
private fun TasksDestination(
    navController: NavHostController,
    navigationPreferencesRepository: NavigationPreferencesRepository,
    pendingNotificationAction: PendingNotificationAction?,
    onConsumePendingNotificationAction: () -> Unit,
    onFloatingBarStateChange: (TasksFloatingBarState) -> Unit,
) {
    val isSingleTabMode = rememberIsSingleTabMode(navigationPreferencesRepository)
    TasksScreen(
        isSingleTabMode = isSingleTabMode,
        topBar = {
            RouteTopBar(
                title = stringResource(R.string.tasks),
                navController = navController,
            )
        },
        showAddTaskRailButton = true,
        onFloatingBarStateChange = onFloatingBarStateChange,
        pendingNotificationAction = pendingNotificationAction,
        onConsumePendingNotificationAction = onConsumePendingNotificationAction,
    )
}

@Composable
private fun RouteTopBar(
    title: String,
    navController: NavHostController,
) {
    val horizontalPadding = MaterialTheme.spacing.large
    val titleStartPadding = horizontalPadding - MaterialTheme.spacing.default
    val settingsEndPadding = horizontalPadding - MaterialTheme.spacing.extraSmall

    TempoTopBar(
        title = title,
        titleModifier = Modifier.padding(start = titleStartPadding),
        actions = {
            SettingsButton(
                onClick = {
                    navController.navigate(SettingsRoute) {
                        launchSingleTop = true
                    }
                },
            )
            Spacer(modifier = Modifier.width(settingsEndPadding))
        },
    )
}

@Composable
private fun SettingsDestination(navController: NavHostController) {
    val context = LocalContext.current
    val onboardingNotLiveMessage = stringResource(R.string.onboarding_not_live)

    SettingsScreen(
        onBackClick = { navController.popBackStack() },
        onOnboardingClick = {
            Toast
                .makeText(
                    context,
                    onboardingNotLiveMessage,
                    Toast.LENGTH_SHORT,
                ).show()
        },
    )
}
