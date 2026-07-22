package com.mandrecode.tempo.features.widget.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.data.preferences.ThemePreferencesRepository
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.repository.CategoryRepository
import com.mandrecode.tempo.features.tasks.domain.usecase.CreateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickAddTaskViewModel
    @Inject
    constructor(
        private val createTaskUseCase: CreateTaskUseCase,
        private val categoryRepository: CategoryRepository,
        private val themePreferencesRepository: ThemePreferencesRepository,
    ) : ViewModel() {
        private val mutableUiState = MutableStateFlow(QuickAddTaskContract.UiState())
        val uiState: StateFlow<QuickAddTaskContract.UiState> = mutableUiState.asStateFlow()

        private val effectChannel = Channel<QuickAddTaskContract.UiEffect>(Channel.BUFFERED)
        val uiEffect = effectChannel.receiveAsFlow()

        init {
            observeCategories()
            observeThemePreferences()
        }

        fun onEvent(event: QuickAddTaskContract.UiEvent) {
            when (event) {
                is QuickAddTaskContract.UiEvent.TitleChanged -> onTitleChanged(event.title)
                is QuickAddTaskContract.UiEvent.CategorySelected -> selectCategory(event.categoryId)
                QuickAddTaskContract.UiEvent.SaveClicked -> save()
                QuickAddTaskContract.UiEvent.CancelClicked -> cancel()
            }
        }

        private fun onTitleChanged(title: String) {
            mutableUiState.update { it.copy(title = title, titleErrorRes = null) }
        }

        private fun selectCategory(categoryId: Long) {
            mutableUiState.update { it.copy(selectedCategoryId = categoryId) }
        }

        private fun cancel() {
            if (mutableUiState.value.isSaving) return
            viewModelScope.launch { effectChannel.send(QuickAddTaskContract.UiEffect.Close) }
        }

        private fun save() {
            val state = mutableUiState.value
            if (state.isSaving) return

            viewModelScope.launch {
                mutableUiState.update { it.copy(isSaving = true) }
                val task =
                    Task(
                        title = state.title,
                        description = "",
                        categoryId = state.selectedCategoryId,
                    )
                when (val result = createTaskUseCase(task)) {
                    is CreateTaskUseCase.Result.ValidationError -> {
                        val errorRes =
                            when (result.type) {
                                CreateTaskUseCase.ValidationErrorType.TITLE_EMPTY ->
                                    R.string.task_title_required
                                CreateTaskUseCase.ValidationErrorType.TITLE_TOO_LONG ->
                                    R.string.error_task_title_too_long
                                CreateTaskUseCase.ValidationErrorType.DESCRIPTION_TOO_LONG ->
                                    R.string.error_task_description_too_long
                            }
                        mutableUiState.update { it.copy(isSaving = false, titleErrorRes = errorRes) }
                    }

                    is CreateTaskUseCase.Result.Success -> {
                        mutableUiState.update { it.copy(isSaving = false) }
                        effectChannel.send(QuickAddTaskContract.UiEffect.Close)
                    }
                }
            }
        }

        private fun observeCategories() {
            viewModelScope.launch {
                categoryRepository.getAllCategories().collect { categories ->
                    mutableUiState.update { current ->
                        val stillValid = categories.any { it.id == current.selectedCategoryId }
                        val selectedCategoryId =
                            if (current.selectedCategoryId != 0L && stillValid) {
                                current.selectedCategoryId
                            } else {
                                categories.firstOrNull { it.isDefault }?.id
                                    ?: categories.firstOrNull()?.id
                                    ?: 0L
                            }
                        current.copy(
                            categories = categories.toImmutableList(),
                            selectedCategoryId = selectedCategoryId,
                            isLoading = false,
                        )
                    }
                }
            }
        }

        private fun observeThemePreferences() {
            viewModelScope.launch {
                themePreferencesRepository.getThemeMode().collect { mode ->
                    mutableUiState.update { it.copy(themeMode = mode) }
                }
            }
            viewModelScope.launch {
                themePreferencesRepository.getUseTempoColors().collect { useTempoColors ->
                    mutableUiState.update { it.copy(useTempoColors = useTempoColors) }
                }
            }
        }
    }
