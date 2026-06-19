package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.AppLanguage
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class AppPreferencesRepositoryTest {
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockContext: Context
    private lateinit var repository: AppPreferencesRepository

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
                every { getString(any(), any()) } returns AppLanguage.SYSTEM.name
            }
        mockContext =
            mockk {
                every { getSharedPreferences(any(), any()) } returns mockPrefs
            }
        repository = AppPreferencesRepositoryImpl(mockContext)
    }

    @Test
    fun `defaultLanguageIsSystem`() =
        runTest {
            repository.appLanguage.test {
                assertThat(awaitItem()).isEqualTo(AppLanguage.SYSTEM)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `saveAppLanguageUpdatesFlow`() =
        runTest {
            repository.saveAppLanguage(AppLanguage.ENGLISH)
            repository.appLanguage.test {
                assertThat(awaitItem()).isEqualTo(AppLanguage.ENGLISH)
                cancelAndIgnoreRemainingEvents()
            }
            verify(exactly = 1) { mockEditor.putString("app_language", "ENGLISH") }
            verify(exactly = 1) { mockEditor.apply() }
        }

    @Test
    fun `getAppLanguageReturnsSystemByDefault`() {
        assertThat(repository.getAppLanguage()).isEqualTo(AppLanguage.SYSTEM)
    }

    @Test
    fun `saveAppLanguageCanSwitchBack`() =
        runTest {
            repository.saveAppLanguage(AppLanguage.ENGLISH)
            repository.saveAppLanguage(AppLanguage.SYSTEM)
            repository.appLanguage.test {
                assertThat(awaitItem()).isEqualTo(AppLanguage.SYSTEM)
                cancelAndIgnoreRemainingEvents()
            }
            verify(exactly = 1) { mockEditor.putString("app_language", "ENGLISH") }
            verify(exactly = 1) { mockEditor.putString("app_language", "SYSTEM") }
            verify(exactly = 2) { mockEditor.apply() }
        }

    @Test
    fun `initialLanguageReadsFromPrefs`() =
        runTest {
            val prefs =
                mockk<SharedPreferences> {
                    every { edit() } returns mockEditor
                    every { getString(any(), any()) } returns "ENGLISH"
                }
            val context =
                mockk<Context> {
                    every { getSharedPreferences(any(), any()) } returns prefs
                }
            val repo = AppPreferencesRepositoryImpl(context)

            repo.appLanguage.test {
                assertThat(awaitItem()).isEqualTo(AppLanguage.ENGLISH)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
