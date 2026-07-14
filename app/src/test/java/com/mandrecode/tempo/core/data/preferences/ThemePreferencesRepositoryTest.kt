package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.ThemeMode
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ThemePreferencesRepositoryTest {
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockContext: Context
    private lateinit var repository: ThemePreferencesRepository

    @Before
    fun setup() {
        mockEditor =
            mockk<SharedPreferences.Editor>(relaxed = true) {
                every { putString(any(), any()) } returns this
                every { apply() } just Runs
            }
        mockPrefs =
            mockk {
                every { edit() } returns mockEditor
                every { getString(any(), any()) } returns null
                every { getBoolean(any(), any()) } answers { secondArg() }
            }
        mockContext =
            mockk {
                every { getSharedPreferences(any(), any()) } returns mockPrefs
            }
        repository = ThemePreferencesRepositoryImpl(mockContext)
    }

    @Test
    fun `defaultThemeModeIsSystem`() =
        runTest {
            repository.getThemeMode().test {
                assertThat(awaitItem()).isEqualTo(ThemeMode.SYSTEM)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `setThemeModeUpdatesFlow`() =
        runTest {
            repository.setThemeMode(ThemeMode.DARK)

            repository.getThemeMode().test {
                assertThat(awaitItem()).isEqualTo(ThemeMode.DARK)
                cancelAndIgnoreRemainingEvents()
            }
            verify(exactly = 1) { mockEditor.putString("theme_mode", "DARK") }
            verify(exactly = 1) { mockEditor.apply() }
        }

    @Test
    fun `setThemeModeCanSwitchBetweenModes`() =
        runTest {
            repository.setThemeMode(ThemeMode.DARK)
            repository.setThemeMode(ThemeMode.LIGHT)
            repository.setThemeMode(ThemeMode.SYSTEM)
            repository.getThemeMode().test {
                assertThat(awaitItem()).isEqualTo(ThemeMode.SYSTEM)
                cancelAndIgnoreRemainingEvents()
            }
            verify(exactly = 1) { mockEditor.putString("theme_mode", "DARK") }
            verify(exactly = 1) { mockEditor.putString("theme_mode", "LIGHT") }
            verify(exactly = 1) { mockEditor.putString("theme_mode", "SYSTEM") }
            verify(exactly = 3) { mockEditor.apply() }
        }

    @Test
    fun `initialThemeModeReadsFromPrefs`() =
        runTest {
            val prefs =
                mockk<SharedPreferences> {
                    every { edit() } returns mockEditor
                    every { getString(any(), any()) } returns "LIGHT"
                    every { getBoolean(any(), any()) } returns false
                }
            val context =
                mockk<Context> {
                    every { getSharedPreferences(any(), any()) } returns prefs
                }
            val repo = ThemePreferencesRepositoryImpl(context)

            repo.getThemeMode().test {
                assertThat(awaitItem()).isEqualTo(ThemeMode.LIGHT)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `defaultUseTempoColorsIsTrue`() =
        runTest {
            repository.getUseTempoColors().test {
                assertThat(awaitItem()).isTrue()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `savedDynamicColorsPreferenceIsPreserved`() =
        runTest {
            every { mockPrefs.getBoolean("use_tempo_colors", true) } returns false
            val repo = ThemePreferencesRepositoryImpl(mockContext)

            repo.getUseTempoColors().test {
                assertThat(awaitItem()).isFalse()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `setUseTempoColorsUpdatesFlow`() =
        runTest {
            repository.setUseTempoColors(true)

            repository.getUseTempoColors().test {
                assertThat(awaitItem()).isTrue()
                cancelAndIgnoreRemainingEvents()
            }
            verify(exactly = 1) { mockEditor.putBoolean("use_tempo_colors", true) }
            verify(exactly = 1) { mockEditor.apply() }
        }

    @Test
    fun `setUseTempoColorsCanToggle`() =
        runTest {
            repository.setUseTempoColors(true)
            repository.setUseTempoColors(false)
            repository.getUseTempoColors().test {
                assertThat(awaitItem()).isFalse()
                cancelAndIgnoreRemainingEvents()
            }
            verify(exactly = 1) { mockEditor.putBoolean("use_tempo_colors", true) }
            verify(exactly = 1) { mockEditor.putBoolean("use_tempo_colors", false) }
            verify(exactly = 2) { mockEditor.apply() }
        }
}
