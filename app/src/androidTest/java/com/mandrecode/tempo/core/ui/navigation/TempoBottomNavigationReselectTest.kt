package com.mandrecode.tempo.core.ui.navigation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.NavKey
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.focus.domain.repository.FocusSessionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Tapping the tab that is already open reselects it instead of navigating (#452). */
class TempoBottomNavigationReselectTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val navigated = mutableListOf<NavKey>()
    private val reselected = mutableListOf<NavKey>()

    @Before
    fun setUp() {
        val focusSessions =
            mockk<FocusSessionRepository>(relaxed = true) {
                every { activeSession } returns MutableStateFlow(null)
            }
        composeTestRule.setContent {
            TempoTheme {
                TempoBottomNavigation(
                    currentRoute = TasksRoute,
                    focusSessionRepository = focusSessions,
                    onNavigateToTopLevel = { navigated += it },
                    onReselect = { reselected += it },
                )
            }
        }
    }

    @Test
    fun tappingTheOpenTab_reselectsItWithoutNavigating() {
        composeTestRule.onNodeWithContentDescription(string(R.string.tasks)).performClick()

        composeTestRule.runOnIdle {
            assertThat(reselected).containsExactly(TasksRoute)
            assertThat(navigated).isEmpty()
        }
    }

    @Test
    fun tappingAnotherTab_navigatesWithoutReselecting() {
        composeTestRule.onNodeWithContentDescription(string(R.string.routines)).performClick()

        composeTestRule.runOnIdle {
            assertThat(navigated).containsExactly(RoutinesRoute)
            assertThat(reselected).isEmpty()
        }
    }

    private fun string(id: Int): String = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
}
