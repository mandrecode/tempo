# Design

## Context

See proposal.md for motivation and specs/ for the behaviour contract. The approved visual design is the "Task Sections Layouts" canvas: phone portrait, phone landscape, foldable, tablet, desktop, and header states.

Current state that shapes the approach:

- `tasks` (Room, DB version 11) carries `categoryId`, `parentTaskId` and a per-category `sortOrder`. Categories are ordered by their own `sortOrder`. There are no foreign keys between tasks and categories; integrity is kept in repository transactions.
- `TasksViewModelDataLoading` groups active tasks by an `ActiveGroupKey` derived from the sort (date/priority/flat). `TasksContent` renders one `LazyColumn` with per-group headers and the Completed block. `buildReorderableRuns` chunks each group into runs of tied tasks that drag-reorder.
- Category deletion is snapshot based (`CategoryDeletionSnapshot` → `restoreDeletedCategory`). The scheduler side effect (`TaskReminderScheduler.cancel`) runs after the DB work, outside the transaction.
- The backup DTO is a stable contract at schema version 1: fields may be added with defaults, decoding uses `ignoreUnknownKeys`, and merge import remaps category and task ids.
- Editors use `ConnectedSelectorButton` / `SelectorButtonGroup` (Material 3 Expressive connected toggle buttons) for single-choice rows.
- Width tiers come from `WindowWidthClass`. Editors dock beside content from Large (≥ 1200 dp), where the docked pane takes 412 dp.
- `feat-324-spaces` is in flight and also adds a Room migration. Sections hang off categories, so spaces do not change this design. Only the migration number is shared.

## Goals / Non-Goals

**Goals:**
- A category without sections stays byte-for-byte on the existing code path. The new grouping only engages when sections exist.
- The "a section lives in exactly one category" invariant is enforced at a single write point, not by every caller.
- One hit-testing model for cross-section drag that works in both the one-column and two-column layouts.

