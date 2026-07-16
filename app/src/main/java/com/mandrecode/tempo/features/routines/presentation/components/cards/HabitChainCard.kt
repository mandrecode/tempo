package com.mandrecode.tempo.features.routines.presentation.components.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.LocalIsDarkTheme
import com.mandrecode.tempo.core.ui.theme.TempoIcon
import com.mandrecode.tempo.core.ui.theme.badgeCount
import com.mandrecode.tempo.core.ui.theme.dialogTitle
import com.mandrecode.tempo.core.ui.theme.resolveColor
import com.mandrecode.tempo.core.ui.util.EnhancedDescriptionText
import com.mandrecode.tempo.core.ui.util.sanitizeDescription
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.util.CompletionHistoryUtil
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Composable
fun HabitChainCard(
    habitChain: HabitChain,
    chainHabits: List<Habit>,
    selectedDate: LocalDate,
    isExpanded: Boolean,
    onEdit: () -> Unit,
    onToggleExpansion: () -> Unit,
    modifier: Modifier = Modifier,
    onAddHabit: (() -> Unit)? = null,
    onHabitToggle: ((Long, Boolean) -> Unit)? = null,
    onHabitClick: ((Long) -> Unit)? = null,
    timeLabel: String? = null,
    showTimeline: Boolean = true,
    isSelected: Boolean = false,
    selectedHabitId: Long? = null,
) {
    val dateStr = selectedDate.toString()
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val yesterday = today.minus(DatePeriod(days = 1))

    val isFuture = selectedDate > today

    // Toggling is only allowed for Yesterday and Today
    val canToggle = selectedDate == today || selectedDate == yesterday

    val (completedCount, totalCount, progress) =
        remember(chainHabits, dateStr) {
            val total = chainHabits.size
            if (total == 0) {
                Triple(0, 0, 0f)
            } else {
                val completed =
                    chainHabits.count { habit ->
                        CompletionHistoryUtil.isDateInHistory(habit.completionHistory, dateStr)
                    }
                Triple(completed, total, completed.toFloat() / total.toFloat())
            }
        }

    val allCompleted = completedCount == totalCount && totalCount > 0
    val sanitizedChainDescription =
        remember(habitChain.description) { sanitizeDescription(habitChain.description) }

    val cardCornerRadius by animateDpAsState(
        targetValue = if (allCompleted) 24.dp else 32.dp,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy, // More stable for day transitions
                stiffness = Spring.StiffnessMedium,
            ),
        label = "habit_chain_card_corner_radius",
    )

    val isDarkTheme = LocalIsDarkTheme.current
    val resolvedChainColor =
        resolveColor(
            colorKey = habitChain.colorKey,
            colorScheme = MaterialTheme.colorScheme,
            isDarkTheme = isDarkTheme,
        )

    val containerColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else if (allCompleted) {
                resolvedChainColor ?: MaterialTheme.colorScheme.primary
            } else {
                resolvedChainColor?.copy(alpha = 0.15f)
                    ?: MaterialTheme.colorScheme.surfaceContainer
            },
        animationSpec = tween(300),
        label = "habit_chain_card_color",
    )

    val contentColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else if (allCompleted) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        animationSpec = tween(300),
        label = "habit_chain_card_content_color",
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "habit_chain_progress",
    )

    val cardShape = RoundedCornerShape(cardCornerRadius)

    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "arrow rotation",
    )

    val animatedBottomPadding by animateDpAsState(
        targetValue = if (isExpanded) 0.dp else 16.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "bottom_padding",
    )

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
                                resolvedChainColor?.copy(alpha = 0.3f)
                                    ?: MaterialTheme.colorScheme.outlineVariant,
                        ),
            )

            Spacer(modifier = Modifier.width(16.dp))
        }

        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .then(
                        if (isSelected) {
                            Modifier.border(2.dp, MaterialTheme.colorScheme.primary, cardShape)
                        } else {
                            Modifier
                        },
                    ).semantics { selected = isSelected }
                    .clickable { onEdit() },
            shape = cardShape,
            colors =
                CardDefaults.cardColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Large faded background icon in the top right
                habitChain.icon?.let { iconName ->
                    TempoIcon.fromName(iconName)?.let { habitIcon ->
                        Box(modifier = Modifier.matchParentSize()) {
                            Icon(
                                painter = painterResource(habitIcon.iconRes),
                                contentDescription = null,
                                modifier =
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .size(180.dp)
                                        .offset(x = 30.dp, y = 0.dp)
                                        .graphicsLayer {
                                            alpha = 0.08f
                                            rotationZ = -25f
                                        },
                                tint =
                                    if (allCompleted) {
                                        contentColor
                                    } else {
                                        (
                                            resolvedChainColor
                                                ?: MaterialTheme.colorScheme.primary
                                        )
                                    },
                            )
                        }
                    }
                }

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                top = 12.dp,
                                bottom = animatedBottomPadding,
                            ),
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Left side: title, description, status bar
                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = habitChain.title,
                                style =
                                    MaterialTheme.typography.dialogTitle,
                                color =
                                    if (allCompleted) {
                                        contentColor
                                    } else {
                                        (
                                            resolvedChainColor
                                                ?: MaterialTheme.colorScheme.primary
                                        )
                                    },
                                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier =
                                    Modifier
                                        .padding(top = 8.dp, end = 48.dp),
                            )

                            if (sanitizedChainDescription.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                EnhancedDescriptionText(
                                    text = sanitizedChainDescription,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = contentColor.copy(alpha = 0.7f),
                                    maxLines =
                                        if (isExpanded) Int.MAX_VALUE else 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(end = 48.dp),
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier =
                                    Modifier
                                        .padding(end = 48.dp)
                                        .height(32.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                            ) {
                                if (!isFuture) {
                                    LinearProgressIndicator(
                                        progress = { animatedProgress },
                                        modifier =
                                            Modifier
                                                .width(100.dp)
                                                .height(10.dp)
                                                .clip(CircleShape),
                                        color =
                                            if (allCompleted) {
                                                contentColor
                                            } else {
                                                (
                                                    resolvedChainColor
                                                        ?: MaterialTheme.colorScheme.primary
                                                )
                                            },
                                        trackColor =
                                            (
                                                if (allCompleted) {
                                                    contentColor
                                                } else {
                                                    (
                                                        resolvedChainColor
                                                            ?: MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            ).copy(alpha = 0.15f),
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))

                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color =
                                            if (allCompleted) {
                                                contentColor.copy(alpha = 0.2f)
                                            } else {
                                                (
                                                    resolvedChainColor
                                                        ?: MaterialTheme.colorScheme.primary
                                                ).copy(alpha = 0.12f)
                                            },
                                    ) {
                                        Text(
                                            text =
                                                stringResource(
                                                    R.string.habit_chain_progress_count,
                                                    completedCount,
                                                    totalCount,
                                                ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = contentColor,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        )
                                    }
                                } else {
                                    val habitsWithResolvedIcons =
                                        chainHabits.mapNotNull { habit ->
                                            habit.icon?.let { TempoIcon.fromName(it) }
                                        }

                                    val totalHabits = chainHabits.size

                                    val maxSlots = 4
                                    val displayList: List<TempoIcon>
                                    val badgeCount: Int

                                    if (totalHabits <= maxSlots && habitsWithResolvedIcons.size == totalHabits) {
                                        displayList = habitsWithResolvedIcons
                                        badgeCount = 0
                                    } else {
                                        displayList = habitsWithResolvedIcons.take(maxSlots - 1)
                                        badgeCount = totalHabits - displayList.size
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    ) {
                                        displayList.forEach { habitIcon ->
                                            Icon(
                                                painter = painterResource(habitIcon.iconRes),
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = contentColor.copy(alpha = 0.6f),
                                            )
                                        }

                                        if (badgeCount > 0) {
                                            Box(
                                                modifier =
                                                    Modifier
                                                        .size(20.dp)
                                                        .background(
                                                            contentColor.copy(alpha = 0.1f),
                                                            CircleShape,
                                                        ),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    text = "+$badgeCount",
                                                    style =
                                                        MaterialTheme.typography.badgeCount,
                                                    color = contentColor.copy(alpha = 0.8f),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Right side: expand arrow + add habit button
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.align(Alignment.Top),
                        ) {
                            Box(
                                modifier = Modifier.size(48.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                IconButton(
                                    onClick = onToggleExpansion,
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                        contentDescription =
                                            if (isExpanded) {
                                                stringResource(R.string.collapse_chain)
                                            } else {
                                                stringResource(R.string.expand_chain)
                                            },
                                        modifier =
                                            Modifier
                                                .size(24.dp)
                                                .rotate(arrowRotation),
                                        tint =
                                            if (allCompleted) {
                                                contentColor
                                            } else {
                                                (
                                                    resolvedChainColor
                                                        ?: MaterialTheme.colorScheme.primary
                                                )
                                            },
                                    )
                                }
                            }

                            if (onAddHabit != null) {
                                IconButton(
                                    onClick = onAddHabit,
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_add_row_below),
                                        contentDescription = stringResource(R.string.add_habit),
                                        modifier = Modifier.size(24.dp),
                                        tint =
                                            if (allCompleted) {
                                                contentColor
                                            } else {
                                                (
                                                    resolvedChainColor
                                                        ?: MaterialTheme.colorScheme.primary
                                                ).copy(alpha = 0.6f)
                                            },
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = chainHabits.isNotEmpty() && isExpanded,
                        enter = expandVertically(spring(stiffness = Spring.StiffnessMedium)) + fadeIn(),
                        exit = shrinkVertically(spring(stiffness = Spring.StiffnessMedium)) + fadeOut(),
                    ) {
                        Column(modifier = Modifier.padding(bottom = 8.dp)) {
                            chainHabits.forEach { habit ->
                                key(habit.id) {
                                    val habitIsCompleted =
                                        CompletionHistoryUtil.isDateInHistory(habit.completionHistory, dateStr)
                                    HabitItem(
                                        habit = habit,
                                        isCompleted = habitIsCompleted,
                                        onToggle = {
                                            onHabitToggle?.invoke(
                                                habit.id,
                                                !habitIsCompleted,
                                            )
                                        },
                                        onClick = { onHabitClick?.invoke(habit.id) },
                                        color = resolvedChainColor,
                                        cardShape = cardShape,
                                        contentColor =
                                            if (habit.id == selectedHabitId) {
                                                MaterialTheme.colorScheme.onSecondaryContainer
                                            } else {
                                                contentColor
                                            },
                                        horizontalPadding = 16.dp,
                                        canToggle = canToggle,
                                        isInsideChain = true,
                                        isContainerCompleted = allCompleted,
                                        isSelected = habit.id == selectedHabitId,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
