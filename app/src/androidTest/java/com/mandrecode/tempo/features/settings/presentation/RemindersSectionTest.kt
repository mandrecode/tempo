package com.mandrecode.tempo.features.settings.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.util.DateTimeFormatter
import kotlinx.datetime.LocalTime
import org.junit.Rule
import org.junit.Test

class RemindersSectionTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun localizedString(id: Int): String = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    @Test
    fun showsCatchUpTimeWhenEnabled() {
        val timeLabel = localizedString(R.string.settings_missed_reminders_time)

        setContent(SettingsContract.UiState(missedReminderCatchUpEnabled = true))

        composeTestRule.onNodeWithText(timeLabel).assertIsDisplayed()
    }

    @Test
    fun showsTheCatchUpTimeAsAClockValue() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val nineAm = LocalTime(hour = 9, minute = 0)

        setContent(
            SettingsContract.UiState(
                missedReminderCatchUpEnabled = true,
                missedReminderCatchUpTime = nineAm,
            ),
        )

        composeTestRule
            .onNodeWithText(DateTimeFormatter.formatTimeOfDay(nineAm, context))
            .assertIsDisplayed()
    }

    @Test
    fun tappingTheTimeValueOpensThePicker() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val nineAm = LocalTime(hour = 9, minute = 0)
        val confirmLabel = localizedString(R.string.ok)

        setContent(
            SettingsContract.UiState(
                missedReminderCatchUpEnabled = true,
                missedReminderCatchUpTime = nineAm,
            ),
        )

        composeTestRule
            .onNodeWithText(DateTimeFormatter.formatTimeOfDay(nineAm, context))
            .performClick()

        composeTestRule.onNodeWithText(confirmLabel).assertIsDisplayed()
    }

    @Test
    fun hidesCatchUpTimeWhenDisabled() {
        val timeLabel = localizedString(R.string.settings_missed_reminders_time)

        setContent(SettingsContract.UiState(missedReminderCatchUpEnabled = false))

        composeTestRule.onNodeWithText(timeLabel).assertDoesNotExist()
    }

    @Test
    fun switchReflectsEnabledState() {
        val switchLabel = localizedString(R.string.settings_repeat_missed_reminders)

        setContent(SettingsContract.UiState(missedReminderCatchUpEnabled = true))

        composeTestRule.onNodeWithContentDescription(switchLabel).assertIsOn()
    }

    @Test
    fun switchReflectsDisabledState() {
        val switchLabel = localizedString(R.string.settings_repeat_missed_reminders)

        setContent(SettingsContract.UiState(missedReminderCatchUpEnabled = false))

        composeTestRule.onNodeWithContentDescription(switchLabel).assertIsOff()
    }

    @Test
    fun togglingTheSwitchEmitsTheEvent() {
        val switchLabel = localizedString(R.string.settings_repeat_missed_reminders)
        var toggledTo: Boolean? = null

        setContent(
            uiState = SettingsContract.UiState(missedReminderCatchUpEnabled = true),
            onEvent = { event ->
                if (event is SettingsContract.UiEvent.MissedReminderCatchUpToggled) {
                    toggledTo = event.enabled
                }
            },
        )

        composeTestRule.onNodeWithContentDescription(switchLabel).performClick()

        assertThat(toggledTo).isFalse()
    }

    @Test
    fun tappingTheTimeRowOpensThePicker() {
        val timeLabel = localizedString(R.string.settings_missed_reminders_time)
        val confirmLabel = localizedString(R.string.ok)

        setContent(
            SettingsContract.UiState(
                missedReminderCatchUpEnabled = true,
                missedReminderCatchUpTime = LocalTime(hour = 9, minute = 0),
            ),
        )

        composeTestRule.onNodeWithText(timeLabel).performClick()

        composeTestRule.onNodeWithText(confirmLabel).assertIsDisplayed()
    }

    @Test
    fun confirmingThePickerEmitsTheChosenTime() {
        val timeLabel = localizedString(R.string.settings_missed_reminders_time)
        val confirmLabel = localizedString(R.string.ok)
        var chosenTime: LocalTime? = null

        setContent(
            uiState =
                SettingsContract.UiState(
                    missedReminderCatchUpEnabled = true,
                    missedReminderCatchUpTime = LocalTime(hour = 9, minute = 0),
                ),
            onEvent = { event ->
                if (event is SettingsContract.UiEvent.MissedReminderCatchUpTimeChanged) {
                    chosenTime = event.time
                }
            },
        )

        composeTestRule.onNodeWithText(timeLabel).performClick()
        composeTestRule.onNodeWithText(confirmLabel).performClick()

        // Confirming without touching the dial keeps the current time.
        assertThat(chosenTime).isEqualTo(LocalTime(hour = 9, minute = 0))
    }

    private fun setContent(
        uiState: SettingsContract.UiState,
        onEvent: (SettingsContract.UiEvent) -> Unit = {},
    ) {
        composeTestRule.setContent {
            TempoTheme {
                RemindersSection(uiState = uiState, onEvent = onEvent)
            }
        }
        composeTestRule.waitForIdle()
    }
}
