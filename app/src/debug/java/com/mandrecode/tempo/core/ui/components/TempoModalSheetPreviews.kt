package com.mandrecode.tempo.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.TempoTheme

@Preview(name = "Bottom - Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Bottom - Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TempoModalBottomSheetPreview() {
    TempoTheme {
        TempoModalBottomSheet(onDismissRequest = {}) {
            SheetPreviewContent(title = "Bottom sheet")
        }
    }
}

@Preview(name = "Top - Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Top - Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun TempoModalTopSheetPreview() {
    TempoTheme {
        TempoModalTopSheet(onDismissRequest = {}) {
            SheetPreviewContent(title = "Top sheet")
        }
    }
}

@Preview(name = "Bottom Tall", showBackground = true, device = "id:pixel_9")
@Composable
private fun TempoModalBottomSheetTallPreview() {
    TempoTheme {
        TempoModalBottomSheet(onDismissRequest = {}) {
            SheetPreviewContent(title = "Tall bottom sheet")
            repeat(10) { index ->
                Text(
                    text = "Scrollable row ${index + 1}",
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun SheetPreviewContent(title: String) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = "Preview text",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Title") },
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {}) {
            Text("Done")
        }
    }
}
