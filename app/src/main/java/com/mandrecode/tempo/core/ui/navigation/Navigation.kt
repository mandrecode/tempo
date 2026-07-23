package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.foundation.background
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
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.ui.adaptive.SheetPlacement
import com.mandrecode.tempo.core.ui.adaptive.rememberSheetPlacement
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
object RoutinesEditorRoute : EditorRoute {
    override fun supports(mainRoute: NavKey): Boolean = mainRoute == RoutinesRoute
}

@Serializable
object TasksEditorRoute : EditorRoute {
    override fun supports(mainRoute: NavKey): Boolean = mainRoute == TasksRoute
}

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

    // Not notification-triggered — set when MainActivity is launched from the home-screen
    // quick-add-task widget, to open the same blank task creation sheet as the in-app "+" button.
    data object OpenNewTaskDialog : PendingNotificationAction
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
    onOnboardingActiveChange: (Boolean) -> Unit = {},
) {
    val navigator = rememberTempoNavigator(startDestination)
    var routinesFloatingBarState by remember { mutableStateOf(RoutinesFloatingBarState()) }
    var tasksFloatingBarState by remember { mutableStateOf(TasksFloatingBarState()) }

    // Onboarding (including a Settings-triggered replay) fully replaces the nav display content,
    // so callers hosting overlays above TempoNavHost (e.g. a "what's new" sheet) need this signal
    // to avoid showing on top of it. A replay is pushed onto whichever back stack is currently
    // active (e.g. settingsBackStack) without changing navigator.section, so this must key off the
    // resolved current route rather than the section alone.
    val currentOnboardingActiveChange by rememberUpdatedState(onOnboardingActiveChange)
    LaunchedEffect(navigator.currentRoute) {
        currentOnboardingActiveChange(navigator.currentRoute is OnboardingRoute)
    }

    NotificationNavigationEffects(
        navigator = navigator,
        routinesNavigationTrigger = routinesNavigationTrigger,
        tasksNavigationTrigger = tasksNavigationTrigger,
        onRouteChange = onRouteChange,
    )

    val editorPaneEnabled = rememberSheetPlacement() == SheetPlacement.DockedPane
    val activeEntries =
        rememberActiveEntries(
            navigator = navigator,
            navigationPreferencesRepository = navigationPreferencesRepository,
            pendingNotificationAction = pendingNotificationAction,
            onConsumePendingNotificationAction = onConsumePendingNotificationAction,
            onRoutinesFloatingBarStateChange = { routinesFloatingBarState = it },
            onTasksFloatingBarStateChange = { tasksFloatingBarState = it },
            includeEditorEntries = editorPaneEnabled,
        )
    val editorSceneStrategy = rememberEditorSupportingPaneSceneStrategy()
    val openSettings: () -> Unit = { navigator.navigate(SettingsRoute) }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                // Explicit fill: this Box's own bounds include the safe-drawing inset margin
                // (e.g. a landscape display cutout) and the space the floating rail sits in,
                // neither of which TempoNavDisplay/PersistentFloatingBar paint themselves —
                // left unset, that margin falls through to MainActivity's root Surface
                // (colorScheme.surface), mismatching the top block's tinted color.
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        TempoNavDisplay(
            entries = activeEntries,
            navigator = navigator,
            editorSceneStrategy = editorSceneStrategy,
        )

        SettingsSlideOverlay(
            visible = navigator.currentRoute == SettingsRoute,
            onDismiss = { navigator.pop() },
        ) {
            SettingsDestination(navigator = navigator)
        }

        PersistentFloatingBar(
            currentRoute = navigator.currentRoute,
            topLevelRoute = navigator.topLevelRoute,
            navigationPreferencesRepository = navigationPreferencesRepository,
            routinesState = routinesFloatingBarState,
            tasksState = tasksFloatingBarState,
            onNavigateToTopLevel = navigator::navigateToTopLevel,
            onOpenSettings = openSettings,
            onRouteChange = onRouteChange,
        )
    }
}

@Composable
private fun TempoNavDisplay(
    entries: List<NavEntry<NavKey>>,
    navigator: TempoNavigator,
    editorSceneStrategy: SceneStrategy<NavKey>,
) {
    NavDisplay(
        entries = entries,
        modifier = Modifier.fillMaxSize(),
        onBack = { navigator.pop() },
        sceneStrategies = listOf(editorSceneStrategy),
        transitionSpec = { navigationTransition(initialScene = initialState, targetScene = targetState) },
        popTransitionSpec = { navigationPopTransition(initialScene = initialState, targetScene = targetState) },
    )
}

