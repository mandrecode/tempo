package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R

/**
 * A minus/value/plus stepper.
 *
 * The shape the repeat-reminder interval already uses, lifted here so anything else that asks for
 * "how many" looks and behaves the same rather than growing its own control.
 */
@Composable
fun ValueStepper(
    value: Int,
    label: String,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange = 1..Int.MAX_VALUE,
    step: Int = 1,
    enabled: Boolean = true,
    buttonHeight: Dp = StepperButtonHeight,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        StepperButton(
            iconRes = R.drawable.ic_remove,
            contentDescription = stringResource(R.string.decrease_interval),
            enabled = enabled && value - step >= range.first,
            height = buttonHeight,
            onClick = { onValueChange((value - step).coerceIn(range)) },
        )

        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.widthIn(min = StepperValueMinWidth),
            textAlign = TextAlign.Center,
        )

        StepperButton(
            iconRes = R.drawable.ic_add,
            contentDescription = stringResource(R.string.increase_interval),
            enabled = enabled && value + step <= range.last,
            height = buttonHeight,
            onClick = { onValueChange((value + step).coerceIn(range)) },
        )
    }
}

@Composable
private fun StepperButton(
    iconRes: Int,
    contentDescription: String,
    enabled: Boolean,
    height: Dp,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        shape = RoundedCornerShape(STEPPER_CORNER_PERCENT),
        color =
            MaterialTheme.colorScheme.surfaceContainerHighest.let {
                if (enabled) it else it.copy(alpha = DISABLED_ALPHA)
            },
        modifier =
            Modifier
                .size(width = StepperButtonWidth, height = height)
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(STEPPER_CORNER_PERCENT))
                .clickable(enabled = enabled) {
                    // Adjusting a value is a small commit, matching the tick a toggle gives.
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(StepperIconSize),
                tint =
                    MaterialTheme.colorScheme.onSurfaceVariant.let {
                        if (enabled) it else it.copy(alpha = DISABLED_ICON_ALPHA)
                    },
            )
        }
    }
}

private val StepperButtonWidth = 32.dp
private val StepperButtonHeight = 40.dp
private val StepperIconSize = 18.dp
private val StepperValueMinWidth = 64.dp
private const val STEPPER_CORNER_PERCENT = 50
private const val DISABLED_ALPHA = 0.5f
private const val DISABLED_ICON_ALPHA = 0.6f
