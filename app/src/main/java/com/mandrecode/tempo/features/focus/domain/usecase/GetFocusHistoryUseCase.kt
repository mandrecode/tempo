package com.mandrecode.tempo.features.focus.domain.usecase

import com.mandrecode.tempo.core.domain.model.DailyFocusActivity
import com.mandrecode.tempo.core.domain.repository.DailyFocusActivityRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

/**
 * The last [days] days ending today, oldest first, with days that were never recorded filled in as
 * quiet ones so the history is always a continuous run rather than a gappy list the UI has to
 * reason about.
 */
class GetFocusHistoryUseCase
    @Inject
    constructor(
        private val repository: DailyFocusActivityRepository,
    ) {
        operator fun invoke(
            today: LocalDate,
            days: Int = DEFAULT_DAYS,
        ): Flow<List<DailyFocusActivity>> {
            val dayCount = days.coerceAtLeast(1)
            val from = today.minus(dayCount - 1, DateTimeUnit.DAY)
            return repository.observeRange(from, today).map { recorded ->
                val byDate = recorded.associateBy { it.date }
                (0 until dayCount).map { offset ->
                    val date = from.plus(offset, DateTimeUnit.DAY)
                    byDate[date] ?: DailyFocusActivity(date = date)
                }
            }
        }

        companion object {
            /** One week, matching the row shown in the summary hero. */
            const val DEFAULT_DAYS = 7
        }
    }
