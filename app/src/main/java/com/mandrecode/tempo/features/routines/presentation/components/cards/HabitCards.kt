package com.mandrecode.tempo.features.routines.presentation.components.cards

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.LocalIsDarkTheme
import com.mandrecode.tempo.core.ui.theme.TempoIcon
import com.mandrecode.tempo.core.ui.theme.cardTitle
import com.mandrecode.tempo.core.ui.theme.neutralCardContainer
import com.mandrecode.tempo.core.ui.theme.resolveColor
import com.mandrecode.tempo.core.ui.theme.subtaskTitle
import com.mandrecode.tempo.core.ui.util.EnhancedDescriptionText
import com.mandrecode.tempo.core.ui.util.sanitizeDescription
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.util.CompletionHistoryUtil
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

private const val HABIT_COLLAPSED_MAX_LINES = 1

@Composable
internal fun HabitItem(
    habit: Habit,
    isCompleted: Boolean,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color? = null,
    cardShape: Shape = RoundedCornerShape(24.dp),
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 16.dp,
    canToggle: Boolean = true,
    isInsideChain: Boolean = false,
    isContainerCompleted: Boolean = isCompleted,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    var isDescriptionExpanded by remember(habit.id) { mutableStateOf(false) }
    var isDescriptionOverflowing by remember(habit.id) { mutableStateOf(false) }
    val sanitizedDescription =
        remember(habit.id, habit.description) { sanitizeDescription(habit.description) }

    LaunchedEffect(sanitizedDescription) {
        isDescriptionExpanded = false
        isDescriptionOverflowing = false
    }

    val descriptionMaxLines =
        when {
            isInsideChain -> 1
            isDescriptionExpanded -> Int.MAX_VALUE
            else -> HABIT_COLLAPSED_MAX_LINES
        }

    // Snappier spring for radius morphing
    val checkboxRadius by animateDpAsState(
        targetValue = if (isCompleted) 16.dp else 28.dp,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "checkbox_radius",
    )

    // Visual "pop" scale animation for toggling
    val checkboxScale by animateFloatAsState(
        targetValue = if (isCompleted) 1.05f else 1f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "checkbox_scale",
    )

    val checkboxBgColor by animateColorAsState(
        targetValue =
            if (isCompleted) {
                if (!isContainerCompleted) {
                    (color ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.08f)
                } else {
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                }
            } else {
                (color ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.12f)
            },
        animationSpec = tween(300),
        label = "checkbox_bg_color",
    )

    val iconTintColor by animateColorAsState(
        targetValue =
            if (!isContainerCompleted) {
                color ?: MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onPrimary
            },
        animationSpec = tween(300),
        label = "icon_tint_color",
    )

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(cardShape)
                .clickable { onClick() }
                .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Large animated checkbox
        Box(
            modifier =
                Modifier
                    .size(56.dp)
                    .graphicsLayer {
                        scaleX = checkboxScale
                        scaleY = checkboxScale
                        alpha = if (canToggle) 1f else 0.5f
                    }.clip(RoundedCornerShape(checkboxRadius))
                    .clickable(enabled = canToggle) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggle()
                    },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(56.dp)
                        .background(
                            color = checkboxBgColor,
                            shape = RoundedCornerShape(checkboxRadius),
                        ).then(
                            if (!isCompleted) {
                                Modifier.border(
                                    width = 2.dp,
                                    color =
                                        (
                                            color
                                                ?: MaterialTheme.colorScheme.primary
                                        ).copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(checkboxRadius),
                                )
                            } else {
                                Modifier
                            },
                        ),
            )

            if (isCompleted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = stringResource(R.string.completed),
                    modifier = Modifier.size(28.dp),
                    tint = iconTintColor,
                )
            } else {
                // Show habit icon if available
                habit.icon?.let { iconName ->
                    TempoIcon.fromName(iconName)?.let { habitIcon ->
                        Icon(
                            painter = painterResource(habitIcon.iconRes),
                            contentDescription = null,
                            modifier =
                                Modifier
                                    .size(28.dp)
                                    .testTag("habitIcon"),
                            tint = iconTintColor,
                        )
                    }
                }
            }

            // Visual indicator when toggle is disabled
            if (!canToggle) {
                Box(
                    modifier =
                        Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.08f)),
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = habit.title,
                style =
                    if (isInsideChain) {
                        MaterialTheme.typography.subtaskTitle.copy(
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                        )
                    } else {
                        MaterialTheme.typography.cardTitle.copy(
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                        )
                    },
                color = if (isCompleted) contentColor.copy(alpha = 0.5f) else contentColor,
                maxLines = if (isInsideChain) 1 else 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (sanitizedDescription.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                EnhancedDescriptionText(
                    text = sanitizedDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (isCompleted) {
                            contentColor.copy(alpha = 0.35f)
                        } else {
                            contentColor.copy(alpha = 0.7f)
                        },
                    maxLines = descriptionMaxLines,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { textLayoutResult ->
                        if (!isInsideChain) {
                            isDescriptionOverflowing =
                                if (isDescriptionExpanded) {
                                    textLayoutResult.lineCount > HABIT_COLLAPSED_MAX_LINES
                                } else {
                                    textLayoutResult.hasVisualOverflow
                                }
                        }
                    },
                )
            }
        }

        if (!isInsideChain && isDescriptionOverflowing) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Top).padding(top = 4.dp),
            ) {
                IconButton(
                    onClick = { isDescriptionExpanded = !isDescriptionExpanded },
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        painter =
                            painterResource(
                                if (isDescriptionExpanded) {
                                    R.drawable.ic_expand_less
                                } else {
                                    R.drawable.ic_expand_more
                                },
                            ),
                        contentDescription =
                            if (isDescriptionExpanded) {
                                stringResource(R.string.collapse)
                            } else {
                                stringResource(R.string.expand)
                            },
                        modifier = Modifier.size(24.dp),
                        tint = contentColor.copy(alpha = 0.6f),
                    )
                }
            }
        }

        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        }
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    selectedDate: LocalDate,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onToggle: ((Long, Boolean) -> Unit)? = null,
    timeLabel: String? = null,
    showTimeline: Boolean = true,
) {
    val dateStr = selectedDate.toString()
    val isCompleted =
        remember(habit.completionHistory, dateStr) {
            CompletionHistoryUtil.isDateInHistory(habit.completionHistory, dateStr)
        }

    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val yesterday = remember(today) { today.minus(DatePeriod(days = 1)) }

    // Toggling is only allowed for Yesterday and Today
    val canToggle = selectedDate == today || selectedDate == yesterday

    val cardCornerRadius by animateDpAsState(
        targetValue = if (isCompleted) 24.dp else 32.dp,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy, // More stable for day transitions
                stiffness = Spring.StiffnessMedium,
            ),
        label = "habit_card_corner_radius",
    )

    val isDarkTheme = LocalIsDarkTheme.current
    val resolvedHabitColor =
        resolveColor(
            colorKey = habit.colorKey,
            colorScheme = MaterialTheme.colorScheme,
            isDarkTheme = isDarkTheme,
        )

    val containerColor by animateColorAsState(
        targetValue =
            if (isCompleted) {
                resolvedHabitColor ?: MaterialTheme.colorScheme.primary
            } else {
                resolvedHabitColor?.copy(alpha = 0.15f)
                    ?: MaterialTheme.colorScheme.neutralCardContainer
            },
        animationSpec = tween(300),
        label = "habit_card_color",
    )

    val contentColor by animateColorAsState(
        targetValue =
            if (isCompleted) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        animationSpec = tween(300),
        label = "habit_card_content_color",
    )

    val cardShape = RoundedCornerShape(cardCornerRadius)

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.Start,
    ) {
        if (showTimeline) {
            Column(
                modifier = Modifier.width(64.dp),
                horizontalAlignment = Alignment.End,
            ) {
                if (timeLabel != null) {
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                }
            }

            Box(
                modifier =
                    Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(
                            color =
                                resolvedHabitColor?.copy(alpha = 0.3f)
                                    ?: MaterialTheme.colorScheme.outlineVariant,
                        ),
            )

            Spacer(modifier = Modifier.width(16.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            colors =
                CardDefaults.cardColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            HabitItem(
                habit = habit,
                isCompleted = isCompleted,
                onToggle = { onToggle?.invoke(habit.id, !isCompleted) },
                onClick = onEdit,
                color = resolvedHabitColor,
                cardShape = cardShape,
                contentColor = contentColor,
                horizontalPadding = 16.dp,
                verticalPadding = 16.dp,
                canToggle = canToggle,
            )
        }
    }
}
