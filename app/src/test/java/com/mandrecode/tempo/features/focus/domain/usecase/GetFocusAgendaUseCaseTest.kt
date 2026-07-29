package com.mandrecode.tempo.features.focus.domain.usecase

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.repository.HabitChainRepository
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.TaskRepository
import io.mockk.every
import io.mockk.mockk
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
    private val useCase =
        GetFocusAgendaUseCase(
            taskRepository = taskRepository,
            habitRepository = habitRepository,
            habitChainRepository = habitChainRepository,
            getUpNextItem = GetUpNextItemUseCase(),
        )

    private fun task(
        id: Long,
        date: LocalDate?,
        hour: Int = 9,
        isCompleted: Boolean = false,
        parentTaskId: Long? = null,
    ) = Task(
        id = id,
        title = "Task $id",
        description = "",
        isCompleted = isCompleted,
        parentTaskId = parentTaskId,
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
        every { taskRepository.getAllTasks() } returns flowOf(tasks)
        every { habitRepository.getAllHabits() } returns flowOf(habits)
        every { habitChainRepository.getAllHabitChains() } returns flowOf(chains)

        useCase(today).test {
            assertions(awaitItem())
            awaitComplete()
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
                assertThat(it.overdue).hasSize(1)
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
                assertThat(it.today).hasSize(1)
                val entry = it.today.single() as FocusAgendaItem.TaskEntry
                assertThat(entry.subtasks).hasSize(1)
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
                assertThat(it.today).hasSize(1)
                assertThat(it.today.single()).isInstanceOf(FocusAgendaItem.ChainEntry::class.java)
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
                val ids = it.today.map { entry -> entry.id }
                assertThat(ids).containsExactly("task_2", "task_1", "habit_4", "task_3").inOrder()
            }
        }

    @Test
    fun `up next is drawn from the agenda without being removed from it`() =
        runTest {
            agenda(tasks = listOf(task(1, today, hour = 9))) {
                assertThat(it.upNext?.id).isEqualTo("task_1")
                assertThat(it.today.map { entry -> entry.id }).contains("task_1")
            }
        }

    @Test
    fun `overdue work is eligible for up next when nothing is due today`() =
        runTest {
            agenda(tasks = listOf(task(1, today.minus(1, DateTimeUnit.DAY)))) {
                assertThat(it.upNext?.id).isEqualTo("task_1")
            }
        }

    @Test
    fun `an entirely finished day has no up next`() =
        runTest {
            agenda(tasks = listOf(task(1, today, isCompleted = true))) {
                assertThat(it.upNext).isNull()
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
