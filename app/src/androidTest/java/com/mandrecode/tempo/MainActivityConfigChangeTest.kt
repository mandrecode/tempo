package com.mandrecode.tempo

import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Rotating, folding or resizing the window must not recreate the activity: a new activity starts
 * every animation at its end value, so the layout would snap to its new shape instead of gliding
 * there (#163).
 */
@RunWith(AndroidJUnit4::class)
class MainActivityConfigChangeTest {
    @Test
    fun sizeAndOrientationChanges_areHandledInPlace() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val info =
            context.packageManager.getActivityInfo(
                android.content.ComponentName(context, MainActivity::class.java),
                PackageManager.GET_META_DATA,
            )
        val handled =
            ActivityInfo.CONFIG_ORIENTATION or
                ActivityInfo.CONFIG_SCREEN_SIZE or
                ActivityInfo.CONFIG_SCREEN_LAYOUT or
                ActivityInfo.CONFIG_SMALLEST_SCREEN_SIZE
        assertThat(info.configChanges and handled).isEqualTo(handled)
    }

    @Test
    fun rotating_keepsTheSameActivity() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            var before: MainActivity? = null
            scenario.onActivity { before = it }

            scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            var after: MainActivity? = null
            scenario.onActivity {
                after = it
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }

            assertThat(after).isSameInstanceAs(before)
        }
    }
}
