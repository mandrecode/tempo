package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import com.mandrecode.tempo.core.ui.theme.TempoSpacing

private const val FLOATING_NAVIGATION_RAIL_BREAKPOINT_DP = 600

@Composable
internal fun isFloatingNavigationRailLayout(): Boolean {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    return screenWidthDp >= FLOATING_NAVIGATION_RAIL_BREAKPOINT_DP
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
