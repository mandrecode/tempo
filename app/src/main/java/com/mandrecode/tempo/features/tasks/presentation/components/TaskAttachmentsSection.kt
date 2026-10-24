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
import androidx.compose.material3.TextButton
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
import com.mandrecode.tempo.core.ui.editor.EditorPropertyRow
import com.mandrecode.tempo.features.tasks.domain.model.TaskAttachment
import com.mandrecode.tempo.infrastructure.attachments.attachmentContentUri

/**
 * The task's attachments (#380): one row per file, which opens it in whatever app the device has
 * for its type, and a button that picks more — pictures, documents, anything the system picker
 * offers.
 */
@Composable
internal fun TaskAttachmentsSection(
    attachments: List<TaskAttachment>,
    onAddAttachments: (List<String>) -> Unit,
    onRemoveAttachment: (TaskAttachment) -> Unit,
    onOpenAttachmentFailed: () -> Unit,
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
        iconBoxModifier = Modifier.width(48.dp).heightIn(min = 48.dp).padding(top = 12.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            attachments.forEach { attachment ->
                TaskAttachmentRow(
                    attachment = attachment,
                    onOpen = {
                        if (!context.openAttachment(attachment)) onOpenAttachmentFailed()
                    },
                    onRemove = { onRemoveAttachment(attachment) },
                )
            }
            TextButton(
                onClick = { picker.launch(arrayOf("*/*")) },
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(text = stringResource(R.string.add_attachment))
            }
        }
    }
}

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
                .heightIn(min = 48.dp)
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
