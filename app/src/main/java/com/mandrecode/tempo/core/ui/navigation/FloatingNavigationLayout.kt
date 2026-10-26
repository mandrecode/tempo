package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.adaptive.SheetPlacement.DockedPane
import com.mandrecode.tempo.core.ui.adaptive.WindowWidthClass
import com.mandrecode.tempo.core.ui.adaptive.currentWindowWidthClass
import com.mandrecode.tempo.core.ui.adaptive.rememberSheetPlacement
import com.mandrecode.tempo.core.ui.theme.TempoSpacing

/** The floating rail replaces the bottom bar from the Medium width class (600 dp) up. */
@Composable
internal fun isFloatingNavigationRailLayout(): Boolean = currentWindowWidthClass().isAtLeast(WindowWidthClass.MEDIUM)

/**
 * Expanded rail tier: tabs carry labels and the add action shows its label on large windows.
 */
@Composable
internal fun isExpandedFloatingRailLayout(): Boolean = rememberSheetPlacement() == DockedPane

/**
 * Start clearance top-level screens must reserve for the floating rail in the current window;
 * zero when the bottom bar is used instead. Animated, like every clearance passed through
 * [animatedRailClearance].
 */
@Composable
internal fun floatingRailContentClearance(): Dp =
    animatedRailClearance(
        when {
            !isFloatingNavigationRailLayout() -> 0.dp
            isExpandedFloatingRailLayout() -> FloatingRailExpandedContentStartPadding
            else -> FloatingRailContentStartPadding
        },
    )

/**
 * Moves screen content to a new rail clearance instead of jumping there (#163). The clearance
 * changes when the window crosses a breakpoint — a foldable opening, a desktop window being
 * resized, split screen — and when an editor docks beside the list; on the same spring the
 * floating controls use, the content slides with the rail rather than snapping under it.
 */
@Composable
internal fun animatedRailClearance(target: Dp): Dp =
    animateDpAsState(
        targetValue = target,
        animationSpec = floatingControlsMotionSpec(),
        label = "railClearance",
    ).value

/**
 * Horizontal bias for [adaptiveScreenContentLayout]: centred normally, and leaning to the end while
 * an editor is docked beside the content. Centring would leave a gap to the pane that depends on the
 * window width (none at 1280 dp, tens of dp at 1600 dp); leaning against it keeps the gutter the
 * pane itself reserves. Animated on the floating controls' spring, like the rail clearance, so the
 * content glides aside with the pane rather than jumping.
 */
@Composable
internal fun animatedDockedContentBias(isEditorDocked: Boolean): Float =
    animateFloatAsState(
        targetValue = if (isEditorDocked) 1f else 0f,
        animationSpec = floatingControlsMotionSpec(),
        label = "dockedContentBias",
    ).value

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
