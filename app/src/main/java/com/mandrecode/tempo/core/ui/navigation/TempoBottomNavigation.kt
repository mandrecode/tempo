package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.mandrecode.tempo.core.domain.model.TempoTab
import com.mandrecode.tempo.core.ui.adaptive.currentWindowWidthDp
import com.mandrecode.tempo.core.ui.glass.glass
import com.mandrecode.tempo.core.ui.outlinedIconRes
import com.mandrecode.tempo.core.ui.selectedIconRes
import com.mandrecode.tempo.core.ui.theme.TempoMotionTokens
import com.mandrecode.tempo.core.ui.theme.spacing
import com.mandrecode.tempo.core.ui.titleRes
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
import com.mandrecode.tempo.features.focus.domain.repository.FocusSessionRepository

private data class NavigationItem(
    val tab: TempoTab,
) {
    val route: NavKey get() = tab.route
    val titleRes: Int get() = tab.titleRes
    val selectedIcon: Int get() = tab.selectedIconRes
    val unselectedIcon: Int get() = tab.outlinedIconRes
}

/**
 * Derived from [TempoTab] rather than hand-listed, so declaration order in the enum is the order
 * shown here and a new tab cannot be half-added.
 */
private val navigationItems = TempoTab.entries.map(::NavigationItem)

internal val FloatingToolbarItemSize = 48.dp

/**
 * The add action is the same size as everything else in the bar, deliberately.
 *
 * Tasks flanks the pill with sort on one side and add on the other, and the group is centred as a
 * whole. That only leaves the pill where it was on a tab with no flanking buttons if the two sides
 * weigh the same: the 4dp this button used to carry over [FloatingToolbarItemSize] landed half of
 * itself on the pill, which slid sideways every time you switched to Tasks and back.
 */
internal val FloatingToolbarActionButtonSize = FloatingToolbarItemSize
internal val FloatingToolbarItemSpacing = 8.dp

/** How far the corners pull in while a bar control is held; shared by the tabs and the chip. */
internal val FloatingToolbarPressedRadius = 12.dp
internal val FloatingToolbarRailSurfacePadding = 8.dp
private val FloatingToolbarShape = RoundedCornerShape(36.dp)

@Composable
fun TempoBottomNavigation(
    currentRoute: NavKey,
    focusSessionRepository: FocusSessionRepository,
    onNavigateToTopLevel: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    onReselect: (NavKey) -> Unit = {},
    onRouteChange: (String) -> Unit = {},
    onOpenSession: () -> Unit = {},
    hasContextualActions: Boolean = false,
) {
    val activeSession by focusSessionRepository.activeSession.collectAsStateWithLifecycle()

    val visibleNavigationItems = navigationItems
    val isRailLayout = isFloatingNavigationRailLayout()
    val isExpandedRail = isRailLayout && isExpandedFloatingRailLayout()

    val sessionSlot =
        sessionSlotFor(
            activeSession = activeSession,
            currentRoute = currentRoute,
            isRailLayout = isRailLayout,
            isExpandedRail = isExpandedRail,
            hasContextualActions = hasContextualActions,
            onOpenSession = onOpenSession,
        )
    // Tapping the tab you are already on goes to that tab's home state (Tasks: the default
    // category, #452) rather than doing nothing.
    // A light tick on every tap, the reselect too: it is a gesture with no visible change of tab.
    val haptic = LocalHapticFeedback.current
    val onItemClick: (NavigationItem) -> Unit = { item ->
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (currentRoute == item.route) {
            onReselect(item.route)
        } else {
            navigateTo(item, onNavigateToTopLevel, onRouteChange)
        }
    }

    Surface(
        modifier = modifier.glass(FloatingToolbarShape),
        shape = FloatingToolbarShape,
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        when {
            isExpandedRail ->
                ExpandedRailPill(
                    items = visibleNavigationItems,
                    currentRoute = currentRoute,
                    onItemClick = onItemClick,
                    sessionSlot = sessionSlot,
                )

            isRailLayout ->
                CompactRailPill(
                    items = visibleNavigationItems,
                    currentRoute = currentRoute,
                    onItemClick = onItemClick,
                    sessionSlot = sessionSlot,
                )

            else ->
                BottomBarPill(
                    items = visibleNavigationItems,
                    currentRoute = currentRoute,
                    onItemClick = onItemClick,
                    sessionSlot = sessionSlot,
                )
        }
    }
}

