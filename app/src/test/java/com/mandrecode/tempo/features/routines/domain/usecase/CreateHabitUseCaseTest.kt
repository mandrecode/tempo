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

class CreateHabitUseCaseTest {
    private lateinit var useCase: CreateHabitUseCase
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
        coEvery { habitRepository.insertHabit(any()) } returns 1L
        coEvery { habitReminderScheduler.scheduleHabit(any<Habit>()) } returns ScheduleResult.Skipped

        useCase = CreateHabitUseCase(habitRepository, habitReminderScheduler, testClock)
    }

    @Test
    fun `creating habit with future reminder on matching repeat day saves unchanged`() =
        runTest {
            // 2099-01-05 is a Monday
            val futureReminder = LocalDateTime(2099, 1, 5, 10, 0)
            val habit = habit(reminderDate = futureReminder, repeatDays = setOf(DayOfWeek.MONDAY))

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.insertHabit(capture(habitSlot)) } returns 1L

            val result = useCase(habit)

            assertThat(habitSlot.captured.reminderDate).isEqualTo(futureReminder)
            assertThat((result as CreateHabitUseCase.Result.Success).reminderAdvanced).isFalse()
        }

    @Test
    fun `creating habit with no reminder saves unchanged`() =
        runTest {
            val habit = habit(reminderDate = null, repeatDays = setOf(DayOfWeek.MONDAY))

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.insertHabit(capture(habitSlot)) } returns 1L

            useCase(habit)

            assertThat(habitSlot.captured.reminderDate).isNull()
        }

    @Test
    fun `creating habit with past reminder and no repeat days defaults to daily and advances`() =
        runTest {
            val pastReminder = LocalDateTime(2020, 1, 1, 10, 0)
            val habit = habit(reminderDate = pastReminder, repeatDays = null)

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.insertHabit(capture(habitSlot)) } returns 1L

            val result = useCase(habit)

            val savedReminder = habitSlot.captured.reminderDate
            val now = testClock.now().toLocalDateTime(TimeZone.currentSystemDefault())

            assertThat(savedReminder).isNotNull()
            assertThat(savedReminder!! > now).isTrue()
            assertThat(savedReminder.hour).isEqualTo(pastReminder.hour)
            assertThat(savedReminder.minute).isEqualTo(pastReminder.minute)
            assertThat((result as CreateHabitUseCase.Result.Success).reminderAdvanced).isTrue()
        }

    @Test
    fun `creating habit with past reminder and empty repeat days defaults to daily and advances`() =
        runTest {
            val pastReminder = LocalDateTime(2020, 1, 1, 10, 0)
            val habit = habit(reminderDate = pastReminder, repeatDays = emptySet())

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.insertHabit(capture(habitSlot)) } returns 1L

            val result = useCase(habit)

            val savedReminder = habitSlot.captured.reminderDate
            val now = testClock.now().toLocalDateTime(TimeZone.currentSystemDefault())

            assertThat(savedReminder).isNotNull()
            assertThat(savedReminder!! > now).isTrue()
            assertThat(savedReminder.hour).isEqualTo(pastReminder.hour)
            assertThat(savedReminder.minute).isEqualTo(pastReminder.minute)
            assertThat((result as CreateHabitUseCase.Result.Success).reminderAdvanced).isTrue()
        }

    @Test
    fun `creating habit with past reminder and repeat days advances to future date`() =
        runTest {
            val pastReminder = LocalDateTime(2020, 1, 1, 10, 0) // Wednesday
            val habit =
                habit(
                    reminderDate = pastReminder,
                    repeatDays = setOf(DayOfWeek.WEDNESDAY),
                )

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.insertHabit(capture(habitSlot)) } returns 1L

            val result = useCase(habit)

            val savedReminder = habitSlot.captured.reminderDate
            val now = testClock.now().toLocalDateTime(TimeZone.currentSystemDefault())

            assertThat(savedReminder).isNotNull()
            assertThat(savedReminder!! > now).isTrue()
            assertThat(savedReminder.hour).isEqualTo(pastReminder.hour)
            assertThat(savedReminder.minute).isEqualTo(pastReminder.minute)
            assertThat((result as CreateHabitUseCase.Result.Success).reminderAdvanced).isTrue()
        }

    @Test
    fun `advanced reminder lands on a day in repeat days`() =
        runTest {
            val pastReminder = LocalDateTime(2020, 1, 1, 10, 0) // Wednesday
            val targetDays = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
            val habit = habit(reminderDate = pastReminder, repeatDays = targetDays)

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.insertHabit(capture(habitSlot)) } returns 1L

            useCase(habit)

            val savedReminder = habitSlot.captured.reminderDate!!
            val savedDayOfWeek = DayOfWeek.fromKotlinDayOfWeek(savedReminder.dayOfWeek)
            assertThat(targetDays).contains(savedDayOfWeek)
        }

    @Test
    fun `creating habit with past reminder and repeat days schedules the alarm`() =
        runTest {
            val pastReminder = LocalDateTime(2020, 1, 1, 10, 0)
            val habit = habit(reminderDate = pastReminder, repeatDays = setOf(DayOfWeek.MONDAY))

            coEvery { habitReminderScheduler.scheduleHabit(any<Habit>()) } returns
                ScheduleResult.Success(
                    LocalDateTime(2099, 1, 6, 10, 0),
                )

            useCase(habit)

            coVerify { habitReminderScheduler.scheduleHabit(any<Habit>()) }
        }

    @Test
    fun `creating habit trims title and description before saving`() =
        runTest {
            val habit = habit(title = "  Padded Title  ", description = "  Padded Desc  ")

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.insertHabit(capture(habitSlot)) } returns 1L

            useCase(habit)

            assertThat(habitSlot.captured.title).isEqualTo("Padded Title")
            assertThat(habitSlot.captured.description).isEqualTo("Padded Desc")
        }

    @Test
    fun `title validation returns error for empty title`() =
        runTest {
            val habit = habit(title = "")

            val result = useCase(habit)

            assertThat(result).isInstanceOf(CreateHabitUseCase.Result.ValidationError::class.java)
            assertThat((result as CreateHabitUseCase.Result.ValidationError).type)
                .isEqualTo(CreateHabitUseCase.ValidationErrorType.TITLE_EMPTY)
        }

    // --- Quit habit auto-reminder ---

    @Test
    fun `creating quit habit without reminder auto-sets 21-00 reminder today when before 21-00`() =
        runTest {
            // testClock is 2025-06-15T12:00 — before 21:00, so today's 21:00 is the next upcoming.
            val habit = habit(habitType = HabitType.QUIT, reminderDate = null)

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.insertHabit(capture(habitSlot)) } returns 1L

            val result = useCase(habit)

            val savedReminder = habitSlot.captured.reminderDate
            assertThat(savedReminder).isNotNull()
            assertThat(savedReminder!!.hour).isEqualTo(21)
            assertThat(savedReminder.minute).isEqualTo(0)
            assertThat(savedReminder.date).isEqualTo(kotlinx.datetime.LocalDate(2025, 6, 15))
            // Default must not be silently advanced — would surface a misleading snackbar.
            assertThat((result as CreateHabitUseCase.Result.Success).reminderAdvanced).isFalse()
        }

    @Test
    fun `creating quit habit without reminder auto-sets 21-00 tomorrow when after 21-00`() =
        runTest {
            // 2025-06-15T22:00 — after 21:00, so tomorrow's 21:00 should be picked.
            val lateClock: Clock =
                object : Clock {
                    override fun now() =
                        LocalDateTime(2025, 6, 15, 22, 0)
                            .toInstant(TimeZone.currentSystemDefault())
                }
            val lateUseCase = CreateHabitUseCase(habitRepository, habitReminderScheduler, lateClock)
            val habit = habit(habitType = HabitType.QUIT, reminderDate = null)

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.insertHabit(capture(habitSlot)) } returns 1L

            val result = lateUseCase(habit)

            val savedReminder = habitSlot.captured.reminderDate
            assertThat(savedReminder).isNotNull()
            assertThat(savedReminder!!.hour).isEqualTo(21)
            assertThat(savedReminder.minute).isEqualTo(0)
            assertThat(savedReminder.date).isEqualTo(kotlinx.datetime.LocalDate(2025, 6, 16))
            assertThat((result as CreateHabitUseCase.Result.Success).reminderAdvanced).isFalse()
        }

    @Test
    fun `creating quit habit with existing reminder does not override it`() =
        runTest {
            val existingReminder = LocalDateTime(2099, 1, 5, 8, 30)
            val habit =
                habit(
                    habitType = HabitType.QUIT,
                    reminderDate = existingReminder,
                    repeatDays = setOf(DayOfWeek.MONDAY),
                )

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.insertHabit(capture(habitSlot)) } returns 1L

            useCase(habit)

            val savedReminder = habitSlot.captured.reminderDate
            assertThat(savedReminder).isNotNull()
            assertThat(savedReminder!!.hour).isEqualTo(8)
            assertThat(savedReminder.minute).isEqualTo(30)
        }

    @Test
    fun `creating build habit without reminder does not auto-set reminder`() =
        runTest {
            val habit = habit(habitType = HabitType.BUILD, reminderDate = null)

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.insertHabit(capture(habitSlot)) } returns 1L

            useCase(habit)

            assertThat(habitSlot.captured.reminderDate).isNull()
        }

    @Test
    fun `creating quit habit coerces repeatDays to null`() =
        runTest {
            val habit =
                habit(
                    habitType = HabitType.QUIT,
                    repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                )

            val habitSlot = slot<Habit>()
            coEvery { habitRepository.insertHabit(capture(habitSlot)) } returns 1L

            useCase(habit)

            assertThat(habitSlot.captured.repeatDays).isNull()
        }

    private fun habit(
        title: String = "Test Habit",
        description: String = "",
        reminderDate: LocalDateTime? = null,
        repeatDays: Set<DayOfWeek>? = null,
        habitType: HabitType = HabitType.BUILD,
    ) = Habit(
        title = title,
        description = description,
        reminderDate = reminderDate,
        habitType = habitType,
        createdDate = LocalDateTime(2020, 1, 1, 0, 0),
        repeatDays = repeatDays,
    )
}
