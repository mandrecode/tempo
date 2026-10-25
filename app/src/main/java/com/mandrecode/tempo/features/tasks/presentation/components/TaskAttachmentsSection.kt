package com.mandrecode.tempo.features.tasks.presentation.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.ExpressiveChip
import com.mandrecode.tempo.core.ui.editor.EDITOR_CONTROL_HEIGHT
import com.mandrecode.tempo.core.ui.editor.EditorPropertyRow
import com.mandrecode.tempo.features.tasks.domain.model.TaskAttachment
import com.mandrecode.tempo.infrastructure.attachments.attachmentContentUri

/**
 * The task's attachments (#380): one row per file, which opens it in whatever app the device has
 * for its type, and an "Attach file" pill that picks more — pictures, documents, anything the
 * system picker offers.
 *
 * Laid out like the sheet's other rows: every item is [EDITOR_CONTROL_HEIGHT] tall and starts where
 * the other rows' controls start, and the icon sits on the first item's line — beside the only file,
 * or the first of several, as a chain's icon sits beside its first habit.
 */
@Composable
internal fun TaskAttachmentsSection(
    attachments: List<TaskAttachment>,
    onAddAttachments: (List<String>) -> Unit,
    onRemoveAttachment: (TaskAttachment) -> Unit,
    onAttachmentOpenFail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val picker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
            onAddAttachments(uris.map { it.toString() })
        }

    EditorPropertyRow(
        iconPainter = painterResource(R.drawable.ic_attach_file),
        iconContentDescription = stringResource(R.string.attachments),
        verticalAlignment = Alignment.Top,
        iconBoxModifier = Modifier.width(48.dp).height(EDITOR_CONTROL_HEIGHT),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(start = 4.dp),
            verticalArrangement = Arrangement.spacedBy(ATTACHMENT_ITEM_GAP),
        ) {
            attachments.forEach { attachment ->
                TaskAttachmentRow(
                    attachment = attachment,
                    onOpen = {
                        if (!context.openAttachment(attachment)) onAttachmentOpenFail()
                    },
                    onRemove = { onRemoveAttachment(attachment) },
                )
            }
            ExpressiveChip(
                label = stringResource(R.string.add_attachment),
                isSelected = false,
                isFirst = true,
                isLast = true,
                onClick = { picker.launch(arrayOf("*/*")) },
                height = EDITOR_CONTROL_HEIGHT,
                verticalInset = 0.dp,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private val ATTACHMENT_ITEM_GAP = 8.dp

@Composable
internal fun TaskAttachmentRow(
    attachment: TaskAttachment,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val openLabel = stringResource(R.string.open_attachment, attachment.displayName)
    Surface(
        onClick = onOpen,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = EDITOR_CONTROL_HEIGHT)
                .semantics { contentDescription = openLabel },
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(attachment.kind.iconRes()),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = attachment.displayName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onRemove) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.remove_attachment, attachment.displayName),
                )
            }
        }
    }
}

private fun TaskAttachment.Kind.iconRes(): Int =
    when (this) {
        TaskAttachment.Kind.IMAGE -> R.drawable.ic_image
        TaskAttachment.Kind.PDF -> R.drawable.ic_picture_as_pdf
        TaskAttachment.Kind.FILE -> R.drawable.ic_description
    }

/** Hands the private copy to a viewer app with a one-off read grant; `false` if none can take it. */
private fun Context.openAttachment(attachment: TaskAttachment): Boolean =
    try {
        val uri = attachmentContentUri(this, attachment.storedName)
        val intent =
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, attachment.mimeType ?: "*/*")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
        true
    } catch (_: ActivityNotFoundException) {
        false
    } catch (_: IllegalArgumentException) {
        false
    }
