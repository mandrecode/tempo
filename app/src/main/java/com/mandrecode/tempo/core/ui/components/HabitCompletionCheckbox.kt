package com.mandrecode.tempo.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.TempoIcon

private const val COMPLETED_RADIUS_FRACTION = 16f / 56f
private const val UNCOMPLETED_RADIUS_FRACTION = 0.5f
private const val ICON_FRACTION = 0.5f
private const val COMPLETED_SCALE = 1.1f
private const val UNCOMPLETED_SCALE = 1f
private const val DISABLED_ALPHA = 0.5f
private const val ENABLED_ALPHA = 1f
private const val COMPLETED_BG_ALPHA_INSIDE = 0.08f
private const val COMPLETED_BG_ALPHA_OVERLAY = 0.15f
private const val UNCOMPLETED_BG_ALPHA = 0.12f
private const val BORDER_ALPHA = 0.4f
private const val DISABLED_OVERLAY_ALPHA = 0.08f
private const val COLOR_TWEEN_DURATION_MS = 300
private val BORDER_WIDTH = 2.dp

const val HABIT_COMPLETION_CHECKBOX_TEST_TAG = "habitCompletionCheckbox"

/**
 * Reusable habit completion checkbox that visually mirrors the one in HabitCard / HabitItem.
 * Used by the habit bottom sheet (single habit view) and by chain rows so users can mark
 * habits complete from the sheet opened via a reminder notification.
 */
@Composable
fun HabitCompletionCheckbox(
    isCompleted: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color? = null,
    iconName: String? = null,
    canToggle: Boolean = true,
    isContainerCompleted: Boolean = isCompleted,
    size: Dp = 56.dp,
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
    val iconTintColor by animateColorAsState(
        targetValue = resolveIconTint(isContainerCompleted = isContainerCompleted, color = color),
        animationSpec = tween(COLOR_TWEEN_DURATION_MS),
        label = "icon_tint_color",
    )

    Box(
        modifier =
            modifier
                .testTag(HABIT_COMPLETION_CHECKBOX_TEST_TAG)
                .size(size)
                .semantics {
                    role = Role.Checkbox
                    toggleableState = if (isCompleted) ToggleableState.On else ToggleableState.Off
                    contentDescription = a11yLabel
                    if (!canToggle) disabled()
                }.graphicsLayer {
                    scaleX = checkboxScale
                    scaleY = checkboxScale
                    alpha = if (canToggle) ENABLED_ALPHA else DISABLED_ALPHA
                }.clip(RoundedCornerShape(checkboxRadius))
                .clickable(enabled = canToggle) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle()
                },
        contentAlignment = Alignment.Center,
    ) {
        HabitCheckboxBackground(
            size = size,
            radius = checkboxRadius,
            isCompleted = isCompleted,
            isContainerCompleted = isContainerCompleted,
            color = color,
        )
        HabitCheckboxIcon(
            isCompleted = isCompleted,
            iconName = iconName,
            tint = iconTintColor,
            size = size * ICON_FRACTION,
        )
        if (!canToggle) {
            DisabledHabitCheckboxOverlay(modifier = Modifier.matchParentSize())
        }
    }
}

@Composable
private fun DisabledHabitCheckboxOverlay(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .background(Color.Black.copy(alpha = DISABLED_OVERLAY_ALPHA)),
    )
}

@Composable
private fun HabitCheckboxBackground(
    size: Dp,
    radius: Dp,
    isCompleted: Boolean,
    isContainerCompleted: Boolean,
    color: Color?,
) {
    val bgColor by animateColorAsState(
        targetValue =
            resolveBgColor(
                isCompleted = isCompleted,
                isContainerCompleted = isContainerCompleted,
                color = color,
            ),
        animationSpec = tween(COLOR_TWEEN_DURATION_MS),
        label = "checkbox_bg_color",
    )
    val shape = RoundedCornerShape(radius)
    Box(
        modifier =
            Modifier
                .size(size)
                .background(color = bgColor, shape = shape)
                .then(
                    if (!isCompleted) {
                        val borderColor = (color ?: MaterialTheme.colorScheme.primary).copy(alpha = BORDER_ALPHA)
                        Modifier.border(width = BORDER_WIDTH, color = borderColor, shape = shape)
                    } else {
                        Modifier
                    },
                ),
    )
}

@Composable
private fun HabitCheckboxIcon(
    isCompleted: Boolean,
    iconName: String?,
    tint: Color,
    size: Dp,
) {
    if (isCompleted) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            modifier = Modifier.size(size),
            tint = tint,
        )
        return
    }
    val habitIcon = iconName?.let(TempoIcon::fromName) ?: return
    Icon(
        painter = painterResource(habitIcon.iconRes),
        contentDescription = null,
        modifier = Modifier.size(size).testTag("habitIcon"),
        tint = tint,
    )
}

@Composable
private fun resolveBgColor(
    isCompleted: Boolean,
    isContainerCompleted: Boolean,
    color: Color?,
): Color {
    val base = color ?: MaterialTheme.colorScheme.primary
    return when {
        isCompleted && !isContainerCompleted -> base.copy(alpha = COMPLETED_BG_ALPHA_INSIDE)
        isCompleted -> MaterialTheme.colorScheme.onPrimary.copy(alpha = COMPLETED_BG_ALPHA_OVERLAY)
        else -> base.copy(alpha = UNCOMPLETED_BG_ALPHA)
    }
}

@Composable
private fun resolveIconTint(
    isContainerCompleted: Boolean,
    color: Color?,
): Color =
    if (!isContainerCompleted) {
        color ?: MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onPrimary
    }
