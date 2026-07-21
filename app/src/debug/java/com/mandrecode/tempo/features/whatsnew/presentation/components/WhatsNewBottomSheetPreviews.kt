package com.mandrecode.tempo.features.whatsnew.presentation.components

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.whatsnew.presentation.WhatsNewRegistry

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun WhatsNewBottomSheetPreview() {
    TempoTheme {
        WhatsNewBottomSheet(
            entry = WhatsNewRegistry.latest,
            onDismissRequest = {},
        )
    }
}
