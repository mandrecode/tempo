package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.tasks.presentation.model.SortOption
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class TasksScreenPreferencesRepositoryTest {
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockContext: Context
    private lateinit var repository: TasksScreenPreferencesRepository

    @Before
    fun setup() {
        mockEditor =
            mockk<SharedPreferences.Editor>(relaxed = true) {
                every { putString(any(), any()) } returns this
                every { putBoolean(any(), any()) } returns this
                every { putLong(any(), any()) } returns this
                every { putStringSet(any(), any()) } returns this
                every { apply() } just Runs
            }
        mockPrefs =
            mockk {
                every { edit() } returns mockEditor
                every { getString(any(), any()) } returns SortOption.MANUAL.name
                every { getBoolean(any(), any()) } returns true
                every { getLong(any(), any()) } returns -1L
                every { getStringSet(any(), any()) } returns emptySet()
            }
        mockContext =
            mockk {
                every { getSharedPreferences(any(), any()) } returns mockPrefs
            }
        repository = TasksScreenPreferencesRepositoryImpl(mockContext)
    }

    @Test
    fun `default sort option is MANUAL`() {
        every { mockPrefs.getString(any(), any()) } returns null
        assertThat(repository.getSortOption(1L)).isEqualTo(SortOption.MANUAL)
    }

    @Test
    fun `setSortOption persists value for given category`() {
        repository.setSortOption(1L, SortOption.BY_DATE)
        verify { mockEditor.putString("sort_option_1", "BY_DATE") }
        verify { mockEditor.apply() }
    }

    @Test
    fun `getSortOption reads per-category key`() {
        every { mockPrefs.getString("sort_option_42", any()) } returns "BY_PRIORITY"
        assertThat(repository.getSortOption(42L)).isEqualTo(SortOption.BY_PRIORITY)
    }

    @Test
    fun `getSortOption returns MANUAL for invalid stored value`() {
        every { mockPrefs.getString(any(), any()) } returns "INVALID"
        assertThat(repository.getSortOption(1L)).isEqualTo(SortOption.MANUAL)
    }

    @Test
    fun `getSortOption returns MANUAL for null stored value`() {
        every { mockPrefs.getString(any(), any()) } returns null
        assertThat(repository.getSortOption(1L)).isEqualTo(SortOption.MANUAL)
    }

    @Test
    fun `default showCompletedTasks is true`() {
        val freshPrefs =
            mockk<SharedPreferences> {
                every { edit() } returns mockEditor
                every { getBoolean("show_completed_tasks", true) } answers { secondArg() }
            }
        val freshContext =
            mockk<Context> {
                every { getSharedPreferences(any(), any()) } returns freshPrefs
            }
        val freshRepo = TasksScreenPreferencesRepositoryImpl(freshContext)
        assertThat(freshRepo.getShowCompletedTasks()).isTrue()
    }

    @Test
    fun `setShowCompletedTasks persists value`() {
        repository.setShowCompletedTasks(false)
        verify { mockEditor.putBoolean("show_completed_tasks", false) }
        verify { mockEditor.apply() }
    }

    @Test
    fun `getShowCompletedTasks reads persisted value`() {
        every { mockPrefs.getBoolean("show_completed_tasks", true) } returns false
        assertThat(repository.getShowCompletedTasks()).isFalse()
    }

    @Test
    fun `different categories have independent sort options`() {
        every { mockPrefs.getString("sort_option_1", any()) } returns "BY_DATE"
        every { mockPrefs.getString("sort_option_2", any()) } returns "BY_TITLE"
        assertThat(repository.getSortOption(1L)).isEqualTo(SortOption.BY_DATE)
        assertThat(repository.getSortOption(2L)).isEqualTo(SortOption.BY_TITLE)
    }

    @Test
    fun `default selectedCategoryId is zero`() {
        every {
            mockPrefs.getLong("selected_category_id", 0L)
        } answers { secondArg() }
        assertThat(repository.getSelectedCategoryId()).isEqualTo(0L)
    }

    @Test
    fun `setSelectedCategoryId persists value`() {
        repository.setSelectedCategoryId(42L)
        verify { mockEditor.putLong("selected_category_id", 42L) }
        verify { mockEditor.apply() }
    }

    @Test
    fun `getSelectedCategoryId reads persisted value`() {
        every {
            mockPrefs.getLong("selected_category_id", 0L)
        } returns 5L
        assertThat(repository.getSelectedCategoryId()).isEqualTo(5L)
    }

    @Test
    fun `no section is collapsed by default`() {
        assertThat(repository.getCollapsedSectionIds()).isEmpty()
    }

    @Test
    fun `collapsing a section adds its id and expanding removes it`() {
        every { mockPrefs.getStringSet("collapsed_section_ids", any()) } returns setOf("3", "junk")

        repository.setSectionCollapsed(8L, collapsed = true)
        verify { mockEditor.putStringSet("collapsed_section_ids", setOf("3", "8")) }

        repository.setSectionCollapsed(3L, collapsed = false)
        verify { mockEditor.putStringSet("collapsed_section_ids", emptySet()) }
    }

    @Test
    fun `setting an unchanged collapse state writes nothing`() {
        every { mockPrefs.getStringSet("collapsed_section_ids", any()) } returns setOf("3")

        repository.setSectionCollapsed(3L, collapsed = true)
        repository.setSectionCollapsed(9L, collapsed = false)

        verify(exactly = 0) { mockEditor.putStringSet(any(), any()) }
    }

    @Test
    fun `collapsed ids skip unreadable entries`() {
        every { mockPrefs.getStringSet("collapsed_section_ids", any()) } returns setOf("3", "x")

        assertThat(repository.getCollapsedSectionIds()).containsExactly(3L)
    }

    @Test
    fun `pruning keeps only the collapsed ids of sections that still exist`() {
        every { mockPrefs.getStringSet("collapsed_section_ids", any()) } returns setOf("3", "8")

        repository.pruneCollapsedSections(existingIds = setOf(8L, 9L))

        verify { mockEditor.putStringSet("collapsed_section_ids", setOf("8")) }
    }

    @Test
    fun `pruning with nothing stale writes nothing`() {
        every { mockPrefs.getStringSet("collapsed_section_ids", any()) } returns setOf("3")

        repository.pruneCollapsedSections(existingIds = setOf(3L))

        verify(exactly = 0) { mockEditor.putStringSet(any(), any()) }
    }
}
