package com.mandrecode.tempo.features.settings.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import org.junit.Rule
import org.junit.Test

class SettingsContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun localizedString(id: Int): String = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    @Test
    fun displaysAppVersion() {
        composeTestRule.setContent {
            TempoTheme {
                SettingsContent(
                    uiState = SettingsContract.UiState(appVersion = "1.0.0"),
                    onEvent = {},
                    onOnboardingClick = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("1.0.0", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun displaysThemeModes() {
        val themeText = localizedString(R.string.theme)

        composeTestRule.setContent {
            TempoTheme {
                SettingsContent(
                    uiState = SettingsContract.UiState(),
                    onEvent = {},
                    onOnboardingClick = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        // Theme section should be visible
        composeTestRule
            .onNodeWithText(themeText, ignoreCase = true)
            .assertIsDisplayed()
    }

    @Test
    fun displaysTabToggleSettings() {
        val routinesText = localizedString(R.string.routines_tab)
        val tasksText = localizedString(R.string.tasks_tab)

        composeTestRule.setContent {
            TempoTheme {
                SettingsContent(
                    uiState =
                        SettingsContract.UiState(
                            isRoutinesTabEnabled = true,
                            isTasksTabEnabled = true,
                        ),
                    onEvent = {},
                    onOnboardingClick = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        // Both toggle items should be displayed (labels come from R.string.routines_tab / tasks_tab)
        composeTestRule
            .onAllNodesWithText(routinesText, ignoreCase = true)[0]
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onAllNodesWithText(tasksText, ignoreCase = true)[0]
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun themeModeSelectionTriggersEvent() {
        val darkText = localizedString(R.string.theme_dark)
        var selectedMode: ThemeMode? = null

        composeTestRule.setContent {
            TempoTheme {
                SettingsContent(
                    uiState = SettingsContract.UiState(selectedThemeMode = ThemeMode.SYSTEM),
                    onEvent = { event ->
                        if (event is SettingsContract.UiEvent.ThemeModeSelected) {
                            selectedMode = event.mode
                        }
                    },
                    onOnboardingClick = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        // Try clicking the dark mode option
        composeTestRule
            .onNodeWithText(darkText, substring = true, ignoreCase = true)
            .performClick()

        composeTestRule.waitForIdle()
        assertThat(selectedMode).isEqualTo(ThemeMode.DARK)
    }

    @Test
    fun displaysDefaultTabSectionWhenBothTabsEnabled() {
        val defaultTabText = localizedString(R.string.default_tab)

        composeTestRule.setContent {
            TempoTheme {
                SettingsContent(
                    uiState =
                        SettingsContract.UiState(
                            isRoutinesTabEnabled = true,
                            isTasksTabEnabled = true,
                        ),
                    onEvent = {},
                    onOnboardingClick = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithText(defaultTabText, ignoreCase = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun hidesDefaultTabSectionWhenOnlyOneTabEnabled() {
        val defaultTabText = localizedString(R.string.default_tab)

        composeTestRule.setContent {
            TempoTheme {
                SettingsContent(
                    uiState =
                        SettingsContract.UiState(
                            isRoutinesTabEnabled = true,
                            isTasksTabEnabled = false,
                            defaultTab = SettingsContract.DefaultTab.ROUTINES,
                        ),
                    onEvent = {},
                    onOnboardingClick = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithText(defaultTabText, ignoreCase = true)
            .assertDoesNotExist()
    }

    @Test
    fun defaultTabSelectionTriggersEvent() {
        val tasksText = localizedString(R.string.tasks)
        var selectedTab: SettingsContract.DefaultTab? = null

        composeTestRule.setContent {
            TempoTheme {
                SettingsContent(
                    uiState =
                        SettingsContract.UiState(
                            isRoutinesTabEnabled = true,
                            isTasksTabEnabled = true,
                            defaultTab = SettingsContract.DefaultTab.ROUTINES,
                        ),
                    onEvent = { event ->
                        if (event is SettingsContract.UiEvent.DefaultTabSelected) {
                            selectedTab = event.defaultTab
                        }
                    },
                    onOnboardingClick = {},
                )
            }
        }

        composeTestRule.waitForIdle()
        // There are two "Tasks" nodes (tab toggle + default tab chip), target the second one
        composeTestRule
            .onAllNodesWithText(tasksText, ignoreCase = true)[1]
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()
        assertThat(selectedTab).isEqualTo(SettingsContract.DefaultTab.TASKS)
    }

    @Test
    fun onboardingSelectionTriggersCallback() {
        var onboardingClicked = false
        val viewOnboardingText = localizedString(R.string.view_onboarding)

        composeTestRule.setContent {
            TempoTheme {
                SettingsContent(
                    uiState = SettingsContract.UiState(),
                    onEvent = {},
                    onOnboardingClick = { onboardingClicked = true },
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithText(viewOnboardingText, ignoreCase = true)
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()
        assertThat(onboardingClicked).isTrue()
    }
}
