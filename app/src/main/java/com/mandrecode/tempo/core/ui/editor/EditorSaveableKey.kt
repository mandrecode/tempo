package com.mandrecode.tempo.core.ui.editor

/**
 * Stable, equals-comparable key for [androidx.compose.runtime.saveable.rememberSaveable] editor
 * state, derived from the sheet's session id plus any feature-specific discriminators (e.g. the
 * id of the entity being edited, or a selected tab).
 */
internal fun editorSaveableKey(
    sessionId: Long,
    vararg extra: Any?,
): List<Any?> = listOf(sessionId, *extra)
