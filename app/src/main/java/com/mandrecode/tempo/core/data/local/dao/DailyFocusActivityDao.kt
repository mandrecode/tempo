package com.mandrecode.tempo.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mandrecode.tempo.core.data.entity.DailyFocusActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyFocusActivityDao {
    @Query(
        "SELECT * FROM daily_focus_activity WHERE date BETWEEN :from AND :to ORDER BY date ASC",
    )
    fun observeRange(
        from: String,
        to: String,
    ): Flow<List<DailyFocusActivityEntity>>

    /**
     * Every recorded day, newest first. Used by the streak, which walks backwards from today, and
     * by backup export.
     */
    @Query("SELECT * FROM daily_focus_activity ORDER BY date DESC")
    suspend fun getAll(): List<DailyFocusActivityEntity>

    @Query("SELECT * FROM daily_focus_activity WHERE date = :date")
    suspend fun getByDate(date: String): DailyFocusActivityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(activity: DailyFocusActivityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(activities: List<DailyFocusActivityEntity>)

    /**
     * Adds to the day's focus minutes without disturbing its counts, creating the row if the day
     * has none yet. Written as a single statement so two sessions ending close together cannot
     * lose an increment to a read-modify-write race.
     */
    @Query(
        """
        INSERT INTO daily_focus_activity (date, scheduledCount, completedCount, focusMinutes)
        VALUES (:date, 0, 0, :minutes)
        ON CONFLICT(date) DO UPDATE SET focusMinutes = focusMinutes + :minutes
        """,
    )
    suspend fun addFocusMinutes(
        date: String,
        minutes: Int,
    )

    /**
     * Records the day's counts, preserving any focus minutes already banked for that date.
     */
    @Query(
        """
        INSERT INTO daily_focus_activity (date, scheduledCount, completedCount, focusMinutes)
        VALUES (:date, :scheduledCount, :completedCount, 0)
        ON CONFLICT(date) DO UPDATE SET
            scheduledCount = :scheduledCount,
            completedCount = :completedCount
        """,
    )
    suspend fun recordCounts(
        date: String,
        scheduledCount: Int,
        completedCount: Int,
    )

    @Query("DELETE FROM daily_focus_activity")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(activities: List<DailyFocusActivityEntity>) {
        deleteAll()
        upsertAll(activities)
    }
}
