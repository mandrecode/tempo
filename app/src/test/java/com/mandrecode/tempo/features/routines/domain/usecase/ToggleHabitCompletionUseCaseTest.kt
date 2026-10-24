package com.mandrecode.tempo.features.routines.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.model.HabitType
import com.mandrecode.tempo.features.routines.domain.repository.HabitChainRepository
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock

class ToggleHabitCompletionUseCaseTest {
    private lateinit var habitRepository: HabitRepository
    private lateinit var habitChainRepository: HabitChainRepository
    private lateinit var habitReminderScheduler: HabitReminderScheduler
    private lateinit var updateHabitUseCase: UpdateHabitUseCase
    private lateinit var useCase: ToggleHabitCompletionUseCase

    // Fixed date: Sunday, June 15, 2025
    private val today = LocalDate(2025, 6, 15)
    private val yesterday = LocalDate(2025, 6, 14)

    private val testClock: Clock =
        object : Clock {
            override fun now() =
                LocalDateTime(2025, 6, 15, 12, 0)
                    .toInstant(TimeZone.currentSystemDefault())
        }

    @Before
    fun setup() {
        habitRepository = mockk(relaxed = true)
        habitChainRepository = mockk(relaxed = true)
        habitReminderScheduler = mockk(relaxed = true)
        updateHabitUseCase = mockk(relaxed = true)

        coEvery { updateHabitUseCase.invoke(any()) } returns
            UpdateHabitUseCase.Result.Success(ScheduleResult.Skipped)
        coEvery { habitChainRepository.getChainsForHabit(any()) } returns emptyList()

        useCase =
            ToggleHabitCompletionUseCase(
                habitRepository,
                habitChainRepository,
                habitReminderScheduler,
                updateHabitUseCase,
                testClock,
            )
    }

    @Test
    fun `completing habit with reminder and repeat days reschedules via updateHabitUseCase`() =
        runTest {
            val habit =
                habit(
                    reminderDate = LocalDateTime(2025, 6, 14, 9, 0),
                    repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                )
            coEvery { habitRepository.getHabitById(1L) } returns habit

            useCase(1L, true, today)

            coVerify { habitRepository.toggleHabitCompletion(1L, true, today) }
            coVerify(exactly = 0) { habitReminderScheduler.cancelHabit(any<Habit>()) }
            coVerify { updateHabitUseCase.invoke(any()) }
        }

    @Test
    fun `completing habit without repeat days defaults to daily and reschedules`() =
        runTest {
            val habit =
                habit(
                    reminderDate = LocalDateTime(2025, 6, 14, 9, 0),
                    repeatDays = null,
                )
            coEvery { habitRepository.getHabitById(1L) } returns habit

            useCase(1L, true, today)

            coVerify { habitRepository.toggleHabitCompletion(1L, true, today) }
            coVerify(exactly = 0) { habitReminderScheduler.cancelHabit(any<Habit>()) }
            coVerify { updateHabitUseCase.invoke(any()) }
        }

    @Test
    fun `completing habit without reminder does not reschedule`() =
        runTest {
            val habit = habit(reminderDate = null, repeatDays = null)
            coEvery { habitRepository.getHabitById(1L) } returns habit

            useCase(1L, true, today)

            coVerify { habitRepository.toggleHabitCompletion(1L, true, today) }
            coVerify { habitReminderScheduler.cancelHabit(habit) }
            coVerify(exactly = 0) { updateHabitUseCase.invoke(any()) }
        }

    @Test
    fun `completing habit with empty repeat days defaults to daily and reschedules`() =
        runTest {
            val habit =
                habit(
                    reminderDate = LocalDateTime(2025, 6, 14, 9, 0),
                    repeatDays = emptySet(),
                )
            coEvery { habitRepository.getHabitById(1L) } returns habit

            useCase(1L, true, today)

            coVerify(exactly = 0) { habitReminderScheduler.cancelHabit(any<Habit>()) }
            coVerify { updateHabitUseCase.invoke(any()) }
        }

