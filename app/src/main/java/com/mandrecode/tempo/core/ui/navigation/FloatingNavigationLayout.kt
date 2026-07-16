package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.mandrecode.tempo.core.ui.theme.TempoSpacing

@Composable
internal fun isFloatingNavigationRailLayout(): Boolean =
    currentWindowAdaptiveInfo()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

/**
 * Expanded rail tier: tabs carry labels and the add action shows its label. Requires expanded
 * width plus non-compact height — labels spend vertical space that landscape phones don't have.
 */
@Composable
internal fun isExpandedFloatingRailLayout(): Boolean {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) &&
        windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
}

/**
 * Start clearance top-level screens must reserve for the floating rail in the current window;
 * zero when the bottom bar is used instead.
 */
@Composable
internal fun floatingRailContentClearance(): Dp =
    when {
        !isFloatingNavigationRailLayout() -> 0.dp
        isExpandedFloatingRailLayout() -> FloatingRailExpandedContentStartPadding
        else -> FloatingRailContentStartPadding
    }

@Composable
internal fun floatingNavigationBottomClearancePadding(defaultPadding: Dp): Dp =
    calculateFloatingNavigationBottomClearancePadding(
        defaultPadding = defaultPadding,
        navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
        isRailLayout = isFloatingNavigationRailLayout(),
    )

internal fun calculateFloatingNavigationBottomClearancePadding(
    defaultPadding: Dp,
    navigationBarPadding: Dp,
    isRailLayout: Boolean,
): Dp =
    navigationBarPadding +
        if (isRailLayout) {
            defaultPadding
        } else {
            TempoSpacing.bottomNavHeight + defaultPadding
        }
