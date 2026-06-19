package com.mandrecode.tempo.core.data.entity

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.routines.domain.model.Habit
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class HabitTest {
    @Test
    fun `habit can be created with minimal fields`() {
        val createdDate = LocalDateTime(2024, 1, 1, 0, 0)
        val habit =
            Habit(
                title = "Go for a walk",
                description = "Daily morning walk",
                createdDate = createdDate,
            )

        assertThat(habit.title).isEqualTo("Go for a walk")
        assertThat(habit.description).isEqualTo("Daily morning walk")
        assertThat(habit.isCompleted).isFalse()
        assertThat(habit.icon).isNull()
        assertThat(habit.colorKey).isNull()
        assertThat(habit.reminderDate).isNull()
        assertThat(habit.createdDate).isEqualTo(createdDate)
    }

    @Test
    fun `habit can have all fields populated`() {
        val createdDate = LocalDateTime(2024, 1, 1, 0, 0)
        val reminderDate = LocalDateTime(2024, 1, 1, 8, 0)
        val colorKey = "color_m3_red"
        val habit =
            Habit(
                id = 1L,
                title = "Running",
                description = "Morning run",
                icon = "directions_run",
                colorKey = colorKey,
                reminderDate = reminderDate,
                isCompleted = true,
                createdDate = createdDate,
                completionHistory = "2024-01-01,2024-01-02",
            )

        assertThat(habit.id).isEqualTo(1L)
        assertThat(habit.title).isEqualTo("Running")
        assertThat(habit.description).isEqualTo("Morning run")
        assertThat(habit.icon).isEqualTo("directions_run")
        assertThat(habit.colorKey).isEqualTo(colorKey)
        assertThat(habit.reminderDate).isEqualTo(reminderDate)
        assertThat(habit.isCompleted).isTrue()
        assertThat(habit.createdDate).isEqualTo(createdDate)
        assertThat(habit.completionHistory).isEqualTo("2024-01-01,2024-01-02")
    }

    @Test
    fun `habit has empty completion history by default`() {
        val createdDate = LocalDateTime(2024, 1, 1, 0, 0)
        val habit =
            Habit(
                title = "Test",
                description = "Test habit",
                createdDate = createdDate,
            )

        assertThat(habit.completionHistory).isEmpty()
    }

    @Test
    fun `habit can have color set`() {
        val createdDate = LocalDateTime(2024, 1, 1, 0, 0)
        val colorKey = "color_m3_green"
        val habit =
            Habit(
                title = "Meditation",
                description = "Daily meditation",
                colorKey = colorKey,
                createdDate = createdDate,
            )

        assertThat(habit.colorKey).isEqualTo(colorKey)
    }
}
