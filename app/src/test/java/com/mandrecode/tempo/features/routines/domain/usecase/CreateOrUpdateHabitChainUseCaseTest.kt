package com.mandrecode.tempo.features.routines.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock

class CreateOrUpdateHabitChainUseCaseTest {
    private lateinit var useCase: CreateOrUpdateHabitChainUseCase
    private lateinit var habitRepository: HabitRepository
    private lateinit var habitChainRepository: HabitChainRepository
    private lateinit var habitReminderScheduler: HabitReminderScheduler

    @Before
    fun setup() {
        habitRepository = mockk(relaxed = true)
        habitChainRepository = mockk(relaxed = true)
        habitReminderScheduler = mockk(relaxed = true)
        coEvery { habitRepository.getHabitsByIds(any()) } returns emptyList()
        coEvery { habitChainRepository.insertHabitChain(any()) } returns 100L
        coEvery { habitReminderScheduler.scheduleHabitChain(any()) } returns ScheduleResult.Skipped

        useCase = CreateOrUpdateHabitChainUseCase(habitRepository, habitChainRepository, habitReminderScheduler)
    }

    // --- Validation ---

    @Test
    fun `empty title returns validation error`() =
        runTest {
            val result = useCase(params(title = ""))

            assertThat(result).isInstanceOf(CreateOrUpdateHabitChainUseCase.Result.ValidationError::class.java)
            assertThat((result as CreateOrUpdateHabitChainUseCase.Result.ValidationError).type)
                .isEqualTo(CreateOrUpdateHabitChainUseCase.ValidationErrorType.TITLE_EMPTY)
        }

    @Test
    fun `title too long returns validation error`() =
        runTest {
            val result = useCase(params(title = "A".repeat(501)))

            assertThat((result as CreateOrUpdateHabitChainUseCase.Result.ValidationError).type)
                .isEqualTo(CreateOrUpdateHabitChainUseCase.ValidationErrorType.TITLE_TOO_LONG)
        }

    @Test
    fun `description too long returns validation error`() =
        runTest {
            val result = useCase(params(description = "A".repeat(5001)))

            assertThat((result as CreateOrUpdateHabitChainUseCase.Result.ValidationError).type)
                .isEqualTo(CreateOrUpdateHabitChainUseCase.ValidationErrorType.DESCRIPTION_TOO_LONG)
        }

    // --- Create ---

    @Test
    fun `creating new chain inserts and returns success`() =
        runTest {
            val result = useCase(params(editingHabitChain = null))

            assertThat(result).isInstanceOf(CreateOrUpdateHabitChainUseCase.Result.Success::class.java)
            assertThat((result as CreateOrUpdateHabitChainUseCase.Result.Success).messageResId)
                .isEqualTo(R.string.msg_habit_chain_created_success)
            coVerify { habitChainRepository.insertHabitChain(any()) }
        }

    @Test
    fun `creating chain with reminder schedules it`() =
        runTest {
            val reminder = LocalDateTime(2099, 6, 15, 10, 0)
            coEvery { habitReminderScheduler.scheduleHabitChain(any()) } returns ScheduleResult.Success(reminder)

            val result = useCase(params(reminderDate = reminder, editingHabitChain = null))

            coVerify { habitReminderScheduler.scheduleHabitChain(any()) }
            assertThat((result as CreateOrUpdateHabitChainUseCase.Result.Success).messageResId)
                .isEqualTo(R.string.msg_habit_chain_created_success)
        }

    @Test
    fun `creating chain with schedule permission error returns permission message`() =
        runTest {
            val reminder = LocalDateTime(2099, 6, 15, 10, 0)
            coEvery { habitReminderScheduler.scheduleHabitChain(any()) } returns ScheduleResult.PermissionError("no perm")

            val result = useCase(params(reminderDate = reminder, editingHabitChain = null))

            assertThat((result as CreateOrUpdateHabitChainUseCase.Result.Success).messageResId)
                .isEqualTo(R.string.msg_permission_needed)
        }

    @Test
    fun `creating chain with schedule failure returns failure message`() =
        runTest {
            val reminder = LocalDateTime(2099, 6, 15, 10, 0)
            coEvery { habitReminderScheduler.scheduleHabitChain(any()) } returns ScheduleResult.Failure("fail")

            val result = useCase(params(reminderDate = reminder, editingHabitChain = null))

            assertThat((result as CreateOrUpdateHabitChainUseCase.Result.Success).messageResId)
                .isEqualTo(R.string.msg_habit_chain_create_failed_scheduling)
        }

    // --- Update ---

    @Test
    fun `updating existing chain updates repository and cancels old`() =
        runTest {
            val existingChain = chain()

            val result = useCase(params(editingHabitChain = existingChain))

            coVerify { habitChainRepository.updateHabitChain(any()) }
            coVerify { habitReminderScheduler.cancelHabitChain(existingChain) }
            assertThat((result as CreateOrUpdateHabitChainUseCase.Result.Success).messageResId)
                .isEqualTo(R.string.msg_habit_chain_updated_success)
        }

