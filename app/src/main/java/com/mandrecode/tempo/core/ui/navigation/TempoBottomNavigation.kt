package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.ui.theme.spacing

private data class NavigationItem<T : NavKey>(
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
internal val FloatingToolbarRailSurfacePadding = 8.dp
private val FloatingToolbarShape = RoundedCornerShape(36.dp)

@Composable
fun TempoBottomNavigation(
    currentRoute: NavKey,
    navigationPreferencesRepository: NavigationPreferencesRepository,
    onNavigateToTopLevel: (NavKey) -> Unit,
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
    val isRailLayout = isFloatingNavigationRailLayout()
    val isExpandedRail = isRailLayout && isExpandedFloatingRailLayout()
    val onItemClick: (NavigationItem<*>) -> Unit = { item ->
        navigateTo(item, onNavigateToTopLevel, onRouteChange)
    }

    Surface(
        modifier = modifier,
        shape = FloatingToolbarShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        when {
            isExpandedRail ->
                ExpandedRailPill(
                    items = visibleNavigationItems,
                    currentRoute = currentRoute,
                    onItemClick = onItemClick,
                )

            isRailLayout ->
                CompactRailPill(
                    items = visibleNavigationItems,
                    currentRoute = currentRoute,
                    onItemClick = onItemClick,
                )

            else ->
                BottomBarPill(
                    items = visibleNavigationItems,
                    currentRoute = currentRoute,
                    onItemClick = onItemClick,
                )
        }
    }
}

@Composable
private fun ExpandedRailPill(
    items: List<NavigationItem<*>>,
    currentRoute: NavKey,
    onItemClick: (NavigationItem<*>) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .width(FloatingRailExpandedSurfaceWidth)
                .padding(horizontal = FloatingToolbarRailSurfacePadding, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(FloatingToolbarItemSpacing),
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            ExpandedRailNavigationRow(
                item = item,
                selected = selected,
                onClick = { if (!selected) onItemClick(item) },
            )
        }
    }
}

@Composable
private fun CompactRailPill(
    items: List<NavigationItem<*>>,
    currentRoute: NavKey,
    onItemClick: (NavigationItem<*>) -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = FloatingToolbarRailSurfacePadding, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(FloatingToolbarItemSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            ToolbarNavigationButton(
                item = item,
                selected = selected,
                onClick = { if (!selected) onItemClick(item) },
            )
        }
    }
}

@Composable
private fun BottomBarPill(
    items: List<NavigationItem<*>>,
    currentRoute: NavKey,
    onItemClick: (NavigationItem<*>) -> Unit,
) {
    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(FloatingToolbarItemSpacing, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            ToolbarNavigationButton(
                item = item,
                selected = selected,
                onClick = { if (!selected) onItemClick(item) },
            )
        }
    }
}

private fun navigateTo(
    item: NavigationItem<*>,
    onNavigateToTopLevel: (NavKey) -> Unit,
    onRouteChange: (String) -> Unit,
) {
    onNavigateToTopLevel(item.route)
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

@Composable
private fun ExpandedRailNavigationRow(
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

    Surface(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(FloatingToolbarItemSize),
        shape = CircleShape,
        color =
            if (selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                Color.Transparent
            },
        contentColor =
            if (selected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.default),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
            )
            Text(
                text = stringResource(item.titleRes),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
