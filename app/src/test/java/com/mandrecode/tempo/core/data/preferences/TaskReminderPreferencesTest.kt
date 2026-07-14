package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.datetime.LocalTime
import org.junit.Before
import org.junit.Test

class TaskReminderPreferencesTest {
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setUp() {
        editor =
            mockk(relaxed = true) {
                every { putInt(any(), any()) } returns this
            }
        sharedPreferences =
            mockk {
                every { getInt(any(), any()) } answers { secondArg() }
                every { edit() } returns editor
            }
    }

    @Test
    fun `given no saved value when created then defaults to nine`() {
        val preferences = createPreferences()

        assertThat(preferences.defaultTime.value).isEqualTo(LocalTime(9, 0))
    }

    @Test
    fun `given selected time when saved then persists and updates flow`() {
        val preferences = createPreferences()

        preferences.setDefaultTime(LocalTime(14, 35))

        assertThat(preferences.defaultTime.value).isEqualTo(LocalTime(14, 35))
        verify { editor.putInt("default_hour", 14) }
        verify { editor.putInt("default_minute", 35) }
    }

    @Test
    fun `given invalid stored values when created then clamps to valid time`() {
        every { sharedPreferences.getInt("default_hour", any()) } returns 27
        every { sharedPreferences.getInt("default_minute", any()) } returns -4

        val preferences = createPreferences()

        assertThat(preferences.defaultTime.value).isEqualTo(LocalTime(23, 0))
    }

    private fun createPreferences(): TaskReminderPreferencesImpl {
        val context =
            mockk<Context> {
                every { getSharedPreferences(any(), any()) } returns sharedPreferences
            }
        return TaskReminderPreferencesImpl(context)
    }
}
