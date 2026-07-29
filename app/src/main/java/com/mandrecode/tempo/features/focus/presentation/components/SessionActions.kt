package com.mandrecode.tempo.features.focus.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupScope
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation

/**
 * How much weight a session action carries.
 *
 * [FILLED] finishes the work for good, [TONAL] is reversible, [DISCARD] throws the session away —
 * three different kinds of act, so three different looks. The running-session card and the session
 * screen share these, so an action never changes weight depending on where you press it.
 */
internal enum class ButtonEmphasis { FILLED, TONAL, DISCARD }

/**
 * The colours a session action draws from.
 *
 * Passed in rather than read from the theme, because the same three weights have to render on two
 * different surfaces: the session screen's plain background and the card's tertiary container.
 */
internal data class SessionActionColors(
    val filledContainer: Color,
    val filledContent: Color,
    val tonalContainer: Color,
    val tonalContent: Color,
    val discardContent: Color,
    val discardBorder: Color,
)

@Composable
internal fun sessionScreenActionColors(): SessionActionColors =
    SessionActionColors(
        filledContainer = MaterialTheme.colorScheme.primary,
        filledContent = MaterialTheme.colorScheme.onPrimary,
        tonalContainer = MaterialTheme.colorScheme.secondaryContainer,
        tonalContent = MaterialTheme.colorScheme.onSecondaryContainer,
        discardContent = MaterialTheme.colorScheme.error,
        discardBorder = MaterialTheme.colorScheme.error.copy(alpha = DISCARD_BORDER_ALPHA),
    )

@Composable
internal fun sessionCardActionColors(): SessionActionColors =
    SessionActionColors(
        filledContainer = MaterialTheme.colorScheme.onTertiaryContainer,
        filledContent = MaterialTheme.colorScheme.tertiaryContainer,
        tonalContainer = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = TONAL_ALPHA),
        tonalContent = MaterialTheme.colorScheme.onTertiaryContainer,
        discardContent = MaterialTheme.colorScheme.onTertiaryContainer,
        discardBorder = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = TONAL_ALPHA),
    )

/** One action's label, icon and weight, so a group can be described as data. */
internal data class SessionAction(
    val label: String,
    val iconRes: Int,
    val emphasis: ButtonEmphasis,
    val onClick: () -> Unit,
)

/**
 * Two actions drawn as a connected button group.
 *
 * A real [ButtonGroup] rather than a plain row: pressing one item widens it and squeezes its
 * neighbour, which is the interaction that tells you the two belong together. Its items are custom
 * because the group's own `clickableItem` offers no colour control, and the whole point here is
 * that the two buttons do *not* look alike.
 *
 * The shapes are the connected treatment — round on the group's outer ends, nearly square where
 * the two meet — so the pair reads as one control split in two rather than as two loose buttons.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun SessionActionGroup(
    actions: List<SessionAction>,
    colors: SessionActionColors,
    modifier: Modifier = Modifier,
) {
    ButtonGroup(
        overflowIndicator = {},
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(GroupItemSpacing),
    ) {
        actions.forEachIndexed { index, action ->
            groupedAction(
                action = action,
                colors = colors,
                isFirst = index == 0,
                isLast = index == actions.lastIndex,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ButtonGroupScope.groupedAction(
    action: SessionAction,
    colors: SessionActionColors,
    isFirst: Boolean,
    isLast: Boolean,
) {
    customItem(
        buttonGroupContent = {
            val (interactionSource, outerRadius) =
                rememberPressableButtonAnimation(
                    baseRadius = PillRadius,
                    pressedRadius = PillPressedRadius,
                )
            SessionActionSurface(
                action = action,
                colors = colors,
                shape =
                    RoundedCornerShape(
                        topStart = if (isFirst) outerRadius.value else InnerRadius,
                        bottomStart = if (isFirst) outerRadius.value else InnerRadius,
                        topEnd = if (isLast) outerRadius.value else InnerRadius,
                        bottomEnd = if (isLast) outerRadius.value else InnerRadius,
                    ),
                interactionSource = interactionSource,
                modifier = Modifier.weight(1f).animateWidth(interactionSource),
            )
        },
        // Nothing overflows: the group only ever holds the two actions handed to it.
        menuContent = {},
    )
}

/** A single action on its own line, for the one that is not part of a group. */
@Composable
internal fun SessionActionButton(
    action: SessionAction,
    colors: SessionActionColors,
    modifier: Modifier = Modifier,
) {
    val (interactionSource, radius) =
        rememberPressableButtonAnimation(baseRadius = PillRadius, pressedRadius = PillPressedRadius)
    SessionActionSurface(
        action = action,
        colors = colors,
        shape = RoundedCornerShape(radius.value),
        interactionSource = interactionSource,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun SessionActionSurface(
    action: SessionAction,
    colors: SessionActionColors,
    shape: RoundedCornerShape,
    interactionSource: MutableInteractionSource,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        onClick = {
            // A commitment gets the firmer confirmation; the reversible ones do not.
            haptic.performHapticFeedback(
                if (action.emphasis == ButtonEmphasis.FILLED) {
                    HapticFeedbackType.LongPress
                } else {
                    HapticFeedbackType.TextHandleMove
                },
            )
            action.onClick()
        },
        modifier = modifier,
        interactionSource = interactionSource,
        shape = shape,
        color =
            when (action.emphasis) {
                ButtonEmphasis.FILLED -> colors.filledContainer
                ButtonEmphasis.TONAL -> colors.tonalContainer
                ButtonEmphasis.DISCARD -> Color.Transparent
            },
        contentColor =
            when (action.emphasis) {
                ButtonEmphasis.FILLED -> colors.filledContent
                ButtonEmphasis.TONAL -> colors.tonalContent
                ButtonEmphasis.DISCARD -> colors.discardContent
            },
        border =
            if (action.emphasis == ButtonEmphasis.DISCARD) {
                BorderStroke(1.dp, colors.discardBorder)
            } else {
                null
            },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // The glyph carries the action at a glance — a tick, a pause bar, a stop square — so
            // the three are told apart before the labels are read.
            Icon(
                painter = painterResource(action.iconRes),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = action.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

private val PillRadius = 24.dp
private val PillPressedRadius = 12.dp
private val InnerRadius = 6.dp
private val GroupItemSpacing: Dp = 2.dp
private const val DISCARD_BORDER_ALPHA = 0.4f
private const val TONAL_ALPHA = 0.12f
