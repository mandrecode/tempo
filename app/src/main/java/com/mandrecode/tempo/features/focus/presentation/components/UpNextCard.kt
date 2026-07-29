package com.mandrecode.tempo.features.focus.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import com.mandrecode.tempo.util.DateTimeFormatter

private val UpNextCornerRadius = 22.dp

/**
 * The one item worth starting next.
 *
 * On `tertiaryContainer` rather than `secondaryContainer`: the hero directly above already owns
 * `primaryContainer`, and secondary is a near neighbour of it in the light scheme, so the two cards
 * would read as one block. Tertiary is the blue already used for the selected category chip, so the
 * hue change is established app language rather than a new colour.
 *
 * The card is not rendered at all when nothing qualifies — see the caller. An empty second coloured
 * card is a lot of surface for no information.
 */
@Composable
internal fun UpNextCard(
    title: String,
    metadata: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(UpNextCornerRadius),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Row(
            modifier =
                Modifier
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (!metadata.isNullOrBlank()) {
                    Text(
                        text = metadata,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = METADATA_ALPHA),
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = if (metadata.isNullOrBlank()) 0.dp else 2.dp),
                )
            }
            trailingContent?.invoke()
        }
    }
}

/**
 * The metadata line: due time when the item has one, in the user's 12h/24h preference. Kept
 * deliberately terse — the card is a prompt, not a detail view.
 */
@Composable
internal fun FocusAgendaItem.upNextMetadata(): String? {
    val context = LocalContext.current
    val time = dueTime ?: return null
    return DateTimeFormatter.formatTimeOfDay(time, context)
}

private const val METADATA_ALPHA = 0.75f