@Composable
private fun rememberActiveEntries(
    navigator: TempoNavigator,
    navigationPreferencesRepository: NavigationPreferencesRepository,
    pendingNotificationAction: PendingNotificationAction?,
    onConsumePendingNotificationAction: () -> Unit,
    onRoutinesFloatingBarStateChange: (RoutinesFloatingBarState) -> Unit,
    onTasksFloatingBarStateChange: (TasksFloatingBarState) -> Unit,
    includeEditorEntries: Boolean,
): List<NavEntry<NavKey>> {
    val decorators =
        listOf<NavEntryDecorator<NavKey>>(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        )
    val entries =
        entryProvider<NavKey> {
            entry<RoutinesRoute>(metadata = mapOf(EDITOR_MAIN_ROUTE_METADATA to RoutinesRoute)) {
                RoutinesDestination(
                    navigator = navigator,
                    navigationPreferencesRepository = navigationPreferencesRepository,
                    pendingNotificationAction = pendingNotificationAction,
                    onConsumePendingNotificationAction = onConsumePendingNotificationAction,
                    onFloatingBarStateChange = onRoutinesFloatingBarStateChange,
                )
            }
            entry<TasksRoute>(metadata = mapOf(EDITOR_MAIN_ROUTE_METADATA to TasksRoute)) {
                TasksDestination(
                    navigator = navigator,
                    navigationPreferencesRepository = navigationPreferencesRepository,
                    pendingNotificationAction = pendingNotificationAction,
                    onConsumePendingNotificationAction = onConsumePendingNotificationAction,
                    onFloatingBarStateChange = onTasksFloatingBarStateChange,
                )
            }
            entry<OnboardingRoute>(metadata = mapOf(ONBOARDING_ROUTE_METADATA to true)) { route ->
                OnboardingDestination(navigator = navigator, isReplay = route.isReplay)
            }
            // navigator.navigate(SettingsRoute) pushes onto whichever section's back stack is
            // currently active, so this registration is needed for every section's entries, not
            // just settingsBackStack's. It supplies SettingsDestination for SettingsSlideOverlay;
            // the resulting entry is filtered out before reaching NavDisplay (see below) since
            // Settings is never rendered as a NavDisplay scene.
            entry<SettingsRoute>(metadata = mapOf(SETTINGS_ROUTE_METADATA to true)) {
                SettingsDestination(navigator = navigator)
            }
            entry<RoutinesEditorRoute>(metadata = mapOf(EDITOR_ROUTE_METADATA to RoutinesEditorRoute)) {}
            entry<TasksEditorRoute>(metadata = mapOf(EDITOR_ROUTE_METADATA to TasksEditorRoute)) {}
        }
    val routinesEntries = rememberDecoratedNavEntries(navigator.routinesBackStack, decorators, entries)
    val tasksEntries = rememberDecoratedNavEntries(navigator.tasksBackStack, decorators, entries)
    val onboardingEntries = rememberDecoratedNavEntries(navigator.onboardingBackStack, decorators, entries)
    val settingsEntries = rememberDecoratedNavEntries(navigator.settingsBackStack, decorators, entries)
    val sectionEntries =
        when (navigator.section) {
            TempoNavigator.Section.ROUTINES -> routinesEntries
            TempoNavigator.Section.TASKS -> tasksEntries
            TempoNavigator.Section.SETTINGS -> settingsEntries
            TempoNavigator.Section.ONBOARDING -> onboardingEntries
        }
    // Settings slides in as its own overlay (SettingsSlideOverlay) rather than as a NavDisplay
    // scene, so its entry never reaches NavDisplay here regardless of which section is active.
    val entriesWithoutSettings = sectionEntries.filterNot { it.metadata.containsKey(SETTINGS_ROUTE_METADATA) }
    return if (includeEditorEntries) {
        entriesWithoutSettings
    } else {
        entriesWithoutSettings.filterNot { it.metadata.containsKey(EDITOR_ROUTE_METADATA) }
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
        // Always true: routines and tasks share one add action via the app's PersistentFloatingBar
        // (see TasksDestination's matching showAddTaskRailButton), at every window tier
        // including single-tab mode. RoutinesContent's own in-content FAB is suppressed as a
        // result — it only renders when RoutinesContent is used standalone (tests, previews).
        showAddHabitRailButton = true,
        onFloatingBarStateChange = onFloatingBarStateChange,
        pendingNotificationAction = pendingNotificationAction,
        onConsumePendingNotificationAction = onConsumePendingNotificationAction,
        onDockedEditorVisibilityChange = { visible ->
            navigator.setEditorVisible(RoutinesEditorRoute, visible)
        },
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
        onDockedEditorVisibilityChange = { visible ->
            navigator.setEditorVisible(TasksEditorRoute, visible)
        },
    )
}

@Composable
private fun RouteTopBarOrStatusInset(
    title: String,
    onOpenSettings: () -> Unit,
) {
    val isRailLayout = isFloatingNavigationRailLayout()
    if (isExpandedFloatingRailLayout()) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
    } else {
        RouteTopBar(
            title = title,
            onOpenSettings = onOpenSettings,
            showSettingsAction = !isRailLayout,
        )
    }
}

@Composable
private fun RouteTopBar(
    title: String,
    onOpenSettings: () -> Unit,
    showSettingsAction: Boolean,
) {
    val horizontalPadding = MaterialTheme.spacing.large
    val titleStartPadding = horizontalPadding - MaterialTheme.spacing.default
    val settingsEndPadding = horizontalPadding - MaterialTheme.spacing.extraSmall

    TempoTopBar(
        title = title,
        titleModifier = Modifier.padding(start = titleStartPadding),
        actions = {
            if (showSettingsAction) {
                SettingsButton(onClick = onOpenSettings)
                Spacer(modifier = Modifier.width(settingsEndPadding))
            }
        },
    )
}

@Composable
private fun SettingsDestination(navigator: TempoNavigator) {
    val isRailLayout = isFloatingNavigationRailLayout()
    SettingsScreen(
        onBackClick = { navigator.pop() },
        onOnboardingClick = { navigator.navigate(OnboardingRoute(isReplay = true)) },
        showBackButton = !isRailLayout,
        showTitle = !isExpandedFloatingRailLayout(),
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
