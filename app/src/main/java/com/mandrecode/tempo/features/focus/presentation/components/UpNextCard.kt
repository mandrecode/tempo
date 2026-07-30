package com.mandrecode.tempo.features.focus.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation
import com.mandrecode.tempo.core.ui.util.titleResId
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
    /** Drawn ahead of [metadata], which leads with the priority the icon belongs to. */
    metadataIconRes: Int? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(UpNextCornerRadius),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Row(
            modifier =
                Modifier
                    .clickable {
                        // Opening the item elsewhere is navigation, not a commit — the lighter tick.
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (!metadata.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        // The flag reads as "priority" before the word does, the same way it does
                        // on the task's own card and in its editor.
                        metadataIconRes?.let { iconRes ->
                            Icon(
                                painter = painterResource(iconRes),
                                contentDescription = null,
                                modifier = Modifier.size(MetadataIconSize),
                                tint =
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                        .copy(alpha = METADATA_ALPHA),
                            )
                        }
                        Text(
                            text = metadata,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color =
                                MaterialTheme.colorScheme.onTertiaryContainer
                                    .copy(alpha = METADATA_ALPHA),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
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
    val parts =
        buildList {
            // Priority, then category, then time — the order the eye needs them in: how much this
            // matters, where it belongs, when it is due.
            priority?.let { add(stringResource(it.titleResId).uppercase()) }
            (this@upNextMetadata as? FocusAgendaItem.TaskEntry)
                ?.categoryName
                ?.takeIf { it.isNotBlank() }
                ?.let { add(it.uppercase()) }
            dueTime?.let { add(DateTimeFormatter.formatTimeOfDay(it, context)) }
        }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(SEPARATOR)
}

private const val SEPARATOR = " · "
private val MetadataIconSize = 12.dp

/**
 * The only place a session starts. One tap, no duration picker — the length comes from Settings so
 * that starting stays a single gesture.
 */
@Composable
internal fun StartSessionButton(
    minutes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val (interactionSource, cornerRadius) =
        rememberPressableButtonAnimation(
            baseRadius = PillRadius,
            pressedRadius = PillPressedRadius,
        )

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius.value),
        color = MaterialTheme.colorScheme.onTertiaryContainer,
        contentColor = MaterialTheme.colorScheme.tertiaryContainer,
        interactionSource = interactionSource,
    ) {
        Text(
            text = stringResource(R.string.focus_session_start, sessionLengthLabel(minutes)),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
        )
    }
}

private const val METADATA_ALPHA = 0.75f
private val PillRadius = 20.dp
private val PillPressedRadius = 10.dp
