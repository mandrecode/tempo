package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ActiveLiveActivityPreferencesTest {
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockContext: Context
    private lateinit var repository: ActiveLiveActivityPreferences

    @Before
    fun setup() {
        mockEditor =
            mockk<SharedPreferences.Editor>(relaxed = true) {
                every { putStringSet(any(), any()) } returns this
                every { apply() } just Runs
            }
        mockPrefs =
            mockk {
                every { edit() } returns mockEditor
                every { getStringSet(any(), any()) } returns emptySet()
            }
        mockContext =
            mockk {
                every { getSharedPreferences(any(), any()) } returns mockPrefs
            }
        repository = ActiveLiveActivityPreferencesImpl(mockContext)
    }

    @Test
    fun `getActiveChainIds returns empty set by default`() {
        assertThat(repository.getActiveChainIds()).isEmpty()
    }

    @Test
    fun `addActiveChainId persists chain id alongside existing ones`() {
        every { mockPrefs.getStringSet(any(), any()) } returns setOf("1")

        repository.addActiveChainId(2L)

        verify { mockEditor.putStringSet("active_chain_ids", setOf("1", "2")) }
    }

    @Test
    fun `removeActiveChainId persists set without the removed chain id`() {
        every { mockPrefs.getStringSet(any(), any()) } returns setOf("1", "2")

        repository.removeActiveChainId(1L)

        verify { mockEditor.putStringSet("active_chain_ids", setOf("2")) }
    }

    @Test
    fun `getActiveChainIds parses persisted string set as longs`() {
        every { mockPrefs.getStringSet(any(), any()) } returns setOf("1", "2", "3")

        assertThat(repository.getActiveChainIds()).containsExactly(1L, 2L, 3L)
    }
}
