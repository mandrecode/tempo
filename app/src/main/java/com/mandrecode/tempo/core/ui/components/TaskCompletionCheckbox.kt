package com.mandrecode.tempo.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R

private const val COMPLETED_RADIUS_FRACTION = 1f / 3f
private const val UNCOMPLETED_RADIUS_FRACTION = 0.5f
private const val ICON_FRACTION = 0.5f
private const val COMPLETED_SCALE = 1.1f
private const val UNCOMPLETED_SCALE = 1f
private const val COMPLETED_BG_ALPHA = 0.15f
private const val UNCOMPLETED_BG_ALPHA = 0.05f
private const val BORDER_ALPHA = 0.2f
private val BORDER_WIDTH = 2.dp

const val TASK_COMPLETION_CHECKBOX_TEST_TAG = "taskCompletionCheckbox"

/**
 * Reusable task completion checkbox that visually mirrors the one in TaskCard.
 * Used by the task bottom sheet so users can mark tasks complete from the sheet
 * opened via a reminder notification.
 */
@Composable
fun TaskCompletionCheckbox(
    isCompleted: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    val haptic = LocalHapticFeedback.current
    val a11yLabel =
        stringResource(
            if (isCompleted) R.string.mark_as_not_completed else R.string.mark_as_completed,
        )

    val checkboxRadius by animateDpAsState(
        targetValue = if (isCompleted) (size * COMPLETED_RADIUS_FRACTION) else (size * UNCOMPLETED_RADIUS_FRACTION),
        animationSpec = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium),
        label = "checkbox_radius",
    )
    val checkboxScale by animateFloatAsState(
        targetValue = if (isCompleted) COMPLETED_SCALE else UNCOMPLETED_SCALE,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "checkbox_scale",
    )

    Box(
        modifier =
            modifier
                .testTag(TASK_COMPLETION_CHECKBOX_TEST_TAG)
                .size(size)
                .semantics {
                    role = Role.Checkbox
                    toggleableState = if (isCompleted) ToggleableState.On else ToggleableState.Off
                    contentDescription = a11yLabel
                }.graphicsLayer {
                    scaleX = checkboxScale
                    scaleY = checkboxScale
                }.clip(RoundedCornerShape(checkboxRadius))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle()
                },
        contentAlignment = Alignment.Center,
    ) {
        TaskCheckboxBackground(size = size, radius = checkboxRadius, isCompleted = isCompleted)
        if (isCompleted) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(size * ICON_FRACTION),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun TaskCheckboxBackground(
    size: Dp,
    radius: Dp,
    isCompleted: Boolean,
) {
    val bgColor =
        if (isCompleted) {
            MaterialTheme.colorScheme.primary.copy(alpha = COMPLETED_BG_ALPHA)
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = UNCOMPLETED_BG_ALPHA)
        }
    val shape = RoundedCornerShape(radius)
    Box(
        modifier =
            Modifier
                .size(size)
                .background(color = bgColor, shape = shape)
                .then(
                    if (!isCompleted) {
                        Modifier.border(
                            width = BORDER_WIDTH,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = BORDER_ALPHA),
                            shape = shape,
                        )
                    } else {
                        Modifier
                    },
                ),
    )
}
