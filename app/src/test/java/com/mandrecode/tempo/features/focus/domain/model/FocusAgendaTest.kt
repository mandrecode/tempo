package com.mandrecode.tempo.features.focus.domain.model

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.junit.Test

class FocusAgendaTest {
    private val today = LocalDate(2026, 7, 29)
    private val nineAm = LocalDateTime(today, LocalTime(9, 0))

    private fun taskEntry(
        id: Long,
        isCompleted: Boolean = false,
        priority: Priority? = null,
        timed: Boolean = true,
    ) = FocusAgendaItem.TaskEntry(
        Task(
            id = id,
            title = "Task $id",
            description = "",
            isCompleted = isCompleted,
            priority = priority,
            reminderDate = nineAm.takeIf { timed },
        ),
    )

    @Test
    fun `entry ids are namespaced by type so a task and a habit cannot collide`() {
        val task = taskEntry(1)
        val habit =
            FocusAgendaItem.HabitEntry(
                habit =
                    Habit(id = 1, title = "Water", description = "", createdDate = nineAm),
                isCompleted = false,
            )
        val chain =
            FocusAgendaItem.ChainEntry(
                chain = HabitChain(id = 1, title = "Morning", createdDate = nineAm),
                habits = emptyList(),
                isCompleted = false,
            )

        assertThat(setOf(task.id, habit.id, chain.id)).hasSize(3)
    }

    @Test
    fun `only tasks carry a priority`() {
        assertThat(taskEntry(1, priority = Priority.HIGH).priority).isEqualTo(Priority.HIGH)

        val habit =
            FocusAgendaItem.HabitEntry(
                habit = Habit(id = 1, title = "Water", description = "", createdDate = nineAm),
                isCompleted = false,
            )
        assertThat(habit.priority).isNull()
    }

    @Test
    fun `an untimed item has no due time`() {
        assertThat(taskEntry(1, timed = false).dueTime).isNull()
        assertThat(taskEntry(2).dueTime).isEqualTo(LocalTime(9, 0))
    }

    @Test
    fun `a chain takes its time from the chain reminder`() {
        val chain =
            FocusAgendaItem.ChainEntry(
                chain =
                    HabitChain(
                        id = 1,
                        title = "Morning",
                        createdDate = nineAm,
                        periodicReminder = LocalDateTime(today, LocalTime(7, 0)),
                    ),
                habits = emptyList(),
                isCompleted = false,
            )

        assertThat(chain.dueTime).isEqualTo(LocalTime(7, 0))
    }

    @Test
    fun `counts span both sections`() {
        val agenda =
            FocusAgenda(
                overdue = listOf(taskEntry(1)),
                today = listOf(taskEntry(2, isCompleted = true), taskEntry(3)),
            )

        assertThat(agenda.scheduledCount).isEqualTo(3)
        assertThat(agenda.completedCount).isEqualTo(1)
    }

    @Test
    fun `an agenda with neither section is empty`() {
        assertThat(FocusAgenda().isEmpty).isTrue()
        assertThat(FocusAgenda(today = listOf(taskEntry(1))).isEmpty).isFalse()
        assertThat(FocusAgenda(overdue = listOf(taskEntry(1))).isEmpty).isFalse()
    }

    @Test
    fun `the promoted up next item is counted, since it was lifted out of its section`() {
        val promoted = taskEntry(1)
        val agenda = FocusAgenda(upNext = promoted, today = listOf(taskEntry(2)))

        assertThat(agenda.scheduledCount).isEqualTo(2)
    }

    @Test
    fun `an agenda holding only an up next item is not empty`() {
        assertThat(FocusAgenda(upNext = taskEntry(1)).isEmpty).isFalse()
    }
}
