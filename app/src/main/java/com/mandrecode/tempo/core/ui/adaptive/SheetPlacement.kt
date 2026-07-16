package com.mandrecode.tempo.core.ui.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo

/**
 * How an editor is presented for the current window.
 */
enum class SheetPlacement {
    /** The editor keeps its native vertical direction (bottom or top). */
    BottomSheet,

    /** The editor is presented beside live content without a scrim. */
    DockedPane,
}

/**
 * Pure placement rule: dock editors beside live content on large windows and keep editors
 * as bottom sheets below that breakpoint. Top-origin sheets such as category editing retain
 * their own direction.
 */
fun sheetPlacement(windowWidthDp: Int): SheetPlacement =
    if (windowWidthDp >= LARGE_WINDOW_WIDTH_DP) {
        SheetPlacement.DockedPane
    } else {
        SheetPlacement.BottomSheet
    }

private const val LARGE_WINDOW_WIDTH_DP = 1200

@Composable
fun rememberSheetPlacement(): SheetPlacement {
    val windowSize = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    return remember(windowSize, density) {
        with(density) {
            sheetPlacement(
                // WindowSizeClass truncates float dp dimensions to integers before matching.
                // Use the same convention so sheets and navigation cannot disagree at a breakpoint.
                windowWidthDp =
                    windowSize.width
                        .toDp()
                        .value
                        .toInt(),
            )
        }
    }
}
