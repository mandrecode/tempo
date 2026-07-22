package com.mandrecode.tempo.features.widget.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.tasks.domain.model.Category
import kotlinx.collections.immutable.ImmutableList

@Composable
fun QuickAddTaskContent(
    uiState: QuickAddTaskContract.UiState,
    onEvent: (QuickAddTaskContract.UiEvent) -> Unit,
) {
    val titleFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        titleFocusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = { onEvent(QuickAddTaskContract.UiEvent.CancelClicked) },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(all = 24.dp),
            ) {
                Text(
                    text = stringResource(R.string.widget_quick_add_task_label),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(24.dp))

                QuickAddTaskTitleField(
                    uiState = uiState,
                    onEvent = onEvent,
                    focusRequester = titleFocusRequester,
                )

                if (uiState.categories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CategoryPickerRow(
                        categories = uiState.categories,
                        selectedCategoryId = uiState.selectedCategoryId,
                        onSelectCategory = { onEvent(QuickAddTaskContract.UiEvent.CategorySelected(it)) },
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                QuickAddTaskActions(
                    isSaving = uiState.isSaving,
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Composable
private fun QuickAddTaskTitleField(
    uiState: QuickAddTaskContract.UiState,
    onEvent: (QuickAddTaskContract.UiEvent) -> Unit,
    focusRequester: FocusRequester,
) {
    OutlinedTextField(
        value = uiState.title,
        onValueChange = { onEvent(QuickAddTaskContract.UiEvent.TitleChanged(it)) },
        label = { Text(stringResource(R.string.widget_quick_add_task_title_hint)) },
        isError = uiState.titleErrorRes != null,
        supportingText =
            uiState.titleErrorRes?.let { errorRes ->
                { Text(stringResource(errorRes)) }
            },
        modifier =
            Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        keyboardOptions =
            KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
            ),
    )
}

@Composable
private fun QuickAddTaskActions(
    isSaving: Boolean,
    onEvent: (QuickAddTaskContract.UiEvent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = { onEvent(QuickAddTaskContract.UiEvent.CancelClicked) },
            modifier = Modifier.height(48.dp),
        ) {
            Text(stringResource(R.string.cancel))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Button(
            onClick = { onEvent(QuickAddTaskContract.UiEvent.SaveClicked) },
            enabled = !isSaving,
            modifier = Modifier.height(48.dp),
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(stringResource(R.string.save))
            }
        }
    }
}

@Composable
private fun CategoryPickerRow(
    categories: ImmutableList<Category>,
    selectedCategoryId: Long,
    onSelectCategory: (Long) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories, key = { it.id }) { category ->
            val isSelected = category.id == selectedCategoryId
            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(category.id) },
                label = { Text(category.name) },
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
            )
        }
    }
}
