package com.mandrecode.tempo.features.tasks.presentation.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SectionDropTargetTest {
    // Unsectioned card, then "Cat" (header + 2 cards), collapsed "Home chores", empty "Garden".
    private val unsectionedCard = DropEntry(DropEntry.Kind.Card, null, top = 0f, bottom = 100f, index = 0)
    private val catHeader = DropEntry(DropEntry.Kind.Header, 10L, top = 100f, bottom = 150f, taskCount = 2)
    private val catFirst = DropEntry(DropEntry.Kind.Card, 10L, top = 150f, bottom = 250f, index = 0)
    private val catSecond = DropEntry(DropEntry.Kind.Card, 10L, top = 250f, bottom = 350f, index = 1)
    private val choresHeader = DropEntry(DropEntry.Kind.Header, 11L, top = 350f, bottom = 400f, taskCount = 4)
    private val gardenHeader = DropEntry(DropEntry.Kind.Header, 12L, top = 400f, bottom = 450f, taskCount = 0)
    private val gardenPlaceholder = DropEntry(DropEntry.Kind.Placeholder, 12L, top = 450f, bottom = 500f)
    private val entries =
        listOf(unsectionedCard, catHeader, catFirst, catSecond, choresHeader, gardenHeader, gardenPlaceholder)

    @Test
    fun `upper half of a card drops before it, lower half after it`() {
        assertThat(resolveDropTarget(170f, entries)?.position).isEqualTo(0)
        assertThat(resolveDropTarget(230f, entries)?.position).isEqualTo(1)
        assertThat(resolveDropTarget(330f, entries)?.position).isEqualTo(2)
    }

    @Test
    fun `a collapsed header appends to its section`() {
        val target = resolveDropTarget(370f, entries)!!

        assertThat(target.sectionId).isEqualTo(11L)
        assertThat(target.position).isEqualTo(4)
        assertThat(target.entry).isEqualTo(choresHeader)
    }

    @Test
    fun `an empty section's placeholder takes the first slot`() {
        assertThat(resolveDropTarget(470f, entries)).isEqualTo(DropTarget(12L, 0, gardenPlaceholder))
    }

    @Test
    fun `the unsectioned area resolves to no section`() {
        assertThat(resolveDropTarget(80f, entries)?.sectionId).isNull()
    }

    @Test
    fun `nothing under the pointer is no target`() {
        assertThat(resolveDropTarget(900f, entries)).isNull()
    }

    @Test
    fun `dropping inside the start section is a reorder that ignores the task's own slot`() {
        // Dragging Cat's first card below the second.
        assertThat(dropActionFor(10L, 0, DropTarget(10L, 2, catSecond)))
            .isEqualTo(SectionDropAction.Reorder(0, 1))
        // Right below itself: nothing changes.
        assertThat(dropActionFor(10L, 0, DropTarget(10L, 1, catFirst))).isNull()
        assertThat(dropActionFor(10L, 1, DropTarget(10L, 0, catFirst)))
            .isEqualTo(SectionDropAction.Reorder(1, 0))
    }

    @Test
    fun `dropping in another section is a move to that position`() {
        assertThat(dropActionFor(null, 0, DropTarget(10L, 1, catFirst)))
            .isEqualTo(SectionDropAction.Move(10L, 1))
        assertThat(dropActionFor(10L, 1, DropTarget(null, 0, unsectionedCard)))
            .isEqualTo(SectionDropAction.Move(null, 0))
    }
}
