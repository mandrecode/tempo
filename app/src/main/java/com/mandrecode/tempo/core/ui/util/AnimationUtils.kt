package com.mandrecode.tempo.core.ui.util

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Creates a remembered MutableInteractionSource and provides animated corner radius
 * for buttons that respond to press events.
 *
 * @param baseRadius The corner radius when not pressed
 * @param pressedRadius The corner radius when pressed (default: 12.dp)
 * @param animationDuration Duration of the animation in milliseconds (default: 150ms)
 * @return Pair of InteractionSource and animated corner radius State
 */
@Composable
fun rememberPressableButtonAnimation(
    baseRadius: Dp = 24.dp,
    pressedRadius: Dp = 12.dp,
    animationDuration: Int = 150,
): Pair<MutableInteractionSource, State<Dp>> {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius: State<Dp> =
        animateDpAsState(
            targetValue = if (isPressed) pressedRadius else baseRadius,
            animationSpec = tween(animationDuration),
            label = "button_corner_radius",
        )

    return Pair(interactionSource, cornerRadius)
}

/**
 * Creates a remembered MutableInteractionSource and provides animated corner radius
 * for icon buttons that respond to press events.
 *
 * @param baseRadius The corner radius when not pressed (default: 24.dp)
 * @param pressedRadius The corner radius when pressed (default: 12.dp)
 * @param animationDuration Duration of the animation in milliseconds (default: 150ms)
 * @return Pair of InteractionSource and animated corner radius State
 */
@Composable
fun rememberPressableIconButtonAnimation(
    baseRadius: Dp = 48.dp,
    pressedRadius: Dp = 16.dp,
    animationDuration: Int = 150,
): Pair<MutableInteractionSource, State<Dp>> = rememberPressableButtonAnimation(baseRadius, pressedRadius, animationDuration)

/**
 * Creates a remembered MutableInteractionSource and provides animated corner radius
 * for FilterChips that respond to press events.
 *
 * @param isSelected Whether the chip is currently selected
 * @param selectedRadius Corner radius when selected but not pressed (default: 12.dp)
 * @param unselectedRadius Corner radius when not selected (default: 20.dp)
 * @param pressedRadius Corner radius when pressed (default: 8.dp)
 * @param animationDuration Duration of the animation in milliseconds (default: 150ms)
 * @return Pair of InteractionSource and animated corner radius State
 */
@Composable
fun rememberPressableChipAnimation(
    isSelected: Boolean,
    interactionSource: MutableInteractionSource,
    selectedRadius: Dp = 12.dp,
    unselectedRadius: Dp = 24.dp,
    pressedRadius: Dp = 12.dp,
    animationDuration: Int = 150,
): State<Dp> {
    val isPressed by interactionSource.collectIsPressedAsState()
    val baseRadius = if (isSelected) selectedRadius else unselectedRadius
    return animateDpAsState(
        targetValue = if (isPressed) pressedRadius else baseRadius,
        animationSpec = tween(animationDuration),
        label = "chip_corner_radius",
    )
}
