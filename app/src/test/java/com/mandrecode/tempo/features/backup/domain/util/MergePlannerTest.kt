package com.mandrecode.tempo.features.backup.domain.util

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.backup.domain.model.BackupData
import com.mandrecode.tempo.features.backup.domain.model.BackupEntityKind
import com.mandrecode.tempo.features.backup.domain.model.ChainMembership
import com.mandrecode.tempo.features.backup.domain.model.ConflictReason
import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.Task
import kotlinx.datetime.LocalDateTime
import org.junit.Test

class MergePlannerTest {
    private val planner = MergePlanner()

    @Test
    fun `all-new incoming data is fully inserted`() {
        val incoming =
            backupData(
                categories = listOf(category(id = 1, name = "Work")),
                tasks = listOf(task(id = 10, categoryId = 1, title = "Report")),
                habits = listOf(habit(id = 20, title = "Run")),
                habitChains = listOf(chain(id = 30, title = "Morning")),
                chainMemberships = listOf(ChainMembership(30, 20, 0)),
            )

        val plan = planner.plan(incoming, emptyData())

        assertThat(plan.categoriesToInsert).hasSize(1)
        assertThat(plan.tasksToInsert).hasSize(1)
        assertThat(plan.habitsToInsert).hasSize(1)
        assertThat(plan.chainsToInsert).hasSize(1)
        assertThat(plan.membershipsToInsert).hasSize(1)
        assertThat(plan.summary.totalImported).isEqualTo(4)
        assertThat(plan.summary.totalConflicts).isEqualTo(0)
        assertThat(plan.summary.conflicts).isEmpty()
    }

    @Test
    fun `merging identical data is a no-op with everything skipped`() {
        val data =
            backupData(
                categories = listOf(category(id = 1, name = "Work")),
                tasks =
                    listOf(
                        task(id = 10, categoryId = 1, title = "Report"),
                        task(id = 11, categoryId = 1, title = "Sub", parentTaskId = 10),
                    ),
                habits = listOf(habit(id = 20, title = "Run")),
                habitChains = listOf(chain(id = 30, title = "Morning")),
                chainMemberships = listOf(ChainMembership(30, 20, 0)),
            )

        val plan = planner.plan(data, data)

        assertThat(plan.categoriesToInsert).isEmpty()
        assertThat(plan.tasksToInsert).isEmpty()
        assertThat(plan.habitsToInsert).isEmpty()
        assertThat(plan.chainsToInsert).isEmpty()
        assertThat(plan.membershipsToInsert).isEmpty()
        assertThat(plan.summary.totalImported).isEqualTo(0)
        assertThat(plan.summary.totalSkipped).isEqualTo(5)
        assertThat(plan.summary.totalConflicts).isEqualTo(0)
    }

    @Test
    fun `category with same name but different content is a conflict resolving to local`() {
        val incoming =
            backupData(
                categories = listOf(category(id = 1, name = "Work", color = "red")),
                tasks = listOf(task(id = 10, categoryId = 1, title = "New task")),
            )
        val local = backupData(categories = listOf(category(id = 77, name = "Work", color = "blue")))

        val plan = planner.plan(incoming, local)

        assertThat(plan.categoriesToInsert).isEmpty()
        assertThat(plan.categoryIdMap).containsEntry(1L, 77L)
        val conflict = plan.summary.conflicts.single()
        assertThat(conflict.kind).isEqualTo(BackupEntityKind.CATEGORY)
        assertThat(conflict.displayName).isEqualTo("Work")
        assertThat(conflict.reason).isEqualTo(ConflictReason.CONTENT_MISMATCH)
        // The task still imports, referencing the local category through the map.
        assertThat(plan.tasksToInsert).hasSize(1)
    }

    @Test
    fun `task conflict cascades to its subtasks`() {
        val incoming =
            backupData(
                categories = listOf(category(id = 1, name = "Work")),
                tasks =
                    listOf(
                        task(id = 10, categoryId = 1, title = "Report", description = "changed"),
                        task(id = 11, categoryId = 1, title = "Sub", parentTaskId = 10),
                    ),
            )
        val local =
            backupData(
                categories = listOf(category(id = 1, name = "Work")),
                tasks = listOf(task(id = 50, categoryId = 1, title = "Report", description = "original")),
            )

        val plan = planner.plan(incoming, local)

        assertThat(plan.tasksToInsert).isEmpty()
        assertThat(plan.summary.tasks.conflicts).isEqualTo(2)
        assertThat(plan.summary.conflicts.map { it.reason })
            .containsExactly(ConflictReason.CONTENT_MISMATCH, ConflictReason.PARENT_CONFLICTED)
    }

    @Test
    fun `subtask of duplicate parent is matched against local children`() {
        val incoming =
            backupData(
                categories = listOf(category(id = 1, name = "Work")),
                tasks =
                    listOf(
                        task(id = 10, categoryId = 1, title = "Report"),
                        task(id = 11, categoryId = 1, title = "New sub", parentTaskId = 10),
                    ),
            )
        val local =
            backupData(
                categories = listOf(category(id = 1, name = "Work")),
                tasks =
                    listOf(
                        task(id = 50, categoryId = 1, title = "Report"),
                        task(id = 51, categoryId = 1, title = "Existing sub", parentTaskId = 50),
                    ),
            )

        val plan = planner.plan(incoming, local)

        val inserted = plan.tasksToInsert.single()
        assertThat(inserted.title).isEqualTo("New sub")
        assertThat(plan.taskIdMap).containsEntry(10L, 50L)
        assertThat(plan.summary.tasks.imported).isEqualTo(1)
        assertThat(plan.summary.tasks.skipped).isEqualTo(1)
    }

