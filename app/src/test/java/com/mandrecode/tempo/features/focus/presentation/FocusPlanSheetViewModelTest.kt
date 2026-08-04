package com.mandrecode.tempo.features.focus.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.focus.presentation.FocusContract.UiEffect.PlanBatchConfirmed
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.util.PlanReminderTimeUtil
import io.mockk.coVerify
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.junit.Test

/**
 * Planning the loose ends: what the sheet opens with, what a chip writes, and what the undo it
 * offers afterwards is answerable for.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FocusPlanSheetViewModelTest : FocusViewModelHarness() {
    private val now get() = clock.now().toLocalDateTime(TimeZone.currentSystemDefault())
    private val tomorrow get() = now.date.plus(1, DateTimeUnit.DAY)

    @Test
    fun `the sheet opens on the loose ends and closes on request`() =
        runTest {
            stubDay()
            undatedTasks.value = listOf(undatedRow(1), undatedRow(2))
            val viewModel = openSheet()

            assertThat(
                viewModel.uiState.value.planSheet
                    ?.rows
                    ?.map { it.task.id },
            ).containsExactly(1L, 2L)

            viewModel.onEvent(FocusContract.UiEvent.ClosePlanSheet)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.planSheet).isNull()
        }

    @Test
    fun `unplanning takes the date back off and returns the task to unplanned`() =
        runTest {
            stubDay()
            undatedTasks.value = listOf(undatedRow(1), undatedRow(2))
            val viewModel = openSheet()

            viewModel.plan(taskId = 1)
            advanceUntilIdle()
            assertThat(
                viewModel.uiState.value.planSheet
                    ?.planned
                    ?.map { it.task.id },
            ).containsExactly(1L)

            viewModel.unplan(taskId = 1)
            advanceUntilIdle()

            val sheet = requireNotNull(viewModel.uiState.value.planSheet)
            assertThat(sheet.planned).isEmpty()
            assertThat(sheet.unplanned.map { it.task.id }).containsExactly(1L, 2L)
        }

    @Test
    fun `unplanning clears the reminder through the update use case`() =
        runTest {
            stubDay()
            undatedTasks.value = listOf(undatedRow(1))
            val viewModel = openSheet()

            viewModel.plan(taskId = 1)
            advanceUntilIdle()
            viewModel.onEvent(FocusContract.UiEvent.UnplanTask(1))
            advanceUntilIdle()

            val written = mutableListOf<Task>()
            coVerify { updateTask(capture(written)) }
            assertThat(written.last().reminderDate).isNull()
        }

    /** Planned then unplanned is where it started, so there is nothing to take back. */
    @Test
    fun `planning then unplanning leaves nothing to undo`() =
        runTest {
            stubDay()
            undatedTasks.value = listOf(undatedRow(1))
            val viewModel = openSheet()

            viewModel.plan(taskId = 1)
            viewModel.unplan(taskId = 1)
            advanceUntilIdle()

            assertThat(
                viewModel.uiState.value.planSheet
                    ?.hasChanges,
            ).isFalse()

            viewModel.uiEffect.test {
                viewModel.onEvent(FocusContract.UiEvent.ClosePlanSheet)
                advanceUntilIdle()

                expectNoEvents()
            }
        }

    /** Ticking one off while planning settles it as surely as giving it a day. */
    @Test
    fun `a completed task stops counting as needing a day`() =
        runTest {
            stubDay()
            undatedTasks.value = listOf(undatedRow(1), undatedRow(2))
            val viewModel = openSheet()

            undatedTasks.value =
                undatedTasks.value.map { row ->
                    if (row.task.id == 1L) row.copy(task = row.task.copy(isCompleted = true)) else row
                }
            advanceUntilIdle()

            val sheet = requireNotNull(viewModel.uiState.value.planSheet)
            assertThat(sheet.unplanned.map { it.task.id }).containsExactly(2L)
            assertThat(sheet.planned.map { it.task.id }).containsExactly(1L)
        }

    /** Completing is not a date change, so there is no reminder for an undo to put back. */
    @Test
    fun `completing a task alone leaves nothing to undo`() =
        runTest {
            stubDay()
            undatedTasks.value = listOf(undatedRow(1))
            val viewModel = openSheet()

            undatedTasks.value =
                undatedTasks.value.map { it.copy(task = it.task.copy(isCompleted = true)) }
            advanceUntilIdle()

            assertThat(
                viewModel.uiState.value.planSheet
                    ?.hasChanges,
            ).isFalse()
        }

    @Test
    fun `a quick-plan chip writes the reminder through the update use case`() =
        runTest {
            stubDay()
            undatedTasks.value = listOf(undatedRow(1))
            val viewModel = openSheet()

            viewModel.onEvent(FocusContract.UiEvent.PlanTask(taskId = 1, date = tomorrow))
            advanceUntilIdle()

            val written = slot<Task>()
            coVerify { updateTask(capture(written)) }
            assertThat(written.captured.id).isEqualTo(1L)
            assertThat(written.captured.reminderDate)
                .isEqualTo(PlanReminderTimeUtil.resolve(tomorrow, now))
        }

    @Test
    fun `planning a task moves it from unplanned to planned`() =
        runTest {
            stubDay()
            undatedTasks.value = listOf(undatedRow(1), undatedRow(2))
            val viewModel = openSheet()

            assertThat(
                viewModel.uiState.value.planSheet
                    ?.showsSectionHeaders,
            ).isFalse()

            viewModel.plan(taskId = 1)
            advanceUntilIdle()

            val sheet = requireNotNull(viewModel.uiState.value.planSheet)
            assertThat(sheet.planned.map { it.task.id }).containsExactly(1L)
            assertThat(sheet.unplanned.map { it.task.id }).containsExactly(2L)
            assertThat(sheet.showsSectionHeaders).isTrue()
        }

    @Test
    fun `a planned task stays in the sheet rather than vanishing from it`() =
        runTest {
            stubDay()
            undatedTasks.value = listOf(undatedRow(1))
            val viewModel = openSheet()

            viewModel.plan(taskId = 1)
            advanceUntilIdle()

            assertThat(
                viewModel.uiState.value.planSheet
                    ?.rows
                    ?.map { it.task.id },
            ).containsExactly(1L)
        }

    @Test
    fun `the sheet knows whether closing has anything to offer back`() =
        runTest {
            stubDay()
            undatedTasks.value = listOf(undatedRow(1))
            val viewModel = openSheet()

            assertThat(
                viewModel.uiState.value.planSheet
                    ?.hasChanges,
            ).isFalse()

            viewModel.plan(taskId = 1)
            advanceUntilIdle()

            assertThat(
                viewModel.uiState.value.planSheet
                    ?.hasChanges,
            ).isTrue()
        }

    @Test
    fun `closing the sheet offers the batch back`() =
        runTest {
            stubDay()
            undatedTasks.value = listOf(undatedRow(1), undatedRow(2), undatedRow(3))
            val viewModel = openSheet()

            viewModel.plan(taskId = 1)
            viewModel.plan(taskId = 2)
            advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onEvent(FocusContract.UiEvent.ClosePlanSheet)
                advanceUntilIdle()

                val effect = awaitItem() as PlanBatchConfirmed
                assertThat(effect.count).isEqualTo(2)
                assertThat(effect.batch.keys).containsExactly(1L, 2L)
                cancelAndIgnoreRemainingEvents()
            }
            assertThat(viewModel.uiState.value.planSheet).isNull()
        }

    /**
     * A chip and a swipe in the same breath. The write is a round trip through the repository, and
     * the sheet is gone before it comes back — but the planning still happened, so the way back has
     * to still be offered.
     */
    @Test
    fun `closing before the write comes back still offers the undo`() =
        runTest {
            stubDay()
            undatedTasks.value = listOf(undatedRow(1))
            val viewModel = openSheet()

            // The chip alone: no flow emission, so the rows still say the task has no date.
            viewModel.onEvent(FocusContract.UiEvent.PlanTask(taskId = 1, date = tomorrow))

            viewModel.uiEffect.test {
                viewModel.onEvent(FocusContract.UiEvent.ClosePlanSheet)
                advanceUntilIdle()

                val effect = awaitItem() as PlanBatchConfirmed
                assertThat(effect.batch).containsExactly(1L, null)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `closing without a change goes quietly`() =
        runTest {
            stubDay()
            undatedTasks.value = listOf(undatedRow(1))
            val viewModel = openSheet()

            viewModel.uiEffect.test {
                viewModel.onEvent(FocusContract.UiEvent.ClosePlanSheet)
                advanceUntilIdle()

                expectNoEvents()
            }
            assertThat(viewModel.uiState.value.planSheet).isNull()
        }

    @Test
    fun `undo restores every reminder the sheet set, and only those`() =
        runTest {
            stubDay()
            undatedTasks.value = listOf(undatedRow(1), undatedRow(2), undatedRow(3))
            val viewModel = openSheet()

            viewModel.plan(taskId = 1)
            viewModel.plan(taskId = 3)
            advanceUntilIdle()
            val offered = closeAndTakeTheOffer(viewModel)
            viewModel.onEvent(FocusContract.UiEvent.UndoPlanBatch(offered.batch))
            advanceUntilIdle()

            val restored = slot<Map<Long, LocalDateTime?>>()
            coVerify { restoreTaskReminders(capture(restored)) }
            assertThat(restored.captured).containsExactly(1L, null, 3L, null)
        }

    @Test
    fun `undo puts a task back to the reminder it had when the sheet opened`() =
        runTest {
            stubDay()
            // Opened on a task that already carried a date — the sheet's job is to undo its own
            // session, not to clear whatever it happened to find.
            val alreadyDated = LocalDateTime(now.date, LocalTime(7, 30))
            undatedTasks.value = listOf(undatedRow(1, reminderDate = alreadyDated))
            val viewModel = openSheet()

            viewModel.plan(taskId = 1)
            advanceUntilIdle()
            val offered = closeAndTakeTheOffer(viewModel)
            viewModel.onEvent(FocusContract.UiEvent.UndoPlanBatch(offered.batch))
            advanceUntilIdle()

            val restored = slot<Map<Long, LocalDateTime?>>()
            coVerify { restoreTaskReminders(capture(restored)) }
            assertThat(restored.captured).containsExactly(1L, alreadyDated)
        }

    /**
     * The snackbar sits on screen for seconds, and a second sheet can be opened and closed inside
     * them. Its undo must still restore the batch it was raised for, not whatever happened last.
     */
    @Test
    fun `an undo restores its own batch even after a later sheet has been and gone`() =
        runTest {
            stubDay()
            undatedTasks.value = listOf(undatedRow(1), undatedRow(2))
            val viewModel = openSheet()

            viewModel.plan(taskId = 1)
            advanceUntilIdle()

            val offered = closeAndTakeTheOffer(viewModel)

            // A second session, planning something else entirely, while that offer is still open.
            viewModel.onEvent(FocusContract.UiEvent.UndatedTasksClicked)
            advanceUntilIdle()
            viewModel.plan(taskId = 2)
            advanceUntilIdle()
            viewModel.onEvent(FocusContract.UiEvent.ClosePlanSheet)
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.UndoPlanBatch(offered.batch))
            advanceUntilIdle()

            val restored = slot<Map<Long, LocalDateTime?>>()
            coVerify { restoreTaskReminders(capture(restored)) }
            assertThat(restored.captured.keys).containsExactly(1L)
        }

    /** A sheet that changed nothing must not clear an offer still standing from an earlier one. */
    @Test
    fun `a no-change sheet does not disturb an offer already made`() =
        runTest {
            stubDay()
            undatedTasks.value = listOf(undatedRow(1))
            val viewModel = openSheet()

            viewModel.plan(taskId = 1)
            advanceUntilIdle()

            val offered = closeAndTakeTheOffer(viewModel)

            viewModel.onEvent(FocusContract.UiEvent.UndatedTasksClicked)
            advanceUntilIdle()
            viewModel.onEvent(FocusContract.UiEvent.ClosePlanSheet)
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.UndoPlanBatch(offered.batch))
            advanceUntilIdle()

            val restored = slot<Map<Long, LocalDateTime?>>()
            coVerify { restoreTaskReminders(capture(restored)) }
            assertThat(restored.captured).containsExactly(1L, null)
        }

    /** Closes the sheet and returns the offer it made, the way the snackbar receives it. */
    private suspend fun TestScope.closeAndTakeTheOffer(viewModel: FocusViewModel): PlanBatchConfirmed {
        lateinit var offer: PlanBatchConfirmed
        viewModel.uiEffect.test {
            viewModel.onEvent(FocusContract.UiEvent.ClosePlanSheet)
            advanceUntilIdle()
            offer = awaitItem() as PlanBatchConfirmed
            cancelAndIgnoreRemainingEvents()
        }
        return offer
    }

    /** Opens the sheet and settles the flows behind it. */
    private fun TestScope.openSheet(): FocusViewModel {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onEvent(FocusContract.UiEvent.UndatedTasksClicked)
        advanceUntilIdle()
        return viewModel
    }

    /** The mirror of [plan]: the chip pressed a second time, and the write landing. */
    private fun FocusViewModel.unplan(taskId: Long) {
        onEvent(FocusContract.UiEvent.UnplanTask(taskId))
        undatedTasks.value =
            undatedTasks.value.map { row ->
                if (row.task.id == taskId) row.copy(task = row.task.copy(reminderDate = null)) else row
            }
    }

    /**
     * A chip press, plus the write landing: the repository is what tells the sheet a task now has a
     * date, so the fake source has to say so too.
     */
    private fun FocusViewModel.plan(taskId: Long) {
        onEvent(FocusContract.UiEvent.PlanTask(taskId, tomorrow))
        undatedTasks.value =
            undatedTasks.value.map { row ->
                if (row.task.id == taskId) {
                    row.copy(task = row.task.copy(reminderDate = PlanReminderTimeUtil.resolve(tomorrow, now)))
                } else {
                    row
                }
            }
    }
}
