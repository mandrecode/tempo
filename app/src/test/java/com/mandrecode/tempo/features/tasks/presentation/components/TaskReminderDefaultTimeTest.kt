package com.mandrecode.tempo.features.tasks.presentation.components

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.junit.Test

class TaskReminderDefaultTimeTest {
    @Test
    fun `given future default combination when checked then action is available`() {
        val result =
            isFutureDefaultReminder(
                date = LocalDate(2030, 1, 2),
                defaultTime = LocalTime(9, 0),
                now = LocalDateTime(2030, 1, 1, 18, 0),
            )

        assertThat(result).isTrue()
    }

    @Test
    fun `given todays default has passed when checked then action is unavailable`() {
        val result =
            isFutureDefaultReminder(
                date = LocalDate(2030, 1, 1),
                defaultTime = LocalTime(9, 0),
                now = LocalDateTime(2030, 1, 1, 9, 1),
            )

        assertThat(result).isFalse()
    }
}
