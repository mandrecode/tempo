package com.mandrecode.tempo.features.tasks.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.WavyDivider
import com.mandrecode.tempo.core.ui.glass.glassCardColor
import com.mandrecode.tempo.core.ui.glass.glassRimOr
import com.mandrecode.tempo.core.ui.glass.glassSheen
import com.mandrecode.tempo.core.ui.theme.LocalTempoGlass
import com.mandrecode.tempo.core.ui.theme.groupLabel
import com.mandrecode.tempo.core.ui.theme.sectionHeader
import kotlinx.datetime.minus
import kotlinx.datetime.plus

@Composable
internal fun CompletedTasksSeparator(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    firstGroupLabel: String? = null,
    // Whether this separator is the first *visible* content in the list, which is a question about
    // whether there are any active tasks above it rather than about its index.
    isFirstVisibleItem: Boolean = false,
    // In a sectioned category the "Add section" button already closes the list above, so the
    // separator follows it more closely.
    followsSectionControls: Boolean = false,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "rotation",
    )

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    top =
                        when {
                            isFirstVisibleItem -> 0.dp
                            followsSectionControls -> 8.dp
                            else -> 28.dp
                        },
                    bottom = 12.dp,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompletedToggle(rotation = rotation, onToggle = onToggle)
        // The line is always there; the first group's label (a day, a priority) slides in beside it
        // when the block opens on a grouped sort.
        WavyDivider(modifier = Modifier.weight(1f))
        AnimatedVisibility(
            visible = showDivider && isExpanded && firstGroupLabel != null,
            enter = expandHorizontally() + fadeIn(),
            exit = shrinkHorizontally() + fadeOut(),
        ) {
            Text(
                text = firstGroupLabel.orEmpty(),
                style = MaterialTheme.typography.groupLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp, end = 8.dp),
            )
        }
    }
}

/** The pill that opens and closes the Completed block. */
@Composable
private fun CompletedToggle(
    rotation: Float,
    onToggle: () -> Unit,
) {
    // Frosted like the cards it opens onto with glass on; the tonal pill it was without.
    val glassOn = LocalTempoGlass.current.enabled
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        color = glassCardColor(MaterialTheme.colorScheme.secondaryContainer),
        shape = CompletedToggleShape,
        border = glassRimOr(null),
        tonalElevation = if (glassOn) 0.dp else 3.dp,
        modifier =
            Modifier
                .padding(end = 12.dp)
                .clip(CompletedToggleShape)
                .glassSheen(CompletedToggleShape)
                .clickable { onToggle() },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.completed_tasks),
                style = MaterialTheme.typography.sectionHeader,
                color = if (glassOn) colorScheme.onSurface else colorScheme.onSecondaryContainer,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier =
                    Modifier
                        .size(20.dp)
                        .rotate(rotation),
                tint = if (glassOn) colorScheme.onSurfaceVariant else colorScheme.onSecondaryContainer,
            )
        }
    }
}

private val CompletedToggleShape = RoundedCornerShape(16.dp)
