package com.mandrecode.tempo.util

import android.content.Context
import android.text.format.DateFormat.is24HourFormat
import com.mandrecode.tempo.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateTimeFormatter {
    enum class Format(
        val pattern24h: String,
        val pattern12h: String,
    ) {
        Short("d MMM yyyy, HH:mm", "d MMM yyyy, h:mm a"),
        Full("E, d MMM yyyy, HH:mm", "E, d MMM yyyy, h:mm a"),
    }

    fun format(
        dateTime: LocalDateTime,
        format: Format,
        context: Context,
        useNaturalDates: Boolean = false,
    ): String {
        val calendar =
            Calendar.getInstance().apply {
                set(
                    dateTime.year,
                    dateTime.month.number - 1,
                    dateTime.day,
                    dateTime.hour,
                    dateTime.minute,
                )
            }

        if (useNaturalDates) {
            val now = Calendar.getInstance()
            val is24Hour = is24HourFormat(context)
            val timePattern = if (is24Hour) "HH:mm" else "h:mm a"
            val timeFormatter = SimpleDateFormat(timePattern, Locale.getDefault())

            if (isSameDay(calendar, now)) {
                val timeString = timeFormatter.format(calendar.time)
                return context.getString(
                    R.string.natural_date_format,
                    context.getString(R.string.today),
                    timeString,
                )
            }

            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            if (isSameDay(calendar, yesterday)) {
                val timeString = timeFormatter.format(calendar.time)
                return context.getString(
                    R.string.natural_date_format,
                    context.getString(R.string.yesterday),
                    timeString,
                )
            }

            val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
            if (isSameDay(calendar, tomorrow)) {
                val timeString = timeFormatter.format(calendar.time)
                return context.getString(
                    R.string.natural_date_format,
                    context.getString(R.string.tomorrow),
                    timeString,
                )
            }
        }

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val showYear = dateTime.year != currentYear

        val basePattern = if (is24HourFormat(context)) format.pattern24h else format.pattern12h
        val pattern =
            if (showYear) {
                basePattern
            } else {
                // Remove year if it's the current year
                basePattern.replace(" yyyy", "")
            }

        return SimpleDateFormat(pattern, Locale.getDefault()).format(calendar.time)
    }

    private fun isSameDay(
        cal1: Calendar,
        cal2: Calendar,
    ): Boolean =
        cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)

    fun formatDate(date: LocalDate): String {
        val calendar =
            Calendar.getInstance().apply {
                set(date.year, date.month.number - 1, date.day)
            }
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val pattern = if (date.year == currentYear) "d MMM" else "d MMM yyyy"
        return SimpleDateFormat(pattern, Locale.getDefault()).format(calendar.time)
    }

    /**
     * Localized date for screen-reader / UI use, optionally collapsing
     * today/yesterday to natural-language strings. Falls back to [formatDate]
     * for anything else.
     */
    fun formatDate(
        date: LocalDate,
        context: Context,
        useNaturalDates: Boolean,
    ): String {
        val natural = if (useNaturalDates) naturalDateOrNull(date, context) else null
        return natural ?: formatDate(date)
    }

    private fun naturalDateOrNull(
        date: LocalDate,
        context: Context,
    ): String? {
        val target =
            Calendar.getInstance().apply {
                set(date.year, date.month.number - 1, date.day)
            }
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        return when {
            isSameDay(target, today) -> context.getString(R.string.today)
            isSameDay(target, yesterday) -> context.getString(R.string.yesterday)
            else -> null
        }
    }
}
