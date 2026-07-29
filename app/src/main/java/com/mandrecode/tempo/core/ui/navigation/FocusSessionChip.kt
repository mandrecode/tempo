package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.util.rememberPressableButtonAnimation
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
import com.mandrecode.tempo.features.focus.presentation.components.asCountdownLabel
import com.mandrecode.tempo.features.focus.presentation.components.rememberSessionCountdown

/**
 * The running session, visible from the other tabs.
 *
 * Leaving Focus should not hide a session the user started — the chip carries the remaining time
 * and returns to Focus when tapped. It sits in the session's own tertiary colour so it reads as
 * belonging to the session rather than to navigation.
 */
@Composable
internal fun FocusSessionChip(
    session: FocusSession,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    selected: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current
    val remaining by rememberSessionCountdown(session)
    val label = remaining.asCountdownLabel()
    val description = stringResource(R.string.focus_session_remaining, label)
    val (interactionSource, cornerRadius) =
        rememberPressableButtonAnimation(
            baseRadius = FloatingToolbarItemSize / 2,
            pressedRadius = FloatingToolbarPressedRadius,
        )

    Surface(
        onClick = {
            // Returning to Focus is navigation, matching the tab buttons beside it.
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier =
            modifier
                .height(FloatingToolbarItemSize)
                .semantics { contentDescription = description },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(cornerRadius.value),
        // Standing in for the Focus tab, it carries that tab's selected treatment when Focus is
        // the current destination, so the bar still shows where the user is.
        color =
            if (selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.tertiaryContainer
            },
        contentColor =
            if (selected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onTertiaryContainer
            },
    ) {
        SessionChipContent(label = label, compact = compact, selected = selected)
    }
}

@Composable
private fun SessionChipContent(
    label: String,
    compact: Boolean,
    selected: Boolean,
) {
    Row(
        modifier =
            Modifier
                .then(if (compact) Modifier.width(FloatingToolbarItemSize) else Modifier)
                .padding(horizontal = if (compact) 0.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (compact) Arrangement.Center else Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            painter =
                painterResource(
                    id = if (selected) R.drawable.ic_focus else R.drawable.ic_focus_outlined,
                ),
            contentDescription = null,
            modifier = Modifier.size(if (compact) 24.dp else 16.dp),
        )
        if (compact) return@Row
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            // Tabular figures and a reserved width: without both, every ticking second
            // remeasures the chip and nudges the whole bar sideways.
            style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = TABULAR_FIGURES),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(CountdownLabelWidth),
        )
    }
}

/** Wide enough for "59:59"; a session cannot show more than two digits of minutes. */
private val CountdownLabelWidth = 46.dp
private const val TABULAR_FIGURES = "tnum"
