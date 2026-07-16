package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mandrecode.tempo.core.ui.adaptive.SheetPlacement
import com.mandrecode.tempo.core.ui.adaptive.rememberSheetPlacement

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
    adaptivePlacement: Boolean = false,
    content: @Composable ColumnScope.(onRequestDismiss: () -> Unit) -> Unit,
) {
    val direction =
        if (adaptivePlacement && rememberSheetPlacement() == SheetPlacement.SideSheet) {
            TempoModalSheetDirection.End
        } else {
            TempoModalSheetDirection.Top
        }
    TempoModalSheet(
        direction = direction,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        hasUnsavedChanges = hasUnsavedChanges,
        content = content,
    )
}
