package com.mandrecode.tempo.core.domain.model

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

/**
 * A stretch of calendar days during which habit tracking is paused ("vacation mode").
 *
 * [endInclusive] is `null` for the open-ended period the user creates by switching vacation
 * mode on without picking an end date — it stays active until they switch it off, at which
 * point it is closed on the day *before* that one, so the Settings switch (which reads "is
 * today paused") turns off immediately rather than springing back on.
 *
 * Periods are kept for good, not just while they are active: streaks are derived by walking
 * backwards over `completionHistory`, so a trip taken last month must still resolve as paused
 * next year or the frozen streak would silently un-freeze.
 */
data class VacationPeriod(
    val start: LocalDate,
    val endInclusive: LocalDate? = null,
) {
    val isOpenEnded: Boolean get() = endInclusive == null

    /** Returns whether [date] falls inside this period, both bounds inclusive. */
    fun covers(date: LocalDate): Boolean = date >= start && (endInclusive == null || date <= endInclusive)

    /** Returns whether this period is well-formed; an inverted range is never stored. */
    fun isValid(): Boolean = endInclusive == null || endInclusive >= start

    companion object {
        /**
         * Returns [periods] in the canonical form the app stores and reads everywhere:
         *
         * 1. inverted ranges are dropped rather than rejected loudly, so a hand-edited backup
         *    file or a corrupted preference degrades to "fewer paused days", never to a crash;
         * 2. periods are sorted ascending by [start];
         * 3. overlapping *and* merely adjacent periods are merged, so "paused" is a set of
         *    maximal, non-touching ranges — two back-to-back trips read as one pause;
         * 4. at most one period is open-ended and it is necessarily the last, because an
         *    open-ended period absorbs everything that starts on or after it.
         */
        fun normalize(periods: List<VacationPeriod>): List<VacationPeriod> {
            val sorted = periods.filter { it.isValid() }.sortedBy { it.start }
            val merged = mutableListOf<VacationPeriod>()

            sorted.forEach { period ->
                val last = merged.lastOrNull()
                val lastEnd = last?.endInclusive
                when {
                    last == null -> merged.add(period)
                    // An open-ended period runs forever, so anything starting later is inside it.
                    lastEnd == null -> Unit
                    period.start <= lastEnd.plus(DatePeriod(days = 1)) -> {
                        merged[merged.lastIndex] =
                            last.copy(endInclusive = period.endInclusive?.let { maxOf(lastEnd, it) })
                    }

                    else -> merged.add(period)
                }
            }

            return merged
        }

        /** Returns the period covering [date], or `null` when [date] is not paused. */
        fun activeOn(
            periods: List<VacationPeriod>,
            date: LocalDate,
        ): VacationPeriod? = periods.firstOrNull { it.covers(date) }
    }
}
