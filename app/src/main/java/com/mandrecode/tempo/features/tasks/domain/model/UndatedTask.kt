package com.mandrecode.tempo.features.tasks.domain.model

/**
 * A task with no date, carrying the category it belongs to.
 *
 * The category travels with the task because the plan sheet lists work pulled out of every
 * category at once: without it, two identically-named errands from different parts of the user's
 * life would be indistinguishable in the one place they are asked to tell them apart.
 */
data class UndatedTask(
    val task: Task,
    val category: Category?,
)
