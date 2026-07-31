package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val LinkButtonRadius = 24.dp
private val LinkButtonIconSize = 18.dp

/**
 * A quiet action that hands the user off somewhere else, styled like the editors' own "Delete
 * task" and "Delete habit" actions: a transparent surface with a plain ripple, so a secondary
 * action looks the same wherever the app offers one.
 *
 * Shared rather than copied per screen, because the two places that hand off to Tasks — the
 * session screen and the Focus agenda's undated footer — are the same promise made twice, and a
 * second implementation is how the two would come to look different.
 *
 * [iconContentDescription] is for callers whose icon carries meaning the label does not. The icon
 * is decorative by default and silent to a screen reader, so a caller that lets the icon say where
 * the button goes has to name the destination here or it is lost.
 */
@Composable
fun TempoLinkButton(
    label: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconContentDescription: String? = null,
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier,
        // Rounded and only as wide as its content: the ripple has to say what it is answering for,
        // and one spanning the whole list said the list.
        shape = RoundedCornerShape(LinkButtonRadius),
        color = Color.Transparent,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // The icon is the promise that this leaves the screen, so the label alone never has to
            // carry that on its own — and where that promise is the only place the destination is
            // named, it has to be spoken too.
            Icon(
                painter = painterResource(iconRes),
                contentDescription = iconContentDescription,
                modifier = Modifier.size(LinkButtonIconSize),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
