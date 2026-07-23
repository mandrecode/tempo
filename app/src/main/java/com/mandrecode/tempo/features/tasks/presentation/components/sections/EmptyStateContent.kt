package com.mandrecode.tempo.features.tasks.presentation.components.sections

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.TempoSpacing
import com.mandrecode.tempo.core.ui.theme.emptyStateTitle

@Composable
fun EmptyStateContent(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
        contentAlignment =
            BiasAlignment(
                horizontalBias = 0f,
                verticalBias = TempoSpacing.CENTERED_CONTENT_VERTICAL_BIAS,
            ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.nothing_to_see_here),
                style = MaterialTheme.typography.emptyStateTitle,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.tap_to_create_first_task),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
