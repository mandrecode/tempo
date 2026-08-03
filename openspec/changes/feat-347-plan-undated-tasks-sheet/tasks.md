## 1. Domain

- [x] 1.1 Add `PlanReminderTimeUtil` in `features/tasks/domain/util/` — pure Kotlin, `kotlinx-datetime`
      only — resolving `(chosenDate, now) -> LocalDateTime`: the day at `PLAN_DEFAULT_REMINDER_HOUR`
      (09:00), or the next whole hour after `now` capped at 23:59 when that has already passed.
- [x] 1.2 Add `GetUndatedTasksUseCase` in `features/tasks/domain/usecase/` returning incomplete,
      top-level, reminder-less tasks paired with their `Category`, as a `Flow`.
- [x] 1.3 Unit-test `PlanReminderTimeUtil`: future day, today before 09:00, today after 09:00,
      today at 23:30 (clamped), and day-boundary behaviour.
- [x] 1.4 Unit-test `GetUndatedTasksUseCase`: excludes subtasks, excludes completed, excludes dated,
      and resolves the category for each task including the missing-category case.

## 2. Shared task card

- [x] 2.1 Add a `CategoryBadge` model (icon resource + name) and an optional `categoryBadge`
      parameter to `TaskItem`, defaulting to `null`.
- [x] 2.2 Render the badge as the first entry of `MetadataRow` in `cards/TaskCardMetadata.kt`,
      handling the icon-less category by showing the name alone.
- [x] 2.3 Add an optional `footer: (@Composable () -> Unit)?` slot to `TaskItem`, rendered inside the
      card below the content row, defaulting to `null`.
- [x] 2.4 Confirm every existing `TaskItem` call site still compiles unchanged and renders as before.

## 3. Focus contract and view model

- [x] 3.1 Add `PlanSheetState` to `FocusContract` holding `taskIds: ImmutableList<Long>`,
      `originalReminders: ImmutableMap<Long, LocalDateTime?>` and `plannedTaskIds: ImmutableList<Long>`,
      plus a nullable `planSheet` field on `UiState`.
- [x] 3.2 Expose `plannedIds` / `unplannedIds` derived against the live task list, and a
      `showsSectionHeaders` rule (headers only once both sections are non-empty).
- [x] 3.3 Add `UiEvent`s: `PlanTask(taskId, date)`, `DismissPlanSheet`, `ConfirmPlanSheet`,
      `UndoPlanBatch`; repurpose `UndatedTasksClicked` to open the sheet.
- [x] 3.4 Remove `UiEffect.OpenTasksTab` and add `UiEffect.ShowPlanUndoSnackbar(count)`.
- [x] 3.5 Implement the handlers in `FocusViewModel`: opening snapshots ids and original reminders;
      `PlanTask` writes through `UpdateTaskUseCase`; `UndoPlanBatch` replays `originalReminders` for
      the planned ids only.
- [x] 3.6 Unit-test the view model: opening snapshots the right ids; planning moves a task between
      sections; `Done` is gated on at least one plan; undo restores the reminder captured at open
      time even after a later edit; undo cancels scheduling via the same use case.

## 4. Plan sheet UI

- [x] 4.1 Create `features/focus/presentation/components/PlanTasksSheet.kt` using
      `TempoModalBottomSheet(adaptivePlacement = true, hasUnsavedChanges = false)`, with the
      "Plan your tasks" title in Tempo's sheet-title style.
- [x] 4.2 Lay the rows out in `LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 328.dp))`,
      with section headers spanning `maxLineSpan`.
- [x] 4.3 Render each row with `TaskItem`, passing the task's `categoryBadge` and a `footer` holding
      the quick-plan chips in a `FlowRow`: Today, Tomorrow, Pick a date.
- [x] 4.4 Draw the "Planned" / "Unplanned" headers in `ActiveGroupHeader`'s style (`WavyDivider` +
      `groupLabel`), shown only when the view model says both sections exist.
