package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NavigationPreferencesRepositoryTest {
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockContext: Context
    private lateinit var repository: NavigationPreferencesRepository

    @Before
    fun setup() {
        mockEditor =
            mockk<SharedPreferences.Editor>(relaxed = true) {
                every { putString(any(), any()) } returns this
                every { putBoolean(any(), any()) } returns this
                every { apply() } just Runs
            }
        mockPrefs =
            mockk {
                every { edit() } returns mockEditor
                every { getBoolean(any(), any()) } returns true
                every { getString(any(), any()) } answers { secondArg() }
            }
        mockContext =
            mockk {
                every { getSharedPreferences(any(), any()) } returns mockPrefs
            }
        repository = NavigationPreferencesRepositoryImpl(mockContext)
    }

    @Test
    fun `defaultRoutinesTabIsEnabled`() =
        runTest {
            repository.isRoutinesTabEnabled().test {
                assertThat(awaitItem()).isTrue()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `defaultTasksTabIsEnabled`() =
        runTest {
            repository.isTasksTabEnabled().test {
                assertThat(awaitItem()).isTrue()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `defaultTabIsRoutines`() =
        runTest {
            repository.getDefaultTab().test {
                assertThat(awaitItem()).isEqualTo(NavigationPreferencesRepository.DEFAULT_TAB_ROUTINES)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `setRoutinesTabDisabledUpdatesFlow`() =
        runTest {
            repository.setRoutinesTabEnabled(false)
            repository.isRoutinesTabEnabled().test {
                assertThat(awaitItem()).isFalse()
                cancelAndIgnoreRemainingEvents()
            }
            verify(exactly = 1) { mockEditor.putBoolean("routines_tab_enabled", false) }
            verify(exactly = 1) { mockEditor.apply() }
        }

    @Test
    fun `setTasksTabDisabledUpdatesFlow`() =
        runTest {
            repository.setTasksTabEnabled(false)
            repository.isTasksTabEnabled().test {
                assertThat(awaitItem()).isFalse()
                cancelAndIgnoreRemainingEvents()
            }
            verify(exactly = 1) { mockEditor.putBoolean("tasks_tab_enabled", false) }
            verify(exactly = 1) { mockEditor.apply() }
        }

    @Test
    fun `setDefaultTabUpdatesFlow`() =
        runTest {
            repository.setDefaultTab(NavigationPreferencesRepository.DEFAULT_TAB_TASKS)
            repository.getDefaultTab().test {
                assertThat(awaitItem()).isEqualTo(NavigationPreferencesRepository.DEFAULT_TAB_TASKS)
                cancelAndIgnoreRemainingEvents()
            }
            verify(exactly = 1) {
                mockEditor.putString(
                    "default_tab",
                    NavigationPreferencesRepository.DEFAULT_TAB_TASKS,
                )
            }
            verify(exactly = 1) { mockEditor.apply() }
        }

    @Test
    fun `saveAndGetLastRoute`() {
        every { mockPrefs.getString("last_route", null) } returns "settings"
        repository.saveLastRoute("settings")
        assertThat(repository.getLastRoute()).isEqualTo("settings")
        verify(exactly = 1) { mockEditor.putString("last_route", "settings") }
        verify(exactly = 1) { mockEditor.apply() }
    }

    @Test
    fun `getLastRouteReturnsNullByDefault`() {
        every { mockPrefs.getString("last_route", null) } returns null
        assertThat(repository.getLastRoute()).isNull()
    }
}
