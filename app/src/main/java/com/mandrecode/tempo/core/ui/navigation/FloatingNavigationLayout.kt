package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import com.mandrecode.tempo.core.ui.theme.TempoSpacing

private const val FLOATING_NAVIGATION_RAIL_BREAKPOINT_DP = 600

@Composable
@Suppress("ktlint:standard:function-expression-body")
internal fun isFloatingNavigationRailLayout(): Boolean {
    return LocalConfiguration.current.screenWidthDp >= FLOATING_NAVIGATION_RAIL_BREAKPOINT_DP
}

@Composable
internal fun floatingNavigationBottomClearancePadding(defaultPadding: Dp): Dp =
    if (isFloatingNavigationRailLayout()) {
        defaultPadding
    } else {
        TempoSpacing.bottomNavHeight + defaultPadding
    }
