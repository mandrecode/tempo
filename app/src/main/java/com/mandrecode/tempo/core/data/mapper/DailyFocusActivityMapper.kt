package com.mandrecode.tempo.core.data.mapper

import com.mandrecode.tempo.core.data.entity.DailyFocusActivityEntity
import com.mandrecode.tempo.core.domain.model.DailyFocusActivity
import kotlinx.datetime.LocalDate

internal fun DailyFocusActivityEntity.toDomain(): DailyFocusActivity? {
    // A row whose date cannot be parsed is unusable rather than fatal — it is dropped so one bad
    // value (a hand-edited database, a botched import) cannot take the whole history down.
    val parsedDate = runCatching { LocalDate.parse(date) }.getOrNull() ?: return null
    return DailyFocusActivity(
        date = parsedDate,
        scheduledCount = scheduledCount,
        completedCount = completedCount,
        focusMinutes = focusMinutes,
    )
}

internal fun DailyFocusActivity.toEntity(): DailyFocusActivityEntity =
    DailyFocusActivityEntity(
        date = date.toString(),
        scheduledCount = scheduledCount,
        completedCount = completedCount,
        focusMinutes = focusMinutes,
    )
