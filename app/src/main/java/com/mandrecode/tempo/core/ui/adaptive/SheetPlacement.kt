package com.mandrecode.tempo.core.ui.adaptive

import androidx.compose.runtime.Composable

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
 * Pure placement rule: dock editors beside live content from the Large width class (1200 dp) up,
 * and keep them as bottom sheets below it. Top-origin sheets such as category editing retain their
 * own direction.
 */
fun sheetPlacement(windowWidthDp: Int): SheetPlacement = sheetPlacement(windowWidthClass(windowWidthDp))

fun sheetPlacement(widthClass: WindowWidthClass): SheetPlacement =
    if (widthClass.isAtLeast(WindowWidthClass.LARGE)) {
        SheetPlacement.DockedPane
    } else {
        SheetPlacement.BottomSheet
    }

@Composable
fun rememberSheetPlacement(): SheetPlacement = sheetPlacement(currentWindowWidthClass())
