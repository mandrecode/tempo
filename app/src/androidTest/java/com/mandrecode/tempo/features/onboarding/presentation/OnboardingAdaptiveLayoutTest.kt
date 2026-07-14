package com.mandrecode.tempo.features.onboarding.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import org.junit.Rule
import org.junit.Test

class OnboardingAdaptiveLayoutTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun givenCompactLayout_whenRendered_thenUsesSinglePane() {
        setContent(OnboardingLayout(isExpanded = false, isShort = false))

        composeTestRule.onNodeWithTag(OnboardingTestTags.SINGLE_PANE).assertIsDisplayed()
    }

    @Test
    fun givenExpandedLayout_whenRendered_thenUsesTwoPanes() {
        setContent(OnboardingLayout(isExpanded = true, isShort = false))

        composeTestRule.onNodeWithTag(OnboardingTestTags.TWO_PANE).assertIsDisplayed()
    }

    private fun setContent(layout: OnboardingLayout) {
        composeTestRule.setContent {
            TempoTheme {
                AdaptiveOnboardingPage(
                    layout = layout,
                    intro = {},
                    body = {},
                )
            }
        }
    }
}
