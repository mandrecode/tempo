package com.mandrecode.tempo.features.focus.domain.model

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.Priority
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
    fun `an untimed item has no due time`() {
        assertThat(taskEntry(1, timed = false).dueTime).isNull()
        assertThat(taskEntry(2).dueTime).isEqualTo(LocalTime(9, 0))
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
    fun `up next is not counted again, since it is a view onto the sections`() {
        val shortlisted = taskEntry(1)
        val agenda = FocusAgenda(upNext = listOf(shortlisted), today = listOf(shortlisted))

        assertThat(agenda.scheduledCount).isEqualTo(1)
    }
}
