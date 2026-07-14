package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class OnboardingPreferencesRepositoryTest {
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var preferences: SharedPreferences
    private lateinit var repository: OnboardingPreferencesRepository

    @Before
    fun setup() {
        editor =
            mockk(relaxed = true) {
                every { putBoolean(any(), any()) } returns this
                every { commit() } returns true
            }
        preferences =
            mockk {
                every { getBoolean(any(), any()) } returns false
                every { edit() } returns editor
            }
        val context =
            mockk<Context> {
                every { getSharedPreferences(any(), any()) } returns preferences
            }
        repository = OnboardingPreferencesRepositoryImpl(context)
    }

    @Test
    fun givenNoStoredValue_whenCreated_thenOnboardingIsIncomplete() {
        assertThat(repository.isCompleted.value).isFalse()
    }

    @Test
    fun givenIncompleteOnboarding_whenCompleted_thenStateAndPreferenceAreUpdated() {
        repository.setCompleted()

        assertThat(repository.isCompleted.value).isTrue()
        verify(exactly = 1) { editor.putBoolean("completed", true) }
        verify(exactly = 1) { editor.commit() }
    }

    @Test
    fun givenCompletedOnboarding_whenCompletedAgain_thenWriteIsIdempotent() {
        repository.setCompleted()
        repository.setCompleted()

        verify(exactly = 1) { editor.putBoolean("completed", true) }
    }
}
