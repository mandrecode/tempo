package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NavigationTransitionTest {
    private fun topLevel(route: NavKey): Scene<NavKey> {
        val metadata = mapOf(TOP_LEVEL_ROUTE_METADATA to route)
        return FakeScene(NavEntry(route, metadata = metadata) {})
    }

    private val onboarding: Scene<NavKey> =
        FakeScene(NavEntry(OnboardingRoute(), metadata = mapOf(ONBOARDING_ROUTE_METADATA to true)) {})

    @Test
    fun givenTabSwitch_whenPickingTransition_thenItIsAnInstantCut() {
        val tasks = topLevel(TasksRoute)
        val routines = topLevel(RoutinesRoute)

        val transform = navigationTransition(initialScene = tasks, targetScene = routines)

        assertThat(transform.targetContentEnter).isEqualTo(EnterTransition.None)
        assertThat(transform.initialContentExit).isEqualTo(ExitTransition.None)
    }

    @Test
    fun givenOnboardingFinishing_whenPickingTransition_thenItIsTheHandoff() {
        val transform = navigationTransition(initialScene = onboarding, targetScene = topLevel(TasksRoute))

        assertThat(transform.targetContentEnter).isEqualTo(onboardingHandoffEnterTransition())
        assertThat(transform.initialContentExit).isEqualTo(onboardingHandoffExitTransition())
    }

    @Test
    fun givenNonTabNavigation_whenPickingTransition_thenItIsNeitherCutNorHandoff() {
        val transform = navigationTransition(initialScene = onboarding, targetScene = onboarding)

        assertThat(transform.targetContentEnter).isNotEqualTo(EnterTransition.None)
        assertThat(transform.targetContentEnter).isNotEqualTo(onboardingHandoffEnterTransition())
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
