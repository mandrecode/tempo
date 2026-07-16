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
    /** The sheet keeps its native vertical direction (bottom or top). */
    Vertical,

    /** The sheet is presented as a full-height side sheet at the end edge. */
    Side,
}

/**
 * Pure placement rule: side sheets on expanded widths (the two-column feel) and on
 * height-compact windows such as landscape phones, where a bottom sheet and the keyboard
 * would compete for the same space.
 */
fun sheetPlacement(
    windowWidthDp: Float,
    windowHeightDp: Float,
): SheetPlacement =
    if (windowWidthDp >= WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND ||
        windowHeightDp < WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND
    ) {
        SheetPlacement.Side
    } else {
        SheetPlacement.Vertical
    }

@Composable
fun rememberSheetPlacement(): SheetPlacement {
    val windowSize = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    return remember(windowSize, density) {
        with(density) {
            sheetPlacement(
                windowWidthDp = windowSize.width.toDp().value,
                windowHeightDp = windowSize.height.toDp().value,
            )
        }
    }
}
