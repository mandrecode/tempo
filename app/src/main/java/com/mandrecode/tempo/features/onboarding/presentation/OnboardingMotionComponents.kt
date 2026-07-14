package com.mandrecode.tempo.features.onboarding.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation

@Composable
internal fun OnboardingProgress(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().testTag(OnboardingTestTags.PROGRESS),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val color by animateColorAsState(
                targetValue = progressColor(isComplete = index <= currentPage),
                animationSpec = tween(durationMillis = PROGRESS_ANIMATION_DURATION),
                label = "onboardingProgressColor",
            )
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(color)
                        .testTag(OnboardingTestTags.PROGRESS_SEGMENT),
            )
        }
    }
}

@Composable
internal fun AnimatedOnboardingButton(
    label: String,
    onClick: () -> Unit,
    style: OnboardingButtonStyle,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val (interactionSource, cornerRadiusState) =
        rememberPressableButtonAnimation(
            baseRadius = 24.dp,
            pressedRadius = 12.dp,
            animationDuration = BUTTON_ANIMATION_DURATION,
        )
    val colors = onboardingButtonColors(style = style)
    val onHapticClick = {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onClick()
    }
    val buttonModifier = modifier.height(48.dp)
    val shape = RoundedCornerShape(cornerRadiusState.value)

    when (style) {
        OnboardingButtonStyle.Primary ->
            PrimaryOnboardingButton(label, onHapticClick, interactionSource, shape, colors, buttonModifier)
        OnboardingButtonStyle.Outlined ->
            OutlinedOnboardingButton(label, onHapticClick, interactionSource, shape, colors, buttonModifier)
        OnboardingButtonStyle.Text ->
            TextOnboardingButton(label, onHapticClick, interactionSource, shape, colors, buttonModifier)
    }
}

@Composable
private fun progressColor(isComplete: Boolean): Color =
    if (isComplete) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.36f)
    }

@Composable
private fun onboardingButtonColors(style: OnboardingButtonStyle): OnboardingButtonColors =
    when (style) {
        OnboardingButtonStyle.Primary ->
            OnboardingButtonColors(
                container = MaterialTheme.colorScheme.primary,
                content = MaterialTheme.colorScheme.onPrimary,
                border = Color.Transparent,
            )
        OnboardingButtonStyle.Outlined ->
            OnboardingButtonColors(
                container = Color.Transparent,
                content = MaterialTheme.colorScheme.primary,
                border = MaterialTheme.colorScheme.outline,
            )
        OnboardingButtonStyle.Text ->
            OnboardingButtonColors(
                container = Color.Transparent,
                content = MaterialTheme.colorScheme.primary,
                border = Color.Transparent,
            )
    }

@Composable
private fun PrimaryOnboardingButton(
    label: String,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource,
    shape: RoundedCornerShape,
    colors: OnboardingButtonColors,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = shape,
        colors = ButtonDefaults.buttonColors(containerColor = colors.container, contentColor = colors.content),
        modifier = modifier,
    ) {
        Text(label)
    }
}

@Composable
private fun OutlinedOnboardingButton(
    label: String,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource,
    shape: RoundedCornerShape,
    colors: OnboardingButtonColors,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = shape,
        colors = ButtonDefaults.outlinedButtonColors(containerColor = colors.container, contentColor = colors.content),
        border = BorderStroke(1.dp, colors.border),
        modifier = modifier,
    ) {
        Text(label)
    }
}

@Composable
private fun TextOnboardingButton(
    label: String,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource,
    shape: RoundedCornerShape,
    colors: OnboardingButtonColors,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = shape,
        colors = ButtonDefaults.textButtonColors(containerColor = colors.container, contentColor = colors.content),
        modifier = modifier,
    ) {
        Text(label)
    }
}

internal enum class OnboardingButtonStyle {
    Primary,
    Outlined,
    Text,
}

private data class OnboardingButtonColors(
    val container: Color,
    val content: Color,
    val border: Color,
)

private const val PROGRESS_ANIMATION_DURATION = 260
private const val BUTTON_ANIMATION_DURATION = 220
