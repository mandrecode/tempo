package com.mandrecode.tempo.features.tasks.presentation

import com.google.common.truth.Truth.assertThat
import com.mandrecode.tempo.features.tasks.domain.model.Task
import com.mandrecode.tempo.features.tasks.presentation.model.DropEntry
import com.mandrecode.tempo.features.tasks.presentation.model.SectionDropAction
import org.junit.Test

/** Dragging a task between sections (#61), without a screen: bounds in, action out. */
class SectionDragStateTest {
    private val state =
        SectionDragState().apply {
            entries["card_1"] = DropEntry(DropEntry.Kind.Card, null, top = 0f, bottom = 100f, index = 0)
            entries["header_10"] = DropEntry(DropEntry.Kind.Header, 10L, top = 100f, bottom = 150f, taskCount = 1)
            entries["card_2"] = DropEntry(DropEntry.Kind.Card, 10L, top = 150f, bottom = 250f, index = 0)
            entries["placeholder_11"] = DropEntry(DropEntry.Kind.Placeholder, 11L, top = 250f, bottom = 300f)
        }

    @Test
    fun `dragging onto an empty section's placeholder is a move to it`() {
        state.start(taskId = 2L, sectionId = 10L, index = 0)
        state.drag(75f)

        assertThat(state.isTargeting(DropEntry.Kind.Placeholder, 11L)).isTrue()
        assertThat(state.finish()).isEqualTo(SectionDropAction.Move(11L, 0))
        assertThat(state.draggedTaskId).isNull()
    }

    @Test
    fun `the dragged card never targets itself`() {
        state.start(taskId = 2L, sectionId = 10L, index = 0)

        assertThat(state.target).isNull()
        assertThat(state.finish()).isNull()
    }

    @Test
    fun `dropping on a header appends to that section`() {
        state.start(taskId = 1L, sectionId = null, index = 0)
        state.drag(75f)

        assertThat(state.isTargeting(DropEntry.Kind.Header, 10L)).isTrue()
        assertThat(state.finish()).isEqualTo(SectionDropAction.Move(10L, 1))
    }

    @Test
    fun `a card that never registered cannot start a drag`() {
        state.start(taskId = 99L, sectionId = null, index = 0)

        assertThat(state.draggedTaskId).isNull()
    }

    @Test
    fun `cancel forgets the drag`() {
        state.start(taskId = 1L, sectionId = null, index = 0)
        state.drag(200f)
        state.cancel()

        assertThat(state.draggedTaskId).isNull()
        assertThat(state.offsetY).isEqualTo(0f)
        assertThat(state.target).isNull()
    }

    @Test
    fun `a drop becomes a reorder in its own section and a move elsewhere`() {
        val task = Task(id = 2L, title = "Buy litter", description = "")
        val sectionTasks = listOf(task)

        assertThat(sectionDropEvent(SectionDropAction.Reorder(0, 1), task, sectionTasks))
            .isEqualTo(TasksContract.UiEvent.ReorderTasks(0, 1, sectionTasks))
        assertThat(sectionDropEvent(SectionDropAction.Move(11L, 0), task, sectionTasks))
            .isEqualTo(TasksContract.UiEvent.MoveTaskToSection(2L, 11L, 0))
    }

    @Test
    fun `edit state starts idle`() {
        val editState = SectionEditState()

        assertThat(editState.renamingSectionId).isNull()
        assertThat(editState.newSectionDraft).isNull()
        assertThat(editState.isReorderSheetVisible).isFalse()
    }

    @Test
    fun `scrolling under the finger moves the card but not the pointer`() {
        state.start(taskId = 2L, sectionId = 10L, index = 0)
        state.drag(75f)
        state.onScrolled(40f)

        assertThat(state.offsetY).isEqualTo(115f)
        assertThat(state.isTargeting(DropEntry.Kind.Placeholder, 11L)).isTrue()
    }
}