    @Test
    fun `uncompleting habit restores reminder to today`() =
        runTest {
            val habit =
                habit(
                    reminderDate = LocalDateTime(2025, 6, 16, 14, 0),
                    repeatDays = setOf(DayOfWeek.SUNDAY),
                )
            coEvery { habitRepository.getHabitById(1L) } returns habit

            val habitSlot = slot<Habit>()
            coEvery { updateHabitUseCase.invoke(capture(habitSlot)) } returns
                UpdateHabitUseCase.Result.Success(ScheduleResult.Skipped)

            useCase(1L, false, today)

            coVerify { habitRepository.toggleHabitCompletion(1L, false, today) }
            coVerify(exactly = 0) { habitReminderScheduler.cancelHabit(any<Habit>()) }

            val restoredReminder = habitSlot.captured.reminderDate
            assertThat(restoredReminder).isNotNull()
            assertThat(restoredReminder!!.date).isEqualTo(today)
            assertThat(restoredReminder.hour).isEqualTo(14)
            assertThat(restoredReminder.minute).isEqualTo(0)
        }

    @Test
    fun `uncompleting habit restores and advances chain reminder`() =
        runTest {
            val habit = habit(reminderDate = null)
            val chain =
                chain(
                    id = 10L,
                    habitIds = listOf(1L, 2L),
                    periodicReminder = LocalDateTime(2025, 6, 16, 14, 0),
                    repeatDays = setOf(DayOfWeek.SUNDAY),
                )
            coEvery { habitRepository.getHabitById(1L) } returns habit
            coEvery { habitChainRepository.getChainsForHabit(1L) } returns listOf(chain)

            useCase(1L, false, today)

            coVerify { habitChainRepository.updateHabitChain(any()) }
            coVerify { habitReminderScheduler.cancelHabitChain(chain) }

            val chainSlot = slot<HabitChain>()
            coVerify { habitReminderScheduler.scheduleHabitChain(capture(chainSlot)) }

            val updatedChain = chainSlot.captured
            val reminder = updatedChain.periodicReminder
            assertThat(reminder).isNotNull()
            // Should restore to today, and since it's 14:00 and today is Sunday (in repeatDays), it stays today
            assertThat(reminder!!.date).isEqualTo(today)
            assertThat(reminder.hour).isEqualTo(14)
            assertThat(reminder.minute).isEqualTo(0)
        }

    @Test
    fun `completing non-existent habit does not crash`() =
        runTest {
            coEvery { habitRepository.getHabitById(999L) } returns null

            useCase(999L, true, today)

            coVerify { habitRepository.toggleHabitCompletion(999L, true, today) }
            coVerify(exactly = 0) { habitReminderScheduler.cancelHabit(any<Habit>()) }
        }

    @Test
    fun `completing habit with repeat days advances reminder to next valid date`() =
        runTest {
            val habit =
                habit(
                    reminderDate = LocalDateTime(2025, 6, 14, 9, 0),
                    repeatDays = setOf(DayOfWeek.WEDNESDAY),
                )
            coEvery { habitRepository.getHabitById(1L) } returns habit

            val habitSlot = slot<Habit>()
            coEvery { updateHabitUseCase.invoke(capture(habitSlot)) } returns
                UpdateHabitUseCase.Result.Success(ScheduleResult.Skipped)

            useCase(1L, true, today)

            assertThat(habitSlot.captured.reminderDate).isNotNull()
            assertThat(habitSlot.captured.reminderDate!!.hour).isEqualTo(9)
            assertThat(habitSlot.captured.reminderDate!!.minute).isEqualTo(0)
        }

    // --- Chain rescheduling ---

    @Test
    fun `completing last habit in chain reschedules chain reminder`() =
        runTest {
            val habit1 =
                habit(id = 1L, completionHistory = today.toString())
            val habit2 =
                habit(id = 2L, completionHistory = today.toString())
            val chain =
                chain(
                    habitIds = listOf(1L, 2L),
                    periodicReminder = LocalDateTime(2025, 6, 15, 8, 0),
                    repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                )

            coEvery { habitRepository.getHabitById(2L) } returns habit2
            coEvery { habitChainRepository.getChainsForHabit(2L) } returns listOf(chain)
            coEvery { habitRepository.getHabitsByIds(listOf(1L, 2L)) } returns listOf(habit1, habit2)
            coEvery { habitReminderScheduler.scheduleHabitChain(any()) } returns
                ScheduleResult.Success(LocalDateTime(2025, 6, 16, 8, 0))

            useCase(2L, true, today)

            coVerify { habitReminderScheduler.cancelHabitChain(chain) }
            coVerify { habitReminderScheduler.scheduleHabitChain(any()) }
            coVerify { habitChainRepository.updateHabitChain(any()) }
        }

