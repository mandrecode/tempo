package com.mandrecode.tempo.features.widget.presentation

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.tasks.domain.model.Category
import kotlinx.collections.immutable.persistentListOf

private val previewCategories =
    persistentListOf(
        Category(id = 1L, name = "Inbox", isDefault = true),
        Category(id = 2L, name = "Work"),
        Category(id = 3L, name = "Personal"),
    )

@Preview(name = "Quick Add - Light", device = "id:pixel_9", showBackground = true)
@Preview(
    name = "Quick Add - Dark",
    device = "id:pixel_9",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Composable
private fun QuickAddTaskContentPreview() {
    TempoTheme {
        QuickAddTaskContent(
            uiState =
                QuickAddTaskContract.UiState(
                    categories = previewCategories,
                    selectedCategoryId = 1L,
                ),
            onEvent = {},
        )
    }
}

@Preview(name = "Quick Add - Validation Error", device = "id:pixel_9", showBackground = true)
@Composable
private fun QuickAddTaskContentErrorPreview() {
    TempoTheme {
        QuickAddTaskContent(
            uiState =
                QuickAddTaskContract.UiState(
                    categories = previewCategories,
                    selectedCategoryId = 1L,
                    titleErrorRes = R.string.task_title_required,
                ),
            onEvent = {},
        )
    }
}

@Preview(name = "Quick Add - Saving", device = "id:pixel_9", showBackground = true)
@Composable
private fun QuickAddTaskContentSavingPreview() {
    TempoTheme {
        QuickAddTaskContent(
            uiState =
                QuickAddTaskContract.UiState(
                    title = "Buy groceries",
                    categories = previewCategories,
                    selectedCategoryId = 1L,
                    isSaving = true,
                ),
            onEvent = {},
        )
    }
}
