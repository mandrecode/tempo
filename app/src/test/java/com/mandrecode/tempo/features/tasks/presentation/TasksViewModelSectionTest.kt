package com.mandrecode.tempo.features.tasks.presentation

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.data.preferences.TasksScreenPreferencesRepository
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.SectionDeletionSnapshot
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.model.TaskSection
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import com.mandrecode.tempo.features.tasks.domain.repository.TaskAttachmentRepository
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import com.mandrecode.tempo.features.tasks.domain.usecase.CreateTaskSectionUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.CreateTaskUseCase
import com.mandrecode.tempo.features.tasks.domain.usecase.TaskSectionUseCases
import com.mandrecode.tempo.features.tasks.presentation.model.SortOption
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Before
import org.junit.Test

/** The Tasks screen's handling of sections inside the selected category (#61). */
@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModelSectionTest {
    private val testDispatcher = StandardTestDispatcher()
    private val taskRepository = mockk<TaskRepository>(relaxed = true)
    private val categoryRepository = mockk<CategoryRepository>(relaxed = true)
    private val createTaskUseCase = mockk<CreateTaskUseCase>(relaxed = true)
    private val preferences = mockk<TasksScreenPreferencesRepository>(relaxed = true)
    private val attachments = mockk<TaskAttachmentRepository>(relaxed = true)
    private val sectionUseCases = mockk<TaskSectionUseCases>(relaxed = true)
    private val tasks = MutableStateFlow<List<Task>>(emptyList())
    private val sections = MutableStateFlow<List<TaskSection>>(emptyList())

    private val sv = Category(id = 3L, name = "S&V", isDefault = true, sectionsEnabled = true)
    private val work = Category(id = 4L, name = "Work")
    private val cat = TaskSection(id = 10L, categoryId = 3L, name = "Cat", sortOrder = 0)
    private val shopping = TaskSection(id = 11L, categoryId = 3L, name = "Shopping", sortOrder = 1)
    private val garden = TaskSection(id = 12L, categoryId = 3L, name = "Garden", sortOrder = 2)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { taskRepository.getAllTasks() } returns tasks
        every { categoryRepository.getAllCategories() } returns flowOf(listOf(sv, work))
        every { sectionUseCases.repository.getAllSections() } returns sections
        every { attachments.observeAttachments(any()) } returns flowOf(emptyList())
        every { preferences.getSelectedCategoryId() } returns sv.id
        every { preferences.getSortOption(any()) } returns SortOption.MANUAL
        every { preferences.getShowCompletedTasks() } returns true
        every { preferences.getCollapsedSectionIds() } returns emptySet()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        TasksViewModel(
            taskRepository,
            categoryRepository,
            createTaskUseCase,
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            mockk(relaxed = true),
            preferences,
            testDispatcher,
            mockk(relaxed = true),
            mockk(relaxed = true),
            attachments,
            sectionUseCases,
            kotlin.time.Clock.System,
        )

    private fun task(
        id: Long,
        sectionId: Long? = null,
        categoryId: Long = sv.id,
        title: String = "Task $id",
        sortOrder: Int = id.toInt(),
        isCompleted: Boolean = false,
        reminderDate: LocalDateTime? = null,
        priority: Priority? = null,
    ) = Task(
        id = id,
        title = title,
        description = "",
        categoryId = categoryId,
        sectionId = sectionId,
        sortOrder = sortOrder,
        isCompleted = isCompleted,
        reminderDate = reminderDate,
        priority = priority,
    )

    @Test
    fun `a category without sections keeps the existing list`() =
        runTest {
            every { categoryRepository.getAllCategories() } returns flowOf(listOf(sv.copy(sectionsEnabled = false)))
            tasks.value = listOf(task(1))
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.sectionedList).isNull()
            assertThat(
                viewModel.uiState.value.activeTasks.values
                    .flatten()
                    .map { it.id },
            ).containsExactly(1L)
        }

    @Test
    fun `sectioned list puts unsectioned first and keeps every section in order, empty ones too`() =
        runTest {
            sections.value = listOf(cat, shopping, garden)
            tasks.value =
                listOf(
                    task(1),
                    task(2, sectionId = cat.id),
                    task(3, sectionId = shopping.id),
                    task(4, sectionId = cat.id),
                    task(5, sectionId = 999L),
                    task(6, sectionId = cat.id, isCompleted = true),
                    task(7, categoryId = work.id),
                )
            val viewModel = createViewModel()
            advanceUntilIdle()

            val list = viewModel.uiState.value.sectionedList!!
            assertThat(list.unsectioned.map { it.id }).containsExactly(1L, 5L).inOrder()
            assertThat(list.sections.map { it.section.name }).containsExactly("Cat", "Shopping", "Garden").inOrder()
            assertThat(list.sections[0].tasks.map { it.id }).containsExactly(2L, 4L).inOrder()
            assertThat(list.sections[1].tasks.map { it.id }).containsExactly(3L)
            assertThat(list.sections[2].tasks).isEmpty()
            // Completed tasks stay in the single Completed block.
            assertThat(
                viewModel.uiState.value.completedTaskGroups.values
                    .flatten()
                    .map { it.id },
            ).containsExactly(6L)
        }

    @Test
    fun `tasks inside a section follow the selected sort without group headers`() =
        runTest {
            every { preferences.getSortOption(any()) } returns SortOption.BY_PRIORITY
            sections.value = listOf(cat)
            tasks.value =
                listOf(
                    task(1, sectionId = cat.id, priority = Priority.LOW),
                    task(2, sectionId = cat.id, priority = Priority.HIGH),
                )
            val viewModel = createViewModel()
            advanceUntilIdle()

            val catTasks =
                viewModel.uiState.value.sectionedList!!
                    .sections
                    .single()
                    .tasks
            assertThat(catTasks.map { it.id }).containsExactly(2L, 1L).inOrder()
        }

    @Test
    fun `a section counts its overdue tasks`() =
        runTest {
            sections.value = listOf(cat)
            tasks.value =
                listOf(
                    task(1, sectionId = cat.id, reminderDate = LocalDateTime(2001, 1, 1, 9, 0)),
                    task(2, sectionId = cat.id, reminderDate = LocalDateTime(2999, 1, 1, 9, 0)),
                    task(3, sectionId = cat.id),
                )
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertThat(
                viewModel.uiState.value.sectionedList!!
                    .sections
                    .single()
                    .overdueCount,
            ).isEqualTo(1)
        }

    @Test
    fun `manual sort makes each section its own reorderable run`() =
        runTest {
            sections.value = listOf(cat, shopping)
            tasks.value =
                listOf(
                    task(1, sectionId = cat.id),
                    task(2, sectionId = cat.id),
                    task(3, sectionId = shopping.id),
                    task(4, sectionId = shopping.id),
                )
            val viewModel = createViewModel()
            advanceUntilIdle()

            val runs = viewModel.uiState.value.reorderableRuns
            assertThat(runs.getValue(1L).tasks.map { it.id }).containsExactly(1L, 2L).inOrder()
            assertThat(runs.getValue(3L).tasks.map { it.id }).containsExactly(3L, 4L).inOrder()
        }

    @Test
    fun `collapse state is read at start and toggling it is persisted`() =
        runTest {
            every { preferences.getCollapsedSectionIds() } returns setOf(cat.id)
            val viewModel = createViewModel()

            assertThat(viewModel.uiState.value.collapsedSectionIds).containsExactly(cat.id)

            viewModel.onEvent(TasksContract.UiEvent.ToggleSectionCollapsed(cat.id))
            viewModel.onEvent(TasksContract.UiEvent.ToggleSectionCollapsed(shopping.id))

            assertThat(viewModel.uiState.value.collapsedSectionIds).containsExactly(shopping.id)
            verify { preferences.setSectionCollapsed(cat.id, false) }
            verify { preferences.setSectionCollapsed(shopping.id, true) }
        }

    @Test
    fun `adding a blank section explains why`() =
        runTest {
            coEvery { sectionUseCases.create(any(), any(), any()) } returns CreateTaskSectionUseCase.Result.EmptyName
            val viewModel = createViewModel()
            val effects = mutableListOf<TasksContract.UiEffect>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiEffect.toList(effects) }
            advanceUntilIdle()

            viewModel.onEvent(TasksContract.UiEvent.AddSection(" "))
            advanceUntilIdle()

            coVerify { sectionUseCases.create(sv.id, " ", null) }
            assertThat((effects.single() as TasksContract.UiEffect.ShowSnackbar).messageResId)
                .isEqualTo(R.string.msg_section_name_empty)
        }

    @Test
    fun `deleting a section offers undo, which restores it and its collapse state`() =
        runTest {
            every { preferences.getCollapsedSectionIds() } returns setOf(shopping.id)
            sections.value = listOf(cat, shopping)
            val snapshot = SectionDeletionSnapshot(shopping, listOf(3L))
            coEvery { sectionUseCases.delete(shopping.id) } returns snapshot
            val viewModel = createViewModel()
            val effects = mutableListOf<TasksContract.UiEffect>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiEffect.toList(effects) }
            advanceUntilIdle()

            viewModel.onEvent(TasksContract.UiEvent.DeleteSection(shopping))
            advanceUntilIdle()

            val snackbar = effects.single() as TasksContract.UiEffect.ShowSnackbar
            assertThat(snackbar.boldSegment?.word).isEqualTo("Shopping")
            assertThat(viewModel.uiState.value.collapsedSectionIds).doesNotContain(shopping.id)

            viewModel.onEvent(TasksContract.UiEvent.UndoDeletion(snackbar.deletionToken!!))
            advanceUntilIdle()

            coVerify { sectionUseCases.restore(snapshot) }
            assertThat(viewModel.uiState.value.collapsedSectionIds).contains(shopping.id)
        }

    @Test
    fun `moving a task goes through the use case`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.onEvent(TasksContract.UiEvent.MoveTaskToSection(taskId = 3L, sectionId = cat.id, index = 1))
            advanceUntilIdle()

            coVerify { sectionUseCases.moveTask(3L, cat.id, 1) }
        }

    @Test
    fun `a task added from a section header is saved into that section`() =
        runTest {
            sections.value = listOf(cat)
            val created = slot<Task>()
            coEvery { createTaskUseCase.invoke(capture(created)) } returns mockk(relaxed = true)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(TasksContract.UiEvent.ShowTaskDialog(sectionId = cat.id))
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.taskForm.sectionId).isEqualTo(cat.id)

            viewModel.onEvent(TasksContract.UiEvent.CreateOrUpdateTask("Buy litter", "", sv.id))
            advanceUntilIdle()

            assertThat(created.captured.sectionId).isEqualTo(cat.id)
        }

    @Test
    fun `a section of another category never travels with the saved task`() =
        runTest {
            sections.value = listOf(cat)
            val created = slot<Task>()
            coEvery { createTaskUseCase.invoke(capture(created)) } returns mockk(relaxed = true)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(TasksContract.UiEvent.ShowTaskDialog(sectionId = cat.id))
            advanceUntilIdle()
            viewModel.onEvent(TasksContract.UiEvent.CreateOrUpdateTask("Standup", "", work.id))
            advanceUntilIdle()

            assertThat(created.captured.sectionId).isNull()
        }

    @Test
    fun `a category with sections turned off hides them and keeps the existing list`() =
        runTest {
            every { categoryRepository.getAllCategories() } returns flowOf(listOf(sv.copy(sectionsEnabled = false)))
            sections.value = listOf(cat)
            tasks.value = listOf(task(1), task(2, sectionId = cat.id))
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.sectionedList).isNull()
            assertThat(
                viewModel.uiState.value.activeTasks.values
                    .flatten()
                    .map { it.id },
            ).containsExactly(1L, 2L)
            // Hidden, not removed: the section is still there for when sections come back on.
            assertThat(viewModel.uiState.value.sectionsByCategory[sv.id]).containsExactly(cat)
        }

    @Test
    fun `a category with sections on but none yet lists its tasks unsectioned`() =
        runTest {
            tasks.value = listOf(task(1))
            val viewModel = createViewModel()
            advanceUntilIdle()

            val list = viewModel.uiState.value.sectionedList!!
            assertThat(list.unsectioned.map { it.id }).containsExactly(1L)
            assertThat(list.sections).isEmpty()
        }

    @Test
    fun `collapsed ids of sections that no longer exist are dropped on the first load`() =
        runTest {
            every { preferences.getCollapsedSectionIds() } returns setOf(cat.id, 99L)
            sections.value = listOf(cat)
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.collapsedSectionIds).containsExactly(cat.id)
            verify { preferences.pruneCollapsedSections(setOf(cat.id)) }
        }
}
