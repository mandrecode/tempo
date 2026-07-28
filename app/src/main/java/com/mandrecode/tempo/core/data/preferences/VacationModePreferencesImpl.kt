package com.mandrecode.tempo.core.data.preferences

import android.content.Context
import androidx.core.content.edit
import com.mandrecode.tempo.core.domain.model.VacationPeriod
import com.mandrecode.tempo.core.domain.repository.VacationModeRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores vacation periods in a single preference string: `start..end` entries joined by `;`,
 * with an empty end for the open-ended period (for example `2026-03-01..2026-03-08;2026-07-14..`).
 *
 * A handful of ranges per year never justifies a Room table, and keeping the list in memory as a
 * [StateFlow] lets the streak math stay synchronous — it is called from composition on every
 * habit card and history view.
 */
@Singleton
class VacationModePreferencesImpl
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : VacationModeRepository {
        private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        private val periodsFlow = MutableStateFlow(readPeriods())

        override val periods: StateFlow<List<VacationPeriod>> = periodsFlow.asStateFlow()

        override fun setPaused(
            today: LocalDate,
            paused: Boolean,
        ) {
            if (paused) start(today) else stop(today)
        }

        private fun start(today: LocalDate) {
            val current = periodsFlow.value
            if (VacationPeriod.activeOn(current, today) != null) return
            write(current + VacationPeriod(start = today))
        }

        private fun stop(today: LocalDate) {
            val current = periodsFlow.value
            val active = VacationPeriod.activeOn(current, today) ?: return
            val lastPausedDay = today.minus(DatePeriod(days = 1))
            val remaining = current - active
            // Switching off must read as off straight away, so the pause ends yesterday; a pause
            // started and stopped on the same day disappears entirely.
            write(
                if (lastPausedDay < active.start) {
                    remaining
                } else {
                    remaining + active.copy(endInclusive = lastPausedDay)
                },
            )
        }

        override fun setPlannedEnd(
            today: LocalDate,
            endInclusive: LocalDate?,
        ) {
            val current = periodsFlow.value
            val active = VacationPeriod.activeOn(current, today) ?: return
            if (endInclusive != null && endInclusive < active.start) return
            write(current - active + active.copy(endInclusive = endInclusive))
        }

        override fun replaceAll(periods: List<VacationPeriod>) {
            write(periods)
        }

        private fun write(periods: List<VacationPeriod>) {
            val normalized = VacationPeriod.normalize(periods)
            prefs.edit { putString(KEY_PERIODS, serialize(normalized)) }
            periodsFlow.value = normalized
        }

        private fun readPeriods(): List<VacationPeriod> =
            VacationPeriod.normalize(
                deserialize(prefs.getString(KEY_PERIODS, null)),
            )

        companion object {
            private const val PREFS_NAME = "vacation_mode_prefs"
            private const val KEY_PERIODS = "periods"
            private const val PERIOD_SEPARATOR = ";"
            private const val RANGE_SEPARATOR = ".."

            /** Serializes to the canonical `start..end;start..` form. */
            internal fun serialize(periods: List<VacationPeriod>): String =
                periods.joinToString(PERIOD_SEPARATOR) { period ->
                    "${period.start}$RANGE_SEPARATOR${period.endInclusive ?: ""}"
                }

            /**
             * Parses the stored string, silently dropping anything unparsable: a corrupted or
             * hand-edited value must degrade to fewer paused days, never crash the app on start.
             */
            internal fun deserialize(stored: String?): List<VacationPeriod> {
                if (stored.isNullOrBlank()) return emptyList()
                return stored
                    .split(PERIOD_SEPARATOR)
                    .mapNotNull { entry -> parsePeriod(entry.trim()) }
            }

            private fun parsePeriod(entry: String): VacationPeriod? {
                val separatorIndex = entry.indexOf(RANGE_SEPARATOR)
                if (separatorIndex <= 0) return null

                val start = parseDate(entry.substring(0, separatorIndex))
                val endText = entry.substring(separatorIndex + RANGE_SEPARATOR.length)
                val end = if (endText.isEmpty()) null else parseDate(endText)

                // An entry that names an end date we cannot read is dropped whole rather than
                // silently downgraded to an open-ended pause.
                val endUnreadable = endText.isNotEmpty() && end == null
                return if (start == null || endUnreadable) {
                    null
                } else {
                    VacationPeriod(start = start, endInclusive = end)
                }
            }

            private fun parseDate(text: String): LocalDate? = runCatching { LocalDate.parse(text) }.getOrNull()
        }
    }
