package com.mandrecode.tempo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.preferences.NavigationPreferencesRepository
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.core.ui.model.MainUiState
import com.mandrecode.tempo.core.ui.navigation.RoutinesRoute
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
