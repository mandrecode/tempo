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
        assertThat(useCase(emptyList())).isEmpty()
    }

    @Test
    fun `the caller's order is kept, so the row agrees with the list below it`() {
        val first = taskEntry(1, hour = 17, priority = Priority.HIGH)
        val second = taskEntry(2, hour = 9)

        // Handed over in the day's order, priority and clock already accounted for. A row that
        // re-sorted would disagree with the list it shortlists.
        assertThat(useCase(listOf(first, second))).containsExactly(first, second).inOrder()
        assertThat(useCase(listOf(second, first))).containsExactly(second, first).inOrder()
    }

    @Test
    fun `completed items are never chosen`() {
        val done = taskEntry(1, hour = 8, priority = Priority.HIGH, isCompleted = true)
        val open = taskEntry(2, hour = 16)

        assertThat(useCase(listOf(done, open))).containsExactly(open)
    }

    @Test
    fun `everything completed yields nothing`() {
        val candidates =
            listOf(
                taskEntry(1, hour = 8, isCompleted = true),
                habitEntry(2, hour = 9, isCompleted = true),
            )

        assertThat(useCase(candidates)).isEmpty()
    }

    @Test
    fun `a habit never takes the slot, however early it is due`() {
        val habit = habitEntry(1, hour = 7)
        val task = taskEntry(2, hour = 9)

        assertThat(useCase(listOf(task, habit))).containsExactly(task)
    }

    @Test
    fun `a day of habits alone leaves the slot empty`() {
        assertThat(useCase(listOf(habitEntry(1, hour = 7), habitEntry(2, hour = 8)))).isEmpty()
    }

    @Test
    fun `the row is a shortlist, not the whole day`() {
        val candidates = (1..8).map { taskEntry(it.toLong(), hour = 9) }

        assertThat(useCase(candidates)).hasSize(5)
        assertThat(useCase(candidates)).containsExactlyElementsIn(candidates.take(5)).inOrder()
    }

    @Test
    fun `completed work is skipped without disturbing what follows it`() {
        val done = taskEntry(1, hour = 8, isCompleted = true)
        val open = taskEntry(2, hour = 9)
        val alsoOpen = taskEntry(3, hour = 10)

        assertThat(useCase(listOf(done, open, alsoOpen))).containsExactly(open, alsoOpen).inOrder()
    }
}
