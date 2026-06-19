package com.mandrecode.tempo.features.routines.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitType
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock

class UpdateHabitUseCaseTest {
    private lateinit var useCase: UpdateHabitUseCase
    private lateinit var habitRepository: HabitRepository
    private lateinit var habitReminderScheduler: HabitReminderScheduler

    // Fixed clock: 2025-06-15T12:00 (Sunday)
    private val testClock: Clock =
        object : Clock {
            override fun now() =
                LocalDateTime(2025, 6, 15, 12, 0)
                    .toInstant(TimeZone.currentSystemDefault())
        }

    @Before
    fun setup() {
        habitRepository = mockk(relaxed = true)
        habitReminderScheduler = mockk(relaxed = true)
        coEvery { habitReminderScheduler.scheduleHabit(any()) } returns ScheduleResult.Skipped
        useCase = UpdateHabitUseCase(habitRepository, habitReminderScheduler, testClock)
    }

    @Test
    fun `updating habit always cancels previous reminder to dismiss notification`() =
        runTest {
            val habit = habit(reminderDate = LocalDateTime(2025, 6, 15, 9, 0))

            useCase(habit)

            coVerify { habitReminderScheduler.cancelHabit(match<Habit> { it.id == habit.id }) }
        }

    @Test
    fun `updating habit with reminder reschedules after cancelling`() =
        runTest {
            val reminder = LocalDateTime(2099, 1, 1, 9, 0)
            val habit = habit(reminderDate = reminder)
            coEvery { habitReminderScheduler.scheduleHabit(any<Habit>()) } returns ScheduleResult.Success(reminder)

            val result = useCase(habit)

            coVerifyOrder {
                habitReminderScheduler.cancelHabit(match<Habit> { it.id == habit.id })
                habitReminderScheduler.scheduleHabit(match<Habit> { it.id == habit.id && it.reminderDate == reminder })
            }
            assertThat((result as UpdateHabitUseCase.Result.Success).scheduleResult)
                .isEqualTo(ScheduleResult.Success(reminder))
        }

    @Test
    fun `updating habit without reminder cancels and skips scheduling`() =
        runTest {
            val habit = habit(reminderDate = null)

            val result = useCase(habit)

            coVerify { habitReminderScheduler.cancelHabit(match<Habit> { it.id == habit.id }) }
            coVerify(exactly = 0) { habitReminderScheduler.scheduleHabit(any()) }
            assertThat((result as UpdateHabitUseCase.Result.Success).scheduleResult)
                .isEqualTo(ScheduleResult.Skipped)
        }

    @Test
    fun `updating habit persists to repository`() =
        runTest {
            val habit = habit()

            useCase(habit)

            val savedHabitSlot = slot<Habit>()
            coVerify { habitRepository.updateHabit(capture(savedHabitSlot)) }
            assertThat(savedHabitSlot.captured).isEqualTo(habit)
        }

    @Test
    fun `empty title returns validation error`() =
        runTest {
            val habit = habit(title = "")

            val result = useCase(habit)

            assertThat(result).isInstanceOf(UpdateHabitUseCase.Result.ValidationError::class.java)
            assertThat((result as UpdateHabitUseCase.Result.ValidationError).type)
                .isEqualTo(CreateHabitUseCase.ValidationErrorType.TITLE_EMPTY)
        }

    @Test
    fun `validation error does not cancel or schedule reminders`() =
        runTest {
            val habit = habit(title = "")

            useCase(habit)

            coVerify(exactly = 0) { habitReminderScheduler.cancelHabit(any<Habit>()) }
            coVerify(exactly = 0) { habitReminderScheduler.scheduleHabit(any()) }
            coVerify(exactly = 0) { habitRepository.updateHabit(any()) }
        }

    @Test
    fun `updating habit with past reminder and repeat days advances to future date`() =
        runTest {
            val pastReminder = LocalDateTime(2020, 1, 1, 10, 0)
            val habit = habit(reminderDate = pastReminder, repeatDays = setOf(DayOfWeek.WEDNESDAY))

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.updateHabit(capture(habitSlot)) } returns Unit

            val result = useCase(habit)

            val savedReminder = habitSlot.captured.reminderDate
            val now = testClock.now().toLocalDateTime(TimeZone.currentSystemDefault())

