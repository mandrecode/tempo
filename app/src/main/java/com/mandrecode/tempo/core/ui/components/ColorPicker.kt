package com.mandrecode.tempo.core.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.ColorOption
import com.mandrecode.tempo.core.ui.theme.LocalIsDarkTheme
import com.mandrecode.tempo.core.ui.theme.getMaterialYouColors
import com.mandrecode.tempo.core.ui.theme.getMonochromeColor
import com.mandrecode.tempo.core.ui.theme.getPastelColors

@Composable
fun ColorPicker(
    selectedColorKey: String?,
    onSelectColorKey: (String) -> Unit,
    onClearColor: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    disabledMessage: String? = null,
) {
    val isDarkTheme = LocalIsDarkTheme.current
    val materialYouColors = getMaterialYouColors(MaterialTheme.colorScheme)
    val pastelColors = getPastelColors()
    val monochromeColor = getMonochromeColor()
    val allColors = materialYouColors + monochromeColor + pastelColors

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // Match the SpaceBetween spacing of IconPicker (6 items × 48dp, 4dp padding each side)
        val itemSize = 48.dp
        val itemsPerRow = 6
        val horizontalPadding = 4.dp

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val spacing =
                ((maxWidth - horizontalPadding * 2 - itemSize * itemsPerRow) / (itemsPerRow - 1))
                    .coerceAtLeast(0.dp)

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(spacing),
                contentPadding = PaddingValues(start = horizontalPadding, end = horizontalPadding),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items(allColors) { colorOption ->
                    SegmentedColorCircle(
                        colorOption = colorOption,
                        isDarkTheme = isDarkTheme,
                        isSelected = selectedColorKey == colorOption.labelKey,
                        enabled = enabled,
                        onClick = { onSelectColorKey(colorOption.labelKey) },
                    )
                }
            }
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
 * Displays a color option as a segmented circle/squircle showing 3 accent variations,
 * matching Android system theme settings.
 */
@Composable
private fun SegmentedColorCircle(
    colorOption: ColorOption,
    isDarkTheme: Boolean,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val accents = colorOption.getAccents(isDarkTheme)
    val size = 48.dp

    val animatedContentSize by animateDpAsState(
        targetValue = if (isSelected) 32.dp else 40.dp,
        animationSpec = tween(300),
        label = "content_size",
    )

    val animatedCornerRadius by animateDpAsState(
        targetValue = if (isSelected) 12.dp else 16.dp,
        animationSpec = tween(300),
        label = "corner_radius",
    )

    Box(
        modifier =
            Modifier
                .size(size)
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
        if (isSelected) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .border(
                            width = 2.dp,
                            color = accents[0],
                            shape = RoundedCornerShape(16.dp),
                        ),
            )
        }

        Canvas(
            modifier =
                Modifier
                    .size(animatedContentSize)
                    .clip(RoundedCornerShape(animatedCornerRadius)),
        ) {
            val canvasSize = this.size

            if (colorOption.isSegmented) {
                // Top half
                drawArc(
                    color = if (enabled) accents[0] else accents[0].copy(alpha = 0.3f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    size = canvasSize,
                )

                // Bottom-left
                drawArc(
                    color = if (enabled) accents[1] else accents[1].copy(alpha = 0.3f),
                    startAngle = 90f,
                    sweepAngle = 90f,
                    useCenter = true,
                    size = canvasSize,
                )

                // Bottom-right
                drawArc(
                    color = if (enabled) accents[2] else accents[2].copy(alpha = 0.3f),
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = true,
                    size = canvasSize,
                )
            } else {
                drawCircle(
                    color =
                        if (enabled) {
                            colorOption.getColor(isDarkTheme)
                        } else {
                            colorOption
                                .getColor(isDarkTheme)
                                .copy(alpha = 0.3f)
                        },
                    radius = canvasSize.minDimension / 2,
                )
            }
        }
    }
}
