package com.mandrecode.tempo.core.domain.model

enum class DayOfWeek(
    val value: Int,
) {
    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
    SUNDAY(7),
    ;

    companion object {
        val ALL_DAYS: Set<DayOfWeek> = entries.toSet()

        fun fromValue(value: Int): DayOfWeek? = entries.find { it.value == value }

        fun fromKotlinDayOfWeek(kotlinDayOfWeek: kotlinx.datetime.DayOfWeek): DayOfWeek =
            when (kotlinDayOfWeek) {
                kotlinx.datetime.DayOfWeek.MONDAY -> MONDAY
                kotlinx.datetime.DayOfWeek.TUESDAY -> TUESDAY
                kotlinx.datetime.DayOfWeek.WEDNESDAY -> WEDNESDAY
                kotlinx.datetime.DayOfWeek.THURSDAY -> THURSDAY
                kotlinx.datetime.DayOfWeek.FRIDAY -> FRIDAY
                kotlinx.datetime.DayOfWeek.SATURDAY -> SATURDAY
                kotlinx.datetime.DayOfWeek.SUNDAY -> SUNDAY
            }
    }
}
