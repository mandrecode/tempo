package com.mandrecode.tempo.features.routines.domain.usecase

import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.model.HabitChainDeletionSnapshot
import com.mandrecode.tempo.features.routines.domain.repository.HabitChainRepository
import com.mandrecode.tempo.features.routines.domain.scheduler.HabitReminderScheduler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Before
import org.junit.Test

class DeleteHabitChainUseCaseTest {
    private lateinit var useCase: DeleteHabitChainUseCase
    private lateinit var habitChainRepository: HabitChainRepository
    private lateinit var habitReminderScheduler: HabitReminderScheduler

    @Before
    fun setup() {
        habitChainRepository = mockk(relaxed = true)
        habitReminderScheduler = mockk(relaxed = true)
        useCase = DeleteHabitChainUseCase(habitChainRepository, habitReminderScheduler)
    }

    @Test
    fun `deleteHabits true deletes habits and cancels each reminder`() =
        runTest {
            val chain = chain(habitIds = listOf(1L, 2L))
            stubSnapshot(chain, deletedHabits = true)

            useCase(chain, deleteHabits = true)

            coVerify { habitChainRepository.deleteHabitChainWithSnapshot(chain.id, true) }
            coVerify { habitReminderScheduler.cancelHabit(1L) }
            coVerify { habitReminderScheduler.cancelHabit(2L) }
            coVerify { habitReminderScheduler.cancelHabitChain(chain) }
        }

    @Test
    fun `deleteHabits false with reminder transfers reminder to individual habits`() =
        runTest {
            val reminder = LocalDateTime(2099, 6, 15, 10, 0)
            val chain = chain(habitIds = listOf(1L, 2L), periodicReminder = reminder)
            stubSnapshot(chain, deletedHabits = false)

            useCase(chain, deleteHabits = false)

            coVerify { habitChainRepository.deleteHabitChainWithSnapshot(chain.id, false) }
            coVerify { habitReminderScheduler.scheduleHabit(1L, reminder) }
            coVerify { habitReminderScheduler.scheduleHabit(2L, reminder) }
            coVerify { habitReminderScheduler.cancelHabitChain(chain) }
        }

    @Test
    fun `deleteHabits false uses persisted chain reminder when input is stale`() =
        runTest {
            val persistedReminder = LocalDateTime(2099, 6, 15, 10, 0)
            val inputChain = chain(habitIds = listOf(1L), periodicReminder = null)
            val persistedChain = inputChain.copy(periodicReminder = persistedReminder)
            stubSnapshot(
                chain = inputChain,
                deletedHabits = false,
                snapshotChain = persistedChain,
            )

            useCase(inputChain, deleteHabits = false)

            coVerify { habitReminderScheduler.scheduleHabit(1L, persistedReminder) }
            coVerify { habitReminderScheduler.cancelHabitChain(persistedChain) }
        }

    @Test
    fun `deleteHabits false without reminder does not transfer reminders`() =
        runTest {
            val chain = chain(habitIds = listOf(1L), periodicReminder = null)
            stubSnapshot(chain, deletedHabits = false)

            useCase(chain, deleteHabits = false)

            coVerify(exactly = 0) { habitReminderScheduler.scheduleHabit(any<Long>(), any()) }
        }

    @Test
    fun `deleteHabits true with empty habitIds skips habit deletion`() =
        runTest {
            val chain = chain(habitIds = emptyList())
            stubSnapshot(chain, deletedHabits = true)

            useCase(chain, deleteHabits = true)

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
            stubSnapshot(chain, deletedHabits = false)

            useCase(chain, deleteHabits = false)
        }

    private fun stubSnapshot(
        chain: HabitChain,
        deletedHabits: Boolean,
        snapshotChain: HabitChain = chain,
    ) {
        coEvery { habitChainRepository.deleteHabitChainWithSnapshot(chain.id, deletedHabits) } returns
            HabitChainDeletionSnapshot(
                chain = snapshotChain,
                habitsBeforeDeletion = emptyList(),
                affectedChains = listOf(snapshotChain),
                deletedHabits = deletedHabits,
            )
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
