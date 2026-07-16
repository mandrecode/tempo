package com.mandrecode.tempo.core.ui.navigation

import com.mandrecode.tempo.features.tasks.presentation.model.SortOption

data class RoutinesFloatingBarState(
    val visible: Boolean = true,
    val compactSoloAction: Boolean = false,
    val onAddHabit: () -> Unit = {},
)

data class TasksFloatingBarState(
    val visible: Boolean = true,
    val compactSoloAction: Boolean = false,
    val hasCompletedTasks: Boolean = false,
    val sortOption: SortOption = SortOption.MANUAL,
    val sortMenuExpanded: Boolean = false,
    val onAddTask: () -> Unit = {},
    val onSort: () -> Unit = {},
    val onSelectSortOption: (SortOption) -> Unit = {},
    val onDismissSort: () -> Unit = {},
    val onClearCompleted: () -> Unit = {},
)
