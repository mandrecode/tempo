package com.mandrecode.tempo.infrastructure.reminders.receivers

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class HabitReminderReceiverTest {
    private val context: Context = mockk(relaxed = true)

    @Test
    fun `shouldShowHabitReminder returns false for completed habits`() {
        val habit =
            Habit(
                id = 1L,
                title = "Walk",
                description = "",
                reminderDate = LocalDateTime(2030, 1, 1, 8, 0),
                isCompleted = true,
                createdDate = LocalDateTime(2024, 1, 1, 0, 0),
            )

        val result = HabitReminderReceiver.shouldShowHabitReminder(habit)

        assertThat(result).isFalse()
    }

    @Test
    fun `shouldShowHabitReminder returns true for incomplete habits`() {
        val habit =
            Habit(
                id = 1L,
                title = "Walk",
                description = "",
                reminderDate = LocalDateTime(2030, 1, 1, 8, 0),
                isCompleted = false,
                createdDate = LocalDateTime(2024, 1, 1, 0, 0),
            )

        val result = HabitReminderReceiver.shouldShowHabitReminder(habit)

        assertThat(result).isTrue()
    }

    @Test
    fun `buildHabitNotificationContentText returns quit-specific copy for QUIT habits`() {
        val expected = "Still going strong? Check off Smoking"
        every {
            context.getString(R.string.quit_habit_reminder_text, "Smoking")
        } returns expected

        val result =
            HabitReminderReceiver.buildHabitNotificationContentText(
                context = context,
                title = "Smoking",
                description = "any description is ignored",
                habitType = HabitType.QUIT,
            )

        assertThat(result).isEqualTo(expected)
        verify { context.getString(R.string.quit_habit_reminder_text, "Smoking") }
    }

    @Test
    fun `buildHabitNotificationContentText returns description for BUILD habits with description`() {
        val result =
            HabitReminderReceiver.buildHabitNotificationContentText(
                context = context,
                title = "Walk",
                description = "30 minutes around the park",
                habitType = HabitType.BUILD,
            )

        assertThat(result).isEqualTo("30 minutes around the park")
        verify(exactly = 0) { context.getString(R.string.quit_habit_reminder_text, any<String>()) }
    }

    @Test
    fun `buildHabitNotificationContentText returns null for BUILD habits with empty description`() {
        val result =
            HabitReminderReceiver.buildHabitNotificationContentText(
                context = context,
                title = "Walk",
                description = "",
                habitType = HabitType.BUILD,
            )

        assertThat(result).isNull()
    }

    @Test
    fun `buildHabitNotificationActionLabel returns still-on-track for QUIT habits`() {
        every { context.getString(R.string.still_on_track) } returns "Still on track"

        val result =
            HabitReminderReceiver.buildHabitNotificationActionLabel(
                context = context,
                habitType = HabitType.QUIT,
            )

        assertThat(result).isEqualTo("Still on track")
        verify { context.getString(R.string.still_on_track) }
        verify(exactly = 0) { context.getString(R.string.mark_as_completed) }
    }

    @Test
    fun `buildHabitNotificationActionLabel returns mark-as-completed for BUILD habits`() {
        every { context.getString(R.string.mark_as_completed) } returns "Mark as completed"

        val result =
            HabitReminderReceiver.buildHabitNotificationActionLabel(
                context = context,
                habitType = HabitType.BUILD,
            )

        assertThat(result).isEqualTo("Mark as completed")
        verify { context.getString(R.string.mark_as_completed) }
        verify(exactly = 0) { context.getString(R.string.still_on_track) }
    }
}
