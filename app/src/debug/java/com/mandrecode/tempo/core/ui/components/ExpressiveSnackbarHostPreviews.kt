package com.mandrecode.tempo.core.ui.components

import android.content.res.Configuration
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.mandrecode.tempo.core.ui.theme.TempoTheme

@Preview(name = "Message - Light", device = "id:pixel_9")
@Preview(
    name = "Message - Dark",
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun MessageSnackbarPreview() {
    TempoTheme {
        Surface {
            ExpressiveSnackbar(snackbarData = previewSnackbarData("Task saved"))
        }
    }
}

@Preview(name = "Undo - Light", device = "id:pixel_9")
@Preview(
    name = "Undo - Dark",
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun UndoSnackbarPreview() {
    TempoTheme {
        Surface {
            ExpressiveSnackbar(snackbarData = previewSnackbarData("Task deleted", "Undo"))
        }
    }
}

@Preview(name = "Bold word - Light", device = "id:pixel_9")
@Preview(
    name = "Bold word - Dark",
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun BoldWordSnackbarPreview() {
    TempoTheme {
        Surface {
            ExpressiveSnackbar(snackbarData = previewBoldSnackbarData())
        }
    }
}

private fun previewBoldSnackbarData(): SnackbarData =
    object : SnackbarData {
        override val visuals: SnackbarVisuals =
            TempoSnackbarVisuals(
                annotatedMessage =
                    buildAnnotatedString {
                        append("Category ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Groceries")
                        }
                        append(" added successfully.")
                    },
                actionLabel = null,
                duration = SnackbarDuration.Short,
            )

        override fun performAction() = Unit

        override fun dismiss() = Unit
    }

private fun previewSnackbarData(
    message: String,
    actionLabel: String? = null,
): SnackbarData =
    object : SnackbarData {
        override val visuals: SnackbarVisuals =
            object : SnackbarVisuals {
                override val message = message
                override val actionLabel = actionLabel
                override val withDismissAction = false
                override val duration = SnackbarDuration.Long
            }

        override fun performAction() = Unit

        override fun dismiss() = Unit
    }
