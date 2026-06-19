package com.mandrecode.tempo.features.tasks.presentation.model

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class SortOptionTest {
    @Test
    fun `sortOption by date sorts tasks by reminder date`() {
        val tasks =
            listOf(
                Task(
                    id = 1,
                    title = "Task with later date",
                    description = "",
                    reminderDate = LocalDateTime(2025, 6, 15, 10, 0),
                ),
                Task(id = 2, title = "Task without date", description = "", reminderDate = null),
                Task(
                    id = 3,
                    title = "Task with earlier date",
                    description = "",
                    reminderDate = LocalDateTime(2025, 1, 1, 10, 0),
                ),
            )

        val sorted =
            tasks.sortedWith(
                compareBy<Task> { it.isCompleted }
                    .thenBy(nullsLast()) { it.reminderDate },
            )

        assertThat(sorted[0].reminderDate).isEqualTo(LocalDateTime(2025, 1, 1, 10, 0))
        assertThat(sorted[1].reminderDate).isEqualTo(LocalDateTime(2025, 6, 15, 10, 0))
        assertThat(sorted[2].reminderDate).isNull()
    }

    @Test
    fun `sortOption keeps completed tasks at bottom`() {
        val tasks =
            listOf(
                Task(
                    id = 1,
                    title = "Incomplete A",
                    description = "",
                    isCompleted = false,
                    sortOrder = 2,
                ),
                Task(
                    id = 2,
                    title = "Completed B",
                    description = "",
                    isCompleted = true,
                    sortOrder = 1,
                ),
                Task(
                    id = 3,
                    title = "Incomplete C",
                    description = "",
                    isCompleted = false,
                    sortOrder = 3,
                ),
            )

        val sorted = tasks.sortedWith(compareBy<Task>({ it.isCompleted }, { it.sortOrder }))

        assertThat(sorted[0].isCompleted).isFalse()
        assertThat(sorted[1].isCompleted).isFalse()
        assertThat(sorted[2].isCompleted).isTrue()

        // Secondary sort check (by sortOrder)
        assertThat(sorted[0].title).isEqualTo("Incomplete A") // Order 2
        assertThat(sorted[1].title).isEqualTo("Incomplete C") // Order 3
    }

    @Test
    fun `sortOption by priority sorts tasks by priority level`() {
        val tasks =
            listOf(
                Task(id = 1, title = "Low priority", description = "", priority = Priority.LOW),
                Task(id = 2, title = "High priority", description = "", priority = Priority.HIGH),
                Task(id = 3, title = "Medium priority", description = "", priority = Priority.MEDIUM),
                Task(id = 4, title = "No priority", description = "", priority = null),
            )

        val sorted =
            tasks.sortedWith(
                compareBy<Task> { it.isCompleted }
                    .thenBy(nullsLast()) { it.priority?.sortOrder },
            )

        assertThat(sorted[0].priority).isEqualTo(Priority.HIGH)
        assertThat(sorted[1].priority).isEqualTo(Priority.MEDIUM)
        assertThat(sorted[2].priority).isEqualTo(Priority.LOW)
        assertThat(sorted[3].priority).isNull()
    }
}