@Composable
private fun ExpandedRailPill(
    items: List<NavigationItem>,
    currentRoute: NavKey,
    onItemClick: (NavigationItem) -> Unit,
    sessionSlot: (@Composable (Modifier) -> Unit)?,
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
            if (item.tab == TempoTab.FOCUS && sessionSlot != null) {
                sessionSlot(Modifier.fillMaxWidth())
            } else {
                ExpandedRailNavigationRow(
                    item = item,
                    selected = selected,
                    onClick = { onItemClick(item) },
                )
            }
        }
    }
}

@Composable
private fun CompactRailPill(
    items: List<NavigationItem>,
    currentRoute: NavKey,
    onItemClick: (NavigationItem) -> Unit,
    sessionSlot: (@Composable (Modifier) -> Unit)?,
) {
    Column(
        modifier = Modifier.padding(horizontal = FloatingToolbarRailSurfacePadding, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(FloatingToolbarItemSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            if (item.tab == TempoTab.FOCUS && sessionSlot != null) {
                sessionSlot(Modifier)
            } else {
                ToolbarNavigationButton(
                    item = item,
                    selected = selected,
                    onClick = { onItemClick(item) },
                )
            }
        }
    }
}

@Composable
private fun BottomBarPill(
    items: List<NavigationItem>,
    currentRoute: NavKey,
    onItemClick: (NavigationItem) -> Unit,
    sessionSlot: (@Composable (Modifier) -> Unit)?,
) {
    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(FloatingToolbarItemSpacing, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            if (item.tab == TempoTab.FOCUS && sessionSlot != null) {
                sessionSlot(Modifier)
            } else {
                ToolbarNavigationButton(
                    item = item,
                    selected = selected,
                    onClick = { onItemClick(item) },
                    expandsWhenSelected = true,
                )
            }
        }
    }
}

private fun navigateTo(
    item: NavigationItem,
    onNavigateToTopLevel: (NavKey) -> Unit,
    onRouteChange: (String) -> Unit,
) {
    onNavigateToTopLevel(item.route)
    onRouteChange(item.tab.preferenceValue)
}

/**
 * A tab in the floating bar.
 *
 * A [Surface] rather than an icon button so it can carry the same press treatment as everything
 * else in the bar: the corners pull in while held and settle back on release. The session chip has
 * always done this, and a tab that stayed rigid beside it read as the odd one out.
 *
 * The selected tab is filled with the primary colour. On the bottom bar ([expandsWhenSelected]) it
 * also widens to show its name, the Material 3 Expressive navigation item (#423): switching tabs,
 * one pill grows while the other shrinks back to an icon. A narrow rail has no width to grow into,
 * so there the selected tab stays an icon.
 */
@Composable
private fun ToolbarNavigationButton(
    item: NavigationItem,
    selected: Boolean,
    onClick: () -> Unit,
    expandsWhenSelected: Boolean = false,
) {
    val (interactionSource, cornerRadius) =
        rememberPressableButtonAnimation(
            baseRadius = FloatingToolbarItemSize / 2,
            pressedRadius = FloatingToolbarPressedRadius,
        )
    val colors = navigationItemColors(selected)
    val title = stringResource(item.titleRes)

    Surface(
        onClick = onClick,
        modifier = Modifier.height(FloatingToolbarItemSize).widthIn(min = FloatingToolbarItemSize),
        interactionSource = interactionSource,
        shape = RoundedCornerShape(cornerRadius.value),
        color = colors.container,
        contentColor = colors.content,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = NavigationItemIconInset),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter =
                    painterResource(id = if (selected) item.selectedIcon else item.unselectedIcon),
                contentDescription = title,
            )
            AnimatedVisibility(
                visible = selected && expandsWhenSelected,
                enter = expandHorizontally(NavigationLabelSpring) + fadeIn(),
                exit = shrinkHorizontally(NavigationLabelSpring) + fadeOut(),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    // The icon already says the name to TalkBack; reading it twice would be noise.
                    modifier =
                        Modifier
                            .padding(start = NavigationLabelGap, end = NavigationLabelTrailing)
                            .clearAndSetSemantics {},
                )
            }
        }
    }
}

