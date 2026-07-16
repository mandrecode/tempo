package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.ui.components.SettingsButton
import com.mandrecode.tempo.core.ui.components.TempoTopBar
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
object RoutinesRoute : NavKey

@Serializable
object TasksRoute : NavKey

@Serializable
object SettingsRoute : NavKey

@Serializable
data class OnboardingRoute(
    val isReplay: Boolean = false,
) : NavKey

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

const val ROUTINES_ROUTE_NAME = NavigationPreferencesRepository.DEFAULT_TAB_ROUTINES
const val TASKS_ROUTE_NAME = NavigationPreferencesRepository.DEFAULT_TAB_TASKS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TempoNavHost(
    navigationPreferencesRepository: NavigationPreferencesRepository,
    modifier: Modifier = Modifier,
    routinesNavigationTrigger: Long = 0L,
    tasksNavigationTrigger: Long = 0L,
    pendingNotificationAction: PendingNotificationAction? = null,
    onConsumePendingNotificationAction: () -> Unit = {},
    startDestination: NavKey = RoutinesRoute,
    onRouteChange: (String) -> Unit = {},
) {
    val navigator = rememberTempoNavigator(startDestination)
    var routinesFloatingBarState by remember { mutableStateOf(RoutinesFloatingBarState()) }
    var tasksFloatingBarState by remember { mutableStateOf(TasksFloatingBarState()) }

    NotificationNavigationEffects(
        navigator = navigator,
        routinesNavigationTrigger = routinesNavigationTrigger,
        tasksNavigationTrigger = tasksNavigationTrigger,
        onRouteChange = onRouteChange,
    )

    val activeEntries =
        rememberActiveEntries(
            navigator = navigator,
            navigationPreferencesRepository = navigationPreferencesRepository,
            pendingNotificationAction = pendingNotificationAction,
            onConsumePendingNotificationAction = onConsumePendingNotificationAction,
            onRoutinesFloatingBarStateChange = { routinesFloatingBarState = it },
            onTasksFloatingBarStateChange = { tasksFloatingBarState = it },
        )

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        NavDisplay(
            entries = activeEntries,
            modifier = Modifier.fillMaxSize(),
            onBack = { navigator.pop() },
            transitionSpec = { navigationTransition(initialState, targetState) },
            popTransitionSpec = { navigationPopTransition(initialState, targetState) },
        )

        PersistentFloatingBar(
            currentRoute = navigator.currentRoute,
            navigationPreferencesRepository = navigationPreferencesRepository,
            routinesState = routinesFloatingBarState,
            tasksState = tasksFloatingBarState,
            onNavigateToTopLevel = navigator::navigateToTopLevel,
            onOpenSettings = { navigator.navigate(SettingsRoute) },
            onRouteChange = onRouteChange,
        )
    }
}

@Composable
private fun rememberActiveEntries(
    navigator: TempoNavigator,
    navigationPreferencesRepository: NavigationPreferencesRepository,
    pendingNotificationAction: PendingNotificationAction?,
    onConsumePendingNotificationAction: () -> Unit,
    onRoutinesFloatingBarStateChange: (RoutinesFloatingBarState) -> Unit,
    onTasksFloatingBarStateChange: (TasksFloatingBarState) -> Unit,
): List<NavEntry<NavKey>> {
    val decorators =
        listOf<NavEntryDecorator<NavKey>>(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        )
    val entries =
        entryProvider<NavKey> {
            entry<RoutinesRoute> {
                RoutinesDestination(
                    navigator = navigator,
                    navigationPreferencesRepository = navigationPreferencesRepository,
                    pendingNotificationAction = pendingNotificationAction,
                    onConsumePendingNotificationAction = onConsumePendingNotificationAction,
                    onFloatingBarStateChange = onRoutinesFloatingBarStateChange,
                )
            }
            entry<TasksRoute> {
                TasksDestination(
                    navigator = navigator,
                    navigationPreferencesRepository = navigationPreferencesRepository,
                    pendingNotificationAction = pendingNotificationAction,
                    onConsumePendingNotificationAction = onConsumePendingNotificationAction,
                    onFloatingBarStateChange = onTasksFloatingBarStateChange,
                )
            }
            entry<OnboardingRoute> { route ->
                OnboardingDestination(navigator = navigator, isReplay = route.isReplay)
            }
            entry<SettingsRoute> { SettingsDestination(navigator = navigator) }
        }
    val routinesEntries = rememberDecoratedNavEntries(navigator.routinesBackStack, decorators, entries)
    val tasksEntries = rememberDecoratedNavEntries(navigator.tasksBackStack, decorators, entries)
    val onboardingEntries = rememberDecoratedNavEntries(navigator.onboardingBackStack, decorators, entries)
    return when (navigator.section) {
        TempoNavigator.Section.ROUTINES -> routinesEntries
        TempoNavigator.Section.TASKS -> tasksEntries
        TempoNavigator.Section.ONBOARDING -> onboardingEntries
    }
}