    @Test
    fun `updating chain with reminder reschedules`() =
        runTest {
            val reminder = LocalDateTime(2099, 6, 15, 10, 0)
            coEvery { habitReminderScheduler.scheduleHabitChain(any()) } returns ScheduleResult.Success(reminder)

            useCase(params(reminderDate = reminder, editingHabitChain = chain()))

            coVerify { habitReminderScheduler.scheduleHabitChain(any()) }
        }

    @Test
    fun `updating chain with schedule failure returns failure message`() =
        runTest {
            val reminder = LocalDateTime(2099, 6, 15, 10, 0)
            coEvery { habitReminderScheduler.scheduleHabitChain(any()) } returns ScheduleResult.Failure("fail")

            val result = useCase(params(reminderDate = reminder, editingHabitChain = chain()))

            assertThat((result as CreateOrUpdateHabitChainUseCase.Result.Success).messageResId)
                .isEqualTo(R.string.msg_habit_chain_update_failed_scheduling)
        }

    // --- Habit cleanup ---

    @Test
    fun `clears individual habit reminders when habitIds provided`() =
        runTest {
            val habits =
                listOf(
                    Habit(id = 1L, title = "H1", description = "", createdDate = LocalDateTime(2020, 1, 1, 0, 0)),
                    Habit(id = 2L, title = "H2", description = "", createdDate = LocalDateTime(2020, 1, 1, 0, 0)),
                )
            coEvery { habitRepository.getHabitsByIds(listOf(1L, 2L)) } returns habits

            useCase(params(habitIds = listOf(1L, 2L), editingHabitChain = null))

            coVerify { habitRepository.clearRemindersForHabits(listOf(1L, 2L)) }
            coVerify { habitReminderScheduler.cancelHabit(habits[0]) }
            coVerify { habitReminderScheduler.cancelHabit(habits[1]) }
        }

    @Test
    fun `syncs color key for habit ids`() =
        runTest {
            useCase(params(habitIds = listOf(1L, 2L), colorKey = "red", editingHabitChain = null))

            coVerify { habitRepository.updateHabitsColorKey(listOf(1L, 2L), "red") }
        }

    @Test
    fun `creating chain trims title and description before saving`() =
        runTest {
            val chainSlot = slot<HabitChain>()
            coEvery { habitChainRepository.insertHabitChain(capture(chainSlot)) } returns 100L

            useCase(params(title = "  Padded Title  ", description = "  Padded Desc  ", editingHabitChain = null))

            assertThat(chainSlot.captured.title).isEqualTo("Padded Title")
            assertThat(chainSlot.captured.description).isEqualTo("Padded Desc")
        }

    @Test
    fun `updating chain trims title and description before saving`() =
        runTest {
            val chainSlot = slot<HabitChain>()
            coEvery { habitChainRepository.updateHabitChain(capture(chainSlot)) } returns Unit

            useCase(params(title = "  Padded Title  ", description = "  Padded Desc  ", editingHabitChain = chain()))

            assertThat(chainSlot.captured.title).isEqualTo("Padded Title")
            assertThat(chainSlot.captured.description).isEqualTo("Padded Desc")
        }

    // --- Reminder advancement ---

    @Test
    fun `creating chain with past reminder advances to future date`() =
        runTest {
            val pastReminder = LocalDateTime(2020, 1, 1, 10, 0)

            val chainSlot = slot<HabitChain>()
            coEvery { habitChainRepository.insertHabitChain(capture(chainSlot)) } returns 100L

            val result = useCase(params(reminderDate = pastReminder, repeatDays = setOf(DayOfWeek.WEDNESDAY), editingHabitChain = null))

            val savedReminder = chainSlot.captured.periodicReminder
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            assertThat(savedReminder).isNotNull()
            assertThat(savedReminder!! > now).isTrue()
            assertThat(savedReminder.hour).isEqualTo(pastReminder.hour)
            assertThat(savedReminder.minute).isEqualTo(pastReminder.minute)
            assertThat((result as CreateOrUpdateHabitChainUseCase.Result.Success).reminderAdvanced).isTrue()
            assertThat(result.messageResId).isEqualTo(R.string.msg_habit_chain_created_reminder_advanced)
        }

    @Test
    fun `updating chain with past reminder advances to future date`() =
        runTest {
            val pastReminder = LocalDateTime(2020, 1, 1, 10, 0)

            val chainSlot = slot<HabitChain>()
            coEvery { habitChainRepository.updateHabitChain(capture(chainSlot)) } returns Unit

            val result =
                useCase(
                    params(
                        reminderDate = pastReminder,
                        repeatDays = setOf(DayOfWeek.MONDAY),
                        editingHabitChain = chain(),
                    ),
                )

            val savedReminder = chainSlot.captured.periodicReminder
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

            assertThat(savedReminder).isNotNull()
            assertThat(savedReminder!! > now).isTrue()
            assertThat(savedReminder.hour).isEqualTo(pastReminder.hour)
            assertThat(savedReminder.minute).isEqualTo(pastReminder.minute)
            assertThat((result as CreateOrUpdateHabitChainUseCase.Result.Success).reminderAdvanced).isTrue()
            assertThat(result.messageResId).isEqualTo(R.string.msg_habit_chain_updated_reminder_advanced)
        }

