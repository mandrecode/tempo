package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A modal sheet that slides up from the bottom of the screen.
 * Keeps IME movement tied to the sheet surface while supporting guarded dismissal.
 */
@Composable
fun TempoModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    hasUnsavedChanges: Boolean = false,
    content: @Composable ColumnScope.(onRequestDismiss: () -> Unit) -> Unit,
) {
    TempoModalSheet(
        direction = TempoModalSheetDirection.Bottom,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        hasUnsavedChanges = hasUnsavedChanges,
        content = content,
    )
}
