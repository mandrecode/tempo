package com.mandrecode.tempo.core.domain.model

import kotlinx.datetime.LocalDateTime

sealed class ScheduleResult {
    data class Success(
        val scheduledTime: LocalDateTime,
    ) : ScheduleResult()

    data class Failure(
        val reason: String,
    ) : ScheduleResult()

    data class PermissionError(
        val message: String,
    ) : ScheduleResult()

    data object Skipped : ScheduleResult()
}
