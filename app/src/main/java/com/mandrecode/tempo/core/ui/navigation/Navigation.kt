package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.ui.components.SettingsButton
import com.mandrecode.tempo.core.ui.components.TempoTopBar
import com.mandrecode.tempo.core.ui.theme.TempoMotionTokens
import com.mandrecode.tempo.core.ui.theme.spacing
import com.mandrecode.tempo.features.onboarding.presentation.OnboardingContract
import com.mandrecode.tempo.features.onboarding.presentation.OnboardingScreen
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

@Serializable
data class OnboardingRoute(
    val isReplay: Boolean = false,
)

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

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
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
        composable<RoutinesRoute>(
            enterTransition = { onboardingHandoffEnterTransition(initialState.destination) },
        ) {
            RoutinesDestination(
                navController = navController,
                navigationPreferencesRepository = navigationPreferencesRepository,
                pendingNotificationAction = pendingNotificationAction,
                onConsumePendingNotificationAction = onConsumePendingNotificationAction,
                onFloatingBarStateChange = onRoutinesFloatingBarStateChange,
            )
        }

        composable<TasksRoute>(
            enterTransition = { onboardingHandoffEnterTransition(initialState.destination) },
        ) {
            TasksDestination(
                navController = navController,
                navigationPreferencesRepository = navigationPreferencesRepository,
                pendingNotificationAction = pendingNotificationAction,
                onConsumePendingNotificationAction = onConsumePendingNotificationAction,
                onFloatingBarStateChange = onTasksFloatingBarStateChange,
            )
        }

        composable<OnboardingRoute>(
            exitTransition = { onboardingHandoffExitTransition(targetState.destination) },
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<OnboardingRoute>()
            OnboardingDestination(
                navController = navController,
                isReplay = route.isReplay,
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
                popUpTo(navController.topLevelPopUpToId()) {
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
                popUpTo(navController.topLevelPopUpToId()) {
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
    SettingsScreen(
        onBackClick = { navController.popBackStack() },
        onOnboardingClick = {
            navController.navigate(OnboardingRoute(isReplay = true)) {
                launchSingleTop = true
            }
        },
    )
}

@Composable
private fun OnboardingDestination(
    navController: NavHostController,
    isReplay: Boolean,
) {
    OnboardingScreen(
        onExit = { defaultTab ->
            if (isReplay) {
                navController.popBackStack()
            } else {
                val destination =
                    when (defaultTab) {
                        OnboardingContract.DefaultTab.ROUTINES -> RoutinesRoute
                        OnboardingContract.DefaultTab.TASKS -> TasksRoute
                    }
                navController.navigate(destination) {
                    popUpTo<OnboardingRoute> { inclusive = true }
                    launchSingleTop = true
                }
            }
        },
    )
}
