package com.mandrecode.tempo.features.tasks.presentation.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.tasks.domain.model.TaskAttachment
import org.junit.Rule
import org.junit.Test

class TaskAttachmentsSectionTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val lease =
        TaskAttachment(
            id = 1L,
            taskId = 7L,
            displayName = "lease.pdf",
            mimeType = "application/pdf",
            sizeBytes = 2048,
            storedName = "s1",
        )

    @Test
    fun listsEachAttachment_andOffersToAddMore() {
        composeTestRule.setContent {
            TempoTheme {
                TaskAttachmentsSection(
                    attachments = listOf(lease, lease.copy(id = 2L, displayName = "photo.jpg", mimeType = "image/jpeg")),
                    errors = emptyList(),
                    onAddAttachments = {},
                    onRemoveAttachment = {},
                    onAttachmentOpenFail = {},
                )
            }
        }

        composeTestRule.onNodeWithText("lease.pdf").assertIsDisplayed()
        composeTestRule.onNodeWithText("photo.jpg").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add attachment").assertIsDisplayed()
    }

    @Test
    fun removeButton_removesThatAttachment() {
        var removed: TaskAttachment? = null
        composeTestRule.setContent {
            TempoTheme {
                TaskAttachmentsSection(
                    attachments = listOf(lease),
                    errors = emptyList(),
                    onAddAttachments = {},
                    onRemoveAttachment = { removed = it },
                    onAttachmentOpenFail = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Remove lease.pdf").performClick()

        assertThat(removed).isEqualTo(lease)
    }

    /** Errors show in the section itself, inside the sheet, not in a snackbar beneath it. */
    @Test
    fun errors_showInTheSection() {
        composeTestRule.setContent {
            TempoTheme {
                TaskAttachmentsSection(
                    attachments = emptyList(),
                    errors = listOf(R.string.msg_attachment_too_large, R.string.msg_attachment_failed),
                    onAddAttachments = {},
                    onRemoveAttachment = {},
                    onAttachmentOpenFail = {},
                )
            }
        }

        composeTestRule.onNodeWithText("That file is too large to attach (25 MB max)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Couldn't attach that file").assertIsDisplayed()
    }
}
