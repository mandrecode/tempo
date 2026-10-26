# Tasks

## 1. Data layer and migration

- [x] 1.1 Add `TaskSectionEntity` (`id`, `categoryId`, `name`, `sortOrder`, index on `categoryId`) and `TaskSectionDao` (observe by category, insert, update, delete, rewrite sort orders, delete by category, get all). Register both in `TempoDatabase` and verify with DAO instrumented tests.
- [x] 1.2 Add nullable `sectionId` plus an index to `TaskEntity`/`Task` and their mappers. Verify the mapper unit tests round-trip `sectionId`.
- [x] 1.3 Write `MIGRATION_11_12` (create `task_sections`, add `tasks.sectionId`, create indexes) and bump the DB version. Run `./gradlew kspDebugKotlin` and commit `app/schemas/`. Verify with a migration test from v11 data where every task reads as unsectioned.
- [x] 1.4 In `TaskRepositoryImpl`, clear `sectionId` whenever an update changes `categoryId`. Verify with a repository test covering editor, widget and bulk update paths.

## 2. Domain: sections and their lifecycle

- [x] 2.1 Add the `TaskSection` model, the `TaskSectionRepository` interface and implementation, and bind it in `core/di/RepositoryModule.kt`. Verify the build and a Hilt graph test.
- [x] 2.2 Implement `CreateTaskSectionUseCase` and `RenameTaskSectionUseCase` (trimmed, non-blank, appended last) and `ReorderTaskSectionsUseCase` (rewrites 0..n-1). Verify with unit tests for blank rejection, append position and idempotent reorder.
- [x] 2.3 Implement `DeleteTaskSectionUseCase` returning a `SectionDeletionSnapshot`, and `RestoreDeletedTaskSectionUseCase` (single transactions; only restore tasks still in the same category). Verify with unit tests, including a task moved category before undo.
- [x] 2.4 Implement `MoveTaskToSectionUseCase(taskId, sectionId?, index)`: reject a section from another category, and rewrite the target section's `sortOrder`. Verify with unit tests for append, insert-at-index and move to unsectioned.
- [x] 2.5 Extend `CategoryDeletionSnapshot` and `deleteCategoryWithSnapshot`/`restoreDeletedCategory` with sections. Verify with a unit test that delete-plus-undo restores sections and memberships.
- [x] 2.6 Pin the recurrence and completion invariants: the spawned next instance keeps `sectionId`, the rollback restores it, and complete/uncomplete leave it unchanged. Verify with tests in `ToggleTaskCompletionUseCaseTest` and `RollOverduePeriodicTaskUseCaseTest`.

## 3. Backup

- [x] 3.1 Add `BackupFileDto.taskSections` and `TaskBackupDto.sectionId` (defaults, schema v1), plus mapper and export. Verify with an export unit test that includes sections.
- [x] 3.2 Replace import: clear and insert sections after categories. Merge import: build `sectionIdMap` and remap or null each task's `sectionId`. Verify with backup round-trip, older-file fixture and merge tests.
- [x] 3.3 Document sections in `docs/BACKUP_FORMAT.md`, including older-app behaviour. Verify the documented fields match the DTO.

## 4. Collapse preference

- [x] 4.1 Add a collapsed-section-ids set to `TasksScreenPreferencesRepository`/`Impl`, with prune on delete and re-add on undo. Verify with a unit test using the fake prefs and with the ViewModel test surviving recreation.

## 5. Tasks ViewModel: sectioned grouping

- [x] 5.1 Add `SectionUi`/`SectionedTaskList` to `TasksContract`, and the events: create, rename, reorder, delete, undo, toggle collapse, move task, add-task-in-section. Use `kotlinx.collections.immutable`. Verify the contract compiles and `detekt` passes.
- [x] 5.2 In `TasksViewModelDataLoading`, build the sectioned list when the category has sections: unsectioned first, each section sorted by the active sort without group headers, counts, overdue counts, reorderable runs per section, and orphan section ids treated as unsectioned. Keep the existing path untouched otherwise. Verify with ViewModel tests per sort option, plus an existing-behaviour regression test for a category without sections.
- [x] 5.3 Wire the section events to their use cases, including the snackbar-with-undo effect for delete. Verify with ViewModel tests using Turbine on `uiEffect`.

