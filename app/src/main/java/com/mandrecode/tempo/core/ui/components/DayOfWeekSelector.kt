package com.mandrecode.tempo.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.DayOfWeek

@Composable
fun DayOfWeekSelector(
    selectedDays: Set<DayOfWeek>?,
    onDaysChange: (Set<DayOfWeek>?) -> Unit,
    modifier: Modifier = Modifier,
    showAllDaysOption: Boolean = true,
    selectedContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    selectedContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    enabled: Boolean = true,
) {
    val normalizedDays =
        if (selectedDays != null && (selectedDays.isEmpty() || selectedDays.size == DayOfWeek.ALL_DAYS.size)) {
            null
        } else {
            selectedDays
        }

    val days =
        listOf(
            DayOfWeek.MONDAY to R.string.monday,
            DayOfWeek.TUESDAY to R.string.tuesday,
            DayOfWeek.WEDNESDAY to R.string.wednesday,
            DayOfWeek.THURSDAY to R.string.thursday,
            DayOfWeek.FRIDAY to R.string.friday,
            DayOfWeek.SATURDAY to R.string.saturday,
            DayOfWeek.SUNDAY to R.string.sunday,
        )

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
    ) {
        if (showAllDaysOption) {
            item {
                val isEverydaySelected = normalizedDays == null
                ExpressiveChip(
                    label = stringResource(R.string.everyday),
                    isSelected = isEverydaySelected,
                    isFirst = true,
                    isLast = false,
                    onClick = {
                        if (!isEverydaySelected) {
                            onDaysChange(null)
                        }
                    },
                    height = 44.dp,
                    selectedContainerColor = selectedContainerColor,
                    selectedContentColor = selectedContentColor,
                    enabled = enabled,
                )
            }
        }

        itemsIndexed(days) { index, (day, labelRes) ->
            val isSelected = normalizedDays?.contains(day) ?: false
            ExpressiveChip(
                label = stringResource(labelRes),
                isSelected = isSelected,
                isFirst = !showAllDaysOption && index == 0,
                isLast = index == days.size - 1,
                onClick = {
                    val currentDays = normalizedDays?.toMutableSet() ?: mutableSetOf()
                    if (isSelected) {
                        currentDays.remove(day)
                    } else {
                        currentDays.add(day)
                    }
                    onDaysChange(
                        if (currentDays.size == DayOfWeek.ALL_DAYS.size || currentDays.isEmpty()) {
                            null
                        } else {
                            currentDays
                        },
                    )
                },
                height = 44.dp,
                selectedContainerColor = selectedContainerColor,
                selectedContentColor = selectedContentColor,
                enabled = enabled,
            )
        }
    }
}
