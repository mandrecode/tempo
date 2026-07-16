package com.mandrecode.tempo.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** How strongly the selection accent tints a card's own resting color. */
private const val SELECTED_TINT_ALPHA = 0.3f

/** Smoothly lifts a source card while its editor is selected in a supporting pane. */
@Composable
fun selectableCardElevation(isSelected: Boolean): Dp {
    val elevation by animateDpAsState(
        targetValue = if (isSelected) 6.dp else 0.dp,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
        label = "selectableCardElevation",
    )
    return elevation
}

/**
 * Tints [baseColor] with the selection accent instead of swapping to a fixed color, so a
 * selected card reads as a highlighted variant of its own resting background — a colorful habit
 * card stays recognizably that color when selected — rather than jumping to an unrelated hue.
 *
 * Some resting card colors (an incomplete habit's tinted fill, for example) are themselves
 * translucent. [baseColor] is always flattened against the screen background before use, in both
 * the selected and unselected branches, so a Card's containerColor never carries translucency —
 * leaving it translucent produced visible compositing glitches, including a brief flash mid-tween
 * while animating between the two branches (interpolating alpha along the way).
 */
@Composable
fun selectedContainerColor(
    baseColor: Color,
    isSelected: Boolean,
): Color {
    val opaqueBase = baseColor.compositeOver(MaterialTheme.colorScheme.background)
    return if (isSelected) {
        MaterialTheme.colorScheme.secondary
            .copy(alpha = SELECTED_TINT_ALPHA)
            .compositeOver(opaqueBase)
    } else {
        opaqueBase
    }
}

/**
 * The selection accent as a translucent overlay, for rows already rendered on top of a resolved
 * background (for example a habit row inside an already-colored chain card) where compositing a
 * new opaque color isn't necessary — the overlay blends with whatever's beneath it on screen.
 */
@Composable
fun selectedTintOverlayColor(isSelected: Boolean): Color =
    if (isSelected) {
        MaterialTheme.colorScheme.secondary.copy(alpha = SELECTED_TINT_ALPHA)
    } else {
        Color.Transparent
    }
