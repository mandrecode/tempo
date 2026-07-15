package com.mandrecode.tempo.features.routines.presentation

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mandrecode.tempo.core.ui.preview.PreviewFormFactors
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.PREVIEW_TODAY
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.chain
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.habit
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.quitHabit
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.sampleQuitHabits
import kotlinx.collections.immutable.toPersistentList

private fun timelineHabit(
    time: Int?,
    habitId: Long,
): RoutinesContract.TimelineItem =
    RoutinesContract.TimelineItem(
        time = time,
        isChain = false,
        habitId = habitId,
    )

private fun timelineChain(
    time: Int?,
    chainId: Long,
): RoutinesContract.TimelineItem =
    RoutinesContract.TimelineItem(
        time = time,
        isChain = true,
        chainId = chainId,
    )

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun RoutinesContentLoadingPreview() {
    TempoTheme {
        RoutinesContent(
            uiState =
                RoutinesContract.UiState(
                    isLoading = true,
                    selectedDate = PREVIEW_TODAY,
                ),
            onEvent = {},
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
private fun RoutinesContentEmptyDayPreview() {
    TempoTheme {
        RoutinesContent(
            uiState =
                RoutinesContract.UiState(
                    isLoading = false,
                    selectedDate = PREVIEW_TODAY,
                ),
            onEvent = {},
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
private fun RoutinesContentBuildHabitsOnlyPreview() {
    val habits =
        listOf(
            habit(id = 1L, title = "Stretch", reminderHour = 7),
            habit(id = 2L, title = "Meditate", icon = "spa", reminderHour = 8, colorKey = "color_m3_purple"),
            habit(id = 3L, title = "Read", icon = "book", reminderHour = null, colorKey = "color_m3_orange"),
        )

    TempoTheme {
        RoutinesContent(
            uiState =
                RoutinesContract.UiState(
                    isLoading = false,
                    selectedDate = PREVIEW_TODAY,
                    habits = habits.toPersistentList(),
                    scheduledTimelineItems =
                        listOf(
                            timelineHabit(time = 420, habitId = 1L),
                            timelineHabit(time = 480, habitId = 2L),
                        ).toPersistentList(),
                    unscheduledTimelineItems =
                        listOf(
                            timelineHabit(time = null, habitId = 3L),
                        ).toPersistentList(),
                ),
            onEvent = {},
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
private fun RoutinesContentMixedPreview() {
    val chainHabits =
        listOf(
            habit(id = 10L, title = "Hydrate", icon = "water", colorKey = "color_m3_cyan"),
            habit(id = 11L, title = "Brush teeth", icon = "spa", colorKey = "color_m3_cyan"),
        )
    val singleHabit =
        habit(
            id = 20L,
            title = "Workout",
            icon = "fitness",
            colorKey = "color_m3_green",
            reminderHour = 18,
        )
    val unscheduled =
        habit(
            id = 21L,
            title = "Journal",
            icon = "book",
            colorKey = "color_m3_orange",
            reminderHour = null,
        )
    val morningChain: HabitChain =
        chain(
            id = 500L,
            title = "Morning Routine",
            colorKey = "color_m3_cyan",
            habitIds = listOf(10L, 11L),
            reminderHour = 7,
        )
    val allHabits: List<Habit> = chainHabits + singleHabit + unscheduled + sampleQuitHabits()

    TempoTheme {
        RoutinesContent(
            uiState =
                RoutinesContract.UiState(
                    isLoading = false,
                    selectedDate = PREVIEW_TODAY,
                    habits = allHabits.toPersistentList(),
                    habitChains = listOf(morningChain).toPersistentList(),
                    scheduledTimelineItems =
                        listOf(
                            timelineChain(time = 420, chainId = 500L),
                            timelineHabit(time = 1080, habitId = 20L),
                        ).toPersistentList(),
                    unscheduledTimelineItems =
                        listOf(
                            timelineHabit(time = null, habitId = 21L),
                        ).toPersistentList(),
                ),
            onEvent = {},
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
private fun RoutinesContentAllCompletedPreview() {
    val today = PREVIEW_TODAY.toString()
    val habits =
        listOf(
            habit(id = 1L, title = "Stretch", reminderHour = 7, completionHistory = today),
            habit(
                id = 2L,
                title = "Meditate",
                icon = "spa",
                reminderHour = 8,
                colorKey = "color_m3_purple",
                completionHistory = today,
            ),
        )

    TempoTheme {
        RoutinesContent(
            uiState =
                RoutinesContract.UiState(
                    isLoading = false,
                    selectedDate = PREVIEW_TODAY,
                    habits = habits.toPersistentList(),
                    scheduledTimelineItems =
                        listOf(
                            timelineHabit(time = 420, habitId = 1L),
                            timelineHabit(time = 480, habitId = 2L),
                        ).toPersistentList(),
                ),
            onEvent = {},
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
private fun RoutinesContentWithQuitSeparatorPreview() {
    val build = habit(id = 1L, title = "Stretch", reminderHour = 7)
    val quits =
        listOf(
            quitHabit(id = 100L, title = "No Smoking"),
            quitHabit(id = 101L, title = "No Sugar", completionHistory = PREVIEW_TODAY.toString()),
        )

    TempoTheme {
        RoutinesContent(
            uiState =
                RoutinesContract.UiState(
                    isLoading = false,
                    selectedDate = PREVIEW_TODAY,
                    habits = (listOf(build) + quits).toPersistentList(),
                    scheduledTimelineItems =
                        listOf(
                            timelineHabit(time = 420, habitId = 1L),
                        ).toPersistentList(),
                ),
            onEvent = {},
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
private fun RoutinesContentQuitsOnlyPreview() {
    val quits = sampleQuitHabits()

    TempoTheme {
        RoutinesContent(
            uiState =
                RoutinesContract.UiState(
                    isLoading = false,
                    selectedDate = PREVIEW_TODAY,
                    habits = quits.toPersistentList(),
                ),
            onEvent = {},
        )
    }
}

@PreviewFormFactors
@Composable
private fun RoutinesContentPreviewFormFactors() {
    val habits =
        listOf(
            habit(id = 1L, title = "Stretch", reminderHour = 7),
            habit(id = 2L, title = "Meditate", icon = "spa", reminderHour = 8, colorKey = "color_m3_purple"),
            habit(id = 3L, title = "Read", icon = "book", reminderHour = null, colorKey = "color_m3_orange"),
        )

    TempoTheme {
        RoutinesContent(
            uiState =
                RoutinesContract.UiState(
                    isLoading = false,
                    selectedDate = PREVIEW_TODAY,
                    habits = habits.toPersistentList(),
                    scheduledTimelineItems =
                        listOf(
                            timelineHabit(time = 420, habitId = 1L),
                            timelineHabit(time = 480, habitId = 2L),
                        ).toPersistentList(),
                    unscheduledTimelineItems =
                        listOf(
                            timelineHabit(time = null, habitId = 3L),
                        ).toPersistentList(),
                ),
            onEvent = {},
        )
    }
}
