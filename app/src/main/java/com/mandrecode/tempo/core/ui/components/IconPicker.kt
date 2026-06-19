package com.mandrecode.tempo.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.TempoIcon
import com.mandrecode.tempo.core.ui.util.rememberPressableIconButtonAnimation

/**
 * Icon picker component for selecting habit icons.
 * Displays a grid of icons that fits the width of the container.
 * When collapsed and an icon is selected, it is shown first in the row.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IconPicker(
    selectedIconName: String?,
    onSelectIcon: (String) -> Unit,
    onClearIcon: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    disabledMessage: String? = null,
) {
    val allIcons = remember { TempoIcon.getAllIcons() }
    var isExpanded by remember { mutableStateOf(false) }
    // Using 6 items per row for a balanced grid
    val itemsPerRow = 6
    val firstRowIconsCount = itemsPerRow - 1

    val iconsToDisplay =
        if (isExpanded) {
            allIcons
        } else {
            val selectedIndex = allIcons.indexOfFirst { it.iconName == selectedIconName }
            if (selectedIndex >= firstRowIconsCount) {
                // Show selected icon first, then fill remaining slots with other icons
                listOf(allIcons[selectedIndex]) + allIcons.filter { it.iconName != selectedIconName }.take(firstRowIconsCount - 1)
            } else {
                allIcons.take(firstRowIconsCount)
            }
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessLow)),
    ) {
        FlowRow(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = itemsPerRow,
        ) {
            iconsToDisplay.forEach { tempoIcon ->
                IconOption(
                    tempoIcon = tempoIcon,
                    isSelected = selectedIconName == tempoIcon.iconName,
                    enabled = enabled,
                    onClick = {
                        if (selectedIconName == tempoIcon.iconName) {
                            onClearIcon()
                        } else {
                            onSelectIcon(tempoIcon.iconName)
                            isExpanded = false
                        }
                    },
                )
            }

            // Add spacers to push the toggle to the bottom right and respect the grid
            // This ensures that partial rows have the same spacing as full rows
            val totalVisible = iconsToDisplay.size + 1
            val remainder = totalVisible % itemsPerRow
            if (remainder != 0) {
                val spacersNeeded = itemsPerRow - remainder
                repeat(spacersNeeded) {
                    Box(modifier = Modifier.size(48.dp))
                }
            }

            ExpandButton(
                isExpanded = isExpanded,
                onClick = { isExpanded = !isExpanded },
                enabled = enabled,
            )
        }

        if (!enabled && disabledMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = disabledMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

/**
 * Individual icon option in the picker
 */
@Composable
private fun IconOption(
    tempoIcon: TempoIcon,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val containerSize = 48.dp
    val iconBoxSize = 40.dp
    val iconSize = 24.dp

    // To make the inner box follow the "exact same shape" as the 16dp outer ring,
    // we use a 12dp radius for the 40dp box (16dp outer - 4dp padding = 12dp inner).
    // This creates perfectly parallel/concentric curves.
    val animatedCornerRadius by animateDpAsState(
        targetValue = if (isSelected) 12.dp else 16.dp,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "icon_corner_radius",
    )

    val containerColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        animationSpec = tween(300),
        label = "icon_container_color",
    )

    val contentColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec = tween(300),
        label = "icon_content_color",
    )

    val borderAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(300),
        label = "border_alpha",
    )

    Box(
        modifier =
            Modifier
                .size(containerSize)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = enabled,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        // High-fidelity selection ring with fade animation
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = borderAlpha }
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(16.dp),
                    ),
        )

        Box(
            modifier =
                Modifier
                    .size(iconBoxSize)
                    .clip(RoundedCornerShape(animatedCornerRadius))
                    .background(
                        color =
                            if (enabled) {
                                containerColor
                            } else {
                                containerColor.copy(alpha = 0.5f)
                            },
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(tempoIcon.iconRes),
                contentDescription = tempoIcon.iconName,
                modifier = Modifier.size(iconSize),
                tint = if (enabled) contentColor else contentColor.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun ExpandButton(
    isExpanded: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    val haptic = LocalHapticFeedback.current
    val (interactionSource, cornerRadius) =
        rememberPressableIconButtonAnimation(
            baseRadius = 16.dp,
            pressedRadius = 12.dp,
        )

    val size = 48.dp
    val iconSize = 20.dp

    val containerColor by animateColorAsState(
        targetValue =
            if (enabled) {
                // Slightly different shade for the action button
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            },
        label = "expand_container_color",
    )

    val contentColor by animateColorAsState(
        targetValue =
            if (enabled) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            },
        label = "expand_content_color",
    )

    Box(
        modifier =
            Modifier
                .size(size)
                .clip(RoundedCornerShape(cornerRadius.value))
                .background(containerColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter =
                painterResource(
                    if (isExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more,
                ),
            contentDescription =
                if (isExpanded) {
                    stringResource(R.string.collapse)
                } else {
                    stringResource(
                        R.string.expand,
                    )
                },
            modifier = Modifier.size(iconSize),
            tint = contentColor,
        )
    }
}