    @Test
    fun `completing habit in chain does not reschedule if not all habits complete`() =
        runTest {
            val habit1 =
                habit(id = 1L, completionHistory = today.toString())
            val habit2 =
                habit(id = 2L, completionHistory = "")
            val chain =
                chain(
                    habitIds = listOf(1L, 2L),
                    periodicReminder = LocalDateTime(2025, 6, 15, 8, 0),
                    repeatDays = setOf(DayOfWeek.MONDAY),
                )

            coEvery { habitRepository.getHabitById(1L) } returns habit1
            coEvery { habitChainRepository.getChainsForHabit(1L) } returns listOf(chain)
            coEvery { habitRepository.getHabitsByIds(listOf(1L, 2L)) } returns listOf(habit1, habit2)

            useCase(1L, true, today)

            coVerify(exactly = 0) { habitReminderScheduler.cancelHabitChain(any()) }
            coVerify(exactly = 0) { habitReminderScheduler.scheduleHabitChain(any()) }
            coVerify(exactly = 0) { habitChainRepository.updateHabitChain(any()) }
        }

    @Test
    fun `completing habit in chain without periodicReminder does not reschedule chain`() =
        runTest {
            val habit1 =
                habit(id = 1L, completionHistory = today.toString())
            val chain =
                chain(
                    habitIds = listOf(1L),
                    periodicReminder = null,
                    repeatDays = setOf(DayOfWeek.MONDAY),
                )

            coEvery { habitRepository.getHabitById(1L) } returns habit1
            coEvery { habitChainRepository.getChainsForHabit(1L) } returns listOf(chain)

            useCase(1L, true, today)

            coVerify(exactly = 0) { habitReminderScheduler.cancelHabitChain(any()) }
            coVerify(exactly = 0) { habitReminderScheduler.scheduleHabitChain(any()) }
        }

    @Test
    fun `completing chain without repeatDays defaults to daily and reschedules`() =
        runTest {
            val habit1 =
                habit(id = 1L, completionHistory = today.toString())
            val chain =
                chain(
                    habitIds = listOf(1L),
                    periodicReminder = LocalDateTime(2025, 6, 15, 8, 0),
                    repeatDays = null,
                )

            coEvery { habitRepository.getHabitById(1L) } returns habit1
            coEvery { habitChainRepository.getChainsForHabit(1L) } returns listOf(chain)
            coEvery { habitRepository.getHabitsByIds(listOf(1L)) } returns listOf(habit1)
            coEvery { habitReminderScheduler.scheduleHabitChain(any()) } returns
                ScheduleResult.Success(LocalDateTime(2025, 6, 16, 8, 0))

            useCase(1L, true, today)

            coVerify { habitReminderScheduler.cancelHabitChain(chain) }
            coVerify { habitReminderScheduler.scheduleHabitChain(any()) }
            coVerify { habitChainRepository.updateHabitChain(any()) }
        }

    @Test
    fun `completing chain advances reminder to next matching day of week`() =
        runTest {
            // today is Sunday June 15 2025, next Wednesday is June 18
            val habit1 =
                habit(id = 1L, completionHistory = today.toString())
            val chain =
                chain(
                    habitIds = listOf(1L),
                    periodicReminder = LocalDateTime(2025, 6, 15, 8, 0),
                    repeatDays = setOf(DayOfWeek.WEDNESDAY),
                )

            coEvery { habitRepository.getHabitById(1L) } returns habit1
            coEvery { habitChainRepository.getChainsForHabit(1L) } returns listOf(chain)
            coEvery { habitRepository.getHabitsByIds(listOf(1L)) } returns listOf(habit1)

            val chainSlot = slot<HabitChain>()
            coEvery { habitChainRepository.updateHabitChain(capture(chainSlot)) } returns Unit
            coEvery { habitReminderScheduler.scheduleHabitChain(any()) } returns
                ScheduleResult.Success(LocalDateTime(2025, 6, 18, 8, 0))

            useCase(1L, true, today)

            val nextReminder = chainSlot.captured.periodicReminder
            assertThat(nextReminder).isNotNull()
            // Must advance to Wednesday June 18, 2025
            assertThat(nextReminder!!.date).isEqualTo(LocalDate(2025, 6, 18))
            assertThat(nextReminder.hour).isEqualTo(8)
            assertThat(nextReminder.minute).isEqualTo(0)
        }

