package com.mandrecode.tempo.features.routines.presentation.components.cards

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.model.HabitType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

private val PREVIEW_DATE = LocalDate(2025, 6, 15)

private val PREVIEW_CREATED =
    LocalDateTime(2025, 6, 1, 8, 0)

private val SHORT_DESCRIPTION = "Quick morning stretch"

private val LONG_DESCRIPTION =
    "Do a full body stretching routine including hamstrings, quads, shoulders and back. " +
        "Hold each stretch for at least 30 seconds and breathe deeply throughout. " +
        "Focus on any tight areas from yesterday's workout."

// region HabitCard Previews

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitCardNoDescriptionPreview() {
    TempoTheme {
        HabitCard(
            habit =
                Habit(
                    id = 1,
                    title = "Morning Stretch",
                    description = "",
                    createdDate = PREVIEW_CREATED,
                ),
            selectedDate = PREVIEW_DATE,
            onEdit = {},
            onDelete = {},
            showTimeline = false,
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitCardShortDescriptionPreview() {
    TempoTheme {
        HabitCard(
            habit =
                Habit(
                    id = 2,
                    title = "Morning Stretch",
                    description = SHORT_DESCRIPTION,
                    icon = "fitness",
                    colorKey = "color_m3_blue",
                    createdDate = PREVIEW_CREATED,
                ),
            selectedDate = PREVIEW_DATE,
            onEdit = {},
            onDelete = {},
            showTimeline = false,
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitCardLongTitlePreview() {
    TempoTheme {
        HabitCard(
            habit =
                Habit(
                    id = 3,
                    title = "Run for at least thirty minutes outside before breakfast",
                    description = LONG_DESCRIPTION,
                    icon = "run",
                    colorKey = "color_m3_orange",
                    createdDate = PREVIEW_CREATED,
                ),
            selectedDate = PREVIEW_DATE,
            onEdit = {},
            onDelete = {},
            showTimeline = false,
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitCardCompletedPreview() {
    TempoTheme {
        HabitCard(
            habit =
                Habit(
                    id = 4,
                    title = "Meditate",
                    description = "",
                    icon = "spa",
                    colorKey = "color_m3_purple",
                    createdDate = PREVIEW_CREATED,
                    completionHistory = "2025-06-15",
                ),
            selectedDate = PREVIEW_DATE,
            onEdit = {},
            onDelete = {},
            showTimeline = false,
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitCardWithReminderAndTimelinePreview() {
    TempoTheme {
        HabitCard(
            habit =
                Habit(
                    id = 5,
                    title = "Workout",
                    description = "",
                    icon = "fitness",
                    colorKey = "color_m3_red",
                    reminderDate = LocalDateTime(2025, 6, 15, 18, 0),
                    createdDate = PREVIEW_CREATED,
                ),
            selectedDate = PREVIEW_DATE,
            onEdit = {},
            onDelete = {},
            timeLabel = "18:00",
            showTimeline = true,
        )
    }
}

// endregion

// region QuitHabitCard Previews

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun QuitHabitCardNoStreakPreview() {
    TempoTheme {
        QuitHabitCard(
            habit =
                Habit(
                    id = 100,
                    title = "No Smoking",
                    description = "",
                    habitType = HabitType.QUIT,
                    createdDate = PREVIEW_CREATED,
                ),
            selectedDate = PREVIEW_DATE,
            onEdit = {},
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun QuitHabitCardWithStreakPreview() {
    TempoTheme {
        QuitHabitCard(
            habit =
                Habit(
                    id = 101,
                    title = "No Alcohol",
                    description = SHORT_DESCRIPTION,
                    habitType = HabitType.QUIT,
                    createdDate = PREVIEW_CREATED,
                    completionHistory = "2025-06-13,2025-06-14,2025-06-15",
                ),
            selectedDate = PREVIEW_DATE,
            onEdit = {},
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun QuitHabitCardLongDescriptionPreview() {
    TempoTheme {
        QuitHabitCard(
            habit =
                Habit(
                    id = 102,
                    title = "No Social Media",
                    description = LONG_DESCRIPTION,
                    habitType = HabitType.QUIT,
                    createdDate = PREVIEW_CREATED,
                    completionHistory = "2025-06-15",
                ),
            selectedDate = PREVIEW_DATE,
            onEdit = {},
        )
    }
}

// endregion

// region HabitChainCard Previews

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitChainCardNoDescriptionPreview() {
    TempoTheme {
        HabitChainCard(
            habitChain =
                HabitChain(
                    id = 1,
                    title = "Morning Routine",
                    description = "",
                    colorKey = "blue",
                ),
            chainHabits =
                listOf(
                    Habit(
                        id = 10,
                        title = "Stretch",
                        description = "",
                        createdDate = PREVIEW_CREATED,
                    ),
                    Habit(
                        id = 11,
                        title = "Meditate",
                        description = "",
                        createdDate = PREVIEW_CREATED,
                    ),
                ),
            selectedDate = PREVIEW_DATE,
            isExpanded = false,
            onEdit = {},
            onToggleExpansion = {},
            onAddHabit = {},
            showTimeline = false,
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitChainCardShortDescriptionPreview() {
    TempoTheme {
        HabitChainCard(
            habitChain =
                HabitChain(
                    id = 2,
                    title = "Morning Routine",
                    description = SHORT_DESCRIPTION,
                    colorKey = "green",
                ),
            chainHabits =
                listOf(
                    Habit(
                        id = 20,
                        title = "Stretch",
                        description = "",
                        createdDate = PREVIEW_CREATED,
                    ),
                ),
            selectedDate = PREVIEW_DATE,
            isExpanded = false,
            onEdit = {},
            onToggleExpansion = {},
            showTimeline = false,
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitChainCardLongDescriptionPreview() {
    TempoTheme {
        HabitChainCard(
            habitChain =
                HabitChain(
                    id = 3,
                    title = "Morning Routine",
                    description = LONG_DESCRIPTION,
                    colorKey = "purple",
                ),
            chainHabits =
                listOf(
                    Habit(
                        id = 30,
                        title = "Stretch",
                        description = "",
                        createdDate = PREVIEW_CREATED,
                    ),
                    Habit(
                        id = 31,
                        title = "Meditate",
                        description = SHORT_DESCRIPTION,
                        createdDate = PREVIEW_CREATED,
                    ),
                ),
            selectedDate = PREVIEW_DATE,
            isExpanded = true,
            onEdit = {},
            onToggleExpansion = {},
            showTimeline = false,
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitChainCardSingleStepPreview() {
    TempoTheme {
        HabitChainCard(
            habitChain =
                HabitChain(
                    id = 4,
                    title = "Wind Down",
                    description = "",
                    colorKey = "color_m3_orange",
                ),
            chainHabits =
                listOf(
                    Habit(
                        id = 40,
                        title = "Stretch",
                        description = "",
                        createdDate = PREVIEW_CREATED,
                    ),
                ),
            selectedDate = PREVIEW_DATE,
            isExpanded = true,
            onEdit = {},
            onToggleExpansion = {},
            showTimeline = false,
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitChainCardAllCompletedPreview() {
    TempoTheme {
        HabitChainCard(
            habitChain =
                HabitChain(
                    id = 5,
                    title = "Morning Routine",
                    description = "",
                    colorKey = "color_m3_green",
                ),
            chainHabits =
                listOf(
                    Habit(
                        id = 50,
                        title = "Stretch",
                        description = "",
                        createdDate = PREVIEW_CREATED,
                        completionHistory = "2025-06-15",
                    ),
                    Habit(
                        id = 51,
                        title = "Meditate",
                        description = "",
                        createdDate = PREVIEW_CREATED,
                        completionHistory = "2025-06-15",
                    ),
                ),
            selectedDate = PREVIEW_DATE,
            isExpanded = true,
            onEdit = {},
            onToggleExpansion = {},
            showTimeline = false,
        )
    }
}

// endregion