@Composable
private fun NotificationNavigationEffects(
    navigator: TempoNavigator,
    routinesNavigationTrigger: Long,
    tasksNavigationTrigger: Long,
    onRouteChange: (String) -> Unit,
) {
    val currentOnRouteChange by rememberUpdatedState(onRouteChange)

    LaunchedEffect(routinesNavigationTrigger) {
        if (routinesNavigationTrigger > 0) {
            navigator.navigateToTopLevel(RoutinesRoute)
            currentOnRouteChange(ROUTINES_ROUTE_NAME)
        }
    }

    LaunchedEffect(tasksNavigationTrigger) {
        if (tasksNavigationTrigger > 0) {
            navigator.navigateToTopLevel(TasksRoute)
            currentOnRouteChange(TASKS_ROUTE_NAME)
        }
    }
}

@Composable
private fun RoutinesDestination(
    navigator: TempoNavigator,
    navigationPreferencesRepository: NavigationPreferencesRepository,
    pendingNotificationAction: PendingNotificationAction?,
    onConsumePendingNotificationAction: () -> Unit,
    onFloatingBarStateChange: (RoutinesFloatingBarState) -> Unit,
) {
    val isSingleTabMode = rememberIsSingleTabMode(navigationPreferencesRepository)
    RoutinesScreen(
        isSingleTabMode = isSingleTabMode,
        topBar = {
            RouteTopBarOrStatusInset(
                title = stringResource(R.string.routines),
                onOpenSettings = { navigator.navigate(SettingsRoute) },
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
    navigator: TempoNavigator,
    navigationPreferencesRepository: NavigationPreferencesRepository,
    pendingNotificationAction: PendingNotificationAction?,
    onConsumePendingNotificationAction: () -> Unit,
    onFloatingBarStateChange: (TasksFloatingBarState) -> Unit,
) {
    val isSingleTabMode = rememberIsSingleTabMode(navigationPreferencesRepository)
    TasksScreen(
        isSingleTabMode = isSingleTabMode,
        topBar = {
            RouteTopBarOrStatusInset(
                title = stringResource(R.string.tasks),
                onOpenSettings = { navigator.navigate(SettingsRoute) },
            )
        },
        showAddTaskRailButton = true,
        onFloatingBarStateChange = onFloatingBarStateChange,
        pendingNotificationAction = pendingNotificationAction,
        onConsumePendingNotificationAction = onConsumePendingNotificationAction,
    )
}

@Composable
private fun RouteTopBarOrStatusInset(
    title: String,
    onOpenSettings: () -> Unit,
) {
    if (isExpandedFloatingRailLayout()) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
    } else {
        RouteTopBar(title = title, onOpenSettings = onOpenSettings)
    }
}

@Composable
private fun RouteTopBar(
    title: String,
    onOpenSettings: () -> Unit,
) {
    val horizontalPadding = MaterialTheme.spacing.large
    val titleStartPadding = horizontalPadding - MaterialTheme.spacing.default
    val settingsEndPadding = horizontalPadding - MaterialTheme.spacing.extraSmall

    TempoTopBar(
        title = title,
        titleModifier = Modifier.padding(start = titleStartPadding),
        actions = {
            SettingsButton(onClick = onOpenSettings)
            Spacer(modifier = Modifier.width(settingsEndPadding))
        },
    )
}

@Composable
private fun SettingsDestination(navigator: TempoNavigator) {
    SettingsScreen(
        onBackClick = { navigator.pop() },
        onOnboardingClick = { navigator.navigate(OnboardingRoute(isReplay = true)) },
    )
}

@Composable
private fun OnboardingDestination(
    navigator: TempoNavigator,
    isReplay: Boolean,
) {
    OnboardingScreen(
        onExit = { defaultTab ->
            if (isReplay) {
                navigator.pop()
            } else {
                navigator.completeOnboarding(
                    when (defaultTab) {
                        OnboardingContract.DefaultTab.ROUTINES -> RoutinesRoute
                        OnboardingContract.DefaultTab.TASKS -> TasksRoute
                    },
                )
            }
        },
    )
}
