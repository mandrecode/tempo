package com.mandrecode.tempo.core.ui.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Tracks the last snapshot dispatched to autosave for an editor sheet, so both the
 * debounced-autosave callback and the dismiss-time flush can share the same
 * compare-against-last-known-state-then-record logic instead of each reimplementing it.
 */
internal class EditorAutosaveController<T>(
    initialSnapshot: T?,
) {
    private var lastSavedSnapshot: T? = initialSnapshot

    /**
     * Invokes [onSave] with [snapshot] and records it as the new baseline if [snapshot] differs
     * from the last known saved/dispatched state and passes [isSaveable].
     */
    fun trySave(
        snapshot: T,
        isSaveable: (T) -> Boolean,
        onSave: (T) -> Unit,
    ) {
        if (snapshot != lastSavedSnapshot && isSaveable(snapshot)) {
            onSave(snapshot)
            lastSavedSnapshot = snapshot
        }
    }
}

@Composable
internal fun <T> rememberEditorAutosaveController(
    initialSnapshot: T?,
    key: Any?,
): EditorAutosaveController<T> = remember(key) { EditorAutosaveController(initialSnapshot) }
