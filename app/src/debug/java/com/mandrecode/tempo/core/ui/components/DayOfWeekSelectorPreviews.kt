package com.mandrecode.tempo.core.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.ui.theme.TempoTheme

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DayOfWeekSelectorAllDaysPreview() {
    TempoTheme {
        DayOfWeekSelector(
            selectedDays = null,
            onDaysChange = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DayOfWeekSelectorWeekdaysPreview() {
    TempoTheme {
        DayOfWeekSelector(
            selectedDays =
                setOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                ),
            onDaysChange = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DayOfWeekSelectorDisabledPreview() {
    TempoTheme {
        DayOfWeekSelector(
            selectedDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
            onDaysChange = {},
            enabled = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}
