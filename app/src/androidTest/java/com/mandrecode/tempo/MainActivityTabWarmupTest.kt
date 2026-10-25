package com.mandrecode.tempo

import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.focus.presentation.FocusViewModel
import com.mandrecode.tempo.features.routines.presentation.RoutinesViewModel
import com.mandrecode.tempo.features.tasks.presentation.TasksViewModel
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Every tab's data is loaded before the tab is first opened, so it appears finished on its first
 * frame instead of as its bare background with the content arriving on top (#438).
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTabWarmupTest {
    @Test
    fun tabsNotYetOpened_haveTheirDataLoadedAlready() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            // Focus is the tab on screen, so its view model exists either way; wait for its data.
            val deadline = System.currentTimeMillis() + WAIT_MS
            var focusLoaded = false
            while (!focusLoaded && System.currentTimeMillis() < deadline) {
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                Thread.sleep(POLL_MS)
                scenario.onActivity { activity ->
                    focusLoaded = !ViewModelProvider(activity)[FocusViewModel::class.java].uiState.value.isLoading
                }
            }
            assertThat(focusLoaded).isTrue()
            Thread.sleep(SETTLE_MS)

            // Routines and Tasks have never been opened. Looked up once: had the activity not
            // created them in the background, this lookup would create them now, still loading.
            var routinesLoading = true
            var tasksLoading = true
            scenario.onActivity { activity ->
                val provider = ViewModelProvider(activity)
                routinesLoading = provider[RoutinesViewModel::class.java].uiState.value.isLoading
                tasksLoading = provider[TasksViewModel::class.java].uiState.value.isLoading
            }

            assertThat(routinesLoading).isFalse()
            assertThat(tasksLoading).isFalse()
        }
    }

    private companion object {
        const val WAIT_MS = 5_000L
        const val POLL_MS = 100L
        const val SETTLE_MS = 500L
    }
}
