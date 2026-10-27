package com.mandrecode.tempo.features.tasks.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.util.ValidationUtils
import com.mandrecode.tempo.features.tasks.domain.model.SectionDeletionSnapshot
import com.mandrecode.tempo.features.tasks.domain.model.TaskSection
import com.mandrecode.tempo.features.tasks.domain.repository.TaskSectionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SectionUseCasesTest {
    private lateinit var repository: TaskSectionRepository

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
    }

    @Test
    fun `create trims the name and appends the section`() =
        runTest {
            coEvery { repository.createSection(3L, "Garden", null) } returns 12L

            val result = CreateTaskSectionUseCase(repository)(3L, "  Garden ")

            assertThat(result).isEqualTo(CreateTaskSectionUseCase.Result.Success(12L))
        }

    @Test
    fun `create rejects a blank name`() =
        runTest {
            val result = CreateTaskSectionUseCase(repository)(3L, "   ")

            assertThat(result).isEqualTo(CreateTaskSectionUseCase.Result.EmptyName)
            coVerify(exactly = 0) { repository.createSection(any(), any(), any()) }
        }

    @Test
    fun `create rejects an over-long name`() =
        runTest {
            val name = "x".repeat(ValidationUtils.MAX_CATEGORY_NAME_LENGTH + 1)

            val result = CreateTaskSectionUseCase(repository)(3L, name)

            assertThat(result).isEqualTo(CreateTaskSectionUseCase.Result.TooLong)
        }

    @Test
    fun `rename trims and saves, and ignores a blank name`() =
        runTest {
            val rename = RenameTaskSectionUseCase(repository)

            coEvery { repository.getSectionById(8L) } returns TaskSection(8L, 3L, "Shopping", 1)
            coEvery { repository.getSectionsByCategory(3L) } returns listOf(TaskSection(8L, 3L, "Shopping", 1))

            assertThat(rename(8L, " Shopping list ")).isEqualTo(RenameTaskSectionUseCase.Result.Success)
            assertThat(rename(8L, " ")).isEqualTo(RenameTaskSectionUseCase.Result.EmptyName)

            coVerify(exactly = 1) { repository.renameSection(8L, "Shopping list") }
        }

    @Test
    fun `rename refuses a name another section of the category has, ignoring case`() =
        runTest {
            coEvery { repository.getSectionById(8L) } returns TaskSection(8L, 3L, "Shopping", 1)
            coEvery { repository.getSectionsByCategory(3L) } returns
                listOf(TaskSection(7L, 3L, "Cat", 0), TaskSection(8L, 3L, "Shopping", 1))

            val clash = RenameTaskSectionUseCase(repository)(8L, " cat ")
            val sameName = RenameTaskSectionUseCase(repository)(8L, "SHOPPING")

            assertThat(clash).isEqualTo(RenameTaskSectionUseCase.Result.AlreadyExists("cat"))
            assertThat(sameName).isEqualTo(RenameTaskSectionUseCase.Result.Success)
            coVerify(exactly = 0) { repository.renameSection(8L, "cat") }
        }

    @Test
    fun `create refuses a name the category already has, ignoring case`() =
        runTest {
            coEvery { repository.getSectionsByCategory(3L) } returns listOf(TaskSection(7L, 3L, "Garden", 0))

            val result = CreateTaskSectionUseCase(repository)(3L, " garden ")

            assertThat(result).isEqualTo(CreateTaskSectionUseCase.Result.AlreadyExists("garden"))
            coVerify(exactly = 0) { repository.createSection(any(), any(), any()) }
        }

    @Test
    fun `create passes the section to place it after`() =
        runTest {
            coEvery { repository.createSection(3L, "Garden", 7L) } returns 12L

            val result = CreateTaskSectionUseCase(repository)(3L, "Garden", afterSectionId = 7L)

            assertThat(result).isEqualTo(CreateTaskSectionUseCase.Result.Success(12L))
        }

    @Test
    fun `reorder moves one section and renumbers all of them`() =
        runTest {
            val sections =
                listOf(
                    TaskSection(id = 1L, categoryId = 3L, name = "Cat", sortOrder = 0),
                    TaskSection(id = 2L, categoryId = 3L, name = "Home chores", sortOrder = 1),
                    TaskSection(id = 3L, categoryId = 3L, name = "Shopping", sortOrder = 2),
                )

            ReorderTaskSectionsUseCase(repository)(fromIndex = 2, toIndex = 1, sections = sections)

            coVerify { repository.updateSortOrders(listOf(1L, 3L, 2L)) }
        }

    @Test
    fun `reorder ignores out-of-range or no-op moves`() =
        runTest {
            val sections = listOf(TaskSection(id = 1L, categoryId = 3L, name = "Cat"))
            val reorder = ReorderTaskSectionsUseCase(repository)

            reorder(0, 0, sections)
            reorder(0, 4, sections)

            coVerify(exactly = 0) { repository.updateSortOrders(any()) }
        }

    @Test
    fun `delete returns the snapshot and restore hands it back`() =
        runTest {
            val snapshot =
                SectionDeletionSnapshot(TaskSection(id = 8L, categoryId = 3L, name = "Shopping"), listOf(20L, 21L))
            coEvery { repository.deleteSectionWithSnapshot(8L) } returns snapshot

            val result = DeleteTaskSectionUseCase(repository)(8L)
            RestoreDeletedTaskSectionUseCase(repository)(result!!)

            coVerify { repository.restoreDeletedSection(snapshot) }
        }

    @Test
    fun `move clamps a negative index and reports the repository's verdict`() =
        runTest {
            coEvery { repository.moveTaskToSection(20L, 8L, 0) } returns true
            coEvery { repository.moveTaskToSection(20L, 99L, 0) } returns false
            val move = MoveTaskToSectionUseCase(repository)

            assertThat(move(20L, 8L, -3)).isTrue()
            assertThat(move(20L, 99L, 0)).isFalse()
        }
}
