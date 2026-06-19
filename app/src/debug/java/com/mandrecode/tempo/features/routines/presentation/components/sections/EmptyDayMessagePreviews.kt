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
import com.mandrecode.tempo.features.routines.presentation.EmptyDayMessage
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.PREVIEW_TODAY
import com.mandrecode.tempo.features.routines.presentation.RoutinesPreviewFixtures.PREVIEW_TOMORROW

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun EmptyDayMessageTodayPreview() {
    TempoTheme {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            EmptyDayMessage(selectedDate = PREVIEW_TODAY)
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
private fun EmptyDayMessageOtherDayPreview() {
    TempoTheme {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            EmptyDayMessage(selectedDate = PREVIEW_TOMORROW)
        }
    }
}
