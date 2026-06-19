package com.mandrecode.tempo.features.routines.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.TempoTheme

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChainHabitCheckboxUncheckedPreview() {
    TempoTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ChainHabitCheckbox(
                isCompleted = false,
                canToggle = true,
                radius = 12.dp,
                accentColor = MaterialTheme.colorScheme.primary,
                iconName = null,
                onToggle = {},
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChainHabitCheckboxCheckedPreview() {
    TempoTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ChainHabitCheckbox(
                isCompleted = true,
                canToggle = true,
                radius = 12.dp,
                accentColor = MaterialTheme.colorScheme.primary,
                iconName = null,
                onToggle = {},
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChainHabitCheckboxCheckedWithIconPreview() {
    TempoTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ChainHabitCheckbox(
                isCompleted = true,
                canToggle = true,
                radius = 12.dp,
                accentColor = MaterialTheme.colorScheme.tertiary,
                iconName = "spa",
                onToggle = {},
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChainHabitCheckboxDisabledPreview() {
    TempoTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ChainHabitCheckbox(
                isCompleted = false,
                canToggle = false,
                radius = 12.dp,
                accentColor = MaterialTheme.colorScheme.primary,
                iconName = null,
                onToggle = {},
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChainHabitCheckboxLargeRadiusPreview() {
    TempoTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ChainHabitCheckbox(
                isCompleted = true,
                canToggle = true,
                radius = 24.dp,
                accentColor = MaterialTheme.colorScheme.secondary,
                iconName = "fitness",
                onToggle = {},
            )
        }
    }
}
