package com.mandrecode.tempo.core.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mandrecode.tempo.R

@Composable
fun DiscardChangesConfirmDialog(
    onCancelDiscard: () -> Unit,
    onConfirmDiscard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TempoConfirmDialog(
        title = stringResource(R.string.discard_changes),
        confirmLabel = stringResource(R.string.discard),
        onConfirm = onConfirmDiscard,
        onCancel = onCancelDiscard,
        modifier = modifier,
        text = { Text(stringResource(R.string.discard_changes_message)) },
    )
}
