package com.mandrecode.tempo.features.tasks.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.core.data.local.InMemoryTempoDatabaseRule
import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskRepositoryRoomIntegrationTest {
    @get:Rule
    val databaseRule = InMemoryTempoDatabaseRule()

    private lateinit var repository: TaskRepositoryImpl

    @Before
    fun setUp() {
        repository = TaskRepositoryImpl(databaseRule.database.taskDao(), databaseRule.database)
    }

    @Test
    fun insertAndFetchTask_roundTripsThroughRoom() =
        runTest {
            val reminder = LocalDateTime(2026, 6, 20, 9, 30)
            val insertedId =
                repository.insertTask(
                    Task(
                        title = "Prepare report",
                        description = "Weekly metrics",
                        categoryId = 7L,
                        reminderDate = reminder,
                        sortOrder = 4,
                    ),
                )

            val fetched = repository.getTaskById(insertedId)

            assertThat(fetched).isNotNull()
            assertThat(fetched!!.title).isEqualTo("Prepare report")
            assertThat(fetched.categoryId).isEqualTo(7L)
            assertThat(fetched.reminderDate).isEqualTo(reminder)
            assertThat(fetched.sortOrder).isEqualTo(4)
        }

    @Test
    fun updateTaskCategory_propagatesToSubtasks() =
        runTest {
            val parentId =
                repository.insertTask(
                    Task(
                        title = "Parent",
                        description = "",
                        categoryId = 1L,
                    ),
                )
            val subtaskId =
                repository.insertTask(
                    Task(
                        title = "Subtask",
                        description = "",
                        categoryId = 1L,
                        parentTaskId = parentId,
                    ),
                )

            repository.updateTask(
                Task(
                    id = parentId,
                    title = "Parent",
                    description = "",
                    categoryId = 2L,
                ),
            )

            val subtask = repository.getTaskById(subtaskId)
            val subtasksFlow = repository.getSubtasks(parentId).first()

            assertThat(subtask).isNotNull()
            assertThat(subtask!!.categoryId).isEqualTo(2L)
            assertThat(subtasksFlow.single().categoryId).isEqualTo(2L)
        }

    @Test
    fun deleteTaskWithSnapshot_restorePreservesIdsAndHierarchy() =
        runTest {
            val parentId = repository.insertTask(Task(title = "Parent", description = "", categoryId = 1))
            val childId =
                repository.insertTask(
                    Task(title = "Child", description = "", categoryId = 1, parentTaskId = parentId, sortOrder = 3),
                )

            val snapshot = repository.deleteTaskWithSnapshot(parentId)
            assertThat(repository.getTaskById(parentId)).isNull()
            assertThat(repository.getTaskById(childId)).isNull()

            repository.restoreDeletedTasks(snapshot)
            repository.restoreDeletedTasks(snapshot)

            assertThat(repository.getTaskById(parentId)).isNotNull()
            assertThat(repository.getTaskById(childId)!!.parentTaskId).isEqualTo(parentId)
            assertThat(repository.getTaskById(childId)!!.sortOrder).isEqualTo(3)
        }

    @Test
    fun deleteCompletedTasksByCategoryId_deletesParentsAndSubtasks() =
        runTest {
            val completedParentId =
                repository.insertTask(
                    Task(
                        title = "Done parent",
                        description = "",
                        categoryId = 3L,
                        isCompleted = true,
                        completedAt = LocalDateTime(2026, 6, 17, 12, 0),
                    ),
                )
            repository.insertTask(
                Task(
                    title = "Done child",
                    description = "",
                    categoryId = 3L,
                    parentTaskId = completedParentId,
                    isCompleted = true,
                    completedAt = LocalDateTime(2026, 6, 17, 12, 0),
                ),
            )

            repository.deleteCompletedTasksByCategoryId(3L)

            assertThat(repository.getTaskById(completedParentId)).isNull()
            assertThat(repository.getSubtasksSync(completedParentId)).isEmpty()
        }

    @Test
    fun deleteCompletedTasksAtOrBefore_removesOnlyEligibleTaskTrees() =
        runTest {
            val oldParentId =
                repository.insertTask(
                    Task(
                        title = "Old completed parent",
                        description = "",
                        isCompleted = true,
                        completedAt = LocalDateTime(2026, 6, 1, 12, 0),
                    ),
                )
            val oldChildId =
                repository.insertTask(
                    Task(
                        title = "Old child",
                        description = "",
                        parentTaskId = oldParentId,
                        isCompleted = true,
                        completedAt = LocalDateTime(2026, 6, 1, 12, 0),
                    ),
                )
            val nextOccurrenceId =
                repository.insertTask(
                    Task(
                        title = "Next periodic occurrence",
                        description = "",
                    ),
                )
            repository.updateTaskNextInstanceId(oldParentId, nextOccurrenceId)
            val recentId =
                repository.insertTask(
                    Task(
                        title = "Recent completed",
                        description = "",
                        isCompleted = true,
                        completedAt = LocalDateTime(2026, 7, 1, 12, 1),
                    ),
                )
            val incompleteId =
                repository.insertTask(
                    Task(
                        title = "Incomplete",
                        description = "",
                        isCompleted = false,
                    ),
                )
            val missingTimestampId =
                repository.insertTask(
                    Task(
                        title = "Missing timestamp",
                        description = "",
                        isCompleted = true,
                    ),
                )

            repository.deleteCompletedTasksAtOrBefore(LocalDateTime(2026, 7, 1, 12, 0))

            assertThat(repository.getTaskById(oldParentId)).isNull()
            assertThat(repository.getTaskById(oldChildId)).isNull()
            assertThat(repository.getTaskById(nextOccurrenceId)).isNotNull()
            assertThat(repository.getTaskById(recentId)).isNotNull()
            assertThat(repository.getTaskById(incompleteId)).isNotNull()
            assertThat(repository.getTaskById(missingTimestampId)).isNotNull()
        }
}
