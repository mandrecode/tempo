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
                every { putString(any(), any()) } returns this
                every { commit() } returns true
            }
        preferences =
            mockk {
                every { getString(any(), any()) } returns null
                every { edit() } returns editor
            }
        val context =
            mockk<Context> {
                every { getSharedPreferences(any(), any()) } returns preferences
            }
        repository = WhatsNewPreferencesRepositoryImpl(context)
    }

    @Test
    fun givenNoStoredValue_whenCreated_thenLastSeenEntryIdIsNull() {
        assertThat(repository.lastSeenEntryId.value).isNull()
    }

    @Test
    fun givenNoStoredValue_whenSetToNewEntryId_thenStateAndPreferenceAreUpdated() {
        repository.setLastSeenEntryId("encryption-at-rest")

        assertThat(repository.lastSeenEntryId.value).isEqualTo("encryption-at-rest")
        verify(exactly = 1) { editor.putString("last_seen_entry_id", "encryption-at-rest") }
        verify(exactly = 1) { editor.commit() }
    }

    @Test
    fun givenLastSeenEntryId_whenSetToSameEntryId_thenWriteIsSkipped() {
        repository.setLastSeenEntryId("encryption-at-rest")
        repository.setLastSeenEntryId("encryption-at-rest")

        assertThat(repository.lastSeenEntryId.value).isEqualTo("encryption-at-rest")
        verify(exactly = 1) { editor.putString("last_seen_entry_id", "encryption-at-rest") }
    }

    @Test
    fun givenLastSeenEntryId_whenSetToDifferentEntryId_thenStateAndPreferenceAreUpdated() {
        repository.setLastSeenEntryId("encryption-at-rest")
        repository.setLastSeenEntryId("chain-completion-checkbox")

        assertThat(repository.lastSeenEntryId.value).isEqualTo("chain-completion-checkbox")
        verify(exactly = 1) { editor.putString("last_seen_entry_id", "chain-completion-checkbox") }
    }
}
