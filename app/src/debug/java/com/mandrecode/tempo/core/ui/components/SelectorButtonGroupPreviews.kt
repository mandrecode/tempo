package com.mandrecode.tempo.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.TempoTheme

@Preview(name = "Light", showBackground = true, widthDp = 300)
@Preview(name = "Dark", showBackground = true, widthDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SelectorButtonGroupPreview() {
    TempoTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            SelectorButtonGroup(
                options = listOf("High", "Medium", "Low"),
                isChecked = { it == "Medium" },
                onClick = {},
                label = { it },
            )
        }
    }
}

@Preview(name = "Scrolling row", showBackground = true)
@Composable
private fun ConnectedSelectorButtonRowPreview() {
    TempoTheme {
        val labels = listOf("Hourly", "Daily", "Weekly", "Monthly")
        Row(modifier = Modifier.padding(16.dp)) {
            labels.forEachIndexed { index, label ->
                ConnectedSelectorButton(
                    label = label,
                    checked = label == "Weekly",
                    onClick = {},
                    index = index,
                    count = labels.size,
                    modifier = Modifier.padding(end = 2.dp).width(96.dp),
                )
            }
        }
    }
}
