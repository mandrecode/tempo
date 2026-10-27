package com.mandrecode.tempo.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.LocalTempoGlass

/** How strongly the selection accent tints a card's own resting color. */
private const val SELECTED_TINT_ALPHA = 0.3f

/**
 * A card's elevation. With glass on: none, in every state, since a shadow under a translucent fill
 * shows through it as a grey cast and a pressed or selected card already says so with its colour.
 * With glass off: as before glass, a selected card (its editor open beside the list) lifts.
 */
@Composable
fun glassCardElevation(isSelected: Boolean = false): CardElevation {
    val lift by animateDpAsState(
        targetValue = if (isSelected && !LocalTempoGlass.current.enabled) SELECTED_CARD_LIFT else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "selectableCardElevation",
    )
    return CardDefaults.cardElevation(
        defaultElevation = lift,
        pressedElevation = lift,
        focusedElevation = lift,
        hoveredElevation = lift,
        draggedElevation = lift,
        disabledElevation = 0.dp,
    )
}

private val SELECTED_CARD_LIFT = 6.dp

/** The frost a plain glass card is filled with: the theme's `TempoGlass.cardFrost`. */
@Composable
@ReadOnlyComposable
fun cardFrostColor(): Color = LocalTempoGlass.current.cardFrost

/**
 * Tints [baseColor] with the selection accent instead of swapping to a fixed color, so a
 * selected card reads as a highlighted variant of its own resting background — a colorful habit
 * card stays recognizably that color when selected — rather than jumping to an unrelated hue.
 *
 * At rest a card keeps its own fill, frost included: glass cards carry no shadow, which is what
 * made a translucent fill glitch before (it showed through). Selected, the tint is laid over the
 * fill flattened against the screen background, so the one card you are editing beside the list
 * stands out solid from the frosted ones around it.
 */
@Composable
fun selectedContainerColor(
    baseColor: Color,
    isSelected: Boolean,
): Color =
    if (isSelected) {
        MaterialTheme.colorScheme.secondary
            .copy(alpha = SELECTED_TINT_ALPHA)
            .compositeOver(baseColor.compositeOver(MaterialTheme.colorScheme.background))
    } else {
        baseColor
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
