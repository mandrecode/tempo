package com.mandrecode.tempo.features.routines.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.routines.domain.model.HabitType

@Preview(name = "Light - Build selected", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark - Build selected",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitTypeSelectorBuildSelectedPreview() {
    TempoTheme {
        HabitTypeSelector(
            selectedType = HabitType.BUILD,
            onTypeSelect = {},
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
        )
    }
}

@Preview(name = "Light - Quit selected", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark - Quit selected",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitTypeSelectorQuitSelectedPreview() {
    TempoTheme {
        HabitTypeSelector(
            selectedType = HabitType.QUIT,
            onTypeSelect = {},
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
        )
    }
}

@Preview(name = "Light - Interactive", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark - Interactive",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HabitTypeSelectorInteractivePreview() {
    TempoTheme {
        var selected by remember { mutableStateOf(HabitType.BUILD) }
        HabitTypeSelector(
            selectedType = selected,
            onTypeSelect = { selected = it },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
        )
    }
}
