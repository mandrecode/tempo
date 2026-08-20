package com.mandrecode.tempo.core.ui.components

internal enum class TempoModalSheetBackRoute(
    val forwardsProgressToSheet: Boolean,
    val clearsFocusOnCompletion: Boolean,
    val dismissesSheetOnCompletion: Boolean,
    val restoresSheetOnCancellation: Boolean,
) {
    Keyboard(
        forwardsProgressToSheet = false,
        clearsFocusOnCompletion = true,
        dismissesSheetOnCompletion = false,
        restoresSheetOnCancellation = false,
    ),
    Sheet(
        forwardsProgressToSheet = true,
        clearsFocusOnCompletion = false,
        dismissesSheetOnCompletion = true,
        restoresSheetOnCancellation = true,
    ),
}

internal fun resolveTempoModalSheetBackRoute(isImeVisible: Boolean): TempoModalSheetBackRoute =
    if (isImeVisible) {
        TempoModalSheetBackRoute.Keyboard
    } else {
        TempoModalSheetBackRoute.Sheet
    }
