package com.mandrecode.tempo.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.filterChipSelected

/**
 * The corner a standalone chip settles into once chosen — square enough to read as pressed in, and
 * the same radius the press itself uses, so the touch does not spring back before the selection
 * lands: the squash of pressing simply becomes the shape of being chosen.
 */
val SquaredSelection = 10.dp

@Composable
fun ExpressiveChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    horizontalPadding: Dp = 16.dp,
    /**
     * The corner the chip animates to when it is selected.
     *
     * Rounder by default, which is what selection has to mean inside a *connected* run: the
     * segments share flat 4dp joins, so the selected one earns its own rounded ends and lifts out
     * of the row. Standing alone with gaps around it a chip has nothing to lift out of — roundness
     * says nothing there, and squareness is what reads as pressed-in. Pass [SquaredSelection] for
     * those.
     */
    selectedCornerRadius: Dp = 20.dp,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primary,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedContainerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val selectedRadius = selectedCornerRadius
    val connectedRadius = 4.dp
    val outerRadius = 16.dp

    val startRadius by animateDpAsState(
        targetValue =
            when {
                isPressed -> 10.dp
                isSelected -> selectedRadius
                isFirst -> outerRadius
                else -> connectedRadius
            },
        animationSpec = tween(400),
        label = "start_radius",
    )

    val endRadius by animateDpAsState(
        targetValue =
            when {
                isPressed -> 10.dp
                isSelected -> selectedRadius
                isLast -> outerRadius
                else -> connectedRadius
            },
        animationSpec = tween(400),
        label = "end_radius",
    )

    val containerColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                selectedContainerColor
            } else {
                unselectedContainerColor
            },
        animationSpec = tween(400),
        label = "expressive_chip_container",
    )

    val contentColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                selectedContentColor
            } else {
                unselectedContentColor.copy(alpha = if (enabled) 1f else 0.5f)
            },
        animationSpec = tween(400),
        label = "expressive_chip_content",
    )

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Surface(
            modifier =
                modifier
                    .height(height)
                    .padding(vertical = 4.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = startRadius,
                            bottomStart = startRadius,
                            topEnd = endRadius,
                            bottomEnd = endRadius,
                        ),
                    ).clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = enabled,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onClick()
                        },
                    ),
            shape =
                RoundedCornerShape(
                    topStart = startRadius,
                    bottomStart = startRadius,
                    topEnd = endRadius,
                    bottomEnd = endRadius,
                ),
            color = containerColor,
            border =
                if (!isSelected) {
                    BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    )
                } else {
                    null
                },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = horizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = horizontalArrangement,
            ) {
                if (icon != null) {
                    icon()
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = label,
                    style =
                        if (isSelected) {
                            MaterialTheme.typography.filterChipSelected
                        } else {
                            MaterialTheme.typography.labelLarge
                        },
                    color = contentColor,
                )
            }
        }
    }
}
