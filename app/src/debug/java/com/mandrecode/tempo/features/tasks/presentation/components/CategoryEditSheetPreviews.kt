package com.mandrecode.tempo.features.tasks.presentation.components

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.theme.TempoTheme
import com.mandrecode.tempo.features.tasks.domain.model.Category

// region CategoryEditSheet – New Category Previews

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoryEditSheetNewPreview() {
    TempoTheme {
        CategoryEditSheet(
            category = null,
            categories = emptyList(),
            nameError = null,
            onDismiss = {},
            onSave = { _, _, _, _ -> },
            onDelete = null,
            onClearError = {},
        )
    }
}

// endregion

// region CategoryEditSheet – Edit Category Previews

// Colors below are `ColorOption.labelKey` values and icons are `TempoIcon.iconName` values,
// because that is what `resolveColor()` and `TempoIcon.fromName()` match on. Drawable-style names
// ("ic_work", "material_blue") resolve to nothing and fall back silently to the default glyph and
// no color — which in this sheet also means the selected swatch and icon read as unset.

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoryEditSheetEditPreview() {
    val category =
        Category(
            id = 2L,
            name = "Work",
            color = "color_m3_blue",
            icon = "work",
            isDefault = false,
        )
    TempoTheme {
        CategoryEditSheet(
            category = category,
            categories = listOf(category),
            nameError = null,
            onDismiss = {},
            onSave = { _, _, _, _ -> },
            onDelete = {},
            onClearError = {},
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoryEditSheetDefaultCategoryPreview() {
    val category =
        Category(
            id = 1L,
            name = "Inbox",
            color = "color_m3_purple",
            icon = "inbox",
            isDefault = true,
        )
    TempoTheme {
        CategoryEditSheet(
            category = category,
            categories = listOf(category),
            nameError = null,
            onDismiss = {},
            onSave = { _, _, _, _ -> },
            onDelete = {},
            onClearError = {},
        )
    }
}

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CategoryEditSheetErrorPreview() {
    val category =
        Category(
            id = 2L,
            name = "Work",
            color = "color_m3_blue",
            icon = "work",
        )
    TempoTheme {
        CategoryEditSheet(
            category = category,
            categories = listOf(category),
            nameError = R.string.error_category_name_too_long,
            onDismiss = {},
            onSave = { _, _, _, _ -> },
            onDelete = {},
            onClearError = {},
        )
    }
}

// endregion
