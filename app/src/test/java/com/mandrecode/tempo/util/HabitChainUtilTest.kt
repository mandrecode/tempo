package com.mandrecode.tempo.util

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class HabitChainUtilTest {
    private val createdDate = LocalDateTime(2026, 6, 17, 12, 0, 0)

    @Test
    fun `isHabitInAnyChain returns false when habit is not in any chain`() {
        val habit =
            Habit(
                id = 1,
                title = "Morning walk",
                description = "30 min walk",
                createdDate = createdDate,
            )

        val habitChain =
            HabitChain(
                title = "Morning Routine",
                description = "Start the day right",
                habitIds = listOf(2L, 3L, 4L),
                createdDate = createdDate,
            )

        val result = isHabitInAnyChain(habit, listOf(habitChain))

        assertThat(result).isFalse()
    }

    @Test
    fun `isHabitInAnyChain returns true when habit is in single chain`() {
        val habit =
            Habit(
                id = 2,
                title = "Meditation",
                description = "10 min meditation",
                createdDate = createdDate,
            )

        val habitChain =
            HabitChain(
                title = "Morning Routine",
                description = "Start the day right",
                habitIds = listOf(1L, 2L, 3L),
                createdDate = createdDate,
            )

        val result = isHabitInAnyChain(habit, listOf(habitChain))

        assertThat(result).isTrue()
    }

    @Test
    fun `isHabitInAnyChain returns true when habit is in multiple chains`() {
        val habit =
            Habit(
                id = 1,
                title = "Morning walk",
                description = "30 min walk",
                createdDate = createdDate,
            )

        val chain1 =
            HabitChain(
                title = "Morning Routine",
                description = "Start the day right",
                habitIds = listOf(1L, 2L),
                createdDate = createdDate,
            )

        val chain2 =
            HabitChain(
                title = "Evening Routine",
                description = "Wind down",
                habitIds = listOf(1L, 5L),
                createdDate = createdDate,
            )

        val result = isHabitInAnyChain(habit, listOf(chain1, chain2))

        assertThat(result).isTrue()
    }

    @Test
    fun `isHabitInAnyChain returns false for empty chain list`() {
        val habit =
            Habit(
                id = 1,
                title = "Morning walk",
                description = "30 min walk",
                createdDate = createdDate,
            )

        val result = isHabitInAnyChain(habit, emptyList())

        assertThat(result).isFalse()
    }

    @Test
    fun `isHabitInAnyChain returns false for chain with empty habitIds`() {
        val habit =
            Habit(
                id = 1,
                title = "Morning walk",
                description = "30 min walk",
                createdDate = createdDate,
            )

        val habitChain =
            HabitChain(
                title = "Empty Chain",
                description = "No habits",
                habitIds = emptyList(),
                createdDate = createdDate,
            )

        val result = isHabitInAnyChain(habit, listOf(habitChain))

        assertThat(result).isFalse()
    }

    @Test
    fun `isHabitInAnyChain returns false for habitId zero`() {
        val habit =
            Habit(
                id = 0,
                title = "Unsaved habit",
                description = "Not yet saved",
                createdDate = createdDate,
            )

        val habitChain =
            HabitChain(
                title = "Morning Routine",
                description = "Start the day right",
                habitIds = listOf(1L, 2L, 3L),
                createdDate = createdDate,
            )

        val result = isHabitInAnyChain(habit, listOf(habitChain))

        assertThat(result).isFalse()
    }

    @Test
    fun `findChainForHabit returns null when habit is not in any chain`() {
        val habit =
            Habit(
                id = 1,
                title = "Morning walk",
                description = "30 min walk",
                createdDate = createdDate,
            )

        val habitChain =
            HabitChain(
                title = "Morning Routine",
                description = "Start the day right",
                habitIds = listOf(2L, 3L, 4L),
                createdDate = createdDate,
            )

        val result = findChainForHabit(habit, listOf(habitChain))

        assertThat(result).isNull()
    }

    @Test
    fun `findChainForHabit returns chain when habit is in single chain`() {
        val habit =
            Habit(
                id = 2,
                title = "Meditation",
                description = "10 min meditation",
                createdDate = createdDate,
            )

        val habitChain =
            HabitChain(
                id = 10,
                title = "Morning Routine",
                description = "Start the day right",
                habitIds = listOf(1L, 2L, 3L),
                createdDate = createdDate,
            )

        val result = findChainForHabit(habit, listOf(habitChain))

        assertThat(result).isNotNull()
        assertThat(result!!.id).isEqualTo(10L)
        assertThat(result.title).isEqualTo("Morning Routine")
    }

    @Test
    fun `findChainForHabit returns first chain when habit is in multiple chains`() {
        val habit =
            Habit(
                id = 1,
                title = "Morning walk",
                description = "30 min walk",
                createdDate = createdDate,
            )

        val chain1 =
            HabitChain(
                id = 10,
                title = "Morning Routine",
                description = "Start the day right",
                habitIds = listOf(1L, 2L),
                createdDate = createdDate,
            )

        val chain2 =
            HabitChain(
                id = 20,
                title = "Evening Routine",
                description = "Wind down",
                habitIds = listOf(1L, 5L),
                createdDate = createdDate,
            )

        val result = findChainForHabit(habit, listOf(chain1, chain2))

        assertThat(result).isNotNull()
        assertThat(result!!.id).isEqualTo(10L)
    }

    @Test
    fun `findChainForHabit returns null for habitId zero`() {
        val habit =
            Habit(
                id = 0,
                title = "Unsaved habit",
                description = "Not yet saved",
                createdDate = createdDate,
            )

        val habitChain =
            HabitChain(
                title = "Morning Routine",
                description = "Start the day right",
                habitIds = listOf(1L, 2L, 3L),
                createdDate = createdDate,
            )

        val result = findChainForHabit(habit, listOf(habitChain))

        assertThat(result).isNull()
    }
}
