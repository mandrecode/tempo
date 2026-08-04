package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.mandrecode.tempo.core.ui.adaptive.SheetPlacement.DockedPane
import com.mandrecode.tempo.core.ui.adaptive.rememberSheetPlacement
import com.mandrecode.tempo.core.ui.theme.TempoSpacing

@Composable
internal fun isFloatingNavigationRailLayout(): Boolean =
    currentWindowAdaptiveInfo()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

/**
 * Expanded rail tier: tabs carry labels and the add action shows its label on large windows.
 */
@Composable
internal fun isExpandedFloatingRailLayout(): Boolean = rememberSheetPlacement() == DockedPane

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

/**
 * How far a snackbar must sit above the bottom edge to clear the floating navigation.
 *
 * The bar spans the bottom of the window on compact layouts, so a snackbar left where Material
 * puts it comes up underneath it and loses its own text. On rail layouts the bar is up the side
 * and only the usual breathing room is needed.
 */
@Composable
internal fun floatingNavigationSnackbarBottomPadding(): Dp =
    if (isFloatingNavigationRailLayout()) {
        SnackbarRailBottomPadding
    } else {
        SnackbarFloatingBarBottomPadding
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
