package com.mandrecode.tempo.features.tasks.presentation

import androidx.lifecycle.viewModelScope
import com.mandrecode.tempo.R
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.presentation.model.ActiveGroupKey
import com.mandrecode.tempo.features.tasks.presentation.model.CompletedGroupKey
import com.mandrecode.tempo.features.tasks.presentation.model.SortOption
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

internal fun TasksViewModel.selectCategory(categoryId: Long) {
    val sortOption = tasksScreenPreferencesRepository.getSortOption(categoryId)
    tasksScreenPreferencesRepository.setSelectedCategoryId(categoryId)
    mutableUiState.update { it.copy(selectedCategoryId = categoryId, sortOption = sortOption) }
}

internal fun TasksViewModel.setSortOption(sortOption: SortOption) {
    val categoryId = mutableUiState.value.selectedCategoryId
    tasksScreenPreferencesRepository.setSortOption(categoryId, sortOption)
    mutableUiState.update { it.copy(sortOption = sortOption) }
}

private data class CalculatedData(
    val tasks: ImmutableList<Task>,
    val categories: ImmutableList<Category>,
    val counts: ImmutableMap<Long, Int>,
    val activeTaskGroups: ImmutableMap<ActiveGroupKey, ImmutableList<Task>>,
    val completedTaskGroups: ImmutableMap<CompletedGroupKey, ImmutableList<Task>>,
    val subtasksMap: ImmutableMap<Long, ImmutableList<Task>>,
    val effectiveSelectedId: Long,
)

private fun <K, V> Map<K, List<V>>.toImmutableListMap(): ImmutableMap<K, ImmutableList<V>> =
    entries
        .associate { (key, values) -> key to values.toPersistentList() }
        .toPersistentMap()

