package com.mandrecode.tempo.features.focus.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import com.mandrecode.tempo.features.focus.domain.model.TaskFocusToday
import com.mandrecode.tempo.features.focus.domain.repository.FocusSessionRepository
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.repository.HabitChainRepository
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.Test

class GetFocusAgendaUseCaseTest {
    // A Wednesday.
    private val today = LocalDate(2026, 7, 29)

    private val taskRepository = mockk<TaskRepository>()
    private val habitRepository = mockk<HabitRepository>()
    private val habitChainRepository = mockk<HabitChainRepository>()
    private val categoryRepository = mockk<CategoryRepository>()
    private val focusTodayFlow = MutableStateFlow<Map<Long, TaskFocusToday>>(emptyMap())
    private val sessionRepository = mockk<FocusSessionRepository>()
    private val useCase =
        GetFocusAgendaUseCase(
            taskRepository = taskRepository,
            habitRepository = habitRepository,
            habitChainRepository = habitChainRepository,
            categoryRepository = categoryRepository,
            getUpNextItem = GetUpNextItemUseCase(),
            sessionRepository = sessionRepository,
        )

    private fun task(
        id: Long,
        date: LocalDate?,
        hour: Int = 9,
        isCompleted: Boolean = false,
        parentTaskId: Long? = null,
        sortOrder: Int = 0,
    ) = Task(
        id = id,
        title = "Task $id",
        description = "",
        isCompleted = isCompleted,
        parentTaskId = parentTaskId,
        sortOrder = sortOrder,
        reminderDate = date?.let { LocalDateTime(it, LocalTime(hour, 0)) },
    )

    private fun habit(
        id: Long,
        repeatDays: Set<DayOfWeek>? = null,
        completionHistory: String = "",
    ) = Habit(
        id = id,
        title = "Habit $id",
        description = "",
        createdDate = LocalDateTime(today, LocalTime(0, 0)),
        repeatDays = repeatDays,
        completionHistory = completionHistory,
    )

