package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Test

class ActiveLiveActivityPreferencesTest {
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockContext: Context
    private lateinit var repository: ActiveLiveActivityPreferences

    private val date = LocalDate(2025, 6, 15)

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
    fun `getActiveChains returns empty map by default`() {
        assertThat(repository.getActiveChains()).isEmpty()
    }

    @Test
    fun `getActiveChainIds returns empty set by default`() {
        assertThat(repository.getActiveChainIds()).isEmpty()
    }

    @Test
    fun `setActiveChain persists chain id with its date alongside existing ones`() {
        every { mockPrefs.getStringSet(any(), any()) } returns setOf("1|2025-06-14")

        repository.setActiveChain(2L, date)

        verify {
            mockEditor.putStringSet("active_chain_ids", setOf("1|2025-06-14", "2|2025-06-15"))
        }
    }

    @Test
    fun `setActiveChain replaces the existing record for the same chain`() {
        every { mockPrefs.getStringSet(any(), any()) } returns setOf("1|2025-06-14")

        repository.setActiveChain(1L, date)

        verify { mockEditor.putStringSet("active_chain_ids", setOf("1|2025-06-15")) }
    }

    @Test
    fun `removeActiveChainId persists records without the removed chain id`() {
        every { mockPrefs.getStringSet(any(), any()) } returns
            setOf("1|2025-06-15", "2|2025-06-15")

        repository.removeActiveChainId(1L)

        verify { mockEditor.putStringSet("active_chain_ids", setOf("2|2025-06-15")) }
    }

    @Test
    fun `getActiveChains parses persisted entries into chain ids and dates`() {
        every { mockPrefs.getStringSet(any(), any()) } returns
            setOf("1|2025-06-15", "2|2025-06-14")

        assertThat(repository.getActiveChains())
            .containsExactly(1L, LocalDate(2025, 6, 15), 2L, LocalDate(2025, 6, 14))
    }

    @Test
    fun `getActiveChainIds derives ids from persisted entries`() {
        every { mockPrefs.getStringSet(any(), any()) } returns
            setOf("1|2025-06-15", "2|2025-06-14", "3|2025-06-13")

        assertThat(repository.getActiveChainIds()).containsExactly(1L, 2L, 3L)
    }

    @Test
    fun `getActiveChains drops undated legacy entries`() {
        // Records written before date-scoping (#254) carry no date and must not be treated
        // as active, otherwise the pre-fix resync loop survives the upgrade.
        every { mockPrefs.getStringSet(any(), any()) } returns setOf("1", "2", "3|2025-06-15")

        assertThat(repository.getActiveChains()).containsExactly(3L, date)
    }

    @Test
    fun `getActiveChains drops entries with an unparseable date`() {
        every { mockPrefs.getStringSet(any(), any()) } returns setOf("1|not-a-date")

        assertThat(repository.getActiveChains()).isEmpty()
    }

    @Test
    fun `getActiveChains drops entries with an unparseable chain id`() {
        every { mockPrefs.getStringSet(any(), any()) } returns setOf("abc|2025-06-15")

        assertThat(repository.getActiveChains()).isEmpty()
    }
}