internal fun TasksViewModel.loadData() {
    viewModelScope.launch {
        combine(
            taskRepository.getAllTasks(),
            categoryRepository.getAllCategories(),
            mutableUiState.map { it.selectedCategoryId }.distinctUntilChanged(),
            mutableUiState.map { it.sortOption }.distinctUntilChanged(),
        ) { tasks, dbCategories, selectedCategoryId, sortOption ->
            val allCategories = dbCategories.toPersistentList()

            val effectiveSelectedId =
                if (allCategories.any { it.id == selectedCategoryId }) {
                    selectedCategoryId
                } else {
                    allCategories.firstOrNull { it.isDefault }?.id
                        ?: allCategories.firstOrNull()?.id
                        ?: 0L
                }

            val uncompletedTasksCounts =
                tasks
                    .filter { !it.isCompleted && it.parentTaskId == null }
                    .groupingBy { it.categoryId }
                    .eachCount()
                    .toPersistentMap()

            val filteredTasks =
                tasks
                    .filter { it.categoryId == effectiveSelectedId }
                    .let { categoryTasks ->
                        when (sortOption) {
                            SortOption.MANUAL ->
                                categoryTasks.sortedWith(
                                    compareBy(
                                        { it.isCompleted },
                                        { it.sortOrder },
                                    ),
                                )

                            SortOption.BY_DATE ->
                                categoryTasks.sortedWith(
                                    compareBy<Task> { it.isCompleted }
                                        .thenBy(nullsLast()) { it.reminderDate },
                                )

                            SortOption.BY_PRIORITY ->
                                categoryTasks.sortedWith(
                                    compareBy<Task> { it.isCompleted }
                                        .thenBy(nullsLast()) { it.priority?.sortOrder },
                                )

                            SortOption.BY_TITLE ->
                                categoryTasks.sortedWith(
                                    compareBy(
                                        { it.isCompleted },
                                        { it.title.lowercase() },
                                    ),
                                )
                        }
                    }

            val parentTasks = filteredTasks.filter { it.parentTaskId == null }
            val (activeTasks, completedTasks) = parentTasks.partition { !it.isCompleted }

            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

            val activeTaskGroups: ImmutableMap<ActiveGroupKey, ImmutableList<Task>> =
                when (sortOption) {
                    SortOption.BY_DATE ->
                        run {
                            val byDateGroups: Map<ActiveGroupKey, List<Task>> =
                                activeTasks
                                    .groupBy { task ->
                                        when {
                                            task.reminderDate == null ->
                                                ActiveGroupKey.ByDate(null)

                                            task.reminderDate.date < today ->
                                                ActiveGroupKey.Overdue

                                            else ->
                                                ActiveGroupKey.ByDate(task.reminderDate.date)
                                        }
                                    }
                            byDateGroups.toImmutableListMap()
                        }

                    SortOption.BY_PRIORITY ->
                        run {
                            val byPriorityGroups: Map<ActiveGroupKey, List<Task>> =
                                activeTasks
                                    .groupBy { ActiveGroupKey.ByPriority(it.priority) }
                            byPriorityGroups.toImmutableListMap()
                        }

                    SortOption.BY_TITLE ->
                        if (activeTasks.isEmpty()) {
                            persistentMapOf()
                        } else {
                            persistentMapOf(ActiveGroupKey.ByTitle to activeTasks.toPersistentList())
                        }

                    SortOption.MANUAL ->
                        if (activeTasks.isEmpty()) {
                            persistentMapOf()
                        } else {
                            persistentMapOf(ActiveGroupKey.Flat to activeTasks.toPersistentList())
                        }
                }

            val completedTaskGroups: ImmutableMap<CompletedGroupKey, ImmutableList<Task>> =
                when (sortOption) {
                    SortOption.BY_DATE ->
                        run {
                            val byDateGroups: Map<CompletedGroupKey, List<Task>> =
                                completedTasks
                                    .sortedByDescending { it.completedAt }
                                    .groupBy { CompletedGroupKey.ByDate(it.completedAt?.date) }
                            byDateGroups.toImmutableListMap()
                        }

                    SortOption.BY_PRIORITY ->
                        run {
                            val byPriorityGroups: Map<CompletedGroupKey, List<Task>> =
                                completedTasks
                                    .sortedWith(compareBy(nullsLast()) { it.priority?.sortOrder })
                                    .groupBy { CompletedGroupKey.ByPriority(it.priority) }
                            byPriorityGroups.toImmutableListMap()
                        }

                    SortOption.BY_TITLE ->
                        if (completedTasks.isEmpty()) {
                            persistentMapOf()
                        } else {
                            persistentMapOf(CompletedGroupKey.ByTitle to completedTasks.toPersistentList())
                        }

                    SortOption.MANUAL ->
                        if (completedTasks.isEmpty()) {
                            persistentMapOf()
                        } else {
                            persistentMapOf(CompletedGroupKey.Flat to completedTasks.toPersistentList())
                        }
                }

            val subtasksMap: ImmutableMap<Long, ImmutableList<Task>> =
                tasks
                    .asSequence()
                    .filter { it.categoryId == effectiveSelectedId }
                    .mapNotNull { task -> task.parentTaskId?.let { parentId -> parentId to task } }
                    .groupBy(
                        keySelector = { (parentId, _) -> parentId },
                        valueTransform = { (_, task) -> task },
                    ).mapValues { (_, subtasks) ->
                        subtasks.sortedWith(
                            compareBy<Task> { it.sortOrder }
                                .thenBy { it.id },
                        )
                    }.toImmutableListMap()

            CalculatedData(
                tasks.toPersistentList(),
                allCategories,
                uncompletedTasksCounts,
                activeTaskGroups,
                completedTaskGroups,
                subtasksMap,
                effectiveSelectedId,
            )
        }.flowOn(defaultDispatcher)
            .catch { exception ->
                if (exception is CancellationException) {
                    throw exception
                }
                mutableUiState.update { it.copy(isLoading = false) }
                showSnackbar(
                    R.string.msg_error_loading_data,
                    listOf(exception.toUserFacingMessage()),
                )
            }.collect { data ->
                val categoryChanged =
                    data.effectiveSelectedId != mutableUiState.value.selectedCategoryId
                val sortOption =
                    if (categoryChanged) {
                        tasksScreenPreferencesRepository.getSortOption(
                            data.effectiveSelectedId,
                        )
                    } else {
                        mutableUiState.value.sortOption
                    }

                mutableUiState.update { currentState ->
                    currentState.copy(
                        tasks = data.tasks,
                        categories = data.categories,
                        uncompletedTasksCounts = data.counts,
                        activeTasks = data.activeTaskGroups,
                        completedTaskGroups = data.completedTaskGroups,
                        subtasksMap = data.subtasksMap,
                        selectedCategoryId = data.effectiveSelectedId,
                        sortOption = sortOption,
                        isLoading = false,
                    )
                }
                if (categoryChanged) {
                    tasksScreenPreferencesRepository.setSelectedCategoryId(
                        data.effectiveSelectedId,
                    )
                }
            }
    }
}
