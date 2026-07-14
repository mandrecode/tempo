package com.mandrecode.tempo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.core.ui.model.MainUiState
import com.mandrecode.tempo.core.ui.navigation.OnboardingRoute
import com.mandrecode.tempo.core.ui.navigation.RoutinesRoute
import com.mandrecode.tempo.core.ui.navigation.TasksRoute
import com.mandrecode.tempo.core.ui.navigation.topLevelPopUpToId
import org.junit.Rule
import org.junit.Test

class MainActivityNavigationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun givenActiveGraph_whenDefaultTabChanges_thenStartDestinationRemainsStable() {
        var state by mutableStateOf(successState())
        var destination: Any? = null
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
    fun givenOnboardingStartRemoved_whenSwitchingTabs_thenTopLevelBackStackDoesNotGrow() {
        lateinit var navController: NavHostController
        composeTestRule.setContent {
            navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = OnboardingRoute(),
            ) {
                composable<OnboardingRoute> { }
                composable<RoutinesRoute> { }
                composable<TasksRoute> { }
            }
        }

        composeTestRule.runOnIdle {
            navController.navigate(RoutinesRoute) {
                popUpTo<OnboardingRoute> { inclusive = true }
            }

            listOf(TasksRoute, RoutinesRoute, TasksRoute).forEach { destination ->
                navController.navigate(destination) {
                    popUpTo(navController.topLevelPopUpToId())
                    launchSingleTop = true
                }
                assertThat(navController.previousBackStackEntry).isNull()
            }
        }
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
}
