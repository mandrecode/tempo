package com.mandrecode.tempo.features.routines.presentation.components.cards

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.LocalIsDarkTheme
import com.mandrecode.tempo.core.ui.theme.resolveColor
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.util.CompletionHistoryUtil
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Composable
fun QuitHabitCard(
    habit: Habit,
    selectedDate: LocalDate,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    onToggle: ((Long, Boolean) -> Unit)? = null,
) {
    val state = rememberQuitHabitCardState(habit = habit, selectedDate = selectedDate)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = state.cardShape,
        colors =
            CardDefaults.cardColors(
                containerColor = state.containerColor,
                contentColor = state.contentColor,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        HabitItem(
            habit = habit,
            isCompleted = state.isCompleted,
            onToggle = { onToggle?.invoke(habit.id, !state.isCompleted) },
            onClick = onEdit,
            color = state.resolvedHabitColor,
            cardShape = state.cardShape,
            contentColor = state.contentColor,
            canToggle = state.canToggle,
            trailingContent =
                if (state.streak > 0) {
                    {
                        StreakBadge(
                            streak = state.streak,
                            isCompleted = state.isCompleted,
                            contentColor = state.contentColor,
                            accentColor = state.accentColor,
                        )
                    }
                } else {
                    null
                },
        )
    }
}

private data class QuitHabitCardState(
    val isCompleted: Boolean,
    val canToggle: Boolean,
    val streak: Int,
    val resolvedHabitColor: Color?,
    val containerColor: Color,
    val contentColor: Color,
    val accentColor: Color,
    val cardShape: Shape,
)

private data class QuitHabitCardColors(
    val resolvedHabitColor: Color?,
    val containerColor: Color,
    val contentColor: Color,
    val accentColor: Color,
)

@Composable
private fun rememberQuitHabitCardState(
    habit: Habit,
    selectedDate: LocalDate,
): QuitHabitCardState {
    val dateStr = selectedDate.toString()
    val isCompleted =
        remember(habit.completionHistory, dateStr) {
            CompletionHistoryUtil.isDateInHistory(habit.completionHistory, dateStr)
        }

    // Recomputed on every recomposition rather than `remember`-ed so the today/yesterday
    // gating stays accurate if the app remains open across midnight.
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val yesterday = today.minus(DatePeriod(days = 1))
    val canToggle = selectedDate == today || selectedDate == yesterday

    val streak =
        remember(habit.completionHistory) {
            CompletionHistoryUtil.getCurrentStreak(habit.completionHistory)
        }

    val cardCornerRadius by animateDpAsState(
        targetValue = if (isCompleted) 24.dp else 32.dp,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "quit_card_corner_radius",
    )

    val colors = rememberQuitHabitCardColors(habit = habit, isCompleted = isCompleted)

    return QuitHabitCardState(
        isCompleted = isCompleted,
        canToggle = canToggle,
        streak = streak,
        resolvedHabitColor = colors.resolvedHabitColor,
        containerColor = colors.containerColor,
        contentColor = colors.contentColor,
        accentColor = colors.accentColor,
        cardShape = RoundedCornerShape(cardCornerRadius),
    )
}

@Composable
private fun rememberQuitHabitCardColors(
    habit: Habit,
    isCompleted: Boolean,
): QuitHabitCardColors {
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
                    ?: MaterialTheme.colorScheme.surfaceContainerLow
            },
        animationSpec = tween(300),
        label = "quit_card_color",
    )

    val contentColor by animateColorAsState(
        targetValue =
            if (isCompleted) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        animationSpec = tween(300),
        label = "quit_card_content_color",
    )

    return QuitHabitCardColors(
        resolvedHabitColor = resolvedHabitColor,
        containerColor = containerColor,
        contentColor = contentColor,
        accentColor = resolvedHabitColor ?: MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun StreakBadge(
    streak: Int,
    isCompleted: Boolean,
    contentColor: Color,
    accentColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(50),
        color =
            if (isCompleted) {
                contentColor.copy(alpha = 0.2f)
            } else {
                accentColor.copy(alpha = 0.12f)
            },
    ) {
        Text(
            text =
                pluralStringResource(
                    R.plurals.habit_streak_quit_label,
                    streak,
                    streak,
                ),
            style = MaterialTheme.typography.labelMedium,
            color =
                if (isCompleted) {
                    contentColor.copy(alpha = 0.7f)
                } else {
                    accentColor
                },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}
