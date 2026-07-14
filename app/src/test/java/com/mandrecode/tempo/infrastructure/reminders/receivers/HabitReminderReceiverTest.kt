package com.mandrecode.tempo.infrastructure.reminders.receivers

import android.content.Context
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitType
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class HabitReminderReceiverTest {
    private val context: Context = mockk(relaxed = true)

    @Test
    fun `shouldShowHabitReminder returns false when scheduled date is completed`() {
        val scheduledDate = LocalDateTime(2030, 1, 1, 8, 0).date
        val habit =
            Habit(
                id = 1L,
                title = "Walk",
                description = "",
                reminderDate = LocalDateTime(2030, 1, 1, 8, 0),
                isCompleted = true,
                createdDate = LocalDateTime(2024, 1, 1, 0, 0),
                completionHistory = "2030-01-01",
            )

        val result = HabitReminderReceiver.shouldShowHabitReminder(habit, scheduledDate)

        assertThat(result).isFalse()
    }

    @Test
    fun `shouldShowHabitReminder returns true when only legacy completed flag is true`() {
        val scheduledDate = LocalDateTime(2030, 1, 2, 8, 0).date
        val habit =
            Habit(
                id = 1L,
                title = "Walk",
                description = "",
                reminderDate = LocalDateTime(2030, 1, 2, 8, 0),
                isCompleted = true,
                createdDate = LocalDateTime(2024, 1, 1, 0, 0),
                completionHistory = "2030-01-01",
            )

        val result = HabitReminderReceiver.shouldShowHabitReminder(habit, scheduledDate)

        assertThat(result).isTrue()
    }

    @Test
    fun `shouldShowHabitReminder returns false for delayed completed occurrence after reminder advanced`() {
        val delayedOccurrenceDate = LocalDateTime(2030, 1, 1, 8, 0).date
        val habit =
            Habit(
                id = 1L,
                title = "Walk",
                description = "",
                reminderDate = LocalDateTime(2030, 1, 2, 8, 0),
                isCompleted = true,
                createdDate = LocalDateTime(2024, 1, 1, 0, 0),
                completionHistory = "2030-01-01",
            )

        val result = HabitReminderReceiver.shouldShowHabitReminder(habit, delayedOccurrenceDate)

        assertThat(result).isFalse()
    }

    @Test
    fun `shouldShowHabitChainReminder returns true when chain has not started`() {
        val scheduledDate = LocalDateTime(2030, 1, 1, 8, 0).date
        val habits =
            listOf(
                habit(id = 1L, completionHistory = ""),
                habit(id = 2L, completionHistory = "2029-12-31"),
            )

        val result = HabitReminderReceiver.shouldShowHabitChainReminder(habits, scheduledDate)

        assertThat(result).isTrue()
    }

    @Test
    fun `shouldShowHabitChainReminder returns false when chain was started manually`() {
        val scheduledDate = LocalDateTime(2030, 1, 1, 8, 0).date
        val habits =
            listOf(
                habit(id = 1L, completionHistory = "2030-01-01"),
                habit(id = 2L, completionHistory = ""),
            )

        val result = HabitReminderReceiver.shouldShowHabitChainReminder(habits, scheduledDate)

        assertThat(result).isFalse()
    }

    @Test
    fun `shouldShowHabitChainReminder returns true when completion belongs to another date`() {
        val scheduledDate = LocalDateTime(2030, 1, 1, 8, 0).date
        val habits =
            listOf(
                habit(id = 1L, completionHistory = "2030-01-02"),
                habit(id = 2L, completionHistory = "2029-12-31"),
            )

        val result = HabitReminderReceiver.shouldShowHabitChainReminder(habits, scheduledDate)

        assertThat(result).isTrue()
    }

    @Test
    fun `getChainHabits skips repository lookup for empty chain`() =
        runTest {
            val habitRepository = mockk<HabitRepository>(relaxed = true)
            val receiver = HabitReminderReceiver().apply { this.habitRepository = habitRepository }

            val result = receiver.getChainHabits(emptyList())

            assertThat(result).isEmpty()
            coVerify(exactly = 0) { habitRepository.getHabitsByIds(any()) }
        }

    @Test
    fun `resolveScheduledDate prefers alarm occurrence date after reminder advances`() {
        val advancedReminderDate = LocalDate(2030, 1, 2)

        val result = HabitReminderReceiver.resolveScheduledDate("2030-01-01", advancedReminderDate)

        assertThat(result).isEqualTo(LocalDate(2030, 1, 1))
    }

    @Test
    fun `resolveScheduledDate falls back when alarm occurrence date is invalid`() {
        val fallbackDate = LocalDate(2030, 1, 2)

        val result = HabitReminderReceiver.resolveScheduledDate("not-a-date", fallbackDate)

        assertThat(result).isEqualTo(fallbackDate)
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

    private fun habit(
        id: Long,
        completionHistory: String,
    ) = Habit(
        id = id,
        title = "Habit $id",
        description = "",
        reminderDate = null,
        createdDate = LocalDateTime(2024, 1, 1, 0, 0),
        completionHistory = completionHistory,
    )
}
