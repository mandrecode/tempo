package com.mandrecode.tempo.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.TempoTheme

// region IconPicker – No Selection Previews

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IconPickerNoSelectionPreview() {
    TempoTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            IconPicker(
                selectedIconName = null,
                onSelectIcon = {},
                onClearIcon = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

// endregion

// region IconPicker – Selection Previews

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun IconPickerSelectedIconPreview() {
    TempoTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            IconPicker(
                selectedIconName = "savings",
                onSelectIcon = {},
                onClearIcon = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

// endregion

// region IconPicker – Disabled Preview

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Composable
private fun IconPickerDisabledPreview() {
    TempoTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            IconPicker(
                selectedIconName = "work",
                onSelectIcon = {},
                onClearIcon = {},
                enabled = false,
                disabledMessage = null,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

// endregion

// region IconPicker – Narrow Container Preview

@Preview(name = "Compact width", showBackground = true, widthDp = 320)
@Composable
private fun IconPickerCompactWidthPreview() {
    TempoTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            IconPicker(
                selectedIconName = null,
                onSelectIcon = {},
                onClearIcon = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

// endregion