    @Test
    fun `fromNotification flag is forwarded to repository`() =
        runTest {
            coEvery { habitRepository.getHabitById(1L) } returns null

            useCase(1L, true, today, fromNotification = true)

            coVerify {
                habitRepository.toggleHabitCompletion(
                    1L,
                    true,
                    today,
                    fromNotification = true,
                )
            }
        }

    // --- Today guard ---

    @Test
    fun `completing habit for yesterday does not reschedule`() =
        runTest {
            val habit =
                habit(
                    reminderDate = LocalDateTime(2025, 6, 14, 9, 0),
                    repeatDays = setOf(DayOfWeek.MONDAY),
                )
            coEvery { habitRepository.getHabitById(1L) } returns habit

            useCase(1L, true, yesterday)

            coVerify { habitRepository.toggleHabitCompletion(1L, true, yesterday) }
            coVerify(exactly = 0) { updateHabitUseCase.invoke(any()) }
            coVerify(exactly = 0) { habitReminderScheduler.cancelHabit(any<Habit>()) }
        }

    @Test
    fun `completing chain for yesterday does not reschedule`() =
        runTest {
            val habit1 =
                habit(id = 1L, completionHistory = yesterday.toString())
            val chain =
                chain(
                    habitIds = listOf(1L),
                    periodicReminder = LocalDateTime(2025, 6, 15, 8, 0),
                    repeatDays = setOf(DayOfWeek.MONDAY),
                )

            coEvery { habitRepository.getHabitById(1L) } returns habit1
            coEvery { habitChainRepository.getChainsForHabit(1L) } returns listOf(chain)
            coEvery { habitRepository.getHabitsByIds(listOf(1L)) } returns listOf(habit1)

            useCase(1L, true, yesterday)

            coVerify(exactly = 0) { habitReminderScheduler.cancelHabitChain(any()) }
            coVerify(exactly = 0) { habitReminderScheduler.scheduleHabitChain(any()) }
        }

    @Test
    fun `completing habit before reminder time today still advances to next occurrence`() =
        runTest {
            // Reminder is later today (14:00) but user already completed at 12:00;
            // the reminder should advance past today to avoid a redundant notification.
            val habit =
                habit(
                    reminderDate = LocalDateTime(2025, 6, 15, 14, 0),
                    repeatDays = DayOfWeek.ALL_DAYS,
                )
            coEvery { habitRepository.getHabitById(1L) } returns habit

            val habitSlot = slot<Habit>()
            coEvery { updateHabitUseCase.invoke(capture(habitSlot)) } returns
                UpdateHabitUseCase.Result.Success(ScheduleResult.Skipped)

            useCase(1L, true, today)

            coVerify { updateHabitUseCase.invoke(any()) }
            // Must advance past today — tomorrow (Monday, June 16)
            assertThat(habitSlot.captured.reminderDate!!.date).isEqualTo(LocalDate(2025, 6, 16))
            assertThat(habitSlot.captured.reminderDate!!.hour).isEqualTo(14)
            assertThat(habitSlot.captured.reminderDate!!.minute).isEqualTo(0)
        }