**Non-Goals:**
- Sync-safe identifiers for sections (handled for every entity by #30).
- The docked-editor edge polish on Large screens (separate change).
- Bringing the section shortcut group below Large.

## Decisions

### Storage: a `task_sections` table and a nullable `tasks.sectionId`, no foreign keys
`TaskSectionEntity(id autoincrement, categoryId, name, sortOrder)` is indexed on `categoryId`. `tasks.sectionId INTEGER DEFAULT NULL` is indexed. Migration 11 → 12 is purely additive: `CREATE TABLE`, `ALTER TABLE ADD COLUMN`, `CREATE INDEX`. Existing rows read as unsectioned.
- *Why no FKs:* adding an FK to `tasks` needs a full table rebuild in SQLite. Category deletion and undo are already snapshot-driven in the repository, and a cascade would delete sections before the snapshot could read them.
- *Defensive read:* a task whose `sectionId` does not name a section in its category is rendered as unsectioned. This keeps a stale or orphaned id from hiding a task.
- *Alternative considered:* a `sectionName` string on the task. Rejected: renaming would mean rewriting every task, and there would be no stable ordering or empty sections.

### The category-change invariant lives in `TaskRepositoryImpl`
Any task update whose `categoryId` differs from the stored row writes `sectionId = NULL` in the same statement or transaction. `UpdateTaskUseCase` also clears it for clarity, but the repository is the guarantee. That covers the editor, widget, backup merge and any future bulk move.

### Transaction boundaries
- *Delete section:* one Room transaction reads the section and its member task ids into a `SectionDeletionSnapshot`, sets `sectionId = NULL` on those tasks, and deletes the section row. There is no scheduler side effect, because reminders are independent of sections.
- *Undo section delete:* one transaction re-inserts the section with its original id and `sortOrder`, then restores `sectionId` only for snapshot tasks that still exist in the same category. A task that has meanwhile moved category or been deleted stays as it is.
- *Delete category:* the existing transaction also snapshots and deletes the category's sections. `CategoryDeletionSnapshot` gains `sections`, and restore inserts sections before tasks. The reminder `cancel` calls stay after the transaction, as today.
- *Reorder sections:* one transaction rewrites `sortOrder` for every section of the category (0..n-1), mirroring `ReorderCategoriesUseCase`. It is idempotent: replaying the same order writes the same values.
- *Move task to section at position:* one transaction sets the task's `sectionId` and rewrites `sortOrder` for the target section's active top-level tasks around the drop index.

### Recurrence and completion invariants
`sectionId` is a plain field on `Task`, so the next-occurrence copy built in `ToggleTaskCompletionUseCase` / `RollOverduePeriodicTaskUseCase` carries it. Undoing a completion that rolls back the spawned instance restores the original, section included. Unit tests pin both paths. Completion never touches `sectionId`, so uncompleting returns the task to its section. Reminder scheduling and its idempotency rules are unchanged, because a section change never reschedules.

### Presentation: a separate sectioned grouping path
`TasksViewModelDataLoading` checks whether the selected category has sections:
- *None:* the existing `ActiveGroupKey` path runs unchanged. This guarantees the "looks as today" requirement and existing screenshot references.
- *Some:* it builds `TasksUiState.sectionedList`: an `unsectioned: ImmutableList<Task>` and an `ImmutableList<SectionUi>` (`id`, `name`, `activeCount`, `overdueCount`, `isCollapsed`, `tasks`). Each list is sorted by the selected sort's comparator without group headers. Reorderable runs are built per section by feeding the section lists into the existing `buildReorderableRuns`. The Completed block keeps using `completedTaskGroups`.

### Collapse state in `TasksScreenPreferencesRepository`
It is stored as a `Set<Long>` of collapsed section ids in SharedPreferences, next to `showCompletedTasks`. It is device-local by design, not in the backup DTO, and ids are pruned when a section is deleted (and re-added on undo). The alternative of an `isCollapsed` column was rejected: it would sync and back up UI state.

### Section headers and pinned headers
`SectionHeader` is a new internal composable in `features/tasks/presentation/components/sections/`. It uses the `sectionHeader` typography, the category's primary text colour, a hairline rule, an add action and an overflow menu. It deliberately differs from the wavy date dividers and the Completed pill. When the window height is under 480 dp, headers are emitted with `LazyListScope.stickyHeader`. Otherwise they are plain `item`s. The decision is read once from the window size, not per item.

### Cross-section drag: a shared bounds registry
The existing `taskReorderGestures` estimates positions from a fixed item height, which only works inside one contiguous run. Cross-section drag (Manual sort only) uses a `SectionDropRegistry` instead. Each header, card and empty placeholder reports its bounds in the list's coordinate space via `onGloballyPositioned`. During a drag, the pointer position resolves to a `(sectionId?, index)` drop target, and the target header or slot is highlighted. This works the same in the one-column `LazyColumn` and the two-column staggered layout. Dropping emits `MoveTaskToSection(taskId, sectionId, index)`. Within-section reordering in non-Manual sorts keeps using the existing tied-run gesture.
- *Alternative considered:* `LazyListState.layoutInfo` hit-testing. Rejected because it does not exist inside the non-lazy section blocks of the two-column layout.
- The editor's Section row is the accessible, gesture-free path to the same move.

### Large and up: shortcut group and two columns
- *Shortcut group:* the `SectionShortcutGroup` composable is shown under `CategoryChipRow` when `currentWindowWidthClass() >= LARGE` and the category has sections. It reuses `ConnectedSelectorButton` with `connectedButtonShapes`, and `selectorButtonColors` is fed the category colour for the checked state. Add section and Reorder sections are separate square icon buttons. Scroll spy is a `derivedStateOf` over the list or grid state, mapping the first visible item to its owning section. Tapping a segment calls `animateScrollToItem(headerIndex)`. `AnimatedContent` keyed by category id gives the slide-in when the category changes.
- *Two columns:* a `BoxWithConstraints` around the list measures the pane. At ≥ 900 dp with sections, the list renders as a `LazyVerticalStaggeredGrid(2)`. The unsectioned block and the Completed block span the full line. Each section is one item holding a non-lazy column of its header and cards, so a section never splits across columns, and staggered placement keeps sections in order. A docked editor shrinks the pane below 900 dp and the content switches back to the `LazyColumn`.
- *Alternative considered:* raising `ReadableContentMaxWidth` and staying single-column. Rejected because the approved design uses the width.

### Editor Section row
`TaskBottomSheetModels` gains `sections` and `selectedSectionId`. `TaskBottomSheetFormSections` renders the row under Category with the scrolling `ConnectedSelectorButton` row, the same as categories. It is hidden when the category has no sections or the task is a subtask. Changing the category in the editor resets the selection to No section. Opening from a section header preselects that section.

### Section management surfaces
- *Rename:* an in-place `TextField` in the header that confirms on IME Done or the check button and discards on back or focus loss.
- *Reorder:* a `TempoModalBottomSheet` (adaptive placement) with a drag-reorderable list.
- *Delete:* immediate deletion with a snackbar carrying Undo, following the pending-deletion pattern already used for tasks.
- *Opt-in per category:* `categories.sectionsEnabled` (`INTEGER NOT NULL DEFAULT 0`, declared with `@ColumnInfo(defaultValue = "0")` so raw inserts such as the fresh-install Inbox seed still succeed and a new install matches 11 → 12) is toggled from the category edit sheet. Turning it off (in `UpdateCategoryUseCase`) clears `sectionId` on every task of the category, folding the list back into one; the sections stay, empty, for when it is turned back on. The merge planner ignores it like the sort order. With it on, the category chip's menu offers "Add section" — the only entry point while the category is empty, since an empty category keeps its usual empty screen. Section UI, the "Add section" action and the Large+ shortcuts all key off it.
- *Add:* with sections on, an "Add section" text button ends the list at every width (also when the category has no section yet), the header menu offers "Add section below", and the shortcut group has an Add button on Large and up. All of them name the section in place, drawn as the header it becomes (chevron slot, name in the header style sized to its text, hairline, cancel and save where the add and menu buttons sit); the keyboard's Done saves. The draft is plain `remember`, not saveable: the new-section item keeps one key, and a saveable draft would reopen pre-filled with the last name. The category sheet's Sections switch is drawn as a settings option card.
- *Look:* the header's chevron sits in a 48 dp slot mirroring the trailing menu button so both ends are symmetric; the hairline keeps 12 dp to the count and to the add glyph; the section menu is drawn exactly like the category chip menu (16 dp corners, 20 dp drawable icons, haptics); an empty section's placeholder is a borderless soft tint.

### Backup
`BackupFileDto.taskSections: List<TaskSectionBackupDto> = emptyList()` and `TaskBackupDto.sectionId: Long? = null` stay under schema version 1, per the DTO's evolution rules.
- *Replace import:* deletes sections together with the other tables and inserts them after categories.
- *Merge import:* builds a `sectionIdMap` after `categoryIdMap`. A task's `sectionId` is remapped, or set to null when its section is missing or belongs to a different category.
- *Older apps:* an older app reading a newer file drops the unknown fields (`ignoreUnknownKeys`), so its tasks import unsectioned. This is documented in `docs/BACKUP_FORMAT.md`.

## Risks / Trade-offs

- [Cross-section drag is the most complex gesture in the app] → Ship the editor Section row first (it covers the move end to end), then land drag behind its own task group with UI tests on the registry's target resolution.
- [Non-lazy section blocks in the two-column grid compose every card of a section at once] → Sections are bounded human lists. If profiling shows jank on very large sections, fall back to one column above a per-section threshold.
- [Sticky headers plus collapse animations can jitter] → Only short windows use sticky headers. A screenshot and UI test covers the push-off behaviour.
- [Migration number collision with `feat-324-spaces`] → Whichever change lands second renumbers its migration and regenerates `app/schemas/`. Migration tests are written against the version constants, not literals.
- [New screenshot references for Large+ layouts] → Add dedicated `@PreviewTest` previews for sectioned categories and leave the existing ones untouched. Review the diff report before committing references.

## Migration Plan

1. Room 11 → 12 additive migration, with exported schema and a migration test from a v11 database containing tasks (all read as unsectioned).
2. No data backfill. Sections are opt-in per category.
3. Rollback: Room downgrades are not supported (as with every prior migration). An app downgrade requires a fresh install plus a backup restore, and that path is covered: older apps ignore the section fields.

## Open Questions

- Maximum section name length: reuse the category name limit unless implementation shows a reason to differ.
