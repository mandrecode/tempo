package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.tasks.domain.repository.MissedReminderPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.datetime.LocalTime
import org.junit.Before
import org.junit.Test

class MissedReminderPreferencesTest {
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setUp() {
        editor =
            mockk(relaxed = true) {
                every { putBoolean(any(), any()) } returns this
                every { putInt(any(), any()) } returns this
            }
        sharedPreferences =
            mockk {
                every { getBoolean(any(), any()) } returns MissedReminderPreferences.DEFAULT_ENABLED
                every { getInt(any(), any()) } returns DEFAULT_MINUTE_OF_DAY
                every { edit() } returns editor
            }
    }

    @Test
    fun `defaults to enabled at 9am`() {
        val preferences = createPreferences()

        assertThat(preferences.isEnabled.value).isTrue()
        assertThat(preferences.catchUpTime.value).isEqualTo(MissedReminderPreferences.DEFAULT_CATCH_UP_TIME)
        verify {
            sharedPreferences.getBoolean("enabled", MissedReminderPreferences.DEFAULT_ENABLED)
            sharedPreferences.getInt("catch_up_minute_of_day", DEFAULT_MINUTE_OF_DAY)
        }
    }

    @Test
    fun `setters persist values and update flows`() {
        val preferences = createPreferences()

        preferences.setEnabled(false)
        preferences.setCatchUpTime(LocalTime(hour = 7, minute = 30))

        assertThat(preferences.isEnabled.value).isFalse()
        assertThat(preferences.catchUpTime.value).isEqualTo(LocalTime(hour = 7, minute = 30))
        verify { editor.putBoolean("enabled", false) }
        verify { editor.putInt("catch_up_minute_of_day", 7 * 60 + 30) }
    }

    @Test
    fun `stored time round-trips through minute of day`() {
        every { sharedPreferences.getInt(any(), any()) } returns 23 * 60 + 45

        val preferences = createPreferences()

        assertThat(preferences.catchUpTime.value).isEqualTo(LocalTime(hour = 23, minute = 45))
    }

    @Test
    fun `stored minute of day beyond a day falls back to the default`() {
        every { sharedPreferences.getInt(any(), any()) } returns 24 * 60

        val preferences = createPreferences()

        assertThat(preferences.catchUpTime.value).isEqualTo(MissedReminderPreferences.DEFAULT_CATCH_UP_TIME)
    }

    @Test
    fun `negative stored minute of day falls back to the default`() {
        every { sharedPreferences.getInt(any(), any()) } returns -1

        val preferences = createPreferences()

        assertThat(preferences.catchUpTime.value).isEqualTo(MissedReminderPreferences.DEFAULT_CATCH_UP_TIME)
    }

    private fun createPreferences(): MissedReminderPreferencesImpl {
        val context =
            mockk<Context> {
                every { getSharedPreferences(any(), any()) } returns sharedPreferences
            }
        return MissedReminderPreferencesImpl(context)
    }

    private companion object {
        const val DEFAULT_MINUTE_OF_DAY = 9 * 60
    }
}
