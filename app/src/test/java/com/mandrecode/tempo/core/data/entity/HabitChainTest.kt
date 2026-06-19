package com.mandrecode.tempo.core.data.entity

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class HabitChainTest {
    @Test
    fun `habitChain can be created with minimal fields`() {
        val createdDate = LocalDateTime(2024, 1, 1, 0, 0)
        val habitChain =
            HabitChain(
                title = "Morning Routine",
                description = "Start the day right",
                createdDate = createdDate,
            )

        assertThat(habitChain.title).isEqualTo("Morning Routine")
        assertThat(habitChain.description).isEqualTo("Start the day right")
        assertThat(habitChain.icon).isNull()
        assertThat(habitChain.colorKey).isNull()
        assertThat(habitChain.periodicReminder).isNull()
        assertThat(habitChain.habitIds).isEmpty()
        assertThat(habitChain.createdDate).isEqualTo(createdDate)
        assertThat(habitChain.completionHistory).isEmpty()
    }

    @Test
    fun `habitChain can have all fields populated`() {
        val createdDate = LocalDateTime(2024, 1, 1, 0, 0)
        val reminderDate = LocalDateTime(2024, 1, 1, 8, 0)
        val colorKey = "color_m3_red"
        val habitChain =
            HabitChain(
                id = 1L,
                title = "Evening Routine",
                description = "Wind down routine",
                colorKey = colorKey,
                icon = "nightlight",
                habitIds = listOf(1L, 2L, 3L, 4L),
                periodicReminder = reminderDate,
                createdDate = createdDate,
                completionHistory = "2024-01-01,2024-01-02",
            )

        assertThat(habitChain.id).isEqualTo(1L)
        assertThat(habitChain.title).isEqualTo("Evening Routine")
        assertThat(habitChain.description).isEqualTo("Wind down routine")
        assertThat(habitChain.colorKey).isEqualTo(colorKey)
        assertThat(habitChain.icon).isEqualTo("nightlight")
        assertThat(habitChain.habitIds).isEqualTo(listOf(1L, 2L, 3L, 4L))
        assertThat(habitChain.periodicReminder).isEqualTo(reminderDate)
        assertThat(habitChain.createdDate).isEqualTo(createdDate)
        assertThat(habitChain.completionHistory).isEqualTo("2024-01-01,2024-01-02")
    }

    @Test
    fun `habitChain has empty habitIds by default`() {
        val createdDate = LocalDateTime(2024, 1, 1, 0, 0)
        val habitChain =
            HabitChain(
                title = "Test Chain",
                description = "Test description",
                createdDate = createdDate,
            )

        assertThat(habitChain.habitIds).isEmpty()
    }

    @Test
    fun `habitChain can have color set`() {
        val createdDate = LocalDateTime(2024, 1, 1, 0, 0)
        val colorKey = "color_m3_green"
        val habitChain =
            HabitChain(
                title = "Wellness Routine",
                description = "Health focused habits",
                colorKey = colorKey,
                createdDate = createdDate,
            )

        assertThat(habitChain.colorKey).isEqualTo(colorKey)
    }

    @Test
    fun `habitChain can have icon set`() {
        val createdDate = LocalDateTime(2024, 1, 1, 0, 0)
        val habitChain =
            HabitChain(
                title = "Fitness Routine",
                description = "Stay active",
                icon = "fitness_center",
                createdDate = createdDate,
            )

        assertThat(habitChain.icon).isEqualTo("fitness_center")
    }

    @Test
    fun `habitChain can have multiple habitIds`() {
        val createdDate = LocalDateTime(2024, 1, 1, 0, 0)
        val habitChain =
            HabitChain(
                title = "Full Day Routine",
                description = "Complete daily routine",
                habitIds = listOf(1L, 5L, 10L, 15L, 20L),
                createdDate = createdDate,
            )

        assertThat(habitChain.habitIds).isEqualTo(listOf(1L, 5L, 10L, 15L, 20L))
    }

    @Test
    fun `habitChain can have periodic reminder`() {
        val createdDate = LocalDateTime(2024, 1, 1, 0, 0)
        val reminderDate = LocalDateTime(2024, 6, 15, 9, 30)
        val habitChain =
            HabitChain(
                title = "Weekly Review",
                description = "Review progress weekly",
                periodicReminder = reminderDate,
                createdDate = createdDate,
            )

        assertThat(habitChain.periodicReminder).isEqualTo(reminderDate)
    }

    @Test
    fun `habitChain has empty description when not provided`() {
        val createdDate = LocalDateTime(2024, 1, 1, 0, 0)
        val habitChain =
            HabitChain(
                title = "Simple Chain",
                createdDate = createdDate,
            )

        assertThat(habitChain.description).isEmpty()
    }
}
