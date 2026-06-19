package com.mandrecode.tempo.core.domain.model

import kotlinx.datetime.DateTimePeriod

enum class Periodicity(
    val period: DateTimePeriod,
) {
    HOURLY(DateTimePeriod(hours = 1)),
    DAILY(DateTimePeriod(days = 1)),
    WEEKLY(DateTimePeriod(days = 7)),
    MONTHLY(DateTimePeriod(months = 1)),
    YEARLY(DateTimePeriod(years = 1)),
    ;

    companion object {
        val periods = entries

        /** Maximum allowed interval when periodicity is [HOURLY] (24h would be [DAILY]). */
        const val MAX_HOURLY_INTERVAL = 23
    }
}
