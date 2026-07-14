package com.mandrecode.tempo.core.domain.model

data class RestoreResult(
    val scheduleResults: List<ScheduleResult>,
) {
    val hasSchedulingFailure: Boolean
        get() = scheduleResults.any { it is ScheduleResult.Failure || it is ScheduleResult.PermissionError }
}
