package com.mandrecode.tempo

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.core.ui.model.MainUiState
import com.mandrecode.tempo.core.ui.navigation.OnboardingRoute
import com.mandrecode.tempo.core.ui.navigation.RoutinesRoute
import com.mandrecode.tempo.core.ui.navigation.SettingsRoute
import com.mandrecode.tempo.core.ui.navigation.TasksRoute
import com.mandrecode.tempo.core.ui.navigation.TempoNavigator
import com.mandrecode.tempo.core.ui.navigation.rememberTempoNavigator
import org.junit.Rule
import org.junit.Test

class MainActivityNavigationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun givenActiveGraph_whenDefaultTabChanges_thenStartDestinationRemainsStable() {
        var state by mutableStateOf(successState())
        var destination: NavKey? = null
        composeTestRule.setContent {
            destination = rememberStartDestination(state)
        }
        composeTestRule.runOnIdle {
            assertThat(destination).isEqualTo(RoutinesRoute)
            state =
                state.copy(
                    defaultTab = NavigationPreferencesRepository.DEFAULT_TAB_TASKS,
                    isRoutinesTabEnabled = false,
                    isTasksTabEnabled = true,
                )
        }

        composeTestRule.runOnIdle {
            assertThat(destination).isEqualTo(RoutinesRoute)
        }
    }

    @Test
    fun givenOnboardingStartRemoved_whenSwitchingTabs_thenTopLevelBackStacksDoNotGrow() {
        lateinit var navigator: TempoNavigator
        composeTestRule.setContent {
            navigator = rememberTempoNavigator(OnboardingRoute())
        }

        composeTestRule.runOnIdle {
            navigator.completeOnboarding(RoutinesRoute)
            listOf(TasksRoute, SettingsRoute, RoutinesRoute, TasksRoute).forEach(navigator::navigateToTopLevel)

            assertThat(navigator.routinesBackStack).containsExactly(RoutinesRoute)
            assertThat(navigator.tasksBackStack).containsExactly(TasksRoute)
            assertThat(navigator.settingsBackStack).containsExactly(SettingsRoute)
        }
    }

    @Test
    fun givenTabSaveableState_whenSwitchingAwayAndBack_thenStateIsRestored() {
        lateinit var navigator: TempoNavigator
        composeTestRule.setContent {
            navigator = rememberTempoNavigator(RoutinesRoute)
            val decorators =
                listOf<NavEntryDecorator<NavKey>>(
                    rememberSaveableStateHolderNavEntryDecorator(),
                )
            val provider =
                entryProvider<NavKey> {
                    entry<RoutinesRoute> {
                        var count by rememberSaveable { mutableIntStateOf(0) }
                        Button(
                            onClick = { count++ },
                            modifier = Modifier.testTag(ROUTINES_STATE_TAG),
                        ) {
                            Text(count.toString())
                        }
                    }
                    entry<TasksRoute> { Text("Tasks") }
                }
            val routinesEntries =
                rememberDecoratedNavEntries(navigator.routinesBackStack, decorators, provider)
            val tasksEntries =
                rememberDecoratedNavEntries(navigator.tasksBackStack, decorators, provider)

            NavDisplay(
                entries =
                    when (navigator.section) {
                        TempoNavigator.Section.ROUTINES -> routinesEntries
                        TempoNavigator.Section.TASKS -> tasksEntries
                        TempoNavigator.Section.SETTINGS -> error("Unexpected settings section")
                        TempoNavigator.Section.ONBOARDING -> error("Unexpected onboarding section")
                    },
                onBack = { navigator.pop() },
            )
        }

        composeTestRule.onNodeWithTag(ROUTINES_STATE_TAG).performClick().assertTextEquals("1")
        composeTestRule.runOnIdle { navigator.navigateToTopLevel(TasksRoute) }
        composeTestRule.runOnIdle { navigator.navigateToTopLevel(RoutinesRoute) }
        composeTestRule.onNodeWithTag(ROUTINES_STATE_TAG).assertTextEquals("1")
    }

    private fun successState(): MainUiState.Success =
        MainUiState.Success(
            themeMode = ThemeMode.SYSTEM,
            useTempoColors = true,
            defaultTab = NavigationPreferencesRepository.DEFAULT_TAB_ROUTINES,
            isRoutinesTabEnabled = true,
            isTasksTabEnabled = true,
            isOnboardingCompleted = true,
        )

    private companion object {
        const val ROUTINES_STATE_TAG = "routines_state"
    }
}
