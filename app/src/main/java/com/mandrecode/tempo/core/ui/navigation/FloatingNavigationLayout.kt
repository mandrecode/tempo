package com.mandrecode.tempo.core.ui.navigation

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
    if (isFloatingNavigationRailLayout()) {
        defaultPadding
    } else {
        TempoSpacing.bottomNavHeight + defaultPadding
    }