- [x] 4.5 Wire "Pick a date" to `TempoDatePickerDialog` with today-and-later selectable dates,
      applying `PlanReminderTimeUtil` to the confirmed date.
- [x] 4.6 Gate the first chip tap of a session behind `HandleReminderPermissions`, holding the
      pending chip and applying it on grant, dropping it on dismiss.
- [x] 4.7 Add the end-aligned footer actions: Close (always enabled) and Done (enabled once at least
      one task has been planned).
- [x] 4.8 Dismiss on Escape via `onPreviewKeyEvent`, alongside the existing handle, back and scrim
      dismissals.
- [x] 4.9 Animate rows between sections so planning a task reads as a move rather than a jump.

## 5. Focus screen wiring

- [x] 5.1 Host `PlanTasksSheet` from `FocusScreen` when `uiState.planSheet != null`.
- [x] 5.2 Add a `SnackbarHostState` + `ExpressiveSnackbarHost` to the Focus `Scaffold`, matching
      `TasksScreen`, and show the undo snackbar on `ShowPlanUndoSnackbar`.
- [x] 5.3 Route a card-body tap to the existing `FocusTaskEditor` so the full editor opens over the
      sheet.
- [x] 5.4 Drop `onNavigateToTasks` from the Focus call site now that nothing emits `OpenTasksTab`,
      and clean up the navigation graph accordingly.
- [x] 5.5 Update the `UndatedTasksFooter` doc comment and its `ic_open_in_new` icon /
      `focus_session_open_in_tasks` description — the control no longer leaves Focus.

## 6. Strings, previews and What's New

- [x] 6.1 Add the new strings to `values/strings.xml` and matching entries to `values-es/strings.xml`
      (sheet title, subtitle, Planned, Unplanned, Today, Tomorrow, Pick a date, Close, Done, the
      undo snackbar message and action, and the category badge content description).
- [x] 6.2 Add `@Preview`s under `src/debug/` for the sheet (empty-of-planned, mixed, all-planned) and
      for the new internal composables (chips row, section header, planned row), Light and Dark.
- [x] 6.3 Replace `WhatsNewRegistry.latest` with a `347` entry and remove the previous entry's now
      unused strings.

## 7. Tests

- [x] 7.1 Compose-test the sheet at 360dp: rows render, chips are reachable and unclipped, the
      category badge does not overflow.
- [x] 7.2 Compose-test the planned/unplanned split: no headers initially, both headers after one
      plan, only "Planned" once everything is planned.
- [x] 7.3 Compose-test the footer: Done disabled until a plan, enabled after, Close always enabled.
- [x] 7.4 Compose-test at an expanded width that the sheet still lays out correctly and the column
      count follows available width.
- [x] 7.5 Update `FocusContentTest`'s undated-footer assertions from "navigates to Tasks" to
      "opens the plan sheet".

## 8. Verification

- [x] 8.1 `openspec validate feat-347-plan-undated-tasks-sheet --strict`
- [x] 8.2 `./gradlew ktlintFormat` then `./gradlew ktlintCheck`
- [x] 8.3 `./gradlew compileDebugKotlin compileDebugUnitTestKotlin`
- [x] 8.4 `./gradlew testDebugUnitTest`
- [x] 8.5 `./gradlew :app:detekt` — fix issues rather than growing `detekt-baseline.xml`
- [x] 8.6 `./gradlew lintDebug` — `strings.xml` was touched, so `MissingTranslation` / `ExtraTranslation`
      must be clean
- [x] 8.7 `./gradlew koverVerifyDebug`
- [x] 8.8 `./gradlew connectedDebugAndroidTest` on the Pixel 10 AVD for the new Compose tests
- [x] 8.9 Manual smoke test on the connected Pixel 7: plan two tasks, confirm the split, Done, undo,
      and confirm both reminders are gone — run on the Pixel 10 AVD instead, to avoid touching the
      physical Pixel 7's real data. Caught the snackbar coming up under the floating bar, now fixed.
