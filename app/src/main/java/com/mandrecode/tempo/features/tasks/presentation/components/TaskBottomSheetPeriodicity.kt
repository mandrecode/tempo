package com.mandrecode.tempo.features.tasks.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.domain.model.MonthDayOption
import com.mandrecode.tempo.core.domain.model.Periodicity
import com.mandrecode.tempo.util.DateTimeFormatter
import kotlinx.datetime.LocalDateTime

private val PILL_BUTTON_WIDTH = 32.dp
private val PILL_BUTTON_HEIGHT = 40.dp
private val PILL_ICON_SIZE = 18.dp
private const val DISABLED_ALPHA = 0.5f
private const val DISABLED_ICON_ALPHA = 0.38f
private const val PILL_CORNER_PERCENT = 50

@Composable
internal fun periodicityChipLabel(
    period: Periodicity,
    isSelected: Boolean,
    interval: Int,
    repeatDays: Set<DayOfWeek>?,
    monthDayOption: MonthDayOption?,
): String {
    val effectiveInterval = if (isSelected) interval else 1
    val base = periodicityBaseLabel(period, effectiveInterval)
    if (!isSelected) return base
    val detail = periodicityDetail(period, repeatDays, monthDayOption)
    return if (detail != null) "$base, $detail" else base
}

@Composable
private fun periodicityBaseLabel(
    period: Periodicity,
    interval: Int,
): String =
    when (period) {
        Periodicity.HOURLY -> pluralStringResource(R.plurals.each_n_hours, interval, interval)
        Periodicity.DAILY -> pluralStringResource(R.plurals.each_n_days, interval, interval)
        Periodicity.WEEKLY -> pluralStringResource(R.plurals.each_n_weeks, interval, interval)
        Periodicity.MONTHLY -> pluralStringResource(R.plurals.each_n_months, interval, interval)
        Periodicity.YEARLY -> pluralStringResource(R.plurals.each_n_years, interval, interval)
    }

@Composable
private fun periodicityDetail(
    period: Periodicity,
    repeatDays: Set<DayOfWeek>?,
    monthDayOption: MonthDayOption?,
): String? =
    when (period) {
        Periodicity.WEEKLY -> repeatDays?.sortedDayNames()
        Periodicity.MONTHLY -> monthDayOptionLabel(monthDayOption)
        else -> null
    }

@Composable
private fun monthDayOptionLabel(option: MonthDayOption?): String? =
    when (option) {
        MonthDayOption.FIRST_DAY -> stringResource(R.string.first_day_of_month_detail)
        MonthDayOption.LAST_DAY -> stringResource(R.string.last_day_of_month_detail)
        else -> null
    }

@Composable
private fun Set<DayOfWeek>.sortedDayNames(): String? {
    if (isEmpty()) return null
    val dayLabels =
        mapOf(
            DayOfWeek.MONDAY to stringResource(R.string.mon),
            DayOfWeek.TUESDAY to stringResource(R.string.tue),
            DayOfWeek.WEDNESDAY to stringResource(R.string.wed),
            DayOfWeek.THURSDAY to stringResource(R.string.thu),
            DayOfWeek.FRIDAY to stringResource(R.string.fri),
            DayOfWeek.SATURDAY to stringResource(R.string.sat),
            DayOfWeek.SUNDAY to stringResource(R.string.sun),
        )
    return sortedBy { it.value }.mapNotNull { dayLabels[it] }.joinToString(", ")
}

@Composable
internal fun PeriodicityIntervalSelector(
    interval: Int,
    onIntervalChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxInterval: Int = Int.MAX_VALUE,
    enabled: Boolean = true,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        PillButton(
            iconRes = R.drawable.ic_remove,
            contentDescription = stringResource(R.string.decrease_interval),
            enabled = enabled && interval > 1,
            onClick = { onIntervalChange((interval - 1).coerceAtLeast(1)) },
        )

        Text(
            text = interval.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(PILL_BUTTON_WIDTH),
            textAlign = TextAlign.Center,
        )

        PillButton(
            iconRes = R.drawable.ic_add,
            contentDescription = stringResource(R.string.increase_interval),
            enabled = enabled && interval < maxInterval,
            onClick = { onIntervalChange((interval + 1).coerceAtMost(maxInterval)) },
        )
    }
}

@Composable
private fun PillButton(
    @androidx.annotation.DrawableRes iconRes: Int,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(PILL_CORNER_PERCENT),
        color =
            MaterialTheme.colorScheme.surfaceContainerHighest.let {
                if (enabled) it else it.copy(alpha = DISABLED_ALPHA)
            },
        modifier =
            Modifier
                .size(width = PILL_BUTTON_WIDTH, height = PILL_BUTTON_HEIGHT)
                .minimumInteractiveComponentSize()
                .clip(RoundedCornerShape(PILL_CORNER_PERCENT))
                .clickable(enabled = enabled, onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(PILL_ICON_SIZE),
                tint =
                    MaterialTheme.colorScheme.onSurfaceVariant.let {
                        if (enabled) it else it.copy(alpha = DISABLED_ICON_ALPHA)
                    },
            )
        }
    }
}

@Composable
internal fun rememberFormattedDateTime(dateTime: LocalDateTime?): String? {
    val context = LocalContext.current
    return remember(dateTime) {
        dateTime?.let {
            DateTimeFormatter.format(
                it,
                DateTimeFormatter.Format.Full,
                context,
            )
        }
    }
}
