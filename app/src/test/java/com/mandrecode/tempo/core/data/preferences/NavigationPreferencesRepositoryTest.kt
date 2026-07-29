package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.TempoTab
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NavigationPreferencesRepositoryTest {
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockContext: Context

    /**
     * Builds a repository over mocked preferences. [storedTabs] is what `enabled_tabs` already
     * holds — `null` models an installation that predates the tab registry, which is the case the
     * migration has to handle.
     */
    private fun createRepository(
        storedTabs: Set<String>? = null,
        legacyRoutinesEnabled: Boolean = true,
        legacyTasksEnabled: Boolean = true,
        storedDefaultTab: String? = null,
    ): NavigationPreferencesRepository {
        mockEditor =
            mockk<SharedPreferences.Editor>(relaxed = true) {
                every { putString(any(), any()) } returns this
                every { putBoolean(any(), any()) } returns this
                every { putStringSet(any(), any()) } returns this
                every { apply() } just Runs
            }
        mockPrefs =
            mockk {
                every { edit() } returns mockEditor
                every { getStringSet("enabled_tabs", null) } returns storedTabs
                every { getBoolean("routines_tab_enabled", true) } returns legacyRoutinesEnabled
                every { getBoolean("tasks_tab_enabled", true) } returns legacyTasksEnabled
                every { getString("default_tab", null) } returns storedDefaultTab
                every { getString("last_route", null) } returns null
                every { contains("default_tab") } returns (storedDefaultTab != null)
            }
        mockContext =
            mockk {
                every { getSharedPreferences(any(), any()) } returns mockPrefs
            }
        return NavigationPreferencesRepositoryImpl(mockContext)
    }

    @Test
    fun `allTabsEnabledByDefault`() =
        runTest {
            createRepository().enabledTabs().test {
                assertThat(awaitItem()).containsExactlyElementsIn(TempoTab.entries)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `migrationKeepsLegacyDisabledTabDisabledAndEnablesFocus`() =
        runTest {
            val repository = createRepository(legacyRoutinesEnabled = true, legacyTasksEnabled = false)

            repository.enabledTabs().test {
                assertThat(awaitItem()).containsExactly(TempoTab.FOCUS, TempoTab.ROUTINES)
                cancelAndIgnoreRemainingEvents()
            }
            verify(exactly = 1) { mockEditor.putStringSet("enabled_tabs", setOf("focus", "routines")) }
        }

    @Test
    fun `migrationDoesNotRunOnceTabSetIsStored`() =
        runTest {
            val repository =
                createRepository(
                    storedTabs = setOf("tasks"),
                    // Legacy values disagree with the stored set; the stored set must win.
                    legacyRoutinesEnabled = true,
                    legacyTasksEnabled = true,
                )

            repository.enabledTabs().test {
                assertThat(awaitItem()).containsExactly(TempoTab.TASKS)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `unknownStoredTabIsIgnored`() =
        runTest {
            createRepository(storedTabs = setOf("focus", "journal")).enabledTabs().test {
                assertThat(awaitItem()).containsExactly(TempoTab.FOCUS)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `emptyStoredTabSetFallsBackToDefaultTab`() =
        runTest {
            createRepository(storedTabs = emptySet()).enabledTabs().test {
                assertThat(awaitItem()).containsExactly(TempoTab.DEFAULT)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `defaultTabIsFocusWhenNothingStored`() =
        runTest {
            createRepository().getDefaultTab().test {
                assertThat(awaitItem()).isEqualTo(TempoTab.FOCUS)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `migrationMovesAnExistingInstallationToFocus`() =
        runTest {
            // An installation that had deliberately chosen Tasks as its start tab.
            val repository = createRepository(storedDefaultTab = "tasks")

            verify(exactly = 1) { mockEditor.putString("default_tab", "focus") }
            repository.getDefaultTab().test {
                assertThat(awaitItem()).isEqualTo(TempoTab.FOCUS)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `migrationDoesNotReapplyOnceTheTabSetIsStored`() =
        runTest {
            // Already migrated, and the user has since picked Tasks again.
            createRepository(storedTabs = setOf("focus", "tasks"), storedDefaultTab = "tasks")
                .getDefaultTab()
                .test {
                    assertThat(awaitItem()).isEqualTo(TempoTab.TASKS)
                    cancelAndIgnoreRemainingEvents()
                }
            verify(exactly = 0) { mockEditor.putString("default_tab", "focus") }
        }

    @Test
    fun `disablingTabUpdatesFlowAndPersists`() =
        runTest {
            val repository = createRepository()

            repository.setTabEnabled(TempoTab.ROUTINES, false)

            repository.enabledTabs().test {
                assertThat(awaitItem()).containsExactly(TempoTab.FOCUS, TempoTab.TASKS)
                cancelAndIgnoreRemainingEvents()
            }
            verify { mockEditor.putStringSet("enabled_tabs", setOf("focus", "tasks")) }
        }

    @Test
    fun `disablingTheLastEnabledTabIsRejected`() =
        runTest {
            val repository = createRepository(storedTabs = setOf("focus"))

            repository.setTabEnabled(TempoTab.FOCUS, false)

            repository.enabledTabs().test {
                assertThat(awaitItem()).containsExactly(TempoTab.FOCUS)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `setDefaultTabUpdatesFlowAndPersists`() =
        runTest {
            val repository = createRepository()

            repository.setDefaultTab(TempoTab.TASKS)

            repository.getDefaultTab().test {
                assertThat(awaitItem()).isEqualTo(TempoTab.TASKS)
                cancelAndIgnoreRemainingEvents()
            }
            verify(exactly = 1) { mockEditor.putString("default_tab", "tasks") }
            verify { mockEditor.apply() }
        }

    @Test
    fun `saveAndGetLastRoute`() {
        val repository = createRepository()
        every { mockPrefs.getString("last_route", null) } returns "settings"

        repository.saveLastRoute("settings")

        assertThat(repository.getLastRoute()).isEqualTo("settings")
        verify(exactly = 1) { mockEditor.putString("last_route", "settings") }
        verify { mockEditor.apply() }
    }

    @Test
    fun `getLastRouteReturnsNullByDefault`() {
        assertThat(createRepository().getLastRoute()).isNull()
    }
}
