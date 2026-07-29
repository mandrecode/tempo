package com.mandrecode.tempo.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per day of recorded activity, keyed by ISO-8601 local date.
 *
 * This table exists because completed tasks are deletable: `DeleteExpiredCompletedTasksUseCase`
 * removes them once the retention window passes, which would silently erase any history derived
 * from `TaskEntity.completedAt`. Counts recorded here survive that.
 *
 * Only counts are stored, never a derived "state" — the quiet/some/all classification is computed
 * from [scheduledCount] and [completedCount] at read time, so its thresholds can change without a
 * migration.
 *
 * The date is a `String` rather than a converted `LocalDate` so the primary key stays
 * lexicographically sortable, which for ISO-8601 is also chronological ordering.
 */
@Entity(tableName = "daily_focus_activity")
data class DailyFocusActivityEntity(
    @PrimaryKey
    val date: String,
    val scheduledCount: Int = 0,
    val completedCount: Int = 0,
    val focusMinutes: Int = 0,
)