    @Test
    fun `creating chain with future reminder does not advance`() =
        runTest {
            val futureReminder = LocalDateTime(2099, 6, 15, 10, 0)
            coEvery { habitReminderScheduler.scheduleHabitChain(any()) } returns ScheduleResult.Success(futureReminder)

            val result = useCase(params(reminderDate = futureReminder, editingHabitChain = null))

            assertThat((result as CreateOrUpdateHabitChainUseCase.Result.Success).reminderAdvanced).isFalse()
        }

    @Test
    fun `updating chain with future reminder does not advance`() =
        runTest {
            val futureReminder = LocalDateTime(2099, 6, 15, 10, 0)
            coEvery { habitReminderScheduler.scheduleHabitChain(any()) } returns ScheduleResult.Success(futureReminder)

            val result = useCase(params(reminderDate = futureReminder, editingHabitChain = chain()))

            assertThat((result as CreateOrUpdateHabitChainUseCase.Result.Success).reminderAdvanced).isFalse()
        }

    // --- Quit habit invariant ---

    @Test
    fun `chain containing a quit habit returns QUIT_HABITS_NOT_ALLOWED validation error`() =
        runTest {
            val quitHabit =
                Habit(
                    id = 1L,
                    title = "Quit Smoking",
                    description = "",
                    createdDate = LocalDateTime(2020, 1, 1, 0, 0),
                    habitType = HabitType.QUIT,
                )
            coEvery { habitRepository.getHabitsByIds(listOf(1L)) } returns listOf(quitHabit)

            val result = useCase(params(habitIds = listOf(1L), editingHabitChain = null))

            assertThat(result).isInstanceOf(CreateOrUpdateHabitChainUseCase.Result.ValidationError::class.java)
            assertThat((result as CreateOrUpdateHabitChainUseCase.Result.ValidationError).type)
                .isEqualTo(CreateOrUpdateHabitChainUseCase.ValidationErrorType.QUIT_HABITS_NOT_ALLOWED)
        }

    @Test
    fun `quit habit rejection does not clear existing reminders`() =
        runTest {
            // Regression: validation must run BEFORE clearRemindersForHabits/cancelHabit
            // so a violating call never destroys the user's existing reminders.
            val quitHabit =
                Habit(
                    id = 1L,
                    title = "Quit Smoking",
                    description = "",
                    createdDate = LocalDateTime(2020, 1, 1, 0, 0),
                    habitType = HabitType.QUIT,
                )
            coEvery { habitRepository.getHabitsByIds(listOf(1L)) } returns listOf(quitHabit)

            useCase(params(habitIds = listOf(1L), editingHabitChain = null))

            coVerify(exactly = 0) { habitRepository.clearRemindersForHabits(any()) }
            coVerify(exactly = 0) { habitReminderScheduler.cancelHabit(any<Habit>()) }
            coVerify(exactly = 0) { habitRepository.updateHabitsColorKey(any(), any()) }
            coVerify(exactly = 0) { habitChainRepository.insertHabitChain(any()) }
        }

    @Test
    fun `chain with only build habits passes the invariant`() =
        runTest {
            val buildHabit =
                Habit(
                    id = 1L,
                    title = "Read",
                    description = "",
                    createdDate = LocalDateTime(2020, 1, 1, 0, 0),
                    habitType = HabitType.BUILD,
                )
            coEvery { habitRepository.getHabitsByIds(listOf(1L)) } returns listOf(buildHabit)

            val result = useCase(params(habitIds = listOf(1L), editingHabitChain = null))

            assertThat(result).isInstanceOf(CreateOrUpdateHabitChainUseCase.Result.Success::class.java)
        }

    // --- Helpers ---

    private fun params(
        title: String = "Chain Title",
        description: String = "desc",
        habitIds: List<Long> = listOf(1L),
        colorKey: String? = null,
        icon: String? = null,
        reminderDate: LocalDateTime? = null,
        repeatDays: Set<DayOfWeek>? = null,
        editingHabitChain: HabitChain? = null,
    ) = CreateOrUpdateHabitChainUseCase.Params(
        title = title,
        description = description,
        habitIds = habitIds,
        colorKey = colorKey,
        icon = icon,
        reminderDate = reminderDate,
        repeatDays = repeatDays,
        editingHabitChain = editingHabitChain,
    )

    private fun chain() =
        HabitChain(
            id = 10L,
            title = "Existing Chain",
            description = "desc",
            habitIds = listOf(1L),
            createdDate = LocalDateTime(2020, 1, 1, 0, 0),
        )
}