            assertThat(savedReminder).isNotNull()
            assertThat(savedReminder!! > now).isTrue()
            assertThat(savedReminder.hour).isEqualTo(pastReminder.hour)
            assertThat(savedReminder.minute).isEqualTo(pastReminder.minute)
            assertThat((result as UpdateHabitUseCase.Result.Success).reminderAdvanced).isTrue()
        }

    @Test
    fun `updating habit with past reminder and no repeat days defaults to daily and advances`() =
        runTest {
            val pastReminder = LocalDateTime(2020, 1, 1, 10, 0)
            val habit = habit(reminderDate = pastReminder, repeatDays = null)

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.updateHabit(capture(habitSlot)) } returns Unit

            val result = useCase(habit)

            val savedReminder = habitSlot.captured.reminderDate
            val now = testClock.now().toLocalDateTime(TimeZone.currentSystemDefault())

            assertThat(savedReminder).isNotNull()
            assertThat(savedReminder!! > now).isTrue()
            assertThat((result as UpdateHabitUseCase.Result.Success).reminderAdvanced).isTrue()
        }

    @Test
    fun `updating habit trims title and description before saving`() =
        runTest {
            val habit = habit(title = "  Padded Title  ", description = "  Padded Desc  ")

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.updateHabit(capture(habitSlot)) } returns Unit

            useCase(habit)

            assertThat(habitSlot.captured.title).isEqualTo("Padded Title")
            assertThat(habitSlot.captured.description).isEqualTo("Padded Desc")
        }

    @Test
    fun `updating habit with future reminder on matching repeat day does not advance`() =
        runTest {
            // 2099-01-05 is a Monday
            val futureReminder = LocalDateTime(2099, 1, 5, 10, 0)
            val habit = habit(reminderDate = futureReminder, repeatDays = setOf(DayOfWeek.MONDAY))

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.updateHabit(capture(habitSlot)) } returns Unit

            val result = useCase(habit)

            assertThat(habitSlot.captured.reminderDate).isEqualTo(futureReminder)
            assertThat((result as UpdateHabitUseCase.Result.Success).reminderAdvanced).isFalse()
        }

    @Test
    fun `updating quit habit coerces repeatDays to null`() =
        runTest {
            val habit =
                habit(
                    habitType = HabitType.QUIT,
                    repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                )

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.updateHabit(capture(habitSlot)) } returns Unit

            useCase(habit)

            assertThat(habitSlot.captured.repeatDays).isNull()
        }

    // --- Quit habit auto-reminder (mirrors CreateHabitUseCase) ---

    @Test
    fun `updating quit habit without reminder auto-sets 21-00 reminder today when before 21-00`() =
        runTest {
            // testClock is 2025-06-15T12:00 — before 21:00, so today's 21:00 is the next upcoming.
            val habit = habit(habitType = HabitType.QUIT, reminderDate = null)

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.updateHabit(capture(habitSlot)) } returns Unit

            val result = useCase(habit)

            val savedReminder = habitSlot.captured.reminderDate
            assertThat(savedReminder).isNotNull()
            assertThat(savedReminder!!.hour).isEqualTo(21)
            assertThat(savedReminder.minute).isEqualTo(0)
            assertThat(savedReminder.date).isEqualTo(kotlinx.datetime.LocalDate(2025, 6, 15))
            // Default must not be silently advanced — would surface a misleading snackbar.
            assertThat((result as UpdateHabitUseCase.Result.Success).reminderAdvanced).isFalse()
            // And the saved reminder must never be in the past relative to the test clock.
            val now = testClock.now().toLocalDateTime(TimeZone.currentSystemDefault())
            assertThat(savedReminder > now).isTrue()
        }

    @Test
    fun `updating quit habit without reminder auto-sets 21-00 tomorrow when after 21-00`() =
        runTest {
            // 2025-06-15T22:00 — after 21:00, so tomorrow's 21:00 should be picked.
            val lateClock: Clock =
                object : Clock {
                    override fun now() =
                        LocalDateTime(2025, 6, 15, 22, 0)
                            .toInstant(TimeZone.currentSystemDefault())
                }
            val lateUseCase = UpdateHabitUseCase(habitRepository, habitReminderScheduler, lateClock)
            val habit = habit(habitType = HabitType.QUIT, reminderDate = null)

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.updateHabit(capture(habitSlot)) } returns Unit

            val result = lateUseCase(habit)

            val savedReminder = habitSlot.captured.reminderDate
            assertThat(savedReminder).isNotNull()
            assertThat(savedReminder!!.hour).isEqualTo(21)
            assertThat(savedReminder.minute).isEqualTo(0)
            assertThat(savedReminder.date).isEqualTo(kotlinx.datetime.LocalDate(2025, 6, 16))
            assertThat((result as UpdateHabitUseCase.Result.Success).reminderAdvanced).isFalse()
            val now = lateClock.now().toLocalDateTime(TimeZone.currentSystemDefault())
            assertThat(savedReminder > now).isTrue()
        }

    @Test
    fun `updating quit habit with existing reminder does not override it`() =
        runTest {
            val existingReminder = LocalDateTime(2099, 1, 5, 8, 30)
            val habit =
                habit(
                    habitType = HabitType.QUIT,
                    reminderDate = existingReminder,
                )

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.updateHabit(capture(habitSlot)) } returns Unit

            useCase(habit)

            val savedReminder = habitSlot.captured.reminderDate
            assertThat(savedReminder).isNotNull()
            assertThat(savedReminder!!.hour).isEqualTo(8)
            assertThat(savedReminder.minute).isEqualTo(30)
        }

    @Test
    fun `updating build habit without reminder does not auto-set reminder`() =
        runTest {
            val habit = habit(habitType = HabitType.BUILD, reminderDate = null)

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.updateHabit(capture(habitSlot)) } returns Unit

            useCase(habit)

            assertThat(habitSlot.captured.reminderDate).isNull()
        }

    private fun habit(
        title: String = "Test Habit",
        description: String = "Test Description",
        reminderDate: LocalDateTime? = null,
        repeatDays: Set<DayOfWeek>? = null,
        habitType: HabitType = HabitType.BUILD,
    ) = Habit(
        id = 1L,
        title = title,
        description = description,
        reminderDate = reminderDate,
        habitType = habitType,
        createdDate = LocalDateTime(2025, 1, 1, 0, 0),
        repeatDays = repeatDays,
    )
}
