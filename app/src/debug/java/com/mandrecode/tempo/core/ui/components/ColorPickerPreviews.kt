package com.mandrecode.tempo.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.TempoTheme

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ColorPickerNoneSelectedPreview() {
    TempoTheme {
        ColorPicker(
            selectedColorKey = null,
            onSelectColorKey = {},
            onClearColor = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ColorPickerSelectedPreview() {
    TempoTheme {
        ColorPicker(
            selectedColorKey = "color_m3_blue",
            onSelectColorKey = {},
            onClearColor = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ColorPickerDisabledPreview() {
    TempoTheme {
        ColorPicker(
            selectedColorKey = null,
            onSelectColorKey = {},
            onClearColor = {},
            enabled = false,
            disabledMessage = "Unavailable with dynamic color",
            modifier = Modifier.padding(16.dp),
        )
    }
}
