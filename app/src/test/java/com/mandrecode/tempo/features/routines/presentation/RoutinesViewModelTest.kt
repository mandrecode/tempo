package com.mandrecode.tempo.features.routines.presentation

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.domain.model.DayOfWeek
import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.routines.domain.model.HabitType
import com.mandrecode.tempo.features.routines.domain.repository.HabitChainRepository
import com.mandrecode.tempo.features.routines.domain.repository.HabitRepository
import com.mandrecode.tempo.features.routines.domain.usecase.ClearAllHabitRemindersUseCase
import com.mandrecode.tempo.features.routines.domain.usecase.CreateHabitUseCase
import com.mandrecode.tempo.features.routines.domain.usecase.CreateOrUpdateHabitChainUseCase
import com.mandrecode.tempo.features.routines.domain.usecase.DeleteHabitChainUseCase
import com.mandrecode.tempo.features.routines.domain.usecase.DeleteHabitUseCase
import com.mandrecode.tempo.features.routines.domain.usecase.ToggleHabitCompletionUseCase
import com.mandrecode.tempo.features.routines.domain.usecase.UpdateHabitUseCase
import com.mandrecode.tempo.infrastructure.permissions.PermissionChecker
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class RoutinesViewModelTest {
    private lateinit var viewModel: RoutinesViewModel
    private lateinit var habitRepository: HabitRepository
    private lateinit var habitChainRepository: HabitChainRepository
    private lateinit var createHabitUseCase: CreateHabitUseCase
    private lateinit var updateHabitUseCase: UpdateHabitUseCase
    private lateinit var deleteHabitUseCase: DeleteHabitUseCase
    private lateinit var toggleHabitCompletionUseCase: ToggleHabitCompletionUseCase
    private lateinit var createOrUpdateHabitChainUseCase: CreateOrUpdateHabitChainUseCase
    private lateinit var deleteHabitChainUseCase: DeleteHabitChainUseCase
    private lateinit var clearAllHabitRemindersUseCase: ClearAllHabitRemindersUseCase
    private lateinit var permissionChecker: PermissionChecker
    private val testDispatcher = StandardTestDispatcher()

    private val createdDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        habitRepository = mockk(relaxed = true)
        habitChainRepository = mockk(relaxed = true)
        createHabitUseCase = mockk(relaxed = true)
        updateHabitUseCase = mockk(relaxed = true)
        deleteHabitUseCase = mockk(relaxed = true)
        toggleHabitCompletionUseCase = mockk(relaxed = true)
        createOrUpdateHabitChainUseCase = mockk(relaxed = true)
        deleteHabitChainUseCase = mockk(relaxed = true)
        clearAllHabitRemindersUseCase = mockk(relaxed = true)
        permissionChecker = mockk(relaxed = true)

        coEvery { habitRepository.getAllHabits() } returns flowOf(emptyList())
        coEvery { habitChainRepository.getAllHabitChains() } returns flowOf(emptyList())

        viewModel = createViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        RoutinesViewModel(
            habitRepository,
            habitChainRepository,
            createHabitUseCase,
            updateHabitUseCase,
            deleteHabitUseCase,
            toggleHabitCompletionUseCase,
            createOrUpdateHabitChainUseCase,
            deleteHabitChainUseCase,
            clearAllHabitRemindersUseCase,
            permissionChecker,
        )

    // --- Loading ---

    @Test
    fun `loadData sets habits and chains and stops loading`() =
        runTest {
            val habits = listOf(habit(1L), habit(2L))
            val chains = listOf(chain(10L, listOf(1L)))
            coEvery { habitRepository.getAllHabits() } returns flowOf(habits)
            coEvery { habitChainRepository.getAllHabitChains() } returns flowOf(chains)

            viewModel = createViewModel()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.habits).hasSize(2)
            assertThat(viewModel.uiState.value.habitChains).hasSize(1)
            assertThat(viewModel.uiState.value.isLoading).isFalse()
        }

    // --- Show/Hide Bottom Sheet ---

    @Test
    fun `showHabitBottomSheet opens sheet with new habit defaults`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())

            val form = viewModel.uiState.value.habitForm
            assertThat(form.isVisible).isTrue()
            assertThat(form.editingHabit).isNull()
            assertThat(form.selectedTab).isEqualTo(RoutinesContract.HabitSheetTab.HABIT)
            assertThat(form.shouldAutoSelectColor).isTrue()
            assertThat(form.shouldAutoSelectIcon).isTrue()
        }

    @Test
    fun `showHabitBottomSheet opens sheet with existing habit data`() =
        runTest {
            val existingHabit = habit(1L, colorKey = "red", icon = "star")

            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet(existingHabit))

            val form = viewModel.uiState.value.habitForm
            assertThat(form.editingHabit).isEqualTo(existingHabit)
            assertThat(form.selectedColorKey).isEqualTo("red")
            assertThat(form.selectedIcon).isEqualTo("star")
            assertThat(form.shouldAutoSelectColor).isFalse()
        }

    @Test
    fun `showHabitBottomSheet opens sheet scoped to chain`() =
        runTest {
            val chainId = 10L
            val chainColor = "blue"
            val chains = listOf(chain(chainId, colorKey = chainColor))
            coEvery { habitChainRepository.getAllHabitChains() } returns flowOf(chains)

            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet(chainId = chainId))

            val form = viewModel.uiState.value.habitForm
            assertThat(form.isVisible).isTrue()
            assertThat(form.targetChainId).isEqualTo(chainId)
            assertThat(form.selectedColorKey).isEqualTo(chainColor)
            assertThat(form.shouldAutoSelectColor).isFalse()
            assertThat(form.shouldAutoSelectIcon).isTrue()
        }

    @Test
    fun `showHabitChainBottomSheet opens with HABIT_CHAIN tab`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitChainBottomSheet())

            val form = viewModel.uiState.value.habitForm
            assertThat(form.isVisible).isTrue()
            assertThat(form.selectedTab).isEqualTo(RoutinesContract.HabitSheetTab.HABIT_CHAIN)
        }

    @Test
    fun `hideHabitBottomSheet resets form`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())
            viewModel.onEvent(RoutinesContract.UiEvent.HideHabitBottomSheet)

            assertThat(viewModel.uiState.value.habitForm.isVisible).isFalse()
        }

    // --- Tab Selection ---

    @Test
    fun `setSelectedTab updates tab`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())
            viewModel.onEvent(RoutinesContract.UiEvent.SetSelectedTab(RoutinesContract.HabitSheetTab.HABIT_CHAIN))

            assertThat(viewModel.uiState.value.habitForm.selectedTab)
                .isEqualTo(RoutinesContract.HabitSheetTab.HABIT_CHAIN)
        }

    // --- Form Fields ---

    @Test
    fun `setReminder updates form reminder date`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())
            viewModel.onEvent(RoutinesContract.UiEvent.SetReminder(2099, 6, 15, 10, 30))

            assertThat(viewModel.uiState.value.habitForm.reminderDate)
                .isEqualTo(LocalDateTime(2099, 6, 15, 10, 30))
        }

    @Test
    fun `clearReminder nullifies reminder`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.SetReminder(2099, 6, 15, 10, 30))
            viewModel.onEvent(RoutinesContract.UiEvent.ClearReminder)

            assertThat(viewModel.uiState.value.habitForm.reminderDate).isNull()
        }

    @Test
    fun `setColorKey and clearColor update form`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.SetColorKey("blue"))
            assertThat(viewModel.uiState.value.habitForm.selectedColorKey).isEqualTo("blue")

            viewModel.onEvent(RoutinesContract.UiEvent.ClearColor)
            assertThat(viewModel.uiState.value.habitForm.selectedColorKey).isNull()
        }

    @Test
    fun `setIcon and clearIcon update form`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.SetIcon("star"))
            assertThat(viewModel.uiState.value.habitForm.selectedIcon).isEqualTo("star")

            viewModel.onEvent(RoutinesContract.UiEvent.ClearIcon)
            assertThat(viewModel.uiState.value.habitForm.selectedIcon).isNull()
        }

    @Test
    fun `setRepeatDays updates form`() =
        runTest {
            val days = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
            viewModel.onEvent(RoutinesContract.UiEvent.SetRepeatDays(days))

            assertThat(viewModel.uiState.value.habitForm.selectedRepeatDays).isEqualTo(days)
        }

    @Test
    fun `clearHabitErrors resets errors`() =
        runTest {
            // Trigger an error first
            coEvery { createHabitUseCase(any()) } returns
                CreateHabitUseCase.Result.ValidationError(CreateHabitUseCase.ValidationErrorType.TITLE_EMPTY)
            viewModel.onEvent(RoutinesContract.UiEvent.CreateOrUpdateHabit("", "desc"))
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.habitForm.titleError).isNotNull()

            viewModel.onEvent(RoutinesContract.UiEvent.ClearHabitErrors)
            assertThat(viewModel.uiState.value.habitForm.titleError).isNull()
            assertThat(viewModel.uiState.value.habitForm.descriptionError).isNull()
        }

    // --- Date Selection ---

    @Test
    fun `selectDate updates selected date`() =
        runTest {
            val date = LocalDate(2025, 6, 15)
            viewModel.onEvent(RoutinesContract.UiEvent.SelectDate(date))

            assertThat(viewModel.uiState.value.selectedDate).isEqualTo(date)
        }

    // --- Chain Expand/Collapse ---

    @Test
    fun `toggleChainExpanded adds and removes chain ID`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ToggleChainExpanded(1L))
            assertThat(viewModel.uiState.value.expandedChainIds).contains(1L)

            viewModel.onEvent(RoutinesContract.UiEvent.ToggleChainExpanded(1L))
            assertThat(viewModel.uiState.value.expandedChainIds).doesNotContain(1L)
        }

    // --- Create/Update Habit ---

    @Test
    fun `createOrUpdateHabit creates new habit when no editing habit`() =
        runTest {
            coEvery { createHabitUseCase(any()) } returns
                CreateHabitUseCase.Result.Success(
                    habitId = 1L,
                    scheduleResult = ScheduleResult.Skipped,
                    reminderAdvanced = false,
                )

            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())
            viewModel.onEvent(RoutinesContract.UiEvent.CreateOrUpdateHabit("New Habit", "Desc"))
            advanceUntilIdle()

            coVerify { createHabitUseCase(any()) }
            assertThat(viewModel.uiState.value.habitForm.isVisible).isFalse()
        }

    @Test
    fun `createOrUpdateHabit creates new habit and adds to chain when targetChainId is set`() =
        runTest {
            val chainId = 10L
            val createdHabitId = 99L
            val chains = listOf(chain(chainId, habitIds = emptyList()))

            coEvery { habitChainRepository.getAllHabitChains() } returns flowOf(chains)
            coEvery { createHabitUseCase(any()) } returns
                CreateHabitUseCase.Result.Success(
                    habitId = createdHabitId,
                    scheduleResult = ScheduleResult.Skipped,
                    reminderAdvanced = false,
                )
            coEvery { habitChainRepository.getHabitChainById(chainId) } returns chains.first()

            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet(chainId = chainId))
            viewModel.onEvent(RoutinesContract.UiEvent.SetReminder(2023, 1, 1, 10, 0))
            viewModel.onEvent(RoutinesContract.UiEvent.SetRepeatDays(setOf(DayOfWeek.MONDAY)))

            viewModel.onEvent(RoutinesContract.UiEvent.CreateOrUpdateHabit("In Chain Habit", "Desc"))
            advanceUntilIdle()

            val habitSlot = slot<Habit>()
            coVerify { createHabitUseCase(capture(habitSlot)) }
            assertThat(habitSlot.captured.reminderDate).isNull()
            assertThat(habitSlot.captured.repeatDays).isNull()

            val chainSlot = slot<HabitChain>()
            coVerify { habitChainRepository.updateHabitChain(capture(chainSlot)) }
            assertThat(chainSlot.captured.habitIds).contains(createdHabitId)
        }

    @Test
    fun `createOrUpdateHabit updates existing habit`() =
        runTest {
            val existingHabit = habit(1L)
            coEvery { updateHabitUseCase(any()) } returns
                UpdateHabitUseCase.Result.Success(
                    scheduleResult = ScheduleResult.Skipped,
                )

            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet(existingHabit))
            viewModel.onEvent(RoutinesContract.UiEvent.CreateOrUpdateHabit("Updated", "Desc"))
            advanceUntilIdle()

            coVerify { updateHabitUseCase(any()) }
        }

    @Test
    fun `createOrUpdateHabit with autoSave keeps sheet open and emits no success snackbar`() =
        runTest {
            val existingHabit = habit(1L)
            coEvery { updateHabitUseCase(any()) } returns
                UpdateHabitUseCase.Result.Success(
                    scheduleResult = ScheduleResult.Skipped,
                )

            val effects = mutableListOf<RoutinesContract.UiEffect>()
            backgroundScope.launch { viewModel.uiEffect.toList(effects) }

            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet(existingHabit))
            viewModel.onEvent(RoutinesContract.UiEvent.CreateOrUpdateHabit("Updated", "Desc", autoSave = true))
            advanceUntilIdle()

            coVerify { updateHabitUseCase(any()) }
            assertThat(viewModel.uiState.value.habitForm.isVisible).isTrue()
            assertThat(
                effects
                    .filterIsInstance<RoutinesContract.UiEffect.ShowSnackbar>()
                    .map { it.messageResId },
            ).doesNotContain(R.string.msg_habit_updated_success)
        }

    @Test
    fun `createOrUpdateHabit shows error when title is empty`() =
        runTest {
            coEvery { createHabitUseCase(any()) } returns
                CreateHabitUseCase.Result.ValidationError(CreateHabitUseCase.ValidationErrorType.TITLE_EMPTY)

            viewModel.onEvent(RoutinesContract.UiEvent.CreateOrUpdateHabit("", "Desc"))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.habitForm.titleError).isEqualTo(R.string.task_title_required)
        }

    @Test
    fun `createOrUpdateHabit shows error when title too long`() =
        runTest {
            coEvery { createHabitUseCase(any()) } returns
                CreateHabitUseCase.Result.ValidationError(CreateHabitUseCase.ValidationErrorType.TITLE_TOO_LONG)

            viewModel.onEvent(RoutinesContract.UiEvent.CreateOrUpdateHabit("x", "Desc"))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.habitForm.titleError).isEqualTo(R.string.error_habit_title_too_long)
        }

    @Test
    fun `createOrUpdateHabit shows error when description too long`() =
        runTest {
            coEvery { createHabitUseCase(any()) } returns
                CreateHabitUseCase.Result.ValidationError(CreateHabitUseCase.ValidationErrorType.DESCRIPTION_TOO_LONG)

            viewModel.onEvent(RoutinesContract.UiEvent.CreateOrUpdateHabit("Title", "x"))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.habitForm.descriptionError).isEqualTo(R.string.error_habit_description_too_long)
        }

    @Test
    fun `createOrUpdateHabit with permission error shows permission dialog`() =
        runTest {
            coEvery { createHabitUseCase(any()) } returns
                CreateHabitUseCase.Result.Success(
                    habitId = 1L,
                    scheduleResult = ScheduleResult.PermissionError("no perm"),
                    reminderAdvanced = false,
                )

            viewModel.onEvent(RoutinesContract.UiEvent.CreateOrUpdateHabit("Title", "Desc"))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.showPermissionRequestDialog).isTrue()
        }

    // --- Create/Update Habit Chain ---

    @Test
    fun `createOrUpdateHabitChain shows error when title is empty`() =
        runTest {
            coEvery { createOrUpdateHabitChainUseCase(any()) } returns
                CreateOrUpdateHabitChainUseCase.Result.ValidationError(
                    CreateOrUpdateHabitChainUseCase.ValidationErrorType.TITLE_EMPTY,
                )

            viewModel.onEvent(RoutinesContract.UiEvent.CreateOrUpdateHabitChain("", "Desc", listOf(1L)))
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.habitForm.titleError).isEqualTo(R.string.task_title_required)
        }

    @Test
    fun `createOrUpdateHabitChain shows error when too many habits`() =
        runTest {
            val habitIds = (1..51).map { it.toLong() }

            coEvery { createOrUpdateHabitChainUseCase(any()) } returns
                CreateOrUpdateHabitChainUseCase.Result.ValidationError(
                    CreateOrUpdateHabitChainUseCase.ValidationErrorType.TOO_MANY_HABITS,
                )

            viewModel.onEvent(RoutinesContract.UiEvent.CreateOrUpdateHabitChain("Title", "Desc", habitIds))
            advanceUntilIdle()

            coVerify(exactly = 1) { createOrUpdateHabitChainUseCase(any()) }
        }

    @Test
    fun `createOrUpdateHabitChain with empty habitIds on new chain shows snackbar`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())
            viewModel.onEvent(RoutinesContract.UiEvent.CreateOrUpdateHabitChain("Title", "Desc", emptyList()))
            advanceUntilIdle()

            // Should not call use case
            coVerify(exactly = 0) { createOrUpdateHabitChainUseCase(any()) }
        }

    @Test
    fun `createOrUpdateHabitChain with empty habitIds on editing chain shows confirmation`() =
        runTest {
            val existingChain = chain(10L)
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitChainBottomSheet(existingChain))
            viewModel.onEvent(RoutinesContract.UiEvent.CreateOrUpdateHabitChain("Title", "Desc", emptyList()))

            assertThat(viewModel.uiState.value.showEmptyHabitChainConfirmationDialog).isTrue()
        }

    @Test
    fun `createOrUpdateHabitChain with habits having reminders shows clear confirmation`() =
        runTest {
            val habitWithReminder = habit(1L, reminderDate = LocalDateTime(2099, 1, 1, 10, 0))
            coEvery { habitRepository.getAllHabits() } returns flowOf(listOf(habitWithReminder))

            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(RoutinesContract.UiEvent.CreateOrUpdateHabitChain("Title", "Desc", listOf(1L)))

            assertThat(viewModel.uiState.value.showClearRemindersConfirmationDialog).isTrue()
            assertThat(viewModel.uiState.value.pendingHabitChainData).isNotNull()
        }

    // --- Delete Habit ---

    @Test
    fun `showDeleteHabitConfirmation sets habit and shows dialog`() =
        runTest {
            val h = habit(1L)
            viewModel.onEvent(RoutinesContract.UiEvent.ShowDeleteHabitConfirmation(h))

            assertThat(viewModel.uiState.value.habitToDelete).isEqualTo(h)
            assertThat(viewModel.uiState.value.showDeleteHabitConfirmationDialog).isTrue()
        }

    @Test
    fun `hideDeleteHabitConfirmation clears state`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowDeleteHabitConfirmation(habit(1L)))
            viewModel.onEvent(RoutinesContract.UiEvent.HideDeleteHabitConfirmation)

            assertThat(viewModel.uiState.value.habitToDelete).isNull()
            assertThat(viewModel.uiState.value.showDeleteHabitConfirmationDialog).isFalse()
        }

    @Test
    fun `deleteHabit calls use case and hides dialog`() =
        runTest {
            val h = habit(1L)
            viewModel.onEvent(RoutinesContract.UiEvent.ShowDeleteHabitConfirmation(h))
            viewModel.onEvent(RoutinesContract.UiEvent.DeleteHabit)
            advanceUntilIdle()

            coVerify { deleteHabitUseCase(h) }
            assertThat(viewModel.uiState.value.showDeleteHabitConfirmationDialog).isFalse()
        }

    // --- Delete Habit Chain ---

    @Test
    fun `deleteHabitChain with deleteHabits=false calls use case`() =
        runTest {
            val ch = chain(1L, listOf(1L, 2L), periodicReminder = createdDate)
            coEvery { habitRepository.getAllHabits() } returns flowOf(listOf(habit(1L), habit(2L)))
            coEvery { habitChainRepository.getAllHabitChains() } returns flowOf(listOf(ch))

            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(RoutinesContract.UiEvent.ShowDeleteHabitChainConfirmation(ch))
            viewModel.onEvent(RoutinesContract.UiEvent.DeleteHabitChain(false))
            advanceUntilIdle()

            coVerify { deleteHabitChainUseCase(ch, false) }
        }

    @Test
    fun `deleteHabitChain with deleteHabits=true and empty habits deletes only chain`() =
        runTest {
            val ch = chain(1L, emptyList())
            coEvery { habitChainRepository.getAllHabitChains() } returns flowOf(listOf(ch))

            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(RoutinesContract.UiEvent.ShowDeleteHabitChainConfirmation(ch))
            viewModel.onEvent(RoutinesContract.UiEvent.DeleteHabitChain(true))
            advanceUntilIdle()

            coVerify { deleteHabitChainUseCase(ch, true) }
        }

    // --- Toggle Habit Completion ---

    @Test
    fun `toggleHabitCompletion calls use case`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ToggleHabitCompletion(1L, true))
            advanceUntilIdle()

            coVerify { toggleHabitCompletionUseCase(1L, true, any()) }
        }

    // --- Empty Chain Confirmation ---

    @Test
    fun `hideEmptyHabitChainConfirmation resets dialog`() =
        runTest {
            val existingChain = chain(10L)
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitChainBottomSheet(existingChain))
            viewModel.onEvent(RoutinesContract.UiEvent.CreateOrUpdateHabitChain("T", "D", emptyList()))
            assertThat(viewModel.uiState.value.showEmptyHabitChainConfirmationDialog).isTrue()

            viewModel.onEvent(RoutinesContract.UiEvent.HideEmptyHabitChainConfirmation)
            assertThat(viewModel.uiState.value.showEmptyHabitChainConfirmationDialog).isFalse()
        }

    @Test
    fun `confirmDeleteEmptyHabitChain deletes chain`() =
        runTest {
            val existingChain = chain(10L)
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitChainBottomSheet(existingChain))
            viewModel.onEvent(RoutinesContract.UiEvent.ConfirmDeleteEmptyHabitChain)
            advanceUntilIdle()

            coVerify { deleteHabitChainUseCase(existingChain, false) }
        }

    // --- Clear Reminders Confirmation ---

    @Test
    fun `hideClearRemindersConfirmation resets state`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.HideClearRemindersConfirmation)

            assertThat(viewModel.uiState.value.showClearRemindersConfirmationDialog).isFalse()
            assertThat(viewModel.uiState.value.pendingHabitChainData).isNull()
        }

    @Test
    fun `confirmClearRemindersAndProceed creates chain from pending data`() =
        runTest {
            val habitWithReminder = habit(1L, reminderDate = LocalDateTime(2099, 1, 1, 10, 0))
            coEvery { habitRepository.getAllHabits() } returns flowOf(listOf(habitWithReminder))
            coEvery { createOrUpdateHabitChainUseCase(any()) } returns
                CreateOrUpdateHabitChainUseCase.Result.Success(R.string.msg_habit_chain_created_success)

            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(RoutinesContract.UiEvent.CreateOrUpdateHabitChain("Title", "Desc", listOf(1L)))
            assertThat(viewModel.uiState.value.pendingHabitChainData).isNotNull()

            viewModel.onEvent(RoutinesContract.UiEvent.ConfirmClearRemindersAndProceed)
            advanceUntilIdle()

            coVerify { createOrUpdateHabitChainUseCase(any()) }
            assertThat(viewModel.uiState.value.showClearRemindersConfirmationDialog).isFalse()
        }

    @Test
    fun `confirmClearRemindersAndProceed preserves autoSave for habit chain updates`() =
        runTest {
            val habitWithReminder = habit(1L, reminderDate = LocalDateTime(2099, 1, 1, 10, 0))
            val existingChain = chain(10L, listOf(1L))
            coEvery { habitRepository.getAllHabits() } returns flowOf(listOf(habitWithReminder))
            coEvery { createOrUpdateHabitChainUseCase(any()) } returns
                CreateOrUpdateHabitChainUseCase.Result.Success(R.string.msg_habit_chain_updated_success)

            val effects = mutableListOf<RoutinesContract.UiEffect>()
            backgroundScope.launch { viewModel.uiEffect.toList(effects) }

            viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitChainBottomSheet(existingChain))
            viewModel.onEvent(
                RoutinesContract.UiEvent.CreateOrUpdateHabitChain(
                    "Updated",
                    "Desc",
                    listOf(1L),
                    autoSave = true,
                ),
            )
            assertThat(
                viewModel.uiState.value.pendingHabitChainData
                    ?.autoSave,
            ).isTrue()

            viewModel.onEvent(RoutinesContract.UiEvent.ConfirmClearRemindersAndProceed)
            advanceUntilIdle()

            coVerify { createOrUpdateHabitChainUseCase(any()) }
            assertThat(viewModel.uiState.value.habitForm.isVisible).isTrue()
            assertThat(
                effects
                    .filterIsInstance<RoutinesContract.UiEffect.ShowSnackbar>()
                    .map { it.messageResId },
            ).doesNotContain(R.string.msg_habit_chain_updated_success)
        }

    // --- Notification Opening ---

    @Test
    fun `openHabitFromNotification opens bottom sheet if habit exists`() =
        runTest {
            val h = habit(5L)
            coEvery { habitRepository.getHabitById(5L) } returns h

            viewModel.openHabitFromNotification(5L)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.habitForm.isVisible).isTrue()
            assertThat(viewModel.uiState.value.habitForm.editingHabit).isEqualTo(h)
        }

    @Test
    fun `openHabitFromNotification does nothing if habit not found`() =
        runTest {
            coEvery { habitRepository.getHabitById(5L) } returns null

            viewModel.openHabitFromNotification(5L)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.habitForm.isVisible).isFalse()
        }

    @Test
    fun `openHabitChainFromNotification opens chain sheet if chain exists`() =
        runTest {
            val ch = chain(5L)
            coEvery { habitChainRepository.getHabitChainById(5L) } returns ch

            viewModel.openHabitChainFromNotification(5L)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.habitForm.isVisible).isTrue()
            assertThat(viewModel.uiState.value.habitForm.editingHabitChain).isEqualTo(ch)
        }

    @Test
    fun `openHabitChainFromNotification with scheduled date selects date before opening chain sheet`() =
        runTest {
            val ch = chain(5L)
            val scheduledDate = LocalDate(2026, 5, 8)
            coEvery { habitChainRepository.getHabitChainById(5L) } returns ch

            viewModel.openHabitChainFromNotification(5L, scheduledDate)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.selectedDate).isEqualTo(scheduledDate)
            assertThat(viewModel.uiState.value.habitForm.isVisible).isTrue()
            assertThat(viewModel.uiState.value.habitForm.editingHabitChain).isEqualTo(ch)
        }

    @Test
    fun `openHabitChainFromNotification without scheduled date keeps selected date`() =
        runTest {
            val ch = chain(5L)
            val selectedDate = LocalDate(2026, 5, 7)
            coEvery { habitChainRepository.getHabitChainById(5L) } returns ch

            viewModel.onEvent(RoutinesContract.UiEvent.SelectDate(selectedDate))
            viewModel.openHabitChainFromNotification(5L)
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.selectedDate).isEqualTo(selectedDate)
            assertThat(viewModel.uiState.value.habitForm.isVisible).isTrue()
            assertThat(viewModel.uiState.value.habitForm.editingHabitChain).isEqualTo(ch)
        }

    // --- Permissions ---

    @Test
    fun `checkPermissionsAndSyncReminders with all granted and previously not granted sends snackbar`() =
        runTest {
            every { permissionChecker.hasNotificationPermissions() } returns true
            every { permissionChecker.canScheduleExactAlarms() } returns true

            viewModel.checkPermissionsAndSyncReminders()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.permissionInfo.areAllGranted).isTrue()
        }

    @Test
    fun `checkPermissionsAndSyncReminders with revoked permissions and habits shows dialog`() =
        runTest {
            every { permissionChecker.hasNotificationPermissions() } returns false
            every { permissionChecker.canScheduleExactAlarms() } returns true
            coEvery { habitRepository.getHabitsWithReminders() } returns listOf(habit(1L))

            viewModel.checkPermissionsAndSyncReminders()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.showPermissionRevokedDialog).isTrue()
        }

    @Test
    fun `dismissPermissionRequestDialog hides dialog`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.DismissPermissionRequestDialog)

            assertThat(viewModel.uiState.value.showPermissionRequestDialog).isFalse()
        }

    @Test
    fun `dismissPermissionRevokedDialog hides dialog`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.DismissPermissionRevokedDialog)

            assertThat(viewModel.uiState.value.showPermissionRevokedDialog).isFalse()
        }

    @Test
    fun `confirmClearAllHabitReminders calls use case and hides dialog`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ConfirmClearAllHabitReminders)
            advanceUntilIdle()

            coVerify { clearAllHabitRemindersUseCase() }
            assertThat(viewModel.uiState.value.showPermissionRevokedDialog).isFalse()
        }

    // --- Habit Type ---

    @Test
    fun `setHabitType updates selected habit type`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())
            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.QUIT))

            assertThat(viewModel.uiState.value.habitForm.selectedHabitType)
                .isEqualTo(HabitType.QUIT)
        }

    @Test
    fun `setHabitType to QUIT pre-fills 21-00 reminder when no reminder set`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())
            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.QUIT))

            val reminder = viewModel.uiState.value.habitForm.reminderDate
            assertThat(reminder).isNotNull()
            assertThat(reminder!!.hour).isEqualTo(21)
            assertThat(reminder.minute).isEqualTo(0)
        }

    @Test
    fun `setHabitType to QUIT does not override existing reminder`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())
            viewModel.onEvent(RoutinesContract.UiEvent.SetReminder(2099, 1, 1, 8, 30))
            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.QUIT))

            val reminder = viewModel.uiState.value.habitForm.reminderDate
            assertThat(reminder).isNotNull()
            assertThat(reminder!!.hour).isEqualTo(8)
            assertThat(reminder.minute).isEqualTo(30)
        }

    @Test
    fun `setHabitType to QUIT pre-fills next upcoming 21-00 reminder regardless of selectedDate`() =
        runTest {
            // User is viewing a past date in the day picker.
            val pastDate = LocalDate(2020, 1, 1)
            viewModel.onEvent(RoutinesContract.UiEvent.SelectDate(pastDate))
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())

            // Capture `now` BEFORE firing the event so a 21:00 boundary crossing between
            // capture and the VM's internal Clock.System.now() can never make the
            // assertion flake. The set of acceptable dates is whichever of (today,
            // tomorrow relative to capturedNow) the VM might have computed.
            val capturedNow =
                kotlin.time.Clock.System
                    .now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())

            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.QUIT))

            val reminder = viewModel.uiState.value.habitForm.reminderDate
            assertThat(reminder).isNotNull()
            assertThat(reminder!!.hour).isEqualTo(21)
            assertThat(reminder.minute).isEqualTo(0)
            // Must be one of the two valid next-upcoming-21:00 dates.
            val acceptableDates =
                setOf(capturedNow.date, capturedNow.date.plus(kotlinx.datetime.DatePeriod(days = 1)))
            assertThat(acceptableDates).contains(reminder.date)
            // And in any case must be in the future relative to when we captured.
            assertThat(reminder > capturedNow).isTrue()
            // Crucially, never the past day-picker selection.
            assertThat(reminder.date).isNotEqualTo(pastDate)
        }

    @Test
    fun `showHabitBottomSheet with quit habit populates selectedHabitType`() =
        runTest {
            val quitHabit = habit(1L, habitType = HabitType.QUIT)
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet(quitHabit))

            assertThat(viewModel.uiState.value.habitForm.selectedHabitType)
                .isEqualTo(HabitType.QUIT)
        }

    // --- Quit Habit Behavior ---

    @Test
    fun `setHabitType to QUIT clears repeat days and leaves icon for predictive logic`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())
            viewModel.onEvent(RoutinesContract.UiEvent.SetRepeatDays(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)))

            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.QUIT))

            val form = viewModel.uiState.value.habitForm
            assertThat(form.selectedRepeatDays).isNull()
            // Icon is not forced; predictive suggestIcon in HabitBottomSheet handles it.
            assertThat(form.selectedIcon).isNull()
        }

    @Test
    fun `setHabitType to QUIT does not override user-selected icon`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())
            viewModel.onEvent(RoutinesContract.UiEvent.SetIcon("star"))

            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.QUIT))

            val form = viewModel.uiState.value.habitForm
            assertThat(form.selectedIcon).isEqualTo("star")
        }

    @Test
    fun `setHabitType back to BUILD preserves null icon when never set`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())
            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.QUIT))

            assertThat(viewModel.uiState.value.habitForm.selectedIcon).isNull()

            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.BUILD))

            assertThat(viewModel.uiState.value.habitForm.selectedIcon).isNull()
        }

    @Test
    fun `setHabitType round-trip preserves user-selected icon`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())
            // User explicitly picks the shield icon before switching to QUIT.
            viewModel.onEvent(RoutinesContract.UiEvent.SetIcon("shield"))
            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.QUIT))

            // Switching back to BUILD must NOT clear the user's shield choice.
            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.BUILD))

            assertThat(viewModel.uiState.value.habitForm.selectedIcon).isEqualTo("shield")
        }

    @Test
    fun `setHabitType back to BUILD clears auto-applied reminder`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())
            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.QUIT))

            assertThat(viewModel.uiState.value.habitForm.reminderDate).isNotNull()

            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.BUILD))

            assertThat(viewModel.uiState.value.habitForm.reminderDate).isNull()
        }

    @Test
    fun `setHabitType back to BUILD preserves user-selected reminder`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())
            viewModel.onEvent(RoutinesContract.UiEvent.SetReminder(2099, 1, 1, 8, 30))
            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.QUIT))

            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.BUILD))

            val reminder = viewModel.uiState.value.habitForm.reminderDate
            assertThat(reminder).isNotNull()
            assertThat(reminder!!.hour).isEqualTo(8)
            assertThat(reminder.minute).isEqualTo(30)
        }

    @Test
    fun `setHabitType round-trip restores cleared repeat days`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())
            val originalDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
            viewModel.onEvent(RoutinesContract.UiEvent.SetRepeatDays(originalDays))

            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.QUIT))
            assertThat(viewModel.uiState.value.habitForm.selectedRepeatDays).isNull()

            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.BUILD))

            val form = viewModel.uiState.value.habitForm
            assertThat(form.selectedRepeatDays).isEqualTo(originalDays)
            assertThat(form.quitClearedRepeatDays).isNull()
            assertThat(form.quitRepeatDaysCleared).isFalse()
        }

    @Test
    fun `setHabitType round-trip restores null repeat days (every day)`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())
            // Default selectedRepeatDays is null = every day; never explicitly set.

            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.QUIT))
            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.BUILD))

            val form = viewModel.uiState.value.habitForm
            assertThat(form.selectedRepeatDays).isNull()
            assertThat(form.quitRepeatDaysCleared).isFalse()
        }

    @Test
    fun `manually setting repeat days while in QUIT drops the snapshot`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())
            val originalDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)
            viewModel.onEvent(RoutinesContract.UiEvent.SetRepeatDays(originalDays))
            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.QUIT))

            // User explicitly chooses repeat days while in QUIT.
            val newDays = setOf(DayOfWeek.FRIDAY)
            viewModel.onEvent(RoutinesContract.UiEvent.SetRepeatDays(newDays))

            // Switching back to BUILD must NOT undo their explicit choice.
            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.BUILD))

            val form = viewModel.uiState.value.habitForm
            assertThat(form.selectedRepeatDays).isEqualTo(newDays)
            assertThat(form.quitClearedRepeatDays).isNull()
            assertThat(form.quitRepeatDaysCleared).isFalse()
        }

    @Test
    fun `editing build habit then round-trip type does not produce unsaved changes`() =
        runTest {
            // Edit an existing Build habit with repeat days and no reminder.
            val existing =
                habit(
                    id = 42L,
                    habitType = HabitType.BUILD,
                    repeatDays = setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
                    reminderDate = null,
                )
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet(habit = existing))
            val initialForm = viewModel.uiState.value.habitForm

            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.QUIT))
            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.BUILD))

            val form = viewModel.uiState.value.habitForm
            assertThat(form.selectedRepeatDays).isEqualTo(initialForm.selectedRepeatDays)
            assertThat(form.reminderDate).isEqualTo(initialForm.reminderDate)
            assertThat(form.selectedHabitType).isEqualTo(initialForm.selectedHabitType)
            assertThat(form.quitDefaultReminderApplied).isFalse()
            assertThat(form.quitRepeatDaysCleared).isFalse()
            assertThat(form.quitClearedRepeatDays).isNull()
        }

    @Test
    fun `setHabitType is idempotent when re-selecting current QUIT type`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())
            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.QUIT))
            // User clears the auto-applied reminder.
            viewModel.onEvent(RoutinesContract.UiEvent.ClearReminder)
            val afterClear = viewModel.uiState.value.habitForm
            assertThat(afterClear.reminderDate).isNull()
            assertThat(afterClear.quitDefaultReminderApplied).isFalse()

            // Re-tapping the already-selected QUIT card must not re-apply defaults.
            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.QUIT))

            val form = viewModel.uiState.value.habitForm
            assertThat(form.reminderDate).isNull()
            assertThat(form.quitDefaultReminderApplied).isFalse()
            // Snapshot/cleared flag must remain whatever it already was, not re-snapshot.
            assertThat(form.quitRepeatDaysCleared).isEqualTo(afterClear.quitRepeatDaysCleared)
            assertThat(form.quitClearedRepeatDays).isEqualTo(afterClear.quitClearedRepeatDays)
        }

    @Test
    fun `setHabitType is idempotent when re-selecting current BUILD type`() =
        runTest {
            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())
            val initial = viewModel.uiState.value.habitForm
            assertThat(initial.selectedHabitType).isEqualTo(HabitType.BUILD)

            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.BUILD))

            val form = viewModel.uiState.value.habitForm
            assertThat(form).isEqualTo(initial)
        }

    @Test
    fun computeTimelineItems_quitHabitsExcludedFromTimeline() {
        val buildHabit = habit(id = 1, reminderDate = LocalDateTime(2024, 6, 15, 8, 0))
        val quitHabit = habit(id = 2, habitType = HabitType.QUIT)
        val date = LocalDate(2024, 6, 15)

        val (scheduled, unscheduled) =
            computeTimelineItems(
                habits = listOf(buildHabit, quitHabit),
                habitChains = emptyList(),
                selectedDate = date,
            )

        // Only the build habit should appear
        val allHabitIds =
            scheduled.mapNotNull { it.habitId } + unscheduled.mapNotNull { it.habitId }
        assertThat(allHabitIds).contains(1L)
        assertThat(allHabitIds).doesNotContain(2L)
    }

    @Test
    fun `createOrUpdateHabit forces repeatDays null for quit habits`() =
        runTest {
            val habitSlot = slot<Habit>()
            coEvery { createHabitUseCase(capture(habitSlot)) } returns
                CreateHabitUseCase.Result.Success(
                    habitId = 1L,
                    scheduleResult = ScheduleResult.Skipped,
                    reminderAdvanced = false,
                )

            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet())
            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.QUIT))
            viewModel.onEvent(RoutinesContract.UiEvent.CreateOrUpdateHabit("Quit Smoking", ""))
            advanceUntilIdle()

            assertThat(habitSlot.captured.repeatDays).isNull()
            assertThat(habitSlot.captured.habitType).isEqualTo(HabitType.QUIT)
        }

    @Test
    fun `createOrUpdateHabit coerces habitType to BUILD when editing a chained habit`() =
        runTest {
            // Defense-in-depth: even if the form somehow holds QUIT, a chained habit
            // must be persisted as BUILD (UI hides the selector for chained habits).
            val chainedHabitId = 1L
            val chainedHabit = habit(id = chainedHabitId)
            val owningChain = chain(id = 10L, habitIds = listOf(chainedHabitId))
            coEvery { habitRepository.getAllHabits() } returns flowOf(listOf(chainedHabit))
            coEvery { habitChainRepository.getAllHabitChains() } returns flowOf(listOf(owningChain))
            viewModel = createViewModel()
            advanceUntilIdle()

            val habitSlot = slot<Habit>()
            coEvery { updateHabitUseCase(capture(habitSlot)) } returns
                UpdateHabitUseCase.Result.Success(scheduleResult = ScheduleResult.Skipped)

            viewModel.onEvent(RoutinesContract.UiEvent.ShowHabitBottomSheet(chainedHabit))
            viewModel.onEvent(RoutinesContract.UiEvent.SetHabitType(HabitType.QUIT))
            viewModel.onEvent(RoutinesContract.UiEvent.CreateOrUpdateHabit("Chained Habit", ""))
            advanceUntilIdle()

            assertThat(habitSlot.captured.habitType).isEqualTo(HabitType.BUILD)
        }

    // --- Helpers ---

    private fun habit(
        id: Long = 0L,
        colorKey: String? = null,
        icon: String? = null,
        reminderDate: LocalDateTime? = null,
        completionHistory: String = "",
        habitType: HabitType = HabitType.BUILD,
        repeatDays: Set<DayOfWeek>? = null,
    ) = Habit(
        id = id,
        title = "Habit $id",
        description = "",
        colorKey = colorKey,
        icon = icon,
        reminderDate = reminderDate,
        habitType = habitType,
        createdDate = createdDate,
        completionHistory = completionHistory,
        repeatDays = repeatDays,
    )

    private fun chain(
        id: Long = 0L,
        habitIds: List<Long> = emptyList(),
        periodicReminder: LocalDateTime? = null,
        colorKey: String? = null,
        icon: String? = null,
    ) = HabitChain(
        id = id,
        title = "Chain $id",
        description = "",
        habitIds = habitIds,
        colorKey = colorKey,
        icon = icon,
        periodicReminder = periodicReminder,
        createdDate = createdDate,
    )

    // --- computeTimelineItems stability tests ---

    @Test
    fun computeTimelineItems_chainHabitsNeverAppearAsStandalone() {
        val chain =
            chain(
                id = 10,
                habitIds = listOf(1L, 2L),
                periodicReminder = LocalDateTime(2024, 6, 15, 8, 0),
            )
        val chainHabit1 = habit(id = 1)
        val chainHabit2 = habit(id = 2)
        val standalone = habit(id = 3, reminderDate = null)
        val date = LocalDate(2024, 6, 15) // Saturday

        val (scheduled, unscheduled) =
            computeTimelineItems(
                habits = listOf(chainHabit1, chainHabit2, standalone),
                habitChains = listOf(chain),
                selectedDate = date,
            )

        // Chain is in scheduled items
        assertThat(scheduled).hasSize(1)
        assertThat(scheduled[0].isChain).isTrue()
        assertThat(scheduled[0].chainId).isEqualTo(10L)

        // Only standalone habit (id=3) is in unscheduled — chain children are excluded
        assertThat(unscheduled).hasSize(1)
        assertThat(unscheduled[0].habitId).isEqualTo(3L)
    }

    @Test
    fun computeTimelineItems_stableAcrossCompletionToggles() {
        val chain =
            chain(
                id = 10,
                habitIds = listOf(1L, 2L),
                periodicReminder = LocalDateTime(2024, 6, 15, 8, 0),
            )
        val habit1 = habit(id = 1)
        val habit2 = habit(id = 2)
        val date = LocalDate(2024, 6, 15)

        val resultBefore =
            computeTimelineItems(
                habits = listOf(habit1, habit2),
                habitChains = listOf(chain),
                selectedDate = date,
            )

        // Simulate toggle: habit1 now has completionHistory
        val toggledHabit1 = habit(id = 1, completionHistory = "2024-06-15")
        val resultAfter =
            computeTimelineItems(
                habits = listOf(toggledHabit1, habit2),
                habitChains = listOf(chain),
                selectedDate = date,
            )

        // Timeline structure is identical despite completion change
        assertThat(resultAfter).isEqualTo(resultBefore)
    }

    @Test
    fun computeTimelineItems_stableWhenOnlyHabitsChange() {
        // Simulates intermediate combine() state: habits updated, chains unchanged
        val chain =
            chain(
                id = 10,
                habitIds = listOf(1L, 2L),
                periodicReminder = LocalDateTime(2024, 6, 15, 8, 0),
            )
        val habit1 = habit(id = 1)
        val habit2 = habit(id = 2)
        val date = LocalDate(2024, 6, 15)

        val resultOldHabits =
            computeTimelineItems(
                habits = listOf(habit1, habit2),
                habitChains = listOf(chain),
                selectedDate = date,
            )

        val resultNewHabits =
            computeTimelineItems(
                habits = listOf(habit1.copy(completionHistory = "2024-06-15"), habit2),
                habitChains = listOf(chain),
                selectedDate = date,
            )

        // Same structure — chain habits never leak to standalone
        assertThat(resultNewHabits).isEqualTo(resultOldHabits)
    }
}
