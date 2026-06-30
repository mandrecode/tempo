package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.launch

private const val SHEET_SCRIM_ALPHA = 0.32f
private val SHEET_CONTENT_TOP_PADDING = 4.dp

/**
 * Tempo wrapper around Material3's modal bottom sheet.
 *
 * Keeps the app-level dismissal contract while letting Material own sheet gestures, IME movement,
 * and predictive-back visuals.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TempoModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    hasUnsavedChanges: Boolean = false,
    content: @Composable ColumnScope.(onRequestDismiss: () -> Unit) -> Unit,
) {
    val showDiscardDialogState = remember { mutableStateOf(false) }
    val forceDismissState = remember { mutableStateOf(false) }
    val currentHasUnsavedChanges by rememberUpdatedState(hasUnsavedChanges)
    val currentOnDismiss by rememberUpdatedState(onDismissRequest)
    val scope = rememberCoroutineScope()

    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { targetValue ->
                if (
                    targetValue == SheetValue.Hidden &&
                    currentHasUnsavedChanges &&
                    !forceDismissState.value
                ) {
                    showDiscardDialogState.value = true
                    false
                } else {
                    true
                }
            },
        )

    val requestDismiss: () -> Unit = {
        scope.launch {
            sheetState.hide()
            if (!sheetState.isVisible) {
                currentOnDismiss()
            }
        }
    }

    if (showDiscardDialogState.value) {
        TempoModalBottomSheetDiscardDialog(
            onCancelDiscard = { showDiscardDialogState.value = false },
            onConfirmDiscard = {
                showDiscardDialogState.value = false
                forceDismissState.value = true
                requestDismiss()
            },
        )
    }

    TempoMaterialModalBottomSheet(
        onDismissRequest = currentOnDismiss,
        sheetState = sheetState,
        onRequestDismiss = requestDismiss,
        modifier = modifier,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TempoMaterialModalBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: androidx.compose.material3.SheetState,
    onRequestDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(onRequestDismiss: () -> Unit) -> Unit,
) {
    val isDarkTheme = LocalIsDarkTheme.current
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        sheetMaxWidth = Dp.Unspecified,
        sheetGesturesEnabled = false,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 1.dp,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = SHEET_SCRIM_ALPHA),
        dragHandle = null,
        properties =
            ModalBottomSheetProperties(
                isAppearanceLightStatusBars = !isDarkTheme,
                isAppearanceLightNavigationBars = !isDarkTheme,
            ),
    ) {
        androidx.compose.foundation.layout.Column(
            modifier =
                Modifier
                    .padding(top = SHEET_CONTENT_TOP_PADDING),
        ) {
            content(onRequestDismiss)
        }
    }
}

@Composable
private fun TempoModalBottomSheetDiscardDialog(
    onCancelDiscard: () -> Unit,
    onConfirmDiscard: () -> Unit,
) {
    DiscardChangesConfirmDialog(
        onCancelDiscard = onCancelDiscard,
        onConfirmDiscard = onConfirmDiscard,
    )
}
