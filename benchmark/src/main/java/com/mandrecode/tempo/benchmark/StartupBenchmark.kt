package com.mandrecode.tempo.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun coldFirstRunStartup() {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(StartupTimingMetric()),
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.COLD,
            iterations = 10,
            setupBlock = {
                resetAppState()
            },
        ) {
            startActivityAndWait()
        }
    }

    @Test
    fun coldFirstRunStartupFirstTabSwitch() {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(StartupTimingMetric(), FrameTimingMetric()),
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.COLD,
            iterations = 10,
            setupBlock = {
                resetAppState()
            },
        ) {
            startActivityAndWait()
            tapTasksTab()
        }
    }

    private fun MacrobenchmarkScope.resetAppState() {
        val result = device.executeShellCommand("pm clear $TARGET_PACKAGE")
        check(result.contains("Success")) {
            "Failed to clear $TARGET_PACKAGE before benchmark iteration: $result"
        }
        pressHome()
    }

    private fun MacrobenchmarkScope.tapTasksTab() {
        val displayWidth = device.displayWidth
        val displayHeight = device.displayHeight
        check(displayWidth > 0 && displayHeight > 0) {
            "Invalid display size: ${displayWidth}x$displayHeight"
        }
        device.click(
            displayWidth * TASKS_TAB_X_PERCENT / PERCENT_MAX,
            displayHeight * BOTTOM_NAV_Y_PERCENT / PERCENT_MAX,
        )
        device.waitForIdle()
    }

    private companion object {
        const val TARGET_PACKAGE = "com.mandrecode.tempo.debug"
        const val TASKS_TAB_X_PERCENT = 75
        const val BOTTOM_NAV_Y_PERCENT = 94
        const val PERCENT_MAX = 100
    }
}
