package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class WhatsNewPreferencesRepositoryTest {
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var preferences: SharedPreferences
    private lateinit var repository: WhatsNewPreferencesRepository

    @Before
    fun setup() {
        editor =
            mockk(relaxed = true) {
                every { putInt(any(), any()) } returns this
                every { commit() } returns true
            }
        preferences =
            mockk {
                every { getInt(any(), any()) } returns 0
                every { edit() } returns editor
            }
        val context =
            mockk<Context> {
                every { getSharedPreferences(any(), any()) } returns preferences
            }
        repository = WhatsNewPreferencesRepositoryImpl(context)
    }

    @Test
    fun givenNoStoredValue_whenCreated_thenLastSeenVersionCodeIsZero() {
        assertThat(repository.lastSeenVersionCode.value).isEqualTo(0)
    }

    @Test
    fun givenLowerLastSeenVersionCode_whenSetToHigherVersionCode_thenStateAndPreferenceAreUpdated() {
        repository.setLastSeenVersionCode(1_001_000)

        assertThat(repository.lastSeenVersionCode.value).isEqualTo(1_001_000)
        verify(exactly = 1) { editor.putInt("last_seen_version_code", 1_001_000) }
        verify(exactly = 1) { editor.commit() }
    }

    @Test
    fun givenLastSeenVersionCode_whenSetToSameOrLowerVersionCode_thenWriteIsSkipped() {
        repository.setLastSeenVersionCode(1_001_000)
        repository.setLastSeenVersionCode(1_001_000)
        repository.setLastSeenVersionCode(1_000_000)

        assertThat(repository.lastSeenVersionCode.value).isEqualTo(1_001_000)
        verify(exactly = 1) { editor.putInt("last_seen_version_code", 1_001_000) }
    }
}
