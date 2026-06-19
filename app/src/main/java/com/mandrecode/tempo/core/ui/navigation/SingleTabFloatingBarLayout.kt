package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun rememberSingleTabFloatingBarLayout(
    maxWidth: Dp,
    isTasksRoute: Boolean,
    compact: Boolean,
    visibleTaskActionsWidth: Dp,
): SingleTabFloatingBarLayout {
    val density = LocalDensity.current
    var expandedAddButtonWidth by remember { mutableStateOf(FloatingToolbarActionButtonSize) }
    val addOffset by animateDpAsState(
        targetValue =
            if (compact) {
                (maxWidth - FloatingToolbarActionButtonSize) / 2
            } else if (isTasksRoute) {
                (visibleTaskActionsWidth + FloatingToolbarItemSpacing) / 2
            } else {
                0.dp
            },
        animationSpec = floatingControlsMotionSpec(),
        label = "single_tab_add_button_offset",
    )
    val actionOffset by animateDpAsState(
        targetValue =
            if (compact) {
                -(maxWidth - visibleTaskActionsWidth) / 2
            } else {
                -(expandedAddButtonWidth + FloatingToolbarItemSpacing) / 2
            },
        animationSpec = floatingControlsMotionSpec(),
        label = "single_tab_task_actions_offset",
    )
    return SingleTabFloatingBarLayout(
        addOffset = addOffset,
        actionOffset = actionOffset,
        onExpandedAddButtonMeasured = { width ->
            expandedAddButtonWidth = with(density) { width.toDp() }
        },
    )
}

internal data class SingleTabFloatingBarLayout(
    val addOffset: Dp,
    val actionOffset: Dp,
    val onExpandedAddButtonMeasured: (Int) -> Unit,
)
