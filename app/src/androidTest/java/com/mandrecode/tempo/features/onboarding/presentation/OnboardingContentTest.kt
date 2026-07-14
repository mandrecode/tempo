package com.mandrecode.tempo.features.onboarding.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import org.junit.Rule
import org.junit.Test

class OnboardingContentTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun givenAnyPage_whenRendered_thenSkipIsAvailable() {
        var uiState by mutableStateOf(OnboardingContract.UiState())
        composeTestRule.setContent {
            TempoTheme {
                OnboardingContent(
                    uiState = uiState,
                    onEvent = {},
                )
            }
        }

        repeat(OnboardingContract.PAGE_COUNT) { page ->
            composeTestRule.runOnIdle {
                uiState = uiState.copy(currentPage = page)
            }

            composeTestRule.onNodeWithTag(OnboardingTestTags.SKIP).assertIsDisplayed().assertIsEnabled()
        }
    }

    @Test
    fun givenFirstPage_whenForwardClicked_thenNextEventIsEmitted() {
        var emittedEvent: OnboardingContract.UiEvent? = null
        setContent(OnboardingContract.UiState(), onEvent = { emittedEvent = it })

        composeTestRule.onNodeWithTag(OnboardingTestTags.FORWARD).performClick()

        assertThat(emittedEvent).isEqualTo(OnboardingContract.UiEvent.NextClicked)
    }

    @Test
    fun givenAnyPage_whenRendered_thenProgressHasOneSegmentPerPage() {
        setContent(OnboardingContract.UiState(currentPage = 1))

        composeTestRule
            .onAllNodesWithTag(OnboardingTestTags.PROGRESS_SEGMENT)
            .assertCountEquals(OnboardingContract.PAGE_COUNT)
    }

    @Test
    fun givenAppearancePage_whenDynamicColorsClicked_thenSelectionEventIsEmitted() {
        var emittedEvent: OnboardingContract.UiEvent? = null
        setContent(
            OnboardingContract.UiState(currentPage = 2),
            onEvent = { emittedEvent = it },
        )

        val dynamicLabel =
            InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.settings_color_scheme_dynamic)
        composeTestRule.onNodeWithText(dynamicLabel).performClick()

        assertThat(emittedEvent).isEqualTo(OnboardingContract.UiEvent.TempoColorsSelected(false))
    }

    @Test
    fun givenSetupPage_whenTasksSwitchClicked_thenToggleEventIsEmitted() {
        var emittedEvent: OnboardingContract.UiEvent? = null
        setContent(
            OnboardingContract.UiState(currentPage = 3),
            onEvent = { emittedEvent = it },
        )

        composeTestRule.onAllNodes(isToggleable())[1].performClick()

        assertThat(emittedEvent).isEqualTo(OnboardingContract.UiEvent.TasksTabToggled(false))
    }

    private fun setContent(
        uiState: OnboardingContract.UiState,
        onEvent: (OnboardingContract.UiEvent) -> Unit = {},
    ) {
        composeTestRule.setContent {
            TempoTheme {
                OnboardingContent(
                    uiState = uiState,
                    onEvent = onEvent,
                )
            }
        }
    }
}
