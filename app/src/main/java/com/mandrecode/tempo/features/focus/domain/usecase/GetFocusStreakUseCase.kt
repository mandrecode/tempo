package com.mandrecode.tempo.features.focus.domain.usecase

import com.mandrecode.tempo.core.domain.repository.DailyFocusActivityRepository
import jakarta.inject.Inject
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/**
 * Consecutive days with at least one completion, counted backwards from today.
 *
 * Forgiving by design, in two specific ways:
 *
 * - A day with nothing scheduled is skipped rather than treated as a break. This mirrors what
 *   `CompletionHistoryUtil.getCurrentStreak` already does for habits outside their repeat days, and
 *   it is why the history can show a quiet square on a day the streak still spans.
 * - Today never breaks the streak, it only extends it. The day is still in progress, so a run built
 *   up to yesterday stays intact until tomorrow.
 *
 * A day with no record at all is treated as a break rather than a skip: nothing was ever completed
 * then, and skipping it would let a streak span arbitrary stretches of inactivity.
 */
class GetFocusStreakUseCase
    @Inject
    constructor(
        private val repository: DailyFocusActivityRepository,
    ) {
        suspend operator fun invoke(today: LocalDate): Int {
            val byDate = repository.getAll().associateBy { it.date }
            val earliestRecorded = byDate.keys.minOrNull()

            var streak = 0
            var cursor = today
            var isToday = true
            var broken = false

            while (earliestRecorded != null && !broken && cursor >= earliestRecorded) {
                val day = byDate[cursor]
                when {
                    day != null && day.hasActivity -> streak++
                    day != null && day.isUnscheduled -> Unit
                    isToday -> Unit
                    else -> broken = true
                }
                isToday = false
                cursor = cursor.minus(1, DateTimeUnit.DAY)
            }
            return streak
        }
    }
