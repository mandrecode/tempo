package com.mandrecode.tempo.features.tasks.domain.usecase

import com.mandrecode.tempo.features.tasks.domain.repository.TaskSectionRepository
import jakarta.inject.Inject

/**
 * Everything a screen needs to work with task sections (#61), injected as one dependency so the
 * screens that offer sections do not each grow by seven constructor parameters.
 */
class TaskSectionUseCases
    @Inject
    constructor(
        val repository: TaskSectionRepository,
        val create: CreateTaskSectionUseCase,
        val rename: RenameTaskSectionUseCase,
        val reorder: ReorderTaskSectionsUseCase,
        val delete: DeleteTaskSectionUseCase,
        val restore: RestoreDeletedTaskSectionUseCase,
        val moveTask: MoveTaskToSectionUseCase,
    )