    @Test
    fun `completing habit does not advance reminder already set for tomorrow`() =
        runTest {
            // Reminder was already advanced to tomorrow (Monday) by the notification receiver
            val habit =
                habit(
                    reminderDate = LocalDateTime(2025, 6, 16, 9, 0),
                    repeatDays = DayOfWeek.ALL_DAYS,
                )
            coEvery { habitRepository.getHabitById(1L) } returns habit

            val habitSlot = slot<Habit>()
            coEvery { updateHabitUseCase.invoke(capture(habitSlot)) } returns
                UpdateHabitUseCase.Result.Success(ScheduleResult.Skipped)

            useCase(1L, true, today)

            // updateHabitUseCase re-syncs alarms; the reminder date stays unchanged
            coVerify { updateHabitUseCase.invoke(any()) }
            assertThat(habitSlot.captured.reminderDate).isEqualTo(LocalDateTime(2025, 6, 16, 9, 0))
        }

    @Test
    fun `completing habit from notification does not advance reminder already set for tomorrow`() =
        runTest {
            // Reminder already advanced to tomorrow by the receiver
            val habit =
                habit(
                    reminderDate = LocalDateTime(2025, 6, 16, 9, 0),
                    repeatDays = DayOfWeek.ALL_DAYS,
                )
            coEvery { habitRepository.getHabitById(1L) } returns habit

            val habitSlot = slot<Habit>()
            coEvery { updateHabitUseCase.invoke(capture(habitSlot)) } returns
                UpdateHabitUseCase.Result.Success(ScheduleResult.Skipped)

            useCase(1L, true, today, fromNotification = true)

            coVerify { updateHabitUseCase.invoke(any()) }
            assertThat(habitSlot.captured.reminderDate).isEqualTo(LocalDateTime(2025, 6, 16, 9, 0))
        }

    @Test
    fun `completing chain re-syncs alarm without advancing reminder already set for tomorrow`() =
        runTest {
            val habit1 =
                habit(id = 1L, completionHistory = today.toString())
            val chain =
                chain(
                    habitIds = listOf(1L),
                    // Reminder already advanced to tomorrow by the notification receiver
                    periodicReminder = LocalDateTime(2025, 6, 16, 8, 0),
                    repeatDays = DayOfWeek.ALL_DAYS,
                )

            coEvery { habitRepository.getHabitById(1L) } returns habit1
            coEvery { habitChainRepository.getChainsForHabit(1L) } returns listOf(chain)
            coEvery { habitRepository.getHabitsByIds(listOf(1L)) } returns listOf(habit1)
            coEvery { habitReminderScheduler.scheduleHabitChain(any()) } returns
                ScheduleResult.Success(LocalDateTime(2025, 6, 16, 8, 0))

            val chainSlot = slot<HabitChain>()
            coEvery { habitChainRepository.updateHabitChain(capture(chainSlot)) } returns Unit

            useCase(1L, true, today)

            // Alarms are re-synced but the reminder date is not advanced
            coVerify { habitReminderScheduler.cancelHabitChain(chain) }
            coVerify { habitReminderScheduler.scheduleHabitChain(any()) }
            assertThat(chainSlot.captured.periodicReminder).isEqualTo(LocalDateTime(2025, 6, 16, 8, 0))
        }

    @Test
    fun `completing chain before reminder time today still advances to next occurrence`() =
        runTest {
            // Chain reminder is later today (14:00) but all habits are already completed at 12:00
            val habit1 =
                habit(id = 1L, completionHistory = today.toString())
            val chain =
                chain(
                    habitIds = listOf(1L),
                    periodicReminder = LocalDateTime(2025, 6, 15, 14, 0),
                    repeatDays = DayOfWeek.ALL_DAYS,
                )

            coEvery { habitRepository.getHabitById(1L) } returns habit1
            coEvery { habitChainRepository.getChainsForHabit(1L) } returns listOf(chain)
            coEvery { habitRepository.getHabitsByIds(listOf(1L)) } returns listOf(habit1)
            coEvery { habitReminderScheduler.scheduleHabitChain(any()) } returns
                ScheduleResult.Success(LocalDateTime(2025, 6, 16, 14, 0))

            val chainSlot = slot<HabitChain>()
            coEvery { habitChainRepository.updateHabitChain(capture(chainSlot)) } returns Unit

            useCase(1L, true, today)

            coVerify { habitReminderScheduler.cancelHabitChain(chain) }
            coVerify { habitReminderScheduler.scheduleHabitChain(any()) }
            // Must advance past today — tomorrow (Monday, June 16)
            assertThat(chainSlot.captured.periodicReminder!!.date).isEqualTo(LocalDate(2025, 6, 16))
            assertThat(chainSlot.captured.periodicReminder!!.hour).isEqualTo(14)
        }

