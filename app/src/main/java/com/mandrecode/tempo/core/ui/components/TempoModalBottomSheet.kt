package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mandrecode.tempo.core.ui.adaptive.SheetPlacement
import com.mandrecode.tempo.core.ui.adaptive.rememberSheetPlacement

/**
 * A modal sheet that slides up from the bottom of the screen.
 * Keeps IME movement tied to the sheet surface while supporting guarded dismissal.
 *
 * With [adaptivePlacement] the sheet follows the window's [SheetPlacement] and presents as a
 * full-height side sheet on expanded or height-compact windows.
 */
@Composable
fun TempoModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    hasUnsavedChanges: Boolean = false,
    adaptivePlacement: Boolean = false,
    placement: SheetPlacement? = null,
    content: @Composable ColumnScope.(onRequestDismiss: () -> Unit) -> Unit,
) {
    val resolvedPlacement = placement ?: if (adaptivePlacement) rememberSheetPlacement() else null
    if (resolvedPlacement == SheetPlacement.DockedPane) {
        TempoDockedSheet(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            hasUnsavedChanges = hasUnsavedChanges,
            content = content,
        )
        return
    }
    val direction =
        if (resolvedPlacement == SheetPlacement.SideSheet) {
            TempoModalSheetDirection.End
        } else {
            TempoModalSheetDirection.Bottom
        }
    TempoModalSheet(
        direction = direction,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        hasUnsavedChanges = hasUnsavedChanges,
        content = content,
    )
}
