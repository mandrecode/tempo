package com.mandrecode.tempo.core.ui.components

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.TempoTheme

@Preview(name = "Education - Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Education - Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PermissionEducationDialogPreview() {
    TempoTheme {
        PermissionEducationDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview(name = "Denied - Light", showBackground = true, device = "id:pixel_9")
@Preview(
    name = "Denied - Dark",
    showBackground = true,
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PermissionRationaleDialogPreview() {
    TempoTheme {
        PermissionRationaleDialog(
            textRes = R.string.notification_permission_rationale,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
