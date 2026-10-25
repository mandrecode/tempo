package com.mandrecode.tempo.features.tasks.presentation.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.tasks.domain.model.TaskAttachment

private val previewAttachments =
    listOf(
        TaskAttachment(1, 7, "lease-agreement-2026-final-signed.pdf", "application/pdf", 245_000, "a"),
        TaskAttachment(2, 7, "kitchen.jpg", "image/jpeg", 1_200_000, "b"),
        TaskAttachment(3, 7, "budget.xlsx", "application/vnd.ms-excel", 18_000, "c"),
    )

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TaskAttachmentsSectionPreview() {
    TempoTheme {
        Surface {
            TaskAttachmentsSection(
                attachments = previewAttachments,
                errors = emptyList(),
                onAddAttachments = {},
                onRemoveAttachment = {},
                onAttachmentOpenFail = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(name = "Empty", showBackground = true)
@Composable
private fun TaskAttachmentsSectionEmptyPreview() {
    TempoTheme {
        Surface {
            TaskAttachmentsSection(
                attachments = emptyList(),
                errors = emptyList(),
                onAddAttachments = {},
                onRemoveAttachment = {},
                onAttachmentOpenFail = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
