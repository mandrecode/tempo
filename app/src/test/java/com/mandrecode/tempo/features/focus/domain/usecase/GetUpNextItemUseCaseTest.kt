package com.mandrecode.tempo.features.focus.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.junit.Test

class GetUpNextItemUseCaseTest {
    private val today = LocalDate(2026, 7, 29)
    private val useCase = GetUpNextItemUseCase()

    private fun taskEntry(
        id: Long,
        hour: Int?,
        priority: Priority? = null,
        isCompleted: Boolean = false,
    ) = FocusAgendaItem.TaskEntry(
        Task(
            id = id,
            title = "Task $id",
            description = "",
            isCompleted = isCompleted,
            priority = priority,
            reminderDate = hour?.let { LocalDateTime(today, LocalTime(it, 0)) },
        ),
    )

    private fun habitEntry(
        id: Long,
        hour: Int?,
        isCompleted: Boolean = false,
    ) = FocusAgendaItem.HabitEntry(
        habit =
            Habit(
                id = id,
                title = "Habit $id",
                description = "",
                createdDate = LocalDateTime(today, LocalTime(0, 0)),
                reminderDate = hour?.let { LocalDateTime(today, LocalTime(it, 0)) },
            ),
        isCompleted = isCompleted,
    )

    @Test
    fun `no candidates yields nothing`() {
        assertThat(useCase(emptyList())).isNull()
    }

    @Test
    fun `priority outranks an earlier due time`() {
        val high = taskEntry(1, hour = 17, priority = Priority.HIGH)
        val untimed = taskEntry(2, hour = 9)

        assertThat(useCase(listOf(untimed, high))).isEqualTo(high)
    }

    @Test
    fun `due time breaks ties within a priority`() {
        val early = taskEntry(1, hour = 9, priority = Priority.MEDIUM)
        val late = taskEntry(2, hour = 12, priority = Priority.MEDIUM)

        assertThat(useCase(listOf(late, early))).isEqualTo(early)
    }

    @Test
    fun `timed items rank ahead of untimed ones at the same priority`() {
        val timed = taskEntry(1, hour = 15)
        val untimed = taskEntry(2, hour = null)

        assertThat(useCase(listOf(untimed, timed))).isEqualTo(timed)
    }

    @Test
    fun `completed items are never chosen`() {
        val done = taskEntry(1, hour = 8, priority = Priority.HIGH, isCompleted = true)
        val open = taskEntry(2, hour = 16)

        assertThat(useCase(listOf(done, open))).isEqualTo(open)
    }

    @Test
    fun `everything completed yields nothing`() {
        val candidates =
            listOf(
                taskEntry(1, hour = 8, isCompleted = true),
                habitEntry(2, hour = 9, isCompleted = true),
            )

        assertThat(useCase(candidates)).isNull()
    }

    @Test
    fun `a habit never takes the slot, however early it is due`() {
        val habit = habitEntry(1, hour = 7)
        val task = taskEntry(2, hour = 9)

        assertThat(useCase(listOf(task, habit))).isEqualTo(task)
    }

    @Test
    fun `a day of habits alone leaves the slot empty`() {
        assertThat(useCase(listOf(habitEntry(1, hour = 7), habitEntry(2, hour = 8)))).isNull()
    }

    @Test
    fun `an unprioritised task cannot outrank a prioritised habit-free candidate`() {
        val low = taskEntry(1, hour = 20, priority = Priority.LOW)
        val none = taskEntry(2, hour = 6)

        assertThat(useCase(listOf(none, low))).isEqualTo(low)
    }

    @Test
    fun `ordering is stable when priority and time are identical`() {
        val first = taskEntry(1, hour = 9)
        val second = taskEntry(2, hour = 9)

        assertThat(useCase(listOf(second, first))).isEqualTo(first)
    }
}
