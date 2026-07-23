package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Full-size centered loading state shared by the tasks and routines list screens: a Material 3
 * Expressive loading indicator. `message` should already be the resolved, screen-specific string
 * (e.g. "Loading tasks…", "Loading habits…") and is exposed only as an accessibility label.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TempoLoadingIndicator(
    message: String,
    modifier: Modifier = Modifier.fillMaxSize(),
) {
    Box(
        modifier = modifier,
        contentAlignment = BiasAlignment(horizontalBias = 0f, verticalBias = -0.4f),
    ) {
        LoadingIndicator(
            modifier =
                Modifier
                    .size(96.dp)
                    .semantics { contentDescription = message },
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
