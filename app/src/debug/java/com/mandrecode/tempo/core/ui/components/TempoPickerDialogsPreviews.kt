package com.mandrecode.tempo.core.ui.components

import android.content.res.Configuration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TempoDatePickerDialogPreview() {
    TempoTheme {
        TempoDatePickerDialog(
            initialDate = LocalDate(2026, 7, 17),
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TempoTimePickerDialogPreview() {
    TempoTheme {
        TempoTimePickerDialog(
            initialHour = 9,
            initialMinute = 30,
            onConfirm = { _, _ -> },
            onDismiss = {},
        )
    }
}