    private suspend fun agenda(
        tasks: List<Task> = emptyList(),
        habits: List<Habit> = emptyList(),
        chains: List<HabitChain> = emptyList(),
        assertions: (com.mandrecode.tempo.features.focus.domain.model.FocusAgenda) -> Unit,
    ) {
        every { sessionRepository.focusToday } returns focusTodayFlow
        every { taskRepository.getAllTasks() } returns flowOf(tasks)
        every { habitRepository.getAllHabits() } returns flowOf(habits)
        every { habitChainRepository.getAllHabitChains() } returns flowOf(chains)
        every { categoryRepository.getAllCategories() } returns flowOf(emptyList())

        useCase(today).test {
            assertions(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `future work is hidden`() =
        runTest {
            agenda(tasks = listOf(task(1, today.plus(1, DateTimeUnit.DAY)))) {
                assertThat(it.today).isEmpty()
                assertThat(it.overdue).isEmpty()
            }
        }

    @Test
    fun `undated tasks are excluded but counted`() =
        runTest {
            agenda(tasks = listOf(task(1, date = null), task(2, date = null))) {
                assertThat(it.today).isEmpty()
                assertThat(it.undatedTaskCount).isEqualTo(2)
            }
        }

    @Test
    fun `a completed undated task is not counted in the footer`() =
        runTest {
            agenda(tasks = listOf(task(1, date = null, isCompleted = true))) {
                assertThat(it.undatedTaskCount).isEqualTo(0)
            }
        }

    @Test
    fun `an open past-due task lands in overdue`() =
        runTest {
            agenda(tasks = listOf(task(1, today.minus(2, DateTimeUnit.DAY)))) {
                assertThat(it.overdue.map { entry -> entry.id }).containsExactly("task_1")
                assertThat(it.today).isEmpty()
            }
        }

    @Test
    fun `a completed past-due task appears nowhere`() =
        runTest {
            agenda(tasks = listOf(task(1, today.minus(2, DateTimeUnit.DAY), isCompleted = true))) {
                assertThat(it.overdue).isEmpty()
                assertThat(it.today).isEmpty()
            }
        }

    @Test
    fun `completed work due today still shows, so the day reads as done`() =
        runTest {
            agenda(tasks = listOf(task(1, today, isCompleted = true))) {
                assertThat(it.today).hasSize(1)
                assertThat(it.completedCount).isEqualTo(1)
            }
        }

    @Test
    fun `subtasks ride with their parent rather than as their own rows`() =
        runTest {
            agenda(
                tasks =
                    listOf(
                        task(1, today),
                        task(2, today, parentTaskId = 1),
                    ),
            ) {
                val entry = it.today.single() as FocusAgendaItem.TaskEntry
                assertThat(entry.subtasks).hasSize(1)
            }
        }

    @Test
    fun `subtasks are listed in their stored order, not the order the query returned them`() =
        runTest {
            agenda(
                tasks =
                    listOf(
                        task(1, today),
                        // As the task query hands them over: descending id, so last step first.
                        task(4, null, parentTaskId = 1, sortOrder = 2),
                        task(3, null, parentTaskId = 1, sortOrder = 1),
                        task(2, null, parentTaskId = 1, sortOrder = 0),
                    ),
            ) {
                val entry = it.today.single() as FocusAgendaItem.TaskEntry
                assertThat(entry.subtasks.map { subtask -> subtask.id })
                    .containsExactly(2L, 3L, 4L)
                    .inOrder()
            }
        }

    @Test
    fun `subtasks sharing a sort order fall back to id`() =
        runTest {
            agenda(
                tasks =
                    listOf(
                        task(1, today),
                        task(3, null, parentTaskId = 1),
                        task(2, null, parentTaskId = 1),
                    ),
            ) {
                val entry = it.today.single() as FocusAgendaItem.TaskEntry
                assertThat(entry.subtasks.map { subtask -> subtask.id }).containsExactly(2L, 3L).inOrder()
            }
        }

    @Test
    fun `a dated subtask under an undated parent stands as its own row`() =
        runTest {
            agenda(
                tasks =
                    listOf(
                        task(1, null),
                        task(2, today, parentTaskId = 1),
                    ),
            ) {
                assertThat(it.today.map { entry -> entry.id }).containsExactly("task_2")
                // And it is something a session can be started on, which is the whole point.
                assertThat(it.upNext.map { entry -> entry.id }).containsExactly("task_2")
            }
        }

    @Test
    fun `an overdue subtask under a future-dated parent stands as its own row`() =
        runTest {
            agenda(
                tasks =
                    listOf(
                        task(1, today.plus(7, DateTimeUnit.DAY)),
                        task(2, today.minus(1, DateTimeUnit.DAY), parentTaskId = 1),
                    ),
            ) {
                assertThat(it.overdue.map { entry -> entry.id }).containsExactly("task_2")
                assertThat(it.today).isEmpty()
            }
        }

    @Test
    fun `a promoted subtask carries its own steps`() =
        runTest {
            agenda(
                tasks =
                    listOf(
                        task(1, null),
                        task(2, today, parentTaskId = 1),
                        task(3, null, parentTaskId = 2),
                    ),
            ) {
                val entry = it.today.single() as FocusAgendaItem.TaskEntry
                assertThat(entry.task.id).isEqualTo(2L)
                assertThat(entry.subtasks.map { subtask -> subtask.id }).containsExactly(3L)
            }
        }

    @Test
    fun `an undated subtask is not counted in the undated footer`() =
        runTest {
            agenda(
                tasks =
                    listOf(
                        task(1, today),
                        task(2, null, parentTaskId = 1),
                        task(3, null),
                    ),
            ) {
                // A step with no time of its own belongs to its parent, not to the loose-ends list.
                assertThat(it.undatedTaskCount).isEqualTo(1)
            }
        }

    @Test
    fun `habits outside their repeat days are hidden`() =
        runTest {
            agenda(habits = listOf(habit(1, repeatDays = setOf(DayOfWeek.MONDAY)))) {
                assertThat(it.today).isEmpty()
            }
        }

    @Test
    fun `a habit completed today is marked complete`() =
        runTest {
            agenda(habits = listOf(habit(1, completionHistory = today.toString()))) {
                val entry = it.today.single() as FocusAgendaItem.HabitEntry
                assertThat(entry.isCompleted).isTrue()
            }
        }

    @Test
    fun `habits belonging to a chain are shown by the chain, not twice`() =
        runTest {
            val chain =
                HabitChain(
                    id = 1,
                    title = "Morning Routine",
                    habitIds = listOf(1),
                    createdDate = LocalDateTime(today, LocalTime(0, 0)),
                )

            agenda(habits = listOf(habit(1)), chains = listOf(chain)) {
                assertThat(it.today.single()).isInstanceOf(FocusAgendaItem.ChainEntry::class.java)
            }
        }

    @Test
    fun `a chain's habits keep the chain's own order, not the habit list's`() =
        runTest {
            // The chain runs 3, 1, 2 — the order its editor was left in. The habits arrive in id
            // order, which is what the screen used to show instead.
            val chain =
                HabitChain(
                    id = 1,
                    title = "Morning Routine",
                    habitIds = listOf(3, 1, 2),
                    createdDate = LocalDateTime(today, LocalTime(0, 0)),
                )

            agenda(habits = listOf(habit(1), habit(2), habit(3)), chains = listOf(chain)) {
                val entry = it.today.single() as FocusAgendaItem.ChainEntry
                assertThat(entry.habits.map(Habit::id)).containsExactly(3L, 1L, 2L).inOrder()
            }
        }

    @Test
    fun `a chain naming a habit that no longer exists just drops it`() =
        runTest {
            val chain =
                HabitChain(
                    id = 1,
                    title = "Morning Routine",
                    habitIds = listOf(3, 99, 1),
                    createdDate = LocalDateTime(today, LocalTime(0, 0)),
                )

            agenda(habits = listOf(habit(1), habit(3)), chains = listOf(chain)) {
                val entry = it.today.single() as FocusAgendaItem.ChainEntry
                assertThat(entry.habits.map(Habit::id)).containsExactly(3L, 1L).inOrder()
            }
        }

    @Test
    fun `timed work sorts before untimed, and completed work sinks last`() =
        runTest {
            agenda(
                tasks =
                    listOf(
                        task(1, today, hour = 15),
                        task(2, today, hour = 8),
                        task(3, today, hour = 6, isCompleted = true),
                    ),
                habits = listOf(habit(4)),
            ) {
                // The section keeps every item; the row is a separate view onto the same day.
                val ids = it.today.map { entry -> entry.id }
                assertThat(ids).containsExactly("task_2", "task_1", "habit_4", "task_3").inOrder()
            }
        }

    @Test
    fun `a task carries what it has had out of today`() =
        runTest {
            focusTodayFlow.value = mapOf(1L to TaskFocusToday(sessions = 2, minutes = 50))

            agenda(tasks = listOf(task(1, today), task(2, today))) {
                val entries = it.today.filterIsInstance<FocusAgendaItem.TaskEntry>()
                val worked = entries.first { e -> e.task.id == 1L }
                assertThat(worked.sessionsToday).isEqualTo(2)
                assertThat(worked.minutesToday).isEqualTo(50)
                // Untouched work says nothing, rather than saying zero.
                assertThat(entries.first { e -> e.task.id == 2L }.focusToday.hasHistory).isFalse()
            }
        }

    @Test
    fun `up next draws from the day's sections without emptying them`() =
        runTest {
            agenda(tasks = listOf(task(1, today, hour = 9))) {
                assertThat(it.upNext.map { entry -> entry.id }).containsExactly("task_1")
                assertThat(it.today.map { entry -> entry.id }).contains("task_1")
            }
        }

    @Test
    fun `the row shortlists the day without inflating its count`() =
        runTest {
            agenda(tasks = listOf(task(1, today, hour = 9), task(2, today, hour = 10))) {
                assertThat(it.upNext).hasSize(2)
                assertThat(it.today).hasSize(2)
                // Counted once, from the sections — the row is a view, not a fourth section.
                assertThat(it.scheduledCount).isEqualTo(2)
            }
        }

    @Test
    fun `today's work leads up next, ahead of anything left over from before`() =
        runTest {
            agenda(
                tasks =
                    listOf(
                        // Earlier in the clock, but a week old: the day you are in comes first.
                        task(1, today.minus(7, DateTimeUnit.DAY), hour = 7),
                        task(2, today, hour = 17),
                    ),
            ) {
                assertThat(it.upNext.map { entry -> entry.id }).containsExactly("task_2", "task_1").inOrder()
            }
        }

    @Test
    fun `overdue work fills the places today leaves free`() =
        runTest {
            agenda(
                tasks =
                    listOf(
                        task(1, today.minus(1, DateTimeUnit.DAY), hour = 8),
                        task(2, today.minus(1, DateTimeUnit.DAY), hour = 9),
                        task(3, today, hour = 10),
                    ),
            ) {
                assertThat(it.upNext.map { entry -> entry.id })
                    .containsExactly("task_3", "task_1", "task_2")
                    .inOrder()
            }
        }

    @Test
    fun `a finished day today still hands the row over to overdue work`() =
        runTest {
            agenda(
                tasks =
                    listOf(
                        task(1, today, isCompleted = true),
                        task(2, today.minus(1, DateTimeUnit.DAY)),
                    ),
            ) {
                assertThat(it.upNext.map { entry -> entry.id }).containsExactly("task_2")
            }
        }

    @Test
    fun `overdue work is eligible for up next when nothing is due today`() =
        runTest {
            agenda(tasks = listOf(task(1, today.minus(1, DateTimeUnit.DAY)))) {
                assertThat(it.upNext.map { entry -> entry.id }).containsExactly("task_1")
            }
        }

    @Test
    fun `an entirely finished day has no up next`() =
        runTest {
            agenda(tasks = listOf(task(1, today, isCompleted = true))) {
                assertThat(it.upNext).isEmpty()
            }
        }

    @Test
    fun `counts cover both sections`() =
        runTest {
            agenda(
                tasks =
                    listOf(
                        task(1, today, isCompleted = true),
                        task(2, today),
                        task(3, today.minus(1, DateTimeUnit.DAY)),
                    ),
            ) {
                assertThat(it.scheduledCount).isEqualTo(3)
                assertThat(it.completedCount).isEqualTo(1)
            }
        }
}
