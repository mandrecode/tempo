package com.mandrecode.tempo.features.widget.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import com.mandrecode.tempo.core.domain.model.ScheduleResult
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import com.mandrecode.tempo.features.tasks.domain.usecase.CreateTaskUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuickAddTaskViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var createTaskUseCase: CreateTaskUseCase
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var themePreferencesRepository: ThemePreferencesRepository
    private lateinit var themeMode: MutableStateFlow<ThemeMode>
    private lateinit var useTempoColors: MutableStateFlow<Boolean>

    private val inboxCategory = Category(id = 1L, name = "Inbox", isDefault = true)
    private val workCategory = Category(id = 2L, name = "Work")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        createTaskUseCase = mockk(relaxed = true)
        categoryRepository = mockk(relaxed = true)
        themePreferencesRepository = mockk(relaxed = true)
        themeMode = MutableStateFlow(ThemeMode.SYSTEM)
        useTempoColors = MutableStateFlow(false)

        every { categoryRepository.getAllCategories() } returns flowOf(listOf(inboxCategory, workCategory))
        every { themePreferencesRepository.getThemeMode() } returns themeMode
        every { themePreferencesRepository.getUseTempoColors() } returns useTempoColors
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        QuickAddTaskViewModel(
            createTaskUseCase = createTaskUseCase,
            categoryRepository = categoryRepository,
            themePreferencesRepository = themePreferencesRepository,
        )

    @Test
    fun whenCategoriesLoad_thenDefaultCategoryIsPreselected() =
        runTest {
            val viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            assertThat(viewModel.uiState.value.selectedCategoryId).isEqualTo(inboxCategory.id)
            assertThat(viewModel.uiState.value.categories).containsExactly(inboxCategory, workCategory)
        }

    @Test
    fun givenEmptyTitle_whenSaveClicked_thenValidationErrorIsShownAndNoCloseEffect() =
        runTest {
            coEvery { createTaskUseCase.invoke(any()) } returns
                CreateTaskUseCase.Result.ValidationError(CreateTaskUseCase.ValidationErrorType.TITLE_EMPTY)
            val viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onEvent(QuickAddTaskContract.UiEvent.SaveClicked)
                testDispatcher.scheduler.advanceUntilIdle()

                assertThat(viewModel.uiState.value.titleErrorRes).isEqualTo(R.string.task_title_required)
                assertThat(viewModel.uiState.value.isSaving).isFalse()
                expectNoEvents()
            }
        }

    @Test
    fun givenValidTitle_whenSaveClicked_thenTaskIsCreatedAndCloseEffectIsEmitted() =
        runTest {
            coEvery { createTaskUseCase.invoke(any()) } returns
                CreateTaskUseCase.Result.Success(1L, ScheduleResult.Skipped)
            val viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.onEvent(QuickAddTaskContract.UiEvent.TitleChanged("Buy groceries"))
            viewModel.onEvent(QuickAddTaskContract.UiEvent.CategorySelected(workCategory.id))

            viewModel.uiEffect.test {
                viewModel.onEvent(QuickAddTaskContract.UiEvent.SaveClicked)
                testDispatcher.scheduler.advanceUntilIdle()

                assertThat(awaitItem()).isEqualTo(QuickAddTaskContract.UiEffect.Close)
            }

            coVerify {
                createTaskUseCase.invoke(
                    Task(title = "Buy groceries", description = "", categoryId = workCategory.id),
                )
            }
        }

    @Test
    fun whenCancelClicked_thenCloseEffectIsEmittedWithoutCreatingTask() =
        runTest {
            val viewModel = createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.uiEffect.test {
                viewModel.onEvent(QuickAddTaskContract.UiEvent.CancelClicked)
                testDispatcher.scheduler.advanceUntilIdle()

                assertThat(awaitItem()).isEqualTo(QuickAddTaskContract.UiEffect.Close)
            }

            coVerify(exactly = 0) { createTaskUseCase.invoke(any()) }
        }
}