    @Test
    fun `fromNotification reschedules even if selectedDate is not today`() =
        runTest {
            val habit =
                habit(
                    reminderDate = LocalDateTime(2025, 6, 14, 9, 0),
                    repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                )
            coEvery { habitRepository.getHabitById(1L) } returns habit

            useCase(1L, true, yesterday, fromNotification = true)

            coVerify { updateHabitUseCase.invoke(any()) }
        }

    @Test
    fun `fromNotification reschedules chain even if selectedDate is not today`() =
        runTest {
            val habit1 =
                habit(id = 1L, completionHistory = yesterday.toString())
            val chain =
                chain(
                    habitIds = listOf(1L),
                    periodicReminder = LocalDateTime(2025, 6, 14, 8, 0),
                    repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                )

            coEvery { habitRepository.getHabitById(1L) } returns habit1
            coEvery { habitChainRepository.getChainsForHabit(1L) } returns listOf(chain)
            coEvery { habitRepository.getHabitsByIds(listOf(1L)) } returns listOf(habit1)
            coEvery { habitReminderScheduler.scheduleHabitChain(any()) } returns
                ScheduleResult.Success(LocalDateTime(2025, 6, 16, 8, 0))

            useCase(1L, true, yesterday, fromNotification = true)

            coVerify { habitReminderScheduler.cancelHabitChain(chain) }
            coVerify { habitReminderScheduler.scheduleHabitChain(any()) }
            coVerify { habitChainRepository.updateHabitChain(any()) }
        }

    @Test
    fun `fromNotification forwards scheduledDate to repository`() =
        runTest {
            coEvery { habitRepository.getHabitById(1L) } returns null

            useCase(1L, true, yesterday, fromNotification = true)

            coVerify {
                habitRepository.toggleHabitCompletion(
                    1L,
                    true,
                    yesterday,
                    fromNotification = true,
                )
            }
        }

    // --- Stale chain member ---

    @Test
    fun `chain not rescheduled when a habit is missing from repository`() =
        runTest {
            val habit1 =
                habit(id = 1L, completionHistory = today.toString())
            val chain =
                chain(
                    habitIds = listOf(1L, 2L),
                    periodicReminder = LocalDateTime(2025, 6, 15, 8, 0),
                    repeatDays = setOf(DayOfWeek.MONDAY),
                )

            coEvery { habitRepository.getHabitById(1L) } returns habit1
            coEvery { habitChainRepository.getChainsForHabit(1L) } returns listOf(chain)
            coEvery { habitRepository.getHabitsByIds(listOf(1L, 2L)) } returns listOf(habit1)

            useCase(1L, true, today)

            coVerify(exactly = 0) { habitReminderScheduler.cancelHabitChain(any()) }
            coVerify(exactly = 0) { habitReminderScheduler.scheduleHabitChain(any()) }
        }

    private fun habit(
        id: Long = 1L,
        reminderDate: LocalDateTime? = null,
        repeatDays: Set<DayOfWeek>? = null,
        completionHistory: String = "",
        habitType: HabitType = HabitType.BUILD,
    ) = Habit(
        id = id,
        title = "Test Habit",
        description = "Test Description",
        reminderDate = reminderDate,
        habitType = habitType,
        createdDate = LocalDateTime(2025, 1, 1, 0, 0),
        repeatDays = repeatDays,
        completionHistory = completionHistory,
    )

    private fun chain(
        id: Long = 10L,
        habitIds: List<Long> = emptyList(),
        periodicReminder: LocalDateTime? = null,
        repeatDays: Set<DayOfWeek>? = null,
    ) = HabitChain(
        id = id,
        title = "Test Chain",
        description = "Test Description",
        habitIds = habitIds,
        periodicReminder = periodicReminder,
        createdDate = LocalDateTime(2025, 1, 1, 0, 0),
        repeatDays = repeatDays,
    )
}
