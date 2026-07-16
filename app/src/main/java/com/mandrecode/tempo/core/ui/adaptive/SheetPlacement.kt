package com.mandrecode.tempo.core.ui.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.window.core.layout.WindowSizeClass

/**
 * How a modal editor sheet is presented for the current window.
 */
enum class SheetPlacement {
    /** The editor keeps its native vertical direction (bottom or top). */
    BottomSheet,

    /** The editor is presented as a modal full-height side sheet at the end edge. */
    SideSheet,

    /** The editor is presented beside live content without a scrim. */
    DockedPane,
}

/**
 * Pure placement rule: side sheets on expanded widths (the two-column feel) and on
 * height-compact windows such as landscape phones, where a bottom sheet and the keyboard
 * would compete for the same space.
 */
fun sheetPlacement(
    windowWidthDp: Int,
    windowHeightDp: Int,
): SheetPlacement =
    if (windowWidthDp >= LARGE_WINDOW_WIDTH_DP) {
        SheetPlacement.DockedPane
    } else if (windowWidthDp >= WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND ||
        windowHeightDp < WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND
    ) {
        SheetPlacement.SideSheet
    } else {
        SheetPlacement.BottomSheet
    }

const val LARGE_WINDOW_WIDTH_DP = 1200

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
                windowHeightDp =
                    windowSize.height
                        .toDp()
                        .value
                        .toInt(),
            )
        }
    }
}
