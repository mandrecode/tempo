package com.mandrecode.tempo.features.tasks.presentation.components.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.R
import com.mandrecode.tempo.core.ui.components.TempoModalBottomSheet
import com.mandrecode.tempo.core.ui.theme.sheetTitle
import com.mandrecode.tempo.core.ui.util.getIconForSortOption
import com.mandrecode.tempo.features.tasks.presentation.model.SortOption

@Composable
fun SortBottomSheet(
    currentSortOption: SortOption,
    onDismiss: () -> Unit,
    onSelectSortOption: (SortOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pendingSortOption = remember { mutableStateOf<SortOption?>(null) }
    TempoModalBottomSheet(
        onDismissRequest = {
            pendingSortOption.value?.let(onSelectSortOption)
            pendingSortOption.value = null
            onDismiss()
        },
        modifier = modifier,
        adaptivePlacement = false,
    ) { onRequestDismiss ->
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .padding(bottom = 32.dp),
        ) {
            // Header
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.sort_by),
                        style = MaterialTheme.typography.sheetTitle,
                    )
                },
                colors =
                    ListItemDefaults.colors(
                        containerColor = Color.Transparent,
                    ),
                modifier =
                    Modifier
                        .padding(top = 12.dp)
                        .padding(horizontal = 16.dp),
            )

            // Sort options
            SortOption.entries.forEach { sortOption ->
                SortOptionItem(
                    sortOption = sortOption,
                    isSelected = currentSortOption == sortOption,
                    onClick = {
                        pendingSortOption.value = sortOption
                        onRequestDismiss()
                    },
                )
            }
        }
    }
}

@Composable
internal fun SortOptionItem(
    sortOption: SortOption,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(sortOption.labelResId),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        leadingContent = {
            Box(modifier = Modifier.padding(start = 16.dp)) {
                Icon(
                    painter = painterResource(getIconForSortOption(sortOption)),
                    contentDescription = sortOption.value,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp),
                )
            }
        },
        trailingContent = {
            Box(modifier = Modifier.padding(end = 16.dp)) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                )
            }
        },
        colors =
            ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
    )
}
