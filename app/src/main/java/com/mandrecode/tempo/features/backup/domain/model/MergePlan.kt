package com.mandrecode.tempo.features.backup.domain.model

import com.mandrecode.tempo.features.routines.domain.model.Habit
import com.mandrecode.tempo.features.routines.domain.model.HabitChain
import com.mandrecode.tempo.features.tasks.domain.model.Category
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.domain.model.TaskSection

/**
 * Execution plan for a merge import.
 *
 * Records in the `*ToInsert` lists still carry their ids and references from the
 * incoming file; the executor inserts them with fresh database ids, extending the
 * corresponding `*IdMap` (incoming id → local id) with each insertion so later
 * references (category, parent task, next instance, chain memberships) resolve.
 * The maps are pre-populated with matches to existing local records (duplicates
 * and conflicts both resolve references to the local row).
 *
 * [tasksToInsert] is ordered parents-before-children. Sort orders on inserted
 * records are already offset past the local maximum of their scope.
 */
data class MergePlan(
    val categoriesToInsert: List<Category>,
    val categoryIdMap: Map<Long, Long>,
    val tasksToInsert: List<Task>,
    val taskIdMap: Map<Long, Long>,
    val habitsToInsert: List<Habit>,
    val habitIdMap: Map<Long, Long>,
    val chainsToInsert: List<HabitChain>,
    val chainIdMap: Map<Long, Long>,
    val membershipsToInsert: List<ChainMembership>,
    val summary: ImportSummary,
    /** Incoming sections with no same-named section in their resolved category (#61). */
    val sectionsToInsert: List<TaskSection> = emptyList(),
    /** Incoming section id → local section id for sections matched by name. */
    val sectionIdMap: Map<Long, Long> = emptyMap(),
)
