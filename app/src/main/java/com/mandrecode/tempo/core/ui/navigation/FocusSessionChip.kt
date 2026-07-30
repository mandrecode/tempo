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
        // Always the session's own colour, on every surface and whichever tab you are on. Swapping
        // it for the selected-tab treatment on Focus made the one control carrying a live session
        // indistinguishable from an ordinary tab, and made the wide rail's copy of it look like a
        // different component from the phone's. Being filled at all is what marks it as current;
        // the icon still switches to its filled variant to say you are here.
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        SessionChipContent(
            label = label,
            compact = compact,
            selected = selected,
            session = session,
        )
    }
}

@Composable
private fun SessionChipContent(
    label: String,
    compact: Boolean,
    selected: Boolean,
    session: FocusSession,
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
                    // A break is not focus time, and the pill is the only part of it visible from
                    // another tab — so it says which of the two is running.
                    id =
                        when {
                            session.isBreak -> R.drawable.ic_coffee
                            selected -> R.drawable.ic_focus
                            else -> R.drawable.ic_focus_outlined
                        },
                ),
            contentDescription = null,
            // The same size the tab icons beside it use. It stands in for the Focus tab, so a
            // smaller glyph made the one slot carrying a session look like the odd one out.
            modifier = Modifier.size(ChipIconSize),
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
private val ChipIconSize = 24.dp
private const val TABULAR_FIGURES = "tnum"
