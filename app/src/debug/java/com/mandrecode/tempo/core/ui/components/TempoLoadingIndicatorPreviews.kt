package com.mandrecode.tempo.core.ui.components

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mandrecode.tempo.core.ui.theme.TempoTheme

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TempoLoadingIndicatorTasksPreview() {
    TempoTheme {
        TempoLoadingIndicator(message = "Loading tasks…")
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TempoLoadingIndicatorHabitsPreview() {
    TempoTheme {
        TempoLoadingIndicator(message = "Loading habits…")
    }
}
