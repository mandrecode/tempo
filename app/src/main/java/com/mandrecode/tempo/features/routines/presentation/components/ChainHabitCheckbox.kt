package com.mandrecode.tempo.features.routines.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

private const val COMPLETED_SCALE = 1.05f
private const val UNCOMPLETED_SCALE = 1f
private const val COMPLETED_BG_ALPHA = 0.08f
private const val UNCOMPLETED_BG_ALPHA = 0.12f
private const val BORDER_ALPHA = 0.4f
private const val DISABLED_ALPHA = 0.5f
private const val ENABLED_ALPHA = 1f
private const val COLOR_ANIM_MS = 300
private val VISUAL_SIZE = 36.dp
private val TOUCH_TARGET_SIZE = 48.dp
private val BORDER_WIDTH = 2.dp
private val ICON_SIZE = 20.dp

const val CHAIN_HABIT_COMPLETION_CHECKBOX_TEST_TAG = "chainHabitCompletionCheckbox"

@Composable
internal fun ChainHabitCheckbox(
    isCompleted: Boolean,
    canToggle: Boolean,
    radius: Dp,
    accentColor: Color,
    iconName: String?,
    onToggle: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val checkboxScale by rememberCheckboxScale(isCompleted)
    val backgroundColor by rememberCheckboxBackground(isCompleted, accentColor)
    val iconTintColor by animateColorAsState(
        targetValue = accentColor,
        animationSpec = tween(COLOR_ANIM_MS),
        label = "chain_habit_checkbox_icon_tint",
    )

    Box(
        modifier =
            Modifier
                .size(TOUCH_TARGET_SIZE)
                .testTag(CHAIN_HABIT_COMPLETION_CHECKBOX_TEST_TAG)
                .chainHabitCheckboxSemantics(isCompleted, canToggle)
                .then(chainHabitCheckboxClickModifier(canToggle, interactionSource, haptic, onToggle)),
        contentAlignment = Alignment.Center,
    ) {
        ChainHabitCheckboxVisual(
            isCompleted = isCompleted,
            canToggle = canToggle,
            radius = radius,
            accentColor = accentColor,
            backgroundColor = backgroundColor,
            iconTintColor = iconTintColor,
            iconName = iconName,
            checkboxScale = checkboxScale,
            interactionSource = interactionSource,
        )
    }
}

@Composable
private fun rememberCheckboxScale(isCompleted: Boolean) =
    animateFloatAsState(
        targetValue = if (isCompleted) COMPLETED_SCALE else UNCOMPLETED_SCALE,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
        label = "chain_habit_checkbox_scale",
    )

@Composable
private fun rememberCheckboxBackground(
    isCompleted: Boolean,
    accentColor: Color,
) = animateColorAsState(
    targetValue =
        if (isCompleted) {
            accentColor.copy(alpha = COMPLETED_BG_ALPHA)
        } else {
            accentColor.copy(alpha = UNCOMPLETED_BG_ALPHA)
        },
    animationSpec = tween(COLOR_ANIM_MS),
    label = "chain_habit_checkbox_bg_color",
)

@Composable
private fun Modifier.chainHabitCheckboxSemantics(
    isCompleted: Boolean,
    canToggle: Boolean,
): Modifier {
    val a11yLabel = stringResource(if (isCompleted) R.string.mark_as_not_completed else R.string.mark_as_completed)
    return semantics {
        role = Role.Checkbox
        toggleableState = if (isCompleted) ToggleableState.On else ToggleableState.Off
        contentDescription = a11yLabel
        if (!canToggle) disabled()
    }
}

private fun chainHabitCheckboxClickModifier(
    canToggle: Boolean,
    interactionSource: MutableInteractionSource,
    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onToggle: () -> Unit,
): Modifier =
    if (canToggle) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle()
            },
        )
    } else {
        Modifier
    }

@Composable
private fun ChainHabitCheckboxVisual(
    isCompleted: Boolean,
    canToggle: Boolean,
    radius: Dp,
    accentColor: Color,
    backgroundColor: Color,
    iconTintColor: Color,
    iconName: String?,
    checkboxScale: Float,
    interactionSource: MutableInteractionSource,
) {
    Box(
        modifier =
            Modifier
                .size(VISUAL_SIZE)
                .graphicsLayer {
                    scaleX = checkboxScale
                    scaleY = checkboxScale
                    alpha = if (canToggle) ENABLED_ALPHA else DISABLED_ALPHA
                }.clip(RoundedCornerShape(radius))
                .background(backgroundColor)
                .then(chainHabitCheckboxBorderModifier(isCompleted, accentColor, radius))
                .then(chainHabitCheckboxIndicationModifier(canToggle, interactionSource)),
        contentAlignment = Alignment.Center,
    ) {
        ChainHabitCheckboxIcon(
            isCompleted = isCompleted,
            iconName = iconName,
            tint = iconTintColor,
        )
    }
}

private fun chainHabitCheckboxBorderModifier(
    isCompleted: Boolean,
    accentColor: Color,
    radius: Dp,
): Modifier =
    if (!isCompleted) {
        Modifier.border(
            width = BORDER_WIDTH,
            color = accentColor.copy(alpha = BORDER_ALPHA),
            shape = RoundedCornerShape(radius),
        )
    } else {
        Modifier
    }

@Composable
private fun chainHabitCheckboxIndicationModifier(
    canToggle: Boolean,
    interactionSource: MutableInteractionSource,
): Modifier =
    if (canToggle) {
        Modifier.indication(interactionSource, LocalIndication.current)
    } else {
        Modifier
    }

@Composable
private fun ChainHabitCheckboxIcon(
    isCompleted: Boolean,
    iconName: String?,
    tint: Color,
) {
    if (isCompleted) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            modifier = Modifier.size(ICON_SIZE),
            tint = tint,
        )
        return
    }
    val habitIcon = remember(iconName) { iconName?.let(TempoIcon::fromName) } ?: return
    Icon(
        painter = painterResource(habitIcon.iconRes),
        contentDescription = null,
        modifier = Modifier.size(ICON_SIZE),
        tint = tint,
    )
}
