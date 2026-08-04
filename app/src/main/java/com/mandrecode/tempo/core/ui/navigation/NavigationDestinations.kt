package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.ui.components.SettingsButton
import com.mandrecode.tempo.core.ui.components.TempoTopBar
import com.mandrecode.tempo.core.ui.theme.spacing
import com.mandrecode.tempo.features.focus.presentation.FocusScreen
import com.mandrecode.tempo.features.focus.presentation.FocusSessionRoute
import com.mandrecode.tempo.features.onboarding.presentation.OnboardingScreen
import com.mandrecode.tempo.features.routines.presentation.RoutinesScreen
import com.mandrecode.tempo.features.routines.presentation.components.VacationModeTitleBadge
import com.mandrecode.tempo.features.settings.presentation.SettingsScreen
import com.mandrecode.tempo.features.tasks.presentation.TasksScreen

@Composable
internal fun FocusDestination(navigator: TempoNavigator) {
    FocusScreen(
        topBar = { isVacationModeActive ->
            RouteTopBarOrStatusInset(
                title = stringResource(R.string.focus),
                onOpenSettings = { navigator.navigate(SettingsRoute) },
                titleBadge =
                    if (isVacationModeActive) {
                        { VacationModeTitleBadge() }
                    } else {
                        null
                    },
            )
        },
        onOpenSession = { navigator.navigate(FocusSessionRoute) },
    )
}

/**
 * The session screen, hosted in the slide-in overlay. It resolves its own [FocusViewModel]: the
 * screen state it needs — the active session and its subtasks — comes from repositories both
 * instances observe, so the two stay in step without sharing a store.
 */
@Composable
internal fun FocusSessionDestination(
    navigator: TempoNavigator,
    onOpenTaskInTasks: (Long) -> Unit,
) {
    FocusSessionRoute(
        onBack = { navigator.pop() },
        onOpenTaskInTasks = { taskId ->
            navigator.pop()
            onOpenTaskInTasks(taskId)
        },
    )
}

@Composable
internal fun RoutinesDestination(
    navigator: TempoNavigator,
    navigationPreferencesRepository: NavigationPreferencesRepository,
    pendingNotificationAction: PendingNotificationAction?,
    onConsumePendingNotificationAction: () -> Unit,
    onFloatingBarStateChange: (RoutinesFloatingBarState) -> Unit,
) {
    val isSingleTabMode = rememberIsSingleTabMode(navigationPreferencesRepository)
    RoutinesScreen(
        isSingleTabMode = isSingleTabMode,
        topBar = { isVacationModeActive ->
            RouteTopBarOrStatusInset(
                title = stringResource(R.string.routines),
                onOpenSettings = { navigator.navigate(SettingsRoute) },
                titleBadge =
                    if (isVacationModeActive) {
                        { VacationModeTitleBadge() }
                    } else {
                        null
                    },
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
internal fun TasksDestination(
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
    titleBadge: @Composable (() -> Unit)? = null,
) {
    val isRailLayout = isFloatingNavigationRailLayout()
    if (isExpandedFloatingRailLayout()) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
    } else {
        RouteTopBar(
            title = title,
            onOpenSettings = onOpenSettings,
            showSettingsAction = !isRailLayout,
            titleBadge = titleBadge,
        )
    }
}

@Composable
private fun RouteTopBar(
    title: String,
    onOpenSettings: () -> Unit,
    showSettingsAction: Boolean,
    titleBadge: @Composable (() -> Unit)? = null,
) {
    val horizontalPadding = MaterialTheme.spacing.large
    val titleStartPadding = horizontalPadding - MaterialTheme.spacing.default
    val settingsEndPadding = horizontalPadding - MaterialTheme.spacing.extraSmall

    TempoTopBar(
        title = title,
        titleModifier = Modifier.padding(start = titleStartPadding),
        titleBadge = titleBadge,
        actions = {
            if (showSettingsAction) {
                SettingsButton(onClick = onOpenSettings)
                Spacer(modifier = Modifier.width(settingsEndPadding))
            }
        },
    )
}

@Composable
internal fun SettingsDestination(navigator: TempoNavigator) {
    val isRailLayout = isFloatingNavigationRailLayout()
    SettingsScreen(
        onBackClick = { navigator.pop() },
        onOnboardingClick = { navigator.navigate(OnboardingRoute(isReplay = true)) },
        showBackButton = !isRailLayout,
        showTitle = !isExpandedFloatingRailLayout(),
    )
}

@Composable
internal fun OnboardingDestination(
    navigator: TempoNavigator,
    isReplay: Boolean,
) {
    OnboardingScreen(
        onExit = { defaultTab ->
            if (isReplay) {
                navigator.pop()
            } else {
                navigator.completeOnboarding(defaultTab.route)
            }
        },
    )
}
