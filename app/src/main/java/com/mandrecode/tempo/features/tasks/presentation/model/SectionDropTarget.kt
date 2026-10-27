package com.mandrecode.tempo.features.tasks.presentation.model

/**
 * Something on screen a task can be dropped on while dragging between sections (#61), with its
 * vertical extent in root coordinates. [sectionId] null is the unsectioned area.
 */
data class DropEntry(
    val kind: Kind,
    val sectionId: Long?,
    val top: Float,
    val bottom: Float,
    /** For [Kind.Card]: the card's position among its section's active tasks. */
    val index: Int = 0,
    /** For [Kind.Header]: how many active tasks the section holds, where a drop appends. */
    val taskCount: Int = 0,
) {
    enum class Kind { Card, Header, Placeholder }
}

/** Where a drop lands: insertion [position] (0..n) among [sectionId]'s active tasks. */
data class DropTarget(
    val sectionId: Long?,
    val position: Int,
    /** The entry under the pointer, for highlighting. */
    val entry: DropEntry,
)

/**
 * Resolves the pointer's [y] to a drop target: on a card, before it in its upper half and after it
 * in its lower half; on a header, appended to that section (collapsed ones included); on an empty
 * section's placeholder, its first slot. Null when nothing droppable is under the pointer.
 */
fun resolveDropTarget(
    y: Float,
    entries: Collection<DropEntry>,
): DropTarget? {
    val entry = entries.firstOrNull { y >= it.top && y < it.bottom } ?: return null
    val position =
        when (entry.kind) {
            DropEntry.Kind.Card -> if (y < (entry.top + entry.bottom) / 2) entry.index else entry.index + 1
            DropEntry.Kind.Header -> entry.taskCount
            DropEntry.Kind.Placeholder -> 0
        }
    return DropTarget(entry.sectionId, position, entry)
}

/** What a drop means for the dragged task, given where it started. */
sealed interface SectionDropAction {
    /** Same section: a plain reorder within it. */
    data class Reorder(
        val fromIndex: Int,
        val toIndex: Int,
    ) : SectionDropAction

    /** Another section (or unsectioned): a move to [index] among that section's tasks. */
    data class Move(
        val sectionId: Long?,
        val index: Int,
    ) : SectionDropAction
}

/**
 * Turns a [target] into the action for a task that started at [fromIndex] in [fromSectionId], or
 * null when the drop changes nothing. Positions in the start section count the dragged task itself,
 * so dropping right below it is not a move.
 */
fun dropActionFor(
    fromSectionId: Long?,
    fromIndex: Int,
    target: DropTarget,
): SectionDropAction? =
    if (target.sectionId == fromSectionId) {
        val toIndex = if (target.position > fromIndex) target.position - 1 else target.position
        if (toIndex == fromIndex) null else SectionDropAction.Reorder(fromIndex, toIndex)
    } else {
        SectionDropAction.Move(target.sectionId, target.position)
    }

/**
 * How far to auto-scroll while the pointer is at [pointerY] in a viewport spanning [top]..[bottom]:
 * nothing beyond [edge] px from either side, then up to [maxStep] px per frame at the side itself,
 * negative towards the top.
 */
fun autoScrollStep(
    pointerY: Float,
    top: Float,
    bottom: Float,
    edge: Float,
    maxStep: Float,
): Float =
    when {
        pointerY < top + edge -> -maxStep * ((top + edge - pointerY) / edge).coerceIn(0f, 1f)
        pointerY > bottom - edge -> maxStep * ((pointerY - (bottom - edge)) / edge).coerceIn(0f, 1f)
        else -> 0f
    }
