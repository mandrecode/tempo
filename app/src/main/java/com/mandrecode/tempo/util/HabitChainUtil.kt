package com.mandrecode.tempo.util

import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain

/**
 * Checks if a habit is contained in any habit chain.
 *
 * @param habit The habit to check.
 * @param habitChains The list of all habit chains to check against.
 * @return true if the habit is part of at least one habit chain, false otherwise.
 */
fun isHabitInAnyChain(
    habit: Habit,
    habitChains: List<HabitChain>,
): Boolean {
    // Unsaved habits (id = 0) can't be in any chain
    if (habit.id == 0L) {
        return false
    }

    return habitChains.any { chain ->
        chain.habitIds.contains(habit.id)
    }
}

/**
 * Finds the habit chain that contains a specific habit.
 *
 * @param habit The habit to find the chain for.
 * @param habitChains The list of all habit chains to search.
 * @return The habit chain containing the habit, or null if not found.
 */
fun findChainForHabit(
    habit: Habit,
    habitChains: List<HabitChain>,
): HabitChain? {
    // Unsaved habits (id = 0) can't be in any chain
    if (habit.id == 0L) {
        return null
    }

    return habitChains.find { chain ->
        chain.habitIds.contains(habit.id)
    }
}
