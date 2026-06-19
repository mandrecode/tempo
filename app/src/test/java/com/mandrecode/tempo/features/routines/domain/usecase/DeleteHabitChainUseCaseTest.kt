package com.mandrecode.tempo.features.routines.domain.usecase

import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.repository.HabitChainRepository
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Before
import org.junit.Test

class DeleteHabitChainUseCaseTest {
    private lateinit var useCase: DeleteHabitChainUseCase
    private lateinit var habitRepository: HabitRepository
    private lateinit var habitChainRepository: HabitChainRepository
    private lateinit var habitReminderScheduler: HabitReminderScheduler

    @Before
    fun setup() {
        habitRepository = mockk(relaxed = true)
        habitChainRepository = mockk(relaxed = true)
        habitReminderScheduler = mockk(relaxed = true)
        useCase = DeleteHabitChainUseCase(habitRepository, habitChainRepository, habitReminderScheduler)
    }

    @Test
    fun `deleteHabits true deletes habits and cancels each reminder`() =
        runTest {
            val chain = chain(habitIds = listOf(1L, 2L))

            useCase(chain, deleteHabits = true)

            coVerify { habitRepository.deleteHabitsByIds(listOf(1L, 2L)) }
            coVerify { habitReminderScheduler.cancelHabit(1L) }
            coVerify { habitReminderScheduler.cancelHabit(2L) }
            coVerify { habitChainRepository.deleteHabitChain(chain) }
            coVerify { habitReminderScheduler.cancelHabitChain(chain) }
        }

    @Test
    fun `deleteHabits false with reminder transfers reminder to individual habits`() =
        runTest {
            val reminder = LocalDateTime(2099, 6, 15, 10, 0)
            val chain = chain(habitIds = listOf(1L, 2L), periodicReminder = reminder)

            useCase(chain, deleteHabits = false)

            coVerify { habitRepository.updateHabitsReminder(listOf(1L, 2L), reminder) }
            coVerify { habitReminderScheduler.scheduleHabit(1L, reminder) }
            coVerify { habitReminderScheduler.scheduleHabit(2L, reminder) }
            coVerify { habitChainRepository.deleteHabitChain(chain) }
            coVerify { habitReminderScheduler.cancelHabitChain(chain) }
        }

    @Test
    fun `deleteHabits false without reminder does not transfer reminders`() =
        runTest {
            val chain = chain(habitIds = listOf(1L), periodicReminder = null)

            useCase(chain, deleteHabits = false)

            coVerify(exactly = 0) { habitRepository.updateHabitsReminder(any(), any()) }
            coVerify(exactly = 0) { habitReminderScheduler.scheduleHabit(any<Long>(), any()) }
            coVerify { habitChainRepository.deleteHabitChain(chain) }
        }

    @Test
    fun `deleteHabits true with empty habitIds skips habit deletion`() =
        runTest {
            val chain = chain(habitIds = emptyList())

            useCase(chain, deleteHabits = true)

            coVerify(exactly = 0) { habitRepository.deleteHabitsByIds(any()) }
            coVerify { habitChainRepository.deleteHabitChain(chain) }
            coVerify { habitReminderScheduler.cancelHabitChain(chain) }
        }

    @Test
    fun `deleteHabits false with empty habitIds and reminder skips transfer`() =
        runTest {
            val chain =
                chain(
                    habitIds = emptyList(),
                    periodicReminder = LocalDateTime(2099, 1, 1, 10, 0),
                )

            useCase(chain, deleteHabits = false)

            coVerify(exactly = 0) { habitRepository.updateHabitsReminder(any(), any()) }
            coVerify { habitChainRepository.deleteHabitChain(chain) }
        }

    private fun chain(
        habitIds: List<Long> = listOf(1L),
        periodicReminder: LocalDateTime? = null,
    ) = HabitChain(
        id = 10L,
        title = "Test Chain",
        description = "desc",
        habitIds = habitIds,
        periodicReminder = periodicReminder,
        createdDate = LocalDateTime(2020, 1, 1, 0, 0),
    )
}
