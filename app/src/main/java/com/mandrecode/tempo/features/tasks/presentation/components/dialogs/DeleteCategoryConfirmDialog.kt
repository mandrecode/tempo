package com.mandrecode.tempo.features.tasks.presentation.components.dialogs

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.TempoConfirmDialog
import com.mandrecode.tempo.features.tasks.domain.model.Category

@Composable
fun DeleteCategoryDialog(
    onCancelDeleteCategory: () -> Unit,
    onDeleteCategory: (Category) -> Unit,
    categoryToDelete: Category,
    modifier: Modifier = Modifier,
) {
    TempoConfirmDialog(
        title = stringResource(R.string.confirm_deletion),
        confirmLabel = stringResource(R.string.delete),
        onConfirm = { onDeleteCategory(categoryToDelete) },
        onCancel = onCancelDeleteCategory,
        modifier = modifier,
        text = {
            val categoryName = categoryToDelete.name
            Text(
                buildAnnotatedString {
                    val messagePrefix = stringResource(R.string.delete_category_message_prefix).trimEnd()
                    append(messagePrefix)
                    append(" ")
                    withStyle(
                        style =
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                            ),
                    ) {
                        append(categoryName)
                    }
                    append(stringResource(R.string.delete_category_message_suffix).trimStart())
                },
            )
        },
    )
}
