package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.tasks.domain.repository.CompletedTaskRetentionPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class CompletedTaskRetentionPreferencesTest {
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
                every { getBoolean(any(), any()) } returns false
                every { getInt(any(), any()) } returns CompletedTaskRetentionPreferences.DEFAULT_RETENTION_DAYS
                every { edit() } returns editor
            }
    }

    @Test
    fun `defaults to disabled with default retention`() {
        val preferences = createPreferences()

        assertThat(preferences.isEnabled.value).isEqualTo(CompletedTaskRetentionPreferences.DEFAULT_ENABLED)
        assertThat(preferences.retentionDays.value)
            .isEqualTo(CompletedTaskRetentionPreferences.DEFAULT_RETENTION_DAYS)
        verify {
            sharedPreferences.getBoolean(
                "enabled",
                CompletedTaskRetentionPreferences.DEFAULT_ENABLED,
            )
            sharedPreferences.getInt(
                "retention_days",
                CompletedTaskRetentionPreferences.DEFAULT_RETENTION_DAYS,
            )
        }
    }

    @Test
    fun `setters persist values and update flows`() {
        val preferences = createPreferences()

        preferences.setEnabled(true)
        preferences.setRetentionDays(45)

        assertThat(preferences.isEnabled.value).isTrue()
        assertThat(preferences.retentionDays.value).isEqualTo(45)
        verify { editor.putBoolean("enabled", true) }
        verify { editor.putInt("retention_days", 45) }
    }

    @Test
    fun `stored and saved retention values are clamped`() {
        every { sharedPreferences.getInt(any(), any()) } returns 500
        val preferences = createPreferences()

        assertThat(preferences.retentionDays.value).isEqualTo(365)

        preferences.setRetentionDays(0)
        assertThat(preferences.retentionDays.value).isEqualTo(1)
        verify { editor.putInt("retention_days", 1) }
    }

    @Test
    fun `unsupported retention value uses nearest supported option`() {
        every { sharedPreferences.getInt(any(), any()) } returns 31

        val preferences = createPreferences()

        assertThat(preferences.retentionDays.value).isEqualTo(30)
    }

    private fun createPreferences(): CompletedTaskRetentionPreferencesImpl {
        val context =
            mockk<Context> {
                every { getSharedPreferences(any(), any()) } returns sharedPreferences
            }
        return CompletedTaskRetentionPreferencesImpl(context)
    }
}
