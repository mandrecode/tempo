package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonColors
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Default height for the editors' selector rows: a full touch target. */
val SelectorButtonHeight: Dp = 48.dp

/**
 * The colours of one selector: the off state matches the repeat-day and Routines day toggles, the
 * on state is the caller's (priority uses its own colour, periodicity the tertiary pair).
 */
@Composable
fun selectorButtonColors(
    checkedContainerColor: Color = MaterialTheme.colorScheme.primary,
    checkedContentColor: Color = MaterialTheme.colorScheme.onPrimary,
): ToggleButtonColors =
    ToggleButtonDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        checkedContainerColor = checkedContainerColor,
        checkedContentColor = checkedContentColor,
    )

/** The connected shapes for the item at [index] of [count]: round outer ends, tight joins. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun connectedButtonShapes(
    index: Int,
    count: Int,
): ToggleButtonShapes =
    when {
        count == 1 -> ToggleButtonDefaults.shapesFor(SelectorButtonHeight)
        index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
        index == count - 1 -> ButtonGroupDefaults.connectedTrailingButtonShapes()
        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
    }

/**
 * One option of a connected selector (#336): a Material 3 Expressive [ToggleButton] with the
 * connected button group shapes for its position, so a row of them reads as one control whose
 * chosen option rounds out and lifts. Used directly where the options scroll (categories,
 * periodicity) and through [SelectorButtonGroup] where they fit.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ConnectedSelectorButton(
    label: String,
    checked: Boolean,
    onClick: () -> Unit,
    index: Int,
    count: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ToggleButtonColors = selectorButtonColors(),
    height: Dp = SelectorButtonHeight,
    interactionSource: MutableInteractionSource? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    ToggleButton(
        checked = checked,
        onCheckedChange = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        enabled = enabled,
        shapes = connectedButtonShapes(index, count),
        colors = colors,
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 12.dp),
        modifier = modifier.heightIn(min = height).semantics { role = Role.RadioButton },
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * A fixed set of options shared out across the row as a connected [ButtonGroup]: every option
 * visible, the pressed one widening into its neighbours.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> SelectorButtonGroup(
    options: List<T>,
    isChecked: (T) -> Boolean,
    onClick: (T) -> Unit,
    label: @Composable (T) -> String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: @Composable (T) -> ToggleButtonColors = { selectorButtonColors() },
    itemModifier: (T) -> Modifier = { Modifier },
    icon: (@Composable (T) -> Unit)? = null,
) {
    ButtonGroup(
        overflowIndicator = {},
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        options.forEachIndexed { index, option ->
            customItem(
                buttonGroupContent = {
                    val interactionSource = remember { MutableInteractionSource() }
                    ConnectedSelectorButton(
                        label = label(option),
                        checked = isChecked(option),
                        onClick = { onClick(option) },
                        index = index,
                        count = options.size,
                        enabled = enabled,
                        colors = colors(option),
                        interactionSource = interactionSource,
                        icon = icon?.let { { it(option) } },
                        modifier =
                            itemModifier(option)
                                .weight(1f)
                                .animateWidth(interactionSource),
                    )
                },
                // A fixed set sized to fit; nothing overflows.
                menuContent = {},
            )
        }
    }
}
