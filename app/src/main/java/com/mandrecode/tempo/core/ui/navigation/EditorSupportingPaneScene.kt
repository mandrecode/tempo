package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

@Composable
internal fun rememberEditorSupportingPaneSceneStrategy(enabled: Boolean): SceneStrategy<NavKey> =
    remember(enabled) { EditorSupportingPaneSceneStrategy(enabled) }

private class EditorSupportingPaneSceneStrategy(
    private val enabled: Boolean,
) : SceneStrategy<NavKey> {
    override fun SceneStrategyScope<NavKey>.calculateScene(entries: List<NavEntry<NavKey>>): Scene<NavKey>? {
        if (!enabled || entries.size < 2) return null
        val mainEntry = entries[entries.lastIndex - 1]
        val editorEntry = entries.last()
        val editorRoute = editorEntry.metadata[EDITOR_ROUTE_METADATA] as? EditorRoute
        val mainRoute = mainEntry.metadata[EDITOR_MAIN_ROUTE_METADATA] as? NavKey
        return if (editorRoute != null && mainRoute != null && editorRoute.supports(mainRoute)) {
            EditorSupportingPaneScene(
                mainEntry = mainEntry,
                editorEntry = editorEntry,
                previousEntries = entries.dropLast(1),
            )
        } else {
            null
        }
    }
}

private data class EditorSupportingPaneScene(
    val mainEntry: NavEntry<NavKey>,
    val editorEntry: NavEntry<NavKey>,
    override val previousEntries: List<NavEntry<NavKey>>,
) : Scene<NavKey> {
    override val key: Any = mainEntry.contentKey to editorEntry.contentKey
    override val entries: List<NavEntry<NavKey>> = listOf(mainEntry, editorEntry)
    override val content: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxSize()) {
            mainEntry.Content()
            editorEntry.Content()
        }
    }
}

internal sealed interface EditorRoute : NavKey {
    fun supports(mainRoute: NavKey): Boolean
}

internal const val EDITOR_ROUTE_METADATA = "tempo.editor.route"
internal const val EDITOR_MAIN_ROUTE_METADATA = "tempo.editor.main.route"
