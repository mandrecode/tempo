package com.mandrecode.tempo.core.ui.adaptive

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.window.core.layout.WindowSizeClass

/**
 * The Material 3 window width classes, including the Large and Extra-large classes added in
 * Jetpack WindowManager 1.5 (#163). Every adaptive decision in the app — rail or bottom bar,
 * bottom sheet or docked pane — reads one of these, so two parts of the screen can never disagree
 * about which side of a breakpoint the window is on.
 */
enum class WindowWidthClass {
    /** Under 600 dp: phones in portrait. Bottom bar, bottom sheets. */
    COMPACT,

    /** 600–839 dp: foldables, small tablets, phones in landscape. Floating rail. */
    MEDIUM,

    /** 840–1199 dp: tablets in landscape. Floating rail. */
    EXPANDED,

    /** 1200–1599 dp: large tablets, desktop windows. Expanded rail, editors docked beside content. */
    LARGE,

    /** 1600 dp and up: desktop and external displays. As Large. */
    EXTRA_LARGE,
    ;

    fun isAtLeast(other: WindowWidthClass): Boolean = this >= other
}

/**
 * Pure classification by width in whole dp, matching [WindowSizeClass]'s own truncation of
 * fractional dp so the result agrees with [currentWindowWidthClass] at every boundary.
 */
fun windowWidthClass(windowWidthDp: Int): WindowWidthClass =
    when {
        windowWidthDp >= WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND -> WindowWidthClass.EXTRA_LARGE
        windowWidthDp >= WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND -> WindowWidthClass.LARGE
        windowWidthDp >= WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND -> WindowWidthClass.EXPANDED
        windowWidthDp >= WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND -> WindowWidthClass.MEDIUM
        else -> WindowWidthClass.COMPACT
    }

/**
 * The current window's width class, from the Large/Extra-large-aware window size class. Its
 * [WindowSizeClass.minWidthDp] is the lower bound of the bucket the window falls in, so the same
 * pure rule as [windowWidthClass] decides both.
 */
@Composable
fun currentWindowWidthClass(): WindowWidthClass {
    val sizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    return windowWidthClass(sizeClass.minWidthDp)
}

/**
 * The window's current width in dp, for layout decisions finer than a [WindowWidthClass]. Follows
 * resizes that don't change the configuration (desktop windowing, some foldable transitions).
 */
@Composable
fun currentWindowWidthDp(): Float =
    with(LocalDensity.current) {
        LocalWindowInfo.current.containerSize.width
            .toDp()
            .value
    }
