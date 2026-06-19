package com.mandrecode.tempo.core.data.entity

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.Periodicity
import com.mandrecode.tempo.core.domain.model.Priority
import com.mandrecode.tempo.features.tasks.domain.model.Task
import org.junit.Test

class TaskTest {
    @Test
    fun `task can be created with minimal fields`() {
        val task =
            Task(
                title = "New Task",
                description = "Description",
                categoryId = 1,
            )

        assertThat(task.title).isEqualTo("New Task")
        assertThat(task.description).isEqualTo("Description")
        assertThat(task.categoryId).isEqualTo(1)
        assertThat(task.isCompleted).isFalse()
        assertThat(task.priority).isNull()
        assertThat(task.reminderDate).isNull()
        assertThat(task.periodicity).isNull()
        assertThat(task.parentTaskId).isNull()
        assertThat(task.sortOrder).isEqualTo(0)
    }

    @Test
    fun `task can have all fields populated`() {
        val reminderDate = kotlinx.datetime.LocalDateTime(2024, 1, 1, 12, 0)
        val task =
            Task(
                id = 100,
                title = "Full Task",
                description = "Full Description",
                categoryId = 2,
                priority = Priority.HIGH,
                isCompleted = true,
                reminderDate = reminderDate,
                periodicity = Periodicity.WEEKLY,
                parentTaskId = 50,
                sortOrder = 5,
            )

        assertThat(task.id).isEqualTo(100)
        assertThat(task.title).isEqualTo("Full Task")
        assertThat(task.description).isEqualTo("Full Description")
        assertThat(task.categoryId).isEqualTo(2)
        assertThat(task.priority).isEqualTo(Priority.HIGH)
        assertThat(task.isCompleted).isTrue()
        assertThat(task.reminderDate).isEqualTo(reminderDate)
        assertThat(task.periodicity).isEqualTo(Periodicity.WEEKLY)
        assertThat(task.parentTaskId).isEqualTo(50)
        assertThat(task.sortOrder).isEqualTo(5)
    }
}
