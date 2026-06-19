package com.mandrecode.tempo.features.routines.domain.model

/**
 * Distinguishes the kind of habit.
 *
 * - [BUILD]: a positive habit the user wants to cultivate (e.g. "Exercise daily").
 * - [QUIT]: a negative habit the user wants to stop (e.g. "No smoking").
 *   Quit habits work like build habits (the user checks them off to confirm
 *   they stayed on track), but they receive an evening reminder by default
 *   and display an encouraging "days clean" streak label.
 */
enum class HabitType {
    BUILD,
    QUIT,
}
