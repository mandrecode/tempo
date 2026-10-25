package com.mandrecode.tempo.features.routines.presentation.components.cards

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.selectableCardElevation
import com.mandrecode.tempo.core.ui.components.selectedContainerColor
import com.mandrecode.tempo.core.ui.theme.LocalIsDarkTheme
import com.mandrecode.tempo.core.ui.theme.TempoIcon
import com.mandrecode.tempo.core.ui.theme.chainTitle
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
    onEdit: () -> Unit,
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
        targetValue = if (allCompleted) 20.dp else 28.dp,
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

    val baseContainerColor =
        if (allCompleted) {
            resolvedChainColor ?: MaterialTheme.colorScheme.primary
        } else {
            resolvedChainColor?.copy(alpha = 0.15f)
                ?: MaterialTheme.colorScheme.background
        }
    val containerColor by animateColorAsState(
        targetValue = selectedContainerColor(baseContainerColor, isSelected),
        animationSpec = tween(300),
        label = "habit_chain_card_color",
    )

    val contentColor by animateColorAsState(
        targetValue =
            if (allCompleted) {
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
    val selectionElevation = selectableCardElevation(isSelected)

    // Title, progress and icons share the chain colour until every habit is done, then the
    // card's content colour.
    val accentColor =
        if (allCompleted) contentColor else resolvedChainColor ?: MaterialTheme.colorScheme.primary

    Row(
        // The caller's modifier reaches the root, which it never used to: the parameter was
        // declared and dropped, so a list handing this card an item animation got nothing back.
        modifier =
            modifier
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
            onClick = onEdit,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .semantics { selected = isSelected },
            shape = cardShape,
            colors =
                CardDefaults.cardColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = selectionElevation),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Faded chain icon behind the header, top right
                habitChain.icon?.let { iconName ->
                    TempoIcon.fromName(iconName)?.let { habitIcon ->
                        Box(modifier = Modifier.matchParentSize()) {
                            Icon(
                                painter = painterResource(habitIcon.iconRes),
                                contentDescription = null,
                                modifier =
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .size(120.dp)
                                        .offset(x = 24.dp, y = (-16).dp)
                                        .graphicsLayer {
                                            alpha = 0.08f
                                            rotationZ = -25f
                                        },
                                tint = accentColor,
                            )
                        }
                    }
                }

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 8.dp),
                ) {
                    HabitChainHeader(
                        title = habitChain.title,
                        description = sanitizedChainDescription,
                        completedCount = completedCount,
                        totalCount = totalCount,
                        progress = { animatedProgress },
                        showProgress = !isFuture && totalCount > 0,
                        accentColor = accentColor,
                        contentColor = contentColor,
                        allCompleted = allCompleted,
                        onAddHabit = onAddHabit,
                    )

                    // The chain's habits are always listed (#454): they are what you tick off,
                    // so hiding them behind an expand arrow only added a tap.
                    if (chainHabits.isNotEmpty()) {
                        Column(modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)) {
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
                                        cardShape = RoundedCornerShape(ChainHabitCheckboxSize / 2 + 4.dp),
                                        contentColor = contentColor,
                                        verticalPadding = 4.dp,
                                        checkboxSize = ChainHabitCheckboxSize,
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

@Composable
private fun HabitChainHeader(
    title: String,
    description: String,
    completedCount: Int,
    totalCount: Int,
    progress: () -> Float,
    showProgress: Boolean,
    accentColor: Color,
    contentColor: Color,
    allCompleted: Boolean,
    onAddHabit: (() -> Unit)?,
) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.chainTitle,
                color = accentColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            if (showProgress) {
                ChainProgressPill(
                    completedCount = completedCount,
                    totalCount = totalCount,
                    // The accent is already the content colour once every habit is done.
                    color = accentColor.copy(alpha = if (allCompleted) 0.2f else 0.12f),
                    contentColor = contentColor,
                )
            }

            if (onAddHabit != null) {
                IconButton(onClick = onAddHabit) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_row_below),
                        contentDescription = stringResource(R.string.add_habit),
                        modifier = Modifier.size(24.dp),
                        tint = if (allCompleted) contentColor else accentColor.copy(alpha = 0.6f),
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(12.dp))
            }
        }

        if (description.isNotBlank()) {
            EnhancedDescriptionText(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
            )
        }

        if (showProgress) {
            LinearProgressIndicator(
                progress = progress,
                modifier =
                    Modifier
                        .padding(top = 8.dp, end = 12.dp)
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(CircleShape),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.15f),
            )
        }
    }
}

@Composable
private fun ChainProgressPill(
    completedCount: Int,
    totalCount: Int,
    color: Color,
    contentColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = color,
        modifier = Modifier.padding(start = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.habit_chain_progress_count, completedCount, totalCount),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}
