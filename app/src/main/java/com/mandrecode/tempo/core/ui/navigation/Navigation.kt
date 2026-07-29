package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.domain.model.TempoTab
import com.mandrecode.tempo.core.ui.adaptive.SheetPlacement
import com.mandrecode.tempo.core.ui.adaptive.rememberSheetPlacement
import com.mandrecode.tempo.features.focus.domain.repository.FocusSessionRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
object FocusRoute : NavKey

@Serializable
object RoutinesRoute : NavKey

@Serializable
object TasksRoute : NavKey

@Serializable
object SettingsRoute : NavKey

/**
 * The running focus session, presented as a slide-in overlay like Settings rather than a tab
 * destination — it is somewhere the user goes and comes back from.
 */
@Serializable
object FocusSessionRoute : NavKey

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TempoNavHost(
    navigationPreferencesRepository: NavigationPreferencesRepository,
    focusSessionRepository: FocusSessionRepository,
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

    // windowInsetsPadding, not a plain padding(startInset, endInset): it also marks this
    // horizontal inset as consumed for descendants, so NavDisplay/Scaffold don't apply the
    // same safe-drawing inset a second time internally (a plain padding() modifier doesn't
    // consume anything, which doubled this offset and visibly displaced the app-bar title).
    val insetPaddingModifier =
        Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        HorizontalInsetMarginStrips()

        TempoNavDisplay(
            entries = activeEntries,
            navigator = navigator,
            editorSceneStrategy = editorSceneStrategy,
            modifier = insetPaddingModifier,
        )

        SlideOverlays(navigator = navigator)

        PersistentFloatingBar(
            modifier = insetPaddingModifier,
            currentRoute = navigator.currentRoute,
            topLevelRoute = navigator.topLevelRoute,
            navigationPreferencesRepository = navigationPreferencesRepository,
            focusSessionRepository = focusSessionRepository,
            routinesState = routinesFloatingBarState,
            tasksState = tasksFloatingBarState,
            onNavigateToTopLevel = navigator::navigateToTopLevel,
            onOpenSettings = openSettings,
            onRouteChange = onRouteChange,
        )
    }
}

/**
 * Tinted fill for the safe-drawing horizontal inset (e.g. a landscape display cutout), painted
 * separately from TempoNavDisplay rather than shared with it as a background: TempoNavDisplay
 * crossfades Tasks/Routines by alpha-blending each scene, and MainActivity's root Surface is
 * deliberately colorScheme.surface so that blend doesn't flash a tinted backdrop. A
 * colorScheme.background fill behind TempoNavDisplay itself would become the crossfade's blend
 * target instead and reintroduce that flash — so only these margin strips get it.
 */
@Composable
private fun BoxScope.HorizontalInsetMarginStrips() {
    val layoutDirection = LocalLayoutDirection.current
    val horizontalInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues()
    val startInset = horizontalInsets.calculateStartPadding(layoutDirection)
    val endInset = horizontalInsets.calculateEndPadding(layoutDirection)

    if (startInset > 0.dp) {
        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .width(startInset)
                    .align(Alignment.CenterStart)
                    .background(MaterialTheme.colorScheme.background),
        )
    }
    if (endInset > 0.dp) {
        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .width(endInset)
                    .align(Alignment.CenterEnd)
                    .background(MaterialTheme.colorScheme.background),
        )
    }
}

/**
 * Settings and the focus session both present as slide-in overlays rather than NavDisplay scenes,
 * for the reason documented on [SettingsSlideOverlay].
 */
@Composable
private fun BoxScope.SlideOverlays(navigator: TempoNavigator) {
    // Computed here rather than passed in: two sibling overlays sharing one caller-supplied
    // modifier trips the "modifiers are used once, by the root layout" rule.
    val insetPadding =
        Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))

    SettingsSlideOverlay(
        visible = navigator.currentRoute == SettingsRoute,
        onDismiss = { navigator.pop() },
        modifier = insetPadding,
    ) {
        SettingsDestination(navigator = navigator)
    }

    SettingsSlideOverlay(
        visible = navigator.currentRoute == FocusSessionRoute,
        onDismiss = { navigator.pop() },
        modifier = insetPadding,
    ) {
        FocusSessionDestination(navigator = navigator)
    }
}

@Composable
private fun TempoNavDisplay(
    entries: List<NavEntry<NavKey>>,
    navigator: TempoNavigator,
    editorSceneStrategy: SceneStrategy<NavKey>,
    modifier: Modifier = Modifier,
) {
    NavDisplay(
        entries = entries,
        modifier = modifier.fillMaxSize(),
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
        rememberNavEntryProvider(
            navigator,
            navigationPreferencesRepository,
            pendingNotificationAction,
            onConsumePendingNotificationAction,
            onRoutinesFloatingBarStateChange,
            onTasksFloatingBarStateChange,
        )

    val focusEntries = rememberDecoratedNavEntries(navigator.focusBackStack, decorators, entries)
    val routinesEntries = rememberDecoratedNavEntries(navigator.routinesBackStack, decorators, entries)
    val tasksEntries = rememberDecoratedNavEntries(navigator.tasksBackStack, decorators, entries)
    val onboardingEntries = rememberDecoratedNavEntries(navigator.onboardingBackStack, decorators, entries)
    val settingsEntries = rememberDecoratedNavEntries(navigator.settingsBackStack, decorators, entries)
    val sectionEntries =
        when (navigator.section) {
            TempoNavigator.Section.FOCUS -> focusEntries
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
private fun rememberNavEntryProvider(
    navigator: TempoNavigator,
    navigationPreferencesRepository: NavigationPreferencesRepository,
    pendingNotificationAction: PendingNotificationAction?,
    onConsumePendingNotificationAction: () -> Unit,
    onRoutinesFloatingBarStateChange: (RoutinesFloatingBarState) -> Unit,
    onTasksFloatingBarStateChange: (TasksFloatingBarState) -> Unit,
) = entryProvider<NavKey> {
    entry<FocusRoute>(metadata = mapOf(EDITOR_MAIN_ROUTE_METADATA to FocusRoute)) {
        FocusDestination(navigator = navigator)
    }
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
    entry<FocusSessionRoute>(metadata = mapOf(SETTINGS_ROUTE_METADATA to true)) {
        FocusSessionDestination(navigator = navigator)
    }
    entry<RoutinesEditorRoute>(metadata = mapOf(EDITOR_ROUTE_METADATA to RoutinesEditorRoute)) {}
    entry<TasksEditorRoute>(metadata = mapOf(EDITOR_ROUTE_METADATA to TasksEditorRoute)) {}
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
            currentOnRouteChange(TempoTab.ROUTINES.preferenceValue)
        }
    }

    LaunchedEffect(tasksNavigationTrigger) {
        if (tasksNavigationTrigger > 0) {
            navigator.navigateToTopLevel(TasksRoute)
            currentOnRouteChange(TempoTab.TASKS.preferenceValue)
        }
    }
}
