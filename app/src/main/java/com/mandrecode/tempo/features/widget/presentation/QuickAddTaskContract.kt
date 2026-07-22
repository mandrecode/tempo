package com.mandrecode.tempo.features.widget.presentation

import androidx.annotation.StringRes
import com.mandrecode.tempo.core.domain.model.ThemeMode
import com.mandrecode.tempo.features.tasks.domain.model.Category
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Contract for the widget-launched quick-add-task surface, following the app's MVI pattern.
 */
object QuickAddTaskContract {
    data class UiState(
        val title: String = "",
        val categories: ImmutableList<Category> = persistentListOf(),
        val selectedCategoryId: Long = 0L,
        val isLoading: Boolean = true,
        val isSaving: Boolean = false,
        @StringRes val titleErrorRes: Int? = null,
        val themeMode: ThemeMode = ThemeMode.SYSTEM,
        val useTempoColors: Boolean = false,
    )

    sealed interface UiEvent {
        data class TitleChanged(
            val title: String,
        ) : UiEvent

        data class CategorySelected(
            val categoryId: Long,
        ) : UiEvent

        data object SaveClicked : UiEvent

        data object CancelClicked : UiEvent
    }

    sealed interface UiEffect {
        data object Close : UiEffect
    }
}
