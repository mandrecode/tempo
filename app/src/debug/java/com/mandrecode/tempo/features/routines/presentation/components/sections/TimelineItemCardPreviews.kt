package com.mandrecode.tempo.features.routines.presentation.components.sections

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.routines.presentation.RoutinesContract
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.PREVIEW_TODAY
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.chain
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.habit
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.quitHabit
import com.mandrecode.tempo.features.routines.presentation.TimelineItemCard
import kotlinx.collections.immutable.toPersistentList

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TimelineItemCardBuildHabitPreview() {
    val h = habit(id = 1L, title = "Stretch", reminderHour = 7, reminderMinute = 30)
    TempoTheme {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            TimelineItemCard(
                item =
                    RoutinesContract.TimelineItem(
                        time = 450,
                        isChain = false,
                        habitId = h.id,
                    ),
                uiState =
                    RoutinesContract.UiState(
                        isLoading = false,
                        selectedDate = PREVIEW_TODAY,
                        habits = listOf(h).toPersistentList(),
                    ),
                habitsById = mapOf(h.id to h),
                onEvent = {},
                showTimeline = true,
            )
        }
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
private fun TimelineItemCardQuitHabitPreview() {
    val h = quitHabit(id = 100L, title = "No Smoking")
    TempoTheme {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            TimelineItemCard(
                item =
                    RoutinesContract.TimelineItem(
                        time = 1260,
                        isChain = false,
                        habitId = h.id,
                    ),
                uiState =
                    RoutinesContract.UiState(
                        isLoading = false,
                        selectedDate = PREVIEW_TODAY,
                        habits = listOf(h).toPersistentList(),
                    ),
                habitsById = mapOf(h.id to h),
                onEvent = {},
                showTimeline = true,
            )
        }
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
private fun TimelineItemCardChainPreview() {
    val a = habit(id = 10L, title = "Hydrate", icon = "water", colorKey = "color_m3_cyan")
    val b = habit(id = 11L, title = "Brush teeth", icon = "spa", colorKey = "color_m3_cyan")
    val ch =
        chain(
            id = 500L,
            title = "Morning Routine",
            colorKey = "color_m3_cyan",
            habitIds = listOf(a.id, b.id),
            reminderHour = 7,
        )

    TempoTheme {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            TimelineItemCard(
                item =
                    RoutinesContract.TimelineItem(
                        time = 420,
                        isChain = true,
                        chainId = ch.id,
                    ),
                uiState =
                    RoutinesContract.UiState(
                        isLoading = false,
                        selectedDate = PREVIEW_TODAY,
                        habits = listOf(a, b).toPersistentList(),
                        habitChains = listOf(ch).toPersistentList(),
                    ),
                habitsById = mapOf(a.id to a, b.id to b),
                onEvent = {},
                showTimeline = true,
            )
        }
    }
}