/** The fill and content colours of a navigation item, eased between the two states. */
@Composable
private fun navigationItemColors(selected: Boolean): NavigationItemColors {
    val container by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS),
        label = "navigationItemContainer",
    )
    val content by animateColorAsState(
        targetValue =
            if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(TempoMotionTokens.DURATION_STANDARD_MILLIS),
        label = "navigationItemContent",
    )
    return NavigationItemColors(container, content)
}

private data class NavigationItemColors(
    val container: Color,
    val content: Color,
)

/** Centres the 24dp icon in the item, and keeps that inset when the label opens beside it. */
private val NavigationItemIconInset = (FloatingToolbarItemSize - 24.dp) / 2
private val NavigationLabelGap = 8.dp
private val NavigationLabelTrailing = 4.dp
private val NavigationLabelSpring =
    spring<IntSize>(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)

@Composable
private fun ExpandedRailNavigationRow(
    item: NavigationItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val iconRes =
        if (selected) {
            item.selectedIcon
        } else {
            item.unselectedIcon
        }

    val (interactionSource, cornerRadius) =
        rememberPressableButtonAnimation(
            baseRadius = FloatingToolbarItemSize / 2,
            pressedRadius = FloatingToolbarPressedRadius,
        )
    val colors = navigationItemColors(selected)

    Surface(
        onClick = onClick,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(FloatingToolbarItemSize),
        interactionSource = interactionSource,
        shape = RoundedCornerShape(cornerRadius.value),
        color = colors.container,
        contentColor = colors.content,
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

/**
 * Whether the session slot shows the countdown or only its icon. It shows it wherever it fits:
 *
 * - An expanded rail has wide, labelled rows. A bare icon there would be the one row saying nothing.
 * - A narrow rail is one icon wide by construction, so there is nowhere for a countdown to go —
 *   expanding pushed the pill straight out of the rail and over the content behind it.
 * - The bottom bar shows it, unless it also carries a tab's own action buttons on a phone-wide
 *   screen: with those beside it and the selected tab showing its name, a countdown no longer fits.
 *
 * (On Focus the slot is not a session pill at all, but the ordinary tab.)
 */
internal fun isSessionChipCompact(
    isRailLayout: Boolean,
    isExpandedRail: Boolean,
    hasContextualActions: Boolean,
    hasRoomForCountdown: Boolean,
): Boolean =
    when {
        isExpandedRail -> false
        isRailLayout -> true
        else -> hasContextualActions && !hasRoomForCountdown
    }

/**
 * The screen width (dp) from which the bottom bar fits the countdown beside a tab's own actions:
 * sort and add, three tabs, the selected one showing its name, and the countdown.
 */
internal const val SESSION_COUNTDOWN_ROOMY_WIDTH_DP = 480

/**
 * The Focus tab's slot while a session is running, or null for the ordinary tab. On Focus itself the
 * tab stays an ordinary selected tab: the session card is right there, and the selection moving
 * between tabs should look the same whichever tab it lands on. Away from Focus, the running
 * session takes the Focus tab's slot so it stays in sight.
 */
@Composable
private fun sessionSlotFor(
    activeSession: FocusSession?,
    currentRoute: NavKey,
    isRailLayout: Boolean,
    isExpandedRail: Boolean,
    hasContextualActions: Boolean,
    onOpenSession: () -> Unit,
): (@Composable (Modifier) -> Unit)? {
    val session = activeSession?.takeIf { currentRoute != FocusRoute } ?: return null
    val compact =
        isSessionChipCompact(
            isRailLayout = isRailLayout,
            isExpandedRail = isExpandedRail,
            hasContextualActions = hasContextualActions,
            hasRoomForCountdown = currentWindowWidthDp() >= SESSION_COUNTDOWN_ROOMY_WIDTH_DP,
        )
    return focusSessionSlot(session = session, compact = compact, onOpenSession = onOpenSession)
}

/** The session pill in the Focus tab's slot. */
private fun focusSessionSlot(
    session: FocusSession,
    compact: Boolean,
    onOpenSession: () -> Unit,
): @Composable (Modifier) -> Unit =
    { slotModifier ->
        FocusSessionChip(
            session = session,
            onClick = onOpenSession,
            modifier = slotModifier,
            compact = compact,
        )
    }
