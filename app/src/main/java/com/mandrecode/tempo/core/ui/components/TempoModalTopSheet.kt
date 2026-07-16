package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A modal sheet that slides down from the top of the screen.
 *
 * Top sheets retain their direction below the docked-pane breakpoint.
 */
@Composable
fun TempoModalTopSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    hasUnsavedChanges: Boolean = false,
    content: @Composable ColumnScope.(onRequestDismiss: () -> Unit) -> Unit,
) {
    TempoModalSheet(
        direction = TempoModalSheetDirection.Top,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        hasUnsavedChanges = hasUnsavedChanges,
        content = content,
    )
}
