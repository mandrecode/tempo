package com.mandrecode.tempo.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(FlowPreview::class)
@Composable
internal fun <T> DebouncedSnapshotEffect(
    enabled: Boolean,
    key: Any?,
    debounceMillis: Long,
    snapshotProvider: () -> T,
    onSnapshot: (T) -> Unit,
) {
    val currentSnapshotProvider = rememberUpdatedState(snapshotProvider)
    val currentOnSnapshot = rememberUpdatedState(onSnapshot)

    LaunchedEffect(enabled, key) {
        if (!enabled) return@LaunchedEffect

        snapshotFlow { currentSnapshotProvider.value() }
            .distinctUntilChanged()
            .debounce(debounceMillis)
            .collect { currentOnSnapshot.value(it) }
    }
}
