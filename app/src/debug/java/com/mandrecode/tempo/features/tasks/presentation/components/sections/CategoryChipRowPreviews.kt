package com.mandrecode.tempo.features.tasks.presentation.components.sections

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.tasks.domain.model.Category

// region CategoryChipRow Previews

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoryChipRowSelectedPreview() {
    val categories =
        listOf(
            Category(id = 1L, name = "Inbox", color = null, icon = "ic_category", isDefault = true, sortOrder = 0),
            Category(id = 2L, name = "Work", color = "material_blue", icon = "ic_work", sortOrder = 1),
            Category(id = 3L, name = "Personal", color = "material_green", icon = "ic_person", sortOrder = 2),
            Category(id = 4L, name = "Shopping", color = "material_orange", icon = "ic_shopping_cart", sortOrder = 3),
        )
    val counts = mapOf(1L to 5, 2L to 3, 3L to 8, 4L to 0)

    TempoTheme {
        CategoryChipRow(
            categories = categories,
            counts = counts,
            selectedCategoryId = 2L,
            onSelectCategory = {},
            onShowCategoryDialog = {},
            onRequestDeleteCategory = {},
            onReorderCategories = { _, _, _ -> },
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoryChipRowNoColorPreview() {
    val categories =
        listOf(
            Category(id = 1L, name = "Inbox", isDefault = true, sortOrder = 0),
            Category(id = 2L, name = "Work", sortOrder = 1),
        )
    val counts = mapOf(1L to 2, 2L to 0)

    TempoTheme {
        CategoryChipRow(
            categories = categories,
            counts = counts,
            selectedCategoryId = 1L,
            onSelectCategory = {},
            onShowCategoryDialog = {},
            onRequestDeleteCategory = {},
            onReorderCategories = { _, _, _ -> },
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoryChipRowSingleCategoryPreview() {
    val categories =
        listOf(
            Category(id = 1L, name = "Inbox", color = "material_purple", icon = "ic_category", isDefault = true),
        )

    TempoTheme {
        CategoryChipRow(
            categories = categories,
            counts = mapOf(1L to 12),
            selectedCategoryId = 1L,
            onSelectCategory = {},
            onShowCategoryDialog = {},
            onRequestDeleteCategory = {},
            onReorderCategories = { _, _, _ -> },
        )
    }
}

// endregion
