## Why

A long category has no internal structure today. The only ways to organise it are to split it into more categories, which means more lists to manage and share, or to fake headings with parent tasks, which then show up as things to complete. Users asked for "grupos de tareas" ([#61](https://github.com/mandrecode/tempo/issues/61)). The motivating case is one shared "S&V" category holding the cat, home chores and shopping, organised inside one list.

Timing: a section is a new entity, so it needs a sync-safe identity and a place in the backup schema. Landing it before [#30](https://github.com/mandrecode/tempo/issues/30) (sync-safe identifiers) and [#31](https://github.com/mandrecode/tempo/issues/31) (sync API contract) means both cover sections in one pass.

## What Changes

- **A `TaskSection` entity** (`id`, `categoryId`, `name`, `sortOrder`), owned by exactly one category. A section is a divider. It has no due date, priority, reminder, recurrence, icon, colour, default flag or sharing identity of its own.
- **Tasks gain an optional `sectionId`.** Tasks with no section keep working exactly as today and render above every section. A category with no sections looks and behaves exactly as it does today.
- **Section headers in the Tasks list** at every width: collapsible, with a task count, an overdue badge while collapsed, a quick "add task here" action and a section menu (Rename in place, Reorder sections, Add section below, Delete). An empty section stays visible with a drop placeholder. The header stays pinned at the top while scrolling on short windows.
- **Sections are the outer grouping under every sort.** The date and priority group headers are not shown inside a section, but tasks inside a section still follow the chosen sort. Completed tasks stay in the single existing "Completed" block at the bottom.
- **Moving tasks:** drag a task into another section in Manual sort. Pick a section in the task editor's new **Section** button group under Category, at any sort and width. Moving a task to another category clears its section.
- **Large and up (≥ 1200 dp):** a connected button group of section shortcuts sits under the category chip row. The checked segment is filled in the category's colour and follows the section scrolled into view. Without a docked editor, sections flow into two columns so the list uses the window's width.
- **Deleting a section** moves its tasks to "No section", with the existing undo snackbar. Deleting a category deletes its sections, and undo brings them back.
- **Collapse state persists per section** and survives navigation and process death. It is stored device-locally.
- **Backup** carries sections and each task's `sectionId` as additive fields under the existing schema version 1, so older backups import unchanged. Merge import remaps section ids just as it remaps category ids.

Non-goals: sections inside habits or Routines (chains already cover that), nested sections, sharing a section on its own, per-section colour or icon, a board/kanban view, and the docked-editor edge polish seen on Large screens. The edge polish is a separate change.

## Capabilities

### New Capabilities

- `task-sections`: what a section is and is not, its ownership by one category, how tasks belong to it, lifecycle rules (create, rename, reorder, delete with undo, category move clears it, category deletion), persistence of collapse state, and backup round-tripping.
- `task-section-list-layout`: how sections render in the Tasks list at each window width class: headers and their states, interaction with sort and the Completed block, drag between sections, the pinned header on short windows, and the section button group and two-column flow on Large and up.

### Modified Capabilities

- `editor-selector-groups`: the task editor gains a single-choice **Section** option set (connected button group, scrollable when long). It is shown only when the selected category has sections.

## Impact

- **Data**: new `TaskSectionEntity` + `TaskSectionDao`; `sectionId` column and index on `tasks`; Room migration 11 → 12; schema JSON regenerated. Coordinates with `feat-324-spaces`, which also adds a migration: whichever change lands second takes the next version.
- **Domain**: `TaskSection` model, `TaskSectionRepository`, use cases for create/rename/reorder/delete/restore and for moving a task between sections. `UpdateTaskUseCase` clears `sectionId` on a category change. Recurring-task rollover keeps the section on the spawned instance.
- **Category deletion**: `CategoryDeletionSnapshot` carries sections so undo restores them.
- **Presentation**: `TasksContract`/`TasksViewModelDataLoading` grouping, `TasksContent` headers, drag gestures across sections, task editor section row, new section management surfaces (menu, rename field, reorder sheet), Large+ button group and two-column layout.
- **Preferences**: per-section collapsed set in `TasksScreenPreferencesRepository`.
- **Backup**: `BackupFileDto.taskSections`, `TaskBackupDto.sectionId`, replace/merge import paths, `docs/BACKUP_FORMAT.md`.
- **Strings**: every new string localised in every existing locale ("Sección" in Spanish).
- **Tests**: use case and ViewModel unit tests, DAO/migration tests, Content UI tests, and screenshot tests for the Large+ adaptive layout.
- **What's New**: `WhatsNewRegistry.latest` entry for sections.
