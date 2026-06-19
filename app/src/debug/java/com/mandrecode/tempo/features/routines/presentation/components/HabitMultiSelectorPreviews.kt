package com.mandrecode.tempo.features.routines.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.PREVIEW_TODAY
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.sampleBuildHabits

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitMultiSelectorEmptyPreview() {
    TempoTheme {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            HabitMultiSelector(
                habits = sampleBuildHabits(),
                selectedHabitIds = emptyList(),
                onSelectHabits = {},
                selectedDate = PREVIEW_TODAY,
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
private fun HabitMultiSelectorPartiallySelectedPreview() {
    val habits = sampleBuildHabits()
    TempoTheme {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            HabitMultiSelector(
                habits = habits,
                selectedHabitIds = listOf(habits[0].id, habits[1].id),
                onSelectHabits = {},
                selectedDate = PREVIEW_TODAY,
                onToggleHabitCompletion = { _, _ -> },
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
private fun HabitMultiSelectorAllSelectedPreview() {
    val habits = sampleBuildHabits()
    TempoTheme {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            HabitMultiSelector(
                habits = habits,
                selectedHabitIds = habits.map { it.id },
                onSelectHabits = {},
                selectedDate = PREVIEW_TODAY,
                onToggleHabitCompletion = { _, _ -> },
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
private fun HabitMultiSelectorNoHabitsAvailablePreview() {
    TempoTheme {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            HabitMultiSelector(
                habits = emptyList(),
                selectedHabitIds = emptyList(),
                onSelectHabits = {},
            )
        }
    }
}
