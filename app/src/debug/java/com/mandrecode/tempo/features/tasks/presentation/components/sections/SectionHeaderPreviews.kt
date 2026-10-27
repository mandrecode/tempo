package com.mandrecode.tempo.features.tasks.presentation.components.sections

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.TempoTheme

@Preview(name = "Light", showBackground = true, device = "id:pixel_9")
@Preview(name = "Dark", showBackground = true, device = "id:pixel_9", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SectionHeaderStatesPreview() {
    TempoTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionHeader(
                    name = "Cat",
                    taskCount = 2,
                    overdueCount = 0,
                    isCollapsed = false,
                    onToggleCollapse = {},
                    onAddTask = {},
                    onRename = {},
                    onReorderSections = {},
                    onAddSectionBelow = {},
                    onDelete = {},
                )
                SectionHeader(
                    name = "Home chores",
                    taskCount = 4,
                    overdueCount = 1,
                    isCollapsed = true,
                    onToggleCollapse = {},
                    onAddTask = {},
                    onRename = {},
                    onReorderSections = {},
                    onAddSectionBelow = {},
                    onDelete = {},
                )
                SectionHeader(
                    name = "Shopping",
                    taskCount = 2,
                    overdueCount = 0,
                    isCollapsed = true,
                    isDropTarget = true,
                    onToggleCollapse = {},
                    onAddTask = {},
                    onRename = {},
                    onReorderSections = {},
                    onAddSectionBelow = {},
                    onDelete = {},
                )
                EmptySectionPlaceholder()
                SectionNameField(initialName = "Shopping list", onConfirm = {}, onDismiss = {})
                AddSectionButton(onClick = {})
            }
        }
    }
}
