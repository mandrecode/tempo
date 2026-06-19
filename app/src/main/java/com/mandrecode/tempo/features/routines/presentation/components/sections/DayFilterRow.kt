package com.mandrecode.tempo.features.routines.presentation.components.sections

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.filterChipSelected
import com.mandrecode.tempo.core.ui.util.rememberPressableChipAnimation
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Composable
fun DayFilterRow(
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

    // Generate a week of days starting from Yesterday
    val days =
        remember(today) {
            (-1..5).map { today.plus(DatePeriod(days = it)) }
        }

    LazyRow(
        modifier =
            modifier
                .fillMaxWidth()
                .height(64.dp),
        // Fixed height to prevent "bouncing" of the content below
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items(days) { date ->
            val isToday = date == today
            val isYesterday = date == today.minus(DatePeriod(days = 1))
            val isTomorrow = date == today.plus(DatePeriod(days = 1))

            val label =
                when {
                    isToday -> stringResource(R.string.today)
                    isYesterday -> stringResource(R.string.yesterday)
                    isTomorrow -> stringResource(R.string.tomorrow)
                    else -> {
                        // Use localized day of week name
                        stringResource(
                            when (date.dayOfWeek) {
                                DayOfWeek.MONDAY -> R.string.monday
                                DayOfWeek.TUESDAY -> R.string.tuesday
                                DayOfWeek.WEDNESDAY -> R.string.wednesday
                                DayOfWeek.THURSDAY -> R.string.thursday
                                DayOfWeek.FRIDAY -> R.string.friday
                                DayOfWeek.SATURDAY -> R.string.saturday
                                DayOfWeek.SUNDAY -> R.string.sunday
                            },
                        )
                    }
                }

            DayFilterChip(
                label = label,
                isSelected = selectedDate == date,
                onClick = { onSelectDate(date) },
                showIcon = isToday,
            )
        }
    }
}

@Composable
@Suppress("LongMethod")
internal fun DayFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showIcon: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val animatedCornerRadius =
        rememberPressableChipAnimation(
            isSelected = isSelected,
            interactionSource = interactionSource,
            selectedRadius = 16.dp,
            unselectedRadius = 12.dp,
        )

    val containerColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
        animationSpec = tween(300),
        label = "container_color",
    )

    val contentColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec = tween(300),
        label = "content_color",
    )

    val horizontalPadding by animateDpAsState(
        targetValue = if (isSelected) 20.dp else 16.dp,
        animationSpec = tween(250), // Changed from spring to tween to remove bouncing overshoot
        label = "horizontal_padding",
    )

    // Use a fixed vertical padding to maintain a stable height for the chip
    val verticalPadding = 10.dp

    Surface(
        modifier =
            modifier
                .clip(RoundedCornerShape(animatedCornerRadius.value))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    },
                ),
        color = containerColor,
        shape = RoundedCornerShape(animatedCornerRadius.value),
        border =
            if (!isSelected) {
                androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                )
            } else {
                null
            },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (showIcon && isSelected) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = contentColor,
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
            Text(
                text = label,
                style =
                    if (isSelected) {
                        MaterialTheme.typography.filterChipSelected
                    } else {
                        MaterialTheme.typography.labelLarge
                    },
                color = contentColor,
            )
        }
    }
}
