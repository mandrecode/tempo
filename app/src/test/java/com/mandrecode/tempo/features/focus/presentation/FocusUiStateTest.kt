package com.mandrecode.tempo.features.focus.presentation

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.focus.domain.model.FocusAgendaItem
import com.mandrecode.tempo.features.focus.domain.model.FocusSession
import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.junit.Test
import kotlin.time.Instant

class FocusUiStateTest {
    private val today = LocalDate(2026, 7, 29)
    private val now = Instant.fromEpochMilliseconds(1_800_000_000_000)

    private fun task(id: Long) =
        Task(
            id = id,
            title = "Task $id",
            description = "",
            reminderDate = LocalDateTime(today, LocalTime(9, 0)),
        )

    @Test
    fun `progress is zero when nothing is scheduled, rather than dividing by zero`() {
        val state = FocusContract.UiState(scheduledCount = 0, completedCount = 0)

        assertThat(state.progress).isEqualTo(0f)
    }

    @Test
    fun `progress is the completed ratio`() {
        val state = FocusContract.UiState(scheduledCount = 4, completedCount = 1)

        assertThat(state.progress).isEqualTo(0.25f)
    }

    @Test
    fun `progress is clamped when more was completed than scheduled`() {
        val state = FocusContract.UiState(scheduledCount = 2, completedCount = 5)

        assertThat(state.progress).isEqualTo(1f)
    }

    @Test
    fun `the day is empty only when both sections are`() {
        assertThat(FocusContract.UiState().isDayEmpty).isTrue()
        assertThat(
            FocusContract
                .UiState(
                    todayItems = persistentListOf(FocusAgendaItem.TaskEntry(task(1))),
                ).isDayEmpty,
        ).isFalse()
    }

    @Test
    fun `session subtasks are empty when nothing is running`() {
        val state =
            FocusContract.UiState(
                todayItems =
                    persistentListOf(
                        FocusAgendaItem.TaskEntry(task(1), subtasks = listOf(task(2))),
                    ),
            )

        assertThat(state.sessionSubtasks).isEmpty()
    }

    @Test
    fun `session subtasks come from the task the session runs on`() {
        val state =
            FocusContract.UiState(
                session = FocusSession.start(taskId = 1, taskTitle = "Task 1", now = now),
                todayItems =
                    persistentListOf(
                        FocusAgendaItem.TaskEntry(task(1), subtasks = listOf(task(2), task(3))),
                        FocusAgendaItem.TaskEntry(task(4), subtasks = listOf(task(5))),
                    ),
            )

        assertThat(state.sessionSubtasks.map { it.id }).containsExactly(2L, 3L)
    }

    @Test
    fun `session subtasks are found in the overdue section too`() {
        val state =
            FocusContract.UiState(
                session = FocusSession.start(taskId = 9, taskTitle = "Overdue", now = now),
                overdue =
                    persistentListOf(FocusAgendaItem.TaskEntry(task(9), subtasks = listOf(task(10)))),
            )

        assertThat(state.sessionSubtasks.map { it.id }).containsExactly(10L)
    }

    @Test
    fun `a session on a task that left the agenda yields no subtasks`() {
        val state =
            FocusContract.UiState(
                session = FocusSession.start(taskId = 99, taskTitle = "Gone", now = now),
                todayItems = persistentListOf(FocusAgendaItem.TaskEntry(task(1))),
            )

        assertThat(state.sessionSubtasks).isEmpty()
    }
}
