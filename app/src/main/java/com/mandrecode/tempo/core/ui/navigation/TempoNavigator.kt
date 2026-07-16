package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack

@Stable
internal class TempoNavigator(
    internal val routinesBackStack: NavBackStack<NavKey>,
    internal val tasksBackStack: NavBackStack<NavKey>,
    internal val onboardingBackStack: NavBackStack<NavKey>,
    sectionState: MutableState<Section>,
) {
    internal enum class Section {
        ROUTINES,
        TASKS,
        ONBOARDING,
    }

    var section by sectionState
        private set

    val activeBackStack: NavBackStack<NavKey>
        get() =
            when (section) {
                Section.ROUTINES -> routinesBackStack
                Section.TASKS -> tasksBackStack
                Section.ONBOARDING -> onboardingBackStack
            }

    val currentRoute: NavKey
        get() = activeBackStack.last { it !is EditorRoute }

    val topLevelRoute: NavKey
        get() =
            when (section) {
                Section.ROUTINES -> RoutinesRoute
                Section.TASKS -> TasksRoute
                Section.ONBOARDING -> onboardingBackStack.first()
            }

    fun navigate(route: NavKey) {
        if (activeBackStack.lastOrNull() != route) {
            activeBackStack.add(route)
        }
    }

    fun setEditorVisible(
        route: EditorRoute,
        visible: Boolean,
    ) {
        val isVisible = activeBackStack.lastOrNull() == route
        when {
            visible && !isVisible -> activeBackStack.add(route)
            !visible && isVisible -> activeBackStack.removeAt(activeBackStack.lastIndex)
        }
    }

    fun navigateToTopLevel(route: NavKey) {
        section = route.toSection()
        while (activeBackStack.size > 1) {
            activeBackStack.removeAt(activeBackStack.lastIndex)
        }
    }

    fun completeOnboarding(route: NavKey) {
        section = route.toSection()
    }

    fun pop(): Boolean {
        if (activeBackStack.size <= 1) return false
        activeBackStack.removeAt(activeBackStack.lastIndex)
        return true
    }

    private fun NavKey.toSection(): Section =
        when (this) {
            RoutinesRoute -> Section.ROUTINES
            TasksRoute -> Section.TASKS
            else -> error("$this is not a top-level route")
        }
}

@Composable
internal fun rememberTempoNavigator(startDestination: NavKey): TempoNavigator {
    val routinesBackStack = rememberNavBackStack(RoutinesRoute)
    val tasksBackStack = rememberNavBackStack(TasksRoute)
    val onboardingStart = startDestination.takeIf { it is OnboardingRoute } ?: OnboardingRoute()
    val onboardingBackStack = rememberNavBackStack(onboardingStart)
    val initialSection =
        when (startDestination) {
            RoutinesRoute -> TempoNavigator.Section.ROUTINES
            TasksRoute -> TempoNavigator.Section.TASKS
            is OnboardingRoute -> TempoNavigator.Section.ONBOARDING
            else -> error("Unsupported start destination: $startDestination")
        }
    val sectionState = rememberSaveable { mutableStateOf(initialSection) }

    return remember(routinesBackStack, tasksBackStack, onboardingBackStack) {
        TempoNavigator(
            routinesBackStack = routinesBackStack,
            tasksBackStack = tasksBackStack,
            onboardingBackStack = onboardingBackStack,
            sectionState = sectionState,
        )
    }
}
