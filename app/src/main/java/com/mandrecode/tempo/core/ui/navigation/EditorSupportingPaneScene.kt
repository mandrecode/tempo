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
internal fun rememberEditorSupportingPaneSceneStrategy(): SceneStrategy<NavKey> {
    val strategy = remember { EditorSupportingPaneSceneStrategy() }
    return strategy
}

/**
 * One scene for a top-level screen, whether or not its docked editor is open (#501).
 *
 * NavDisplay animates whenever the scene's class or key changes. The screen draws its docked pane
 * itself and the editor entry draws nothing, so opening the editor must not count as a scene change:
 * a crossfade would empty the outgoing scene (an entry is drawn in one scene at a time) while the
 * same screen faded in, blinking the whole window and hiding the pane's own slide-in. Keying by the
 * main entry alone keeps the scene identical across docking; switching tabs still changes the key.
 */
internal class EditorSupportingPaneSceneStrategy : SceneStrategy<NavKey> {
    override fun SceneStrategyScope<NavKey>.calculateScene(entries: List<NavEntry<NavKey>>): Scene<NavKey>? {
        val topEntry = entries.lastOrNull()
        val belowEntry = entries.getOrNull(entries.lastIndex - 1)
        val previous = entries.dropLast(1)
        return when {
            topEntry == null -> null
            topEntry.metadata.containsKey(EDITOR_MAIN_ROUTE_METADATA) ->
                EditorSupportingPaneScene(mainEntry = topEntry, editorEntry = null, previousEntries = previous)
            belowEntry != null && isSupportedEditor(editorEntry = topEntry, mainEntry = belowEntry) ->
                EditorSupportingPaneScene(mainEntry = belowEntry, editorEntry = topEntry, previousEntries = previous)
            else -> null
        }
    }

    private fun isSupportedEditor(
        editorEntry: NavEntry<NavKey>,
        mainEntry: NavEntry<NavKey>,
    ): Boolean {
        val editorRoute = editorEntry.metadata[EDITOR_ROUTE_METADATA] as? EditorRoute
        val mainRoute = mainEntry.metadata[EDITOR_MAIN_ROUTE_METADATA] as? NavKey
        return editorRoute != null && mainRoute != null && editorRoute.supports(mainRoute)
    }
}

internal data class EditorSupportingPaneScene(
    val mainEntry: NavEntry<NavKey>,
    val editorEntry: NavEntry<NavKey>?,
    override val previousEntries: List<NavEntry<NavKey>>,
) : Scene<NavKey> {
    override val key: Any = mainEntry.contentKey
    override val entries: List<NavEntry<NavKey>> = listOfNotNull(mainEntry, editorEntry)
    override val content: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxSize()) {
            // The same call site with or without the editor, so the screen's composition is kept.
            mainEntry.Content()
            editorEntry?.Content()
        }
    }
}

internal sealed interface EditorRoute : NavKey {
    fun supports(mainRoute: NavKey): Boolean
}

internal const val EDITOR_ROUTE_METADATA = "tempo.editor.route"
internal const val EDITOR_MAIN_ROUTE_METADATA = "tempo.editor.main.route"

/**
 * Marks a top-level route's [NavEntry] for identity checks that can't rely on [NavEntry.contentKey]
 * (it defaults to `key.toString()`, and the raw [NavKey] instance isn't otherwise exposed).
 */
internal const val ONBOARDING_ROUTE_METADATA = "tempo.onboarding.route"
internal const val SETTINGS_ROUTE_METADATA = "tempo.settings.route"
