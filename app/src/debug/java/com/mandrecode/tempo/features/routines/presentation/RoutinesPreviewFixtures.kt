package com.mandrecode.tempo.features.routines.presentation

import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.model.HabitType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * Reusable fixtures for `@Preview` composables in the routines feature.
 *
 * Anchored on a fixed date (June 15 2025 — a Sunday) so previews are
 * deterministic and reproducible regardless of when the IDE renders them.
 */
internal object RoutinesPreviewFixtures {
    val PREVIEW_TODAY: LocalDate = LocalDate(2025, 6, 15)
    val PREVIEW_YESTERDAY: LocalDate = LocalDate(2025, 6, 14)
    val PREVIEW_TOMORROW: LocalDate = LocalDate(2025, 6, 16)

    val PREVIEW_CREATED: LocalDateTime = LocalDateTime(2025, 6, 1, 8, 0)

    const val SHORT_DESCRIPTION: String = "Quick morning stretch"
    const val LONG_DESCRIPTION: String =
        "Do a full body stretching routine including hamstrings, quads, shoulders and back. " +
            "Hold each stretch for at least 30 seconds and breathe deeply throughout. " +
            "Focus on any tight areas from yesterday's workout."

    /** Build a sample [Habit]. Defaults give a typical scheduled morning habit. */
    fun habit(
        id: Long = 1L,
        title: String = "Morning Stretch",
        description: String = "",
        icon: String? = "fitness",
        colorKey: String? = "color_m3_blue",
        reminderHour: Int? = 8,
        reminderMinute: Int = 0,
        isCompleted: Boolean = false,
        habitType: HabitType = HabitType.BUILD,
        completionHistory: String = "",
        repeatDays: Set<DayOfWeek>? = null,
    ): Habit =
        Habit(
            id = id,
            title = title,
            description = description,
            icon = icon,
            colorKey = colorKey,
            reminderDate =
                reminderHour?.let {
                    LocalDateTime(
                        PREVIEW_TODAY.year,
                        PREVIEW_TODAY.month,
                        PREVIEW_TODAY.day,
                        it,
                        reminderMinute,
                    )
                },
            isCompleted = isCompleted,
            habitType = habitType,
            createdDate = PREVIEW_CREATED,
            completionHistory = completionHistory,
            repeatDays = repeatDays,
        )

    /** Build a sample quit [Habit]. */
    fun quitHabit(
        id: Long = 100L,
        title: String = "No Smoking",
        description: String = "",
        completionHistory: String = "",
    ): Habit =
        habit(
            id = id,
            title = title,
            description = description,
            icon = "smoke_free",
            colorKey = "color_m3_red",
            reminderHour = 21,
            habitType = HabitType.QUIT,
            completionHistory = completionHistory,
        )

    /** Build a sample [HabitChain]. */
    fun chain(
        id: Long = 1000L,
        title: String = "Morning Routine",
        description: String = "",
        colorKey: String? = "color_m3_purple",
        icon: String? = "spa",
        habitIds: List<Long> = listOf(1L, 2L, 3L),
        reminderHour: Int? = 7,
        reminderMinute: Int = 30,
    ): HabitChain =
        HabitChain(
            id = id,
            title = title,
            description = description,
            colorKey = colorKey,
            icon = icon,
            habitIds = habitIds,
            periodicReminder =
                reminderHour?.let {
                    LocalDateTime(
                        PREVIEW_TODAY.year,
                        PREVIEW_TODAY.month,
                        PREVIEW_TODAY.day,
                        it,
                        reminderMinute,
                    )
                },
            createdDate = PREVIEW_CREATED,
        )

    /** A representative collection of build habits, useful for the bottom-sheet selector. */
    fun sampleBuildHabits(): List<Habit> =
        listOf(
            habit(id = 1L, title = "Stretch", icon = "fitness"),
            habit(
                id = 2L,
                title = "Meditate",
                icon = "spa",
                colorKey = "color_m3_purple",
                reminderHour = 7,
            ),
            habit(
                id = 3L,
                title = "Read",
                icon = "book",
                colorKey = "color_m3_orange",
                reminderHour = 22,
            ),
            habit(
                id = 4L,
                title = "Drink water",
                icon = "water",
                colorKey = "color_m3_cyan",
                reminderHour = null,
            ),
        )

    /** A representative quit-habits collection. */
    fun sampleQuitHabits(): List<Habit> =
        listOf(
            quitHabit(id = 100L, title = "No Smoking", completionHistory = "2025-06-13,2025-06-14,2025-06-15"),
            quitHabit(id = 101L, title = "No Sugar", completionHistory = "2025-06-15"),
        )

    /** A representative chain. */
    fun sampleChain(): HabitChain = chain(habitIds = listOf(1L, 2L))
}