## 6. Tasks list UI: headers and management

- [ ] 6.1 Build the `SectionHeader` composable (expanded, collapsed with overdue badge, accessible labels, 48 dp targets), the empty-section placeholder and the end-of-list "Add section" button, with `@Preview`s in `src/debug/`. Verify the previews render in light and dark.
- [ ] 6.2 Render the sectioned list in `TasksContent` (unsectioned, sections, Completed block unchanged), with collapse animation and `stickyHeader` when the window height is under 480 dp. Verify with Content UI tests for order, collapse, empty placeholder and no-section parity.
- [ ] 6.3 Add the section menu, in-place rename and the reorder sheet (`TempoModalBottomSheet`, adaptive placement). Verify with Content UI tests for rename confirm/dismiss and reorder.
- [ ] 6.4 Make the header's add action open the editor with category and section preselected. Verify with a UI test.

## 7. Task editor Section row

- [ ] 7.1 Add `sections`/`selectedSectionId` to `TaskBottomSheetModels` and render the Section row under Category (scrolling connected buttons, hidden for unsectioned categories and subtasks, reset on category change). Verify with UI tests for the three `editor-selector-groups` scenarios.

## 8. Cross-section drag (Manual sort)

- [ ] 8.1 Implement `SectionDropRegistry` (bounds of headers, cards and placeholders) and drop-target resolution. Verify with unit tests on target resolution: between cards, collapsed header, empty placeholder, unsectioned area.
- [ ] 8.2 Extend the task drag gesture to emit `MoveTaskToSection`, with a target highlight, only in Manual sort. Verify with a Content UI test dragging a card into another section and onto a collapsed header.

## 9. Large and up: shortcut group and two columns

- [ ] 9.1 Build `SectionShortcutGroup` (connected buttons, category-coloured checked state, counts, separate Add and Reorder buttons, `AnimatedContent` per category), shown from Large up. Verify with a UI test for tap-to-scroll and a scroll-spy ViewModel/Content test.
- [ ] 9.2 Add the two-column `LazyVerticalStaggeredGrid` layout for panes ≥ 900 dp (full-line unsectioned and Completed blocks, whole sections per lane), falling back to one column when the editor docks. Verify with a UI test that toggles the docked editor.
- [ ] 9.3 Add `@PreviewTest` screenshot previews for a sectioned category on phone, foldable, tablet and desktop (light and dark), run `./gradlew updateDebugScreenshotTest`, review the diff report, and commit the references. Verify `./gradlew validateDebugScreenshotTest` passes and existing references are unchanged.

## 10. Copy and announcement

- [ ] 10.1 Add every new string to `values/strings.xml` and every `values-<locale>/strings.xml` ("Sección" in Spanish, reading naturally for "grupos"). Verify `./gradlew lintDebug` reports no Missing or Extra translations.
- [ ] 10.2 Replace `WhatsNewRegistry.latest` with a `WhatsNewEntry` for sections (id `61`) and remove the previous entry's strings. Verify the What's New unit test.

## 11. Integration verification

- [ ] 11.1 Run `./gradlew ktlintFormat`, `./gradlew ktlintCheck`, `./gradlew :app:detekt` (baseline must not grow) and `./gradlew testDebugUnitTest`, and verify all pass.
- [ ] 11.2 Run `./gradlew koverVerifyDebug` and verify the 80% line and 70% branch thresholds hold.
- [ ] 11.3 Run `./gradlew connectedDebugAndroidTest` on the Pixel 10 AVD, then smoke-test the S&V scenario on the Pixel 7: create sections, move tasks, collapse, delete with undo, backup round-trip. Verify the issue's acceptance criteria.
- [ ] 11.4 Run `openspec validate feat-61-task-sections --strict` and verify it passes.

## Workflow follow-up

- Open the PR (`feat(#61): task sections within categories`, `Closes #61`) only once the user gives the go.
- Archive the change with `openspec-archive-change` in the same PR before its final CI run.
