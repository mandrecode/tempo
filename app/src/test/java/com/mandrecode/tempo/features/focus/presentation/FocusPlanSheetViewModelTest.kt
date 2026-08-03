package com.mandrecode.tempo.features.focus.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
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

            viewModel.onEvent(FocusContract.UiEvent.DismissPlanSheet)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.planSheet).isNull()
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
    fun `confirming is barred until something has actually moved`() =
        runTest {
            stubDay()
            undatedTasks.value = listOf(undatedRow(1))
            val viewModel = openSheet()

            assertThat(
                viewModel.uiState.value.planSheet
                    ?.canConfirm,
            ).isFalse()

            viewModel.plan(taskId = 1)
            advanceUntilIdle()

            assertThat(
                viewModel.uiState.value.planSheet
                    ?.canConfirm,
            ).isTrue()
        }

    @Test
    fun `confirming closes the sheet and offers the batch back`() =
        runTest {
            stubDay()
            undatedTasks.value = listOf(undatedRow(1), undatedRow(2), undatedRow(3))
            val viewModel = openSheet()

            viewModel.plan(taskId = 1)
            viewModel.plan(taskId = 2)
            advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onEvent(FocusContract.UiEvent.ConfirmPlanSheet)
                advanceUntilIdle()

                assertThat(awaitItem()).isEqualTo(FocusContract.UiEffect.PlanBatchConfirmed(2))
                cancelAndIgnoreRemainingEvents()
            }
            assertThat(viewModel.uiState.value.planSheet).isNull()
        }

    @Test
    fun `confirming without a change closes quietly`() =
        runTest {
            stubDay()
            undatedTasks.value = listOf(undatedRow(1))
            val viewModel = openSheet()

            viewModel.uiEffect.test {
                viewModel.onEvent(FocusContract.UiEvent.ConfirmPlanSheet)
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
            viewModel.onEvent(FocusContract.UiEvent.ConfirmPlanSheet)
            advanceUntilIdle()

            viewModel.onEvent(FocusContract.UiEvent.UndoPlanBatch)
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
            viewModel.onEvent(FocusContract.UiEvent.ConfirmPlanSheet)
            viewModel.onEvent(FocusContract.UiEvent.UndoPlanBatch)
            advanceUntilIdle()

            val restored = slot<Map<Long, LocalDateTime?>>()
            coVerify { restoreTaskReminders(capture(restored)) }
            assertThat(restored.captured).containsExactly(1L, alreadyDated)
        }

    @Test
    fun `undo is offered once and cannot be taken twice`() =
        runTest {
            stubDay()
            undatedTasks.value = listOf(undatedRow(1))
            val viewModel = openSheet()

            viewModel.plan(taskId = 1)
            advanceUntilIdle()
            viewModel.onEvent(FocusContract.UiEvent.ConfirmPlanSheet)
            viewModel.onEvent(FocusContract.UiEvent.UndoPlanBatch)
            viewModel.onEvent(FocusContract.UiEvent.UndoPlanBatch)
            advanceUntilIdle()

            coVerify(exactly = 1) { restoreTaskReminders(any()) }
        }

    /** Opens the sheet and settles the flows behind it. */
    private fun TestScope.openSheet(): FocusViewModel {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onEvent(FocusContract.UiEvent.UndatedTasksClicked)
        advanceUntilIdle()
        return viewModel
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
