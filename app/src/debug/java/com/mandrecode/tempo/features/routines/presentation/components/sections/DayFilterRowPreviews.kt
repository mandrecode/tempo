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
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.PREVIEW_TODAY
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.PREVIEW_TOMORROW
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.PREVIEW_YESTERDAY

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DayFilterRowTodaySelectedPreview() {
    TempoTheme {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            DayFilterRow(
                selectedDate = PREVIEW_TODAY,
                onSelectDate = {},
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
private fun DayFilterRowYesterdaySelectedPreview() {
    TempoTheme {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            DayFilterRow(
                selectedDate = PREVIEW_YESTERDAY,
                onSelectDate = {},
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
private fun DayFilterRowFutureDaySelectedPreview() {
    TempoTheme {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            DayFilterRow(
                selectedDate = PREVIEW_TOMORROW,
                onSelectDate = {},
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DayFilterChipUnselectedPreview() {
    TempoTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DayFilterChip(
                label = "Mon",
                isSelected = false,
                onClick = {},
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DayFilterChipSelectedPreview() {
    TempoTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DayFilterChip(
                label = "Today",
                isSelected = true,
                onClick = {},
                showIcon = true,
            )
        }
    }
}
