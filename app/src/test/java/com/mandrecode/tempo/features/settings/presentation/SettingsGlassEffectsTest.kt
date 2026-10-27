package com.mandrecode.tempo.features.settings.presentation

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import com.mandrecode.tempo.core.domain.model.VacationPeriod
import com.mandrecode.tempo.core.domain.repository.VacationModeRepository
import com.mandrecode.tempo.features.focus.domain.repository.FocusSessionRepository
import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import com.mandrecode.tempo.features.tasks.domain.repository.MissedReminderPreferences
import com.mandrecode.tempo.util.AppVersionInfo
import com.mandrecode.tempo.util.AppVersionProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalTime
import org.junit.After
import org.junit.Before
import org.junit.Test

/** The glass look's setting (#423): Settings mirrors the stored preference and writes it back. */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsGlassEffectsTest {
    private val testDispatcher = StandardTestDispatcher()
    private val glassEffects = MutableStateFlow(true)
    private val themePreferencesRepository =
        mockk<ThemePreferencesRepository>(relaxed = true) {
            every { getGlassEffects() } returns glassEffects
            every { getUseTempoColors() } returns MutableStateFlow(true)
        }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun givenGlassOff_whenSettingsOpen_thenTheSwitchShowsOff() =
        runTest {
            glassEffects.value = false

            val viewModel = createViewModel()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.glassEffects).isFalse()
        }

    @Test
    fun givenGlassOn_whenToggledOff_thenThePreferenceIsSaved() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(SettingsContract.UiEvent.GlassEffectsToggled(false))

            verify { themePreferencesRepository.setGlassEffects(false) }
        }

    private fun createViewModel() =
        SettingsViewModel(
            themePreferencesRepository = themePreferencesRepository,
            navigationPreferencesRepository = mockk(relaxed = true),
            appVersionProvider =
                mockk<AppVersionProvider> { every { getVersionInfo() } returns AppVersionInfo("1.0", 1) },
            completedTaskRetentionPreferences =
                mockk<CompletedTaskRetentionPreferences>(relaxed = true) {
                    every { isEnabled } returns MutableStateFlow(false)
                    every { retentionDays } returns MutableStateFlow(30)
                },
            configureCompletedTaskRetention = mockk(relaxed = true),
            missedReminderPreferences =
                mockk<MissedReminderPreferences>(relaxed = true) {
                    every { isEnabled } returns MutableStateFlow(false)
                    every { catchUpTime } returns MutableStateFlow(LocalTime(hour = 9, minute = 0))
                },
            missedReminderScheduler = mockk(relaxed = true),
            backupDelegate = mockk(relaxed = true),
            vacationModeRepository =
                mockk<VacationModeRepository>(relaxed = true) {
                    every { periods } returns MutableStateFlow(emptyList<VacationPeriod>())
                },
            focusSessionRepository =
                mockk<FocusSessionRepository>(relaxed = true) {
                    every { defaultLengthMinutes } returns MutableStateFlow(25)
                    every { breakLengthMinutes } returns MutableStateFlow(5)
                },
            appContext = mockk(relaxed = true),
        )
}
