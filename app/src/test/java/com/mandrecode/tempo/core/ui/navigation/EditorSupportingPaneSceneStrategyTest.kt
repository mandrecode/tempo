package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategyScope
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EditorSupportingPaneSceneStrategyTest {
    private val strategy = EditorSupportingPaneSceneStrategy()

    private fun scene(vararg entries: NavEntry<NavKey>): Scene<NavKey>? =
        with(strategy) { SceneStrategyScope<NavKey>().calculateScene(entries.toList()) }

    private fun mainEntry(route: NavKey) = NavEntry(route, metadata = mapOf(EDITOR_MAIN_ROUTE_METADATA to route)) {}

    private fun editorEntry(route: EditorRoute): NavEntry<NavKey> {
        val metadata = mapOf(EDITOR_ROUTE_METADATA to route)
        return NavEntry(route, metadata = metadata) {}
    }

    private val tasks = mainEntry(TasksRoute)
    private val routines = mainEntry(RoutinesRoute)
    private val tasksEditor = editorEntry(TasksEditorRoute)
    private val onboarding = NavEntry<NavKey>(OnboardingRoute(), metadata = mapOf(ONBOARDING_ROUTE_METADATA to true)) {}

    @Test
    fun givenTasks_whenEditorDocks_thenSceneClassAndKeyStayTheSame() {
        val before = scene(tasks)
        val after = scene(tasks, tasksEditor)

        assertThat(before).isNotNull()
        assertThat(after).isNotNull()
        assertThat(after!!::class).isEqualTo(before!!::class)
        assertThat(after.key).isEqualTo(before.key)
    }

    @Test
    fun givenEditorDocked_whenCalculatingScene_thenItHoldsBothEntriesWithMainFirst() {
        val docked = scene(tasks, tasksEditor)!!

        assertThat(docked.entries).containsExactly(tasks, tasksEditor).inOrder()
        assertThat(docked.previousEntries).containsExactly(tasks)
    }

    @Test
    fun givenTwoTabs_whenCalculatingScenes_thenKeysDiffer() {
        assertThat(scene(routines)!!.key).isNotEqualTo(scene(tasks)!!.key)
    }

    @Test
    fun givenEditorOverUnsupportedScreen_whenCalculatingScene_thenFallsBack() {
        assertThat(scene(routines, tasksEditor)).isNull()
    }

    @Test
    fun givenOnboarding_whenCalculatingScene_thenFallsBack() {
        assertThat(scene(onboarding)).isNull()
    }

    @Test
    fun givenTabSwitchWithEditorDocked_whenPickingTransition_thenItIsStillAnInstantCut() {
        val transform =
            navigationTransition(initialScene = scene(tasks, tasksEditor)!!, targetScene = scene(routines)!!)

        assertThat(transform.targetContentEnter).isEqualTo(EnterTransition.None)
        assertThat(transform.initialContentExit).isEqualTo(ExitTransition.None)
    }

    @Test
    fun givenOnboardingFinishing_whenPickingTransition_thenItIsTheHandoff() {
        val onboardingScene = FakeScene(onboarding)

        val transform = navigationTransition(initialScene = onboardingScene, targetScene = scene(tasks)!!)

        assertThat(transform.targetContentEnter).isNotEqualTo(EnterTransition.None)
    }

    private class FakeScene(
        entry: NavEntry<NavKey>,
    ) : Scene<NavKey> {
        override val key: Any = entry.contentKey
        override val entries: List<NavEntry<NavKey>> = listOf(entry)
        override val previousEntries: List<NavEntry<NavKey>> = emptyList()
        override val content: @Composable () -> Unit = {}
    }
}