    @Test
    fun `inserted top-level tasks are offset past the local max sort order`() {
        val incoming =
            backupData(
                categories = listOf(category(id = 1, name = "Work")),
                tasks =
                    listOf(
                        task(id = 10, categoryId = 1, title = "A", sortOrder = 0),
                        task(id = 11, categoryId = 1, title = "B", sortOrder = 1),
                    ),
            )
        val local =
            backupData(
                categories = listOf(category(id = 1, name = "Work")),
                tasks = listOf(task(id = 50, categoryId = 1, title = "Existing", sortOrder = 4)),
            )

        val plan = planner.plan(incoming, local)

        assertThat(plan.tasksToInsert.map { it.sortOrder }).containsExactly(5, 6).inOrder()
    }

    @Test
    fun `inserted categories are offset past the local max sort order and never default`() {
        val incoming =
            backupData(
                categories = listOf(category(id = 1, name = "New", isDefault = true, sortOrder = 0)),
            )
        val local = backupData(categories = listOf(category(id = 9, name = "Work", sortOrder = 2)))

        val plan = planner.plan(incoming, local)

        val inserted = plan.categoriesToInsert.single()
        assertThat(inserted.sortOrder).isEqualTo(3)
        assertThat(inserted.isDefault).isFalse()
    }

    @Test
    fun `chain with same title but different members is a conflict and members are not inserted`() {
        val incoming =
            backupData(
                habits = listOf(habit(id = 20, title = "Run"), habit(id = 21, title = "Stretch")),
                habitChains = listOf(chain(id = 30, title = "Morning")),
                chainMemberships = listOf(ChainMembership(30, 20, 0), ChainMembership(30, 21, 1)),
            )
        val local =
            backupData(
                habits = listOf(habit(id = 80, title = "Run")),
                habitChains = listOf(chain(id = 90, title = "Morning")),
                chainMemberships = listOf(ChainMembership(90, 80, 0)),
            )

        val plan = planner.plan(incoming, local)

        assertThat(plan.chainsToInsert).isEmpty()
        assertThat(plan.membershipsToInsert).isEmpty()
        assertThat(plan.summary.habitChains.conflicts).isEqualTo(1)
        assertThat(
            plan.summary.conflicts
                .single()
                .kind,
        ).isEqualTo(BackupEntityKind.HABIT_CHAIN)
    }

    @Test
    fun `habit with same title but different content is a conflict`() {
        val incoming = backupData(habits = listOf(habit(id = 20, title = "Run", description = "5k")))
        val local = backupData(habits = listOf(habit(id = 80, title = "Run", description = "10k")))

        val plan = planner.plan(incoming, local)

        assertThat(plan.habitsToInsert).isEmpty()
        assertThat(plan.habitIdMap).containsEntry(20L, 80L)
        assertThat(plan.summary.habits.conflicts).isEqualTo(1)
    }

    @Test
    fun `tasks in newly inserted categories keep their incoming sort order`() {
        val incoming =
            backupData(
                categories = listOf(category(id = 1, name = "Brand new")),
                tasks = listOf(task(id = 10, categoryId = 1, title = "A", sortOrder = 3)),
            )

        val plan = planner.plan(incoming, emptyData())

        assertThat(plan.tasksToInsert.single().sortOrder).isEqualTo(3)
    }

    private fun emptyData() = backupData()

    private fun backupData(
        categories: List<Category> = emptyList(),
        tasks: List<Task> = emptyList(),
        habits: List<Habit> = emptyList(),
        habitChains: List<HabitChain> = emptyList(),
        chainMemberships: List<ChainMembership> = emptyList(),
    ) = BackupData(categories, tasks, habits, habitChains, chainMemberships)

    private fun category(
        id: Long,
        name: String,
        color: String? = null,
        isDefault: Boolean = false,
        sortOrder: Int = 0,
    ) = Category(id = id, name = name, color = color, isDefault = isDefault, sortOrder = sortOrder)

    private fun task(
        id: Long,
        categoryId: Long,
        title: String,
        description: String = "",
        parentTaskId: Long? = null,
        sortOrder: Int = 0,
    ) = Task(
        id = id,
        title = title,
        description = description,
        categoryId = categoryId,
        parentTaskId = parentTaskId,
        sortOrder = sortOrder,
    )

    private fun habit(
        id: Long,
        title: String,
        description: String = "",
    ) = Habit(
        id = id,
        title = title,
        description = description,
        createdDate = LocalDateTime(2026, 1, 1, 8, 0),
    )

    private fun chain(
        id: Long,
        title: String,
    ) = HabitChain(
        id = id,
        title = title,
        createdDate = LocalDateTime(2026, 1, 1, 8, 0),
    )
}
