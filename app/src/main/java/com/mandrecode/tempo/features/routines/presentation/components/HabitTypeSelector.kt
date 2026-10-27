package com.mandrecode.tempo.features.routines.presentation.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.LocalIsDarkTheme
import com.mandrecode.tempo.core.ui.theme.PastelGreenDark
import com.mandrecode.tempo.core.ui.theme.PastelGreenLight
import com.mandrecode.tempo.core.ui.theme.PastelRedDark
import com.mandrecode.tempo.core.ui.theme.PastelRedLight
import com.mandrecode.tempo.features.routines.domain.model.HabitType

/**
 * Illustrated card selector for choosing the habit type (Build vs Quit).
 *
 * Designed to be visually distinct from the [com.mandrecode.tempo.core.ui.components.ExpressiveChip]
 * row used to switch between the Habit and Habit Chain entity tabs above it.
 */
@Composable
fun HabitTypeSelector(
    selectedType: HabitType,
    onTypeSelect: (HabitType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HabitType.entries.forEach { type ->
            HabitTypeCard(
                type = type,
                isSelected = selectedType == type,
                onClick = { onTypeSelect(type) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private data class HabitTypeResources(
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
) {
    companion object {
        fun forType(type: HabitType): HabitTypeResources =
            when (type) {
                HabitType.BUILD ->
                    HabitTypeResources(
                        iconRes = R.drawable.ic_psychiatry,
                        titleRes = R.string.habit_type_build,
                        subtitleRes = R.string.habit_type_build_subtitle,
                    )
                HabitType.QUIT ->
                    HabitTypeResources(
                        iconRes = R.drawable.ic_smoke_free,
                        titleRes = R.string.habit_type_quit,
                        subtitleRes = R.string.habit_type_quit_subtitle,
                    )
            }
    }
}

@Composable
private fun HabitTypeCard(
    type: HabitType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val isDarkTheme = LocalIsDarkTheme.current

    val baseColor: Color =
        when (type) {
            HabitType.BUILD -> if (isDarkTheme) PastelGreenDark else PastelGreenLight
            HabitType.QUIT -> if (isDarkTheme) PastelRedDark else PastelRedLight
        }

    val containerColor by animateColorAsState(
        targetValue =
            if (isSelected) baseColor.copy(alpha = 0.28f) else baseColor.copy(alpha = 0.12f),
        animationSpec = tween(durationMillis = 400),
        label = "habit_type_card_container",
    )
    val contentColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 400),
        label = "habit_type_card_content",
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 1.dp,
        animationSpec = tween(durationMillis = 400),
        label = "habit_type_card_border_width",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) baseColor else baseColor.copy(alpha = 0.30f),
        animationSpec = tween(durationMillis = 400),
        label = "habit_type_card_border_color",
    )

    val cardShape = RoundedCornerShape(20.dp)

    Surface(
        modifier =
            modifier
                .clip(cardShape)
                .selectable(
                    selected = isSelected,
                    role = Role.RadioButton,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    },
                ),
        shape = cardShape,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(borderWidth, borderColor),
    ) {
        HabitTypeCardContent(
            resources = HabitTypeResources.forType(type),
            contentColor = contentColor,
        )
    }
}

@Composable
private fun HabitTypeCardContent(
    resources: HabitTypeResources,
    contentColor: Color,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(resources.iconRes),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(32.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(resources.titleRes),
            style = MaterialTheme.typography.titleMedium,
            color = contentColor,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(resources.subtitleRes),
            style = MaterialTheme.typography.bodySmall,
            color = contentColor.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
    }
}
