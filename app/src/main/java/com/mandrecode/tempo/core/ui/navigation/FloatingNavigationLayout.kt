package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.window.core.layout.WindowSizeClass
import com.mandrecode.tempo.core.ui.theme.TempoSpacing

@Composable
internal fun isFloatingNavigationRailLayout(): Boolean =
    currentWindowAdaptiveInfo()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

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
