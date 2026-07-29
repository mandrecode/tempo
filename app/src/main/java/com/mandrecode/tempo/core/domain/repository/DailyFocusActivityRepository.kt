package com.mandrecode.tempo.core.domain.repository

import com.mandrecode.tempo.core.domain.model.DailyFocusActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Durable per-day activity history, independent of the task rows it was derived from.
 */
interface DailyFocusActivityRepository {
    fun observeRange(
        from: LocalDate,
        to: LocalDate,
    ): Flow<List<DailyFocusActivity>>

    /** Every recorded day, newest first. */
    suspend fun getAll(): List<DailyFocusActivity>

    suspend fun getForDate(date: LocalDate): DailyFocusActivity?

    /** Records the day's counts, leaving any focus minutes already banked for that date intact. */
    suspend fun recordCounts(
        date: LocalDate,
        scheduledCount: Int,
        completedCount: Int,
    )

    /** Adds session minutes to a day, leaving its counts intact. */
    suspend fun addFocusMinutes(
        date: LocalDate,
        minutes: Int,
    )

    /** Replaces all history, used by a Replace import. */
    suspend fun replaceAll(activities: List<DailyFocusActivity>)
}
