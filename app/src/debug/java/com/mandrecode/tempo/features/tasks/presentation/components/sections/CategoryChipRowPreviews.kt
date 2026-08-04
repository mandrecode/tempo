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
    // Colors are `ColorOption.labelKey` values and icons are `TempoIcon.iconName` values, because
    // that is what `resolveColor()` and `TempoIcon.fromName()` match on. Drawable-style names
    // ("ic_work", "material_blue") resolve to nothing and fall back silently to the default glyph
    // and no color, so the preview stops showing what the screen actually renders.
    val categories =
        listOf(
            Category(id = 1L, name = "Inbox", color = null, icon = "inbox", isDefault = true, sortOrder = 0),
            Category(id = 2L, name = "Work", color = "color_m3_blue", icon = "work", sortOrder = 1),
            Category(id = 3L, name = "Personal", color = "color_m3_green", icon = "home", sortOrder = 2),
            Category(id = 4L, name = "Shopping", color = "color_m3_orange", icon = "shopping_cart", sortOrder = 3),
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
            Category(id = 1L, name = "Inbox", color = "color_m3_purple", icon = "inbox", isDefault = true),
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
