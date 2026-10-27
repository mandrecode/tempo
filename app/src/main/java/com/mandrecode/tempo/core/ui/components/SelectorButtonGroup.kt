package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Default height for the editors' selector rows: a full touch target. */
val SelectorButtonHeight: Dp = 48.dp

/**
 * The colours of one selector: the off state matches the repeat-day and Routines day toggles, the
 * on state is the caller's (priority uses its own colour) and otherwise the primary container, the
 * same selected tone as the repeat selector and the day toggles.
 */
@Composable
fun selectorButtonColors(
    checkedContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    checkedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
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
        contentPadding = PaddingValues(horizontal = SelectorContentPadding),
        modifier = modifier.heightIn(min = height).semantics { role = Role.RadioButton },
    ) {
        if (icon != null) {
            icon()
            Spacer(modifier = Modifier.width(SelectorIconGap))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
        )
    }
}

private val SelectorContentPadding = 12.dp
private val SelectorIconSize = 18.dp
private val SelectorIconGap = 8.dp

/**
 * A fixed set of options as one connected selector. When every label fits an equal share of the
 * row on one line, it is a [ButtonGroup] filling the width, the pressed option widening into its
 * neighbours. When one does not — a long translation, a large system font — the same buttons sit
 * in a row that scrolls, each as wide as its label, so no option is ever cut short.
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
    val labels = options.map { label(it) }
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val spacing = ButtonGroupDefaults.ConnectedSpaceBetween
        val share = (maxWidth - spacing * (options.size - 1)) / options.size
        if (labelsFit(labels, share, hasIcon = icon != null)) {
            ButtonGroup(
                overflowIndicator = {},
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing),
            ) {
                options.forEachIndexed { index, option ->
                    customItem(
                        buttonGroupContent = {
                            val interactionSource = remember { MutableInteractionSource() }
                            ConnectedSelectorButton(
                                label = labels[index],
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
                        // Only used when everything fits; nothing overflows.
                        menuContent = {},
                    )
                }
            }
        } else {
            ScrollingSelectorRow(options, labels, isChecked, onClick, enabled, colors, itemModifier, icon)
        }
    }
}

/** Whether the widest label, on one line with the button's padding and icon, fits [share]. */
@Composable
private fun labelsFit(
    labels: List<String>,
    share: Dp,
    hasIcon: Boolean,
): Boolean {
    val textMeasurer = rememberTextMeasurer()
    val style = MaterialTheme.typography.labelLarge
    val density = LocalDensity.current
    val chrome = SelectorContentPadding * 2 + if (hasIcon) SelectorIconSize + SelectorIconGap else 0.dp
    val widest =
        labels.maxOfOrNull { text ->
            with(density) {
                textMeasurer
                    .measure(text, style, maxLines = 1)
                    .size.width
                    .toDp()
            }
        } ?: 0.dp
    return widest + chrome <= share
}

/** The same connected buttons, each as wide as its label, in a row that scrolls. */
@Composable
private fun <T> ScrollingSelectorRow(
    options: List<T>,
    labels: List<String>,
    isChecked: (T) -> Boolean,
    onClick: (T) -> Unit,
    enabled: Boolean,
    colors: @Composable (T) -> ToggleButtonColors,
    itemModifier: (T) -> Modifier,
    icon: (@Composable (T) -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        options.forEachIndexed { index, option ->
            ConnectedSelectorButton(
                label = labels[index],
                checked = isChecked(option),
                onClick = { onClick(option) },
                index = index,
                count = options.size,
                enabled = enabled,
                colors = colors(option),
                icon = icon?.let { { it(option) } },
                modifier = itemModifier(option),
            )
        }
    }
}
