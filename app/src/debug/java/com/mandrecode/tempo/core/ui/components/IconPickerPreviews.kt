package com.mandrecode.tempo.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.TempoTheme

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun IconPickerNoneSelectedPreview() {
    TempoTheme {
        IconPicker(
            selectedIconName = null,
            onSelectIcon = {},
            onClearIcon = {},
            modifier = Modifier.padding(16.dp),
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
private fun IconPickerSelectedPreview() {
    TempoTheme {
        IconPicker(
            selectedIconName = "fitness",
            onSelectIcon = {},
            onClearIcon = {},
            modifier = Modifier.padding(16.dp),
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
private fun IconPickerDisabledPreview() {
    TempoTheme {
        IconPicker(
            selectedIconName = null,
            onSelectIcon = {},
            onClearIcon = {},
            enabled = false,
            disabledMessage = "Icons unavailable for this habit type",
            modifier = Modifier.padding(16.dp),
        )
    }
}
