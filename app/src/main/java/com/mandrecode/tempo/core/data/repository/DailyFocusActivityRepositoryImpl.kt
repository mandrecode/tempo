package com.mandrecode.tempo.core.data.repository

import com.mandrecode.tempo.core.data.local.dao.DailyFocusActivityDao
import com.mandrecode.tempo.core.data.mapper.toDomain
import com.mandrecode.tempo.core.data.mapper.toEntity
import com.mandrecode.tempo.core.domain.model.DailyFocusActivity
import com.mandrecode.tempo.core.domain.repository.DailyFocusActivityRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class DailyFocusActivityRepositoryImpl
    @Inject
    constructor(
        private val dao: DailyFocusActivityDao,
    ) : DailyFocusActivityRepository {
        override fun observeRange(
            from: LocalDate,
            to: LocalDate,
        ): Flow<List<DailyFocusActivity>> =
            dao
                .observeRange(from.toString(), to.toString())
                .map { entities -> entities.mapNotNull { it.toDomain() } }

        override suspend fun getAll(): List<DailyFocusActivity> = dao.getAll().mapNotNull { it.toDomain() }

        override suspend fun getForDate(date: LocalDate): DailyFocusActivity? {
            val entity = dao.getByDate(date.toString())
            return entity?.toDomain()
        }

        override suspend fun recordCounts(
            date: LocalDate,
            scheduledCount: Int,
            completedCount: Int,
        ) {
            dao.recordCounts(
                date = date.toString(),
                scheduledCount = scheduledCount.coerceAtLeast(0),
                completedCount = completedCount.coerceAtLeast(0),
            )
        }

        override suspend fun addFocusMinutes(
            date: LocalDate,
            minutes: Int,
        ) {
            if (minutes <= 0) return
            dao.addFocusMinutes(date.toString(), minutes)
        }

        override suspend fun replaceAll(activities: List<DailyFocusActivity>) {
            dao.replaceAll(activities.map { it.toEntity() })
        }
    }
