# Design

## Context

`TasksScreen` works out `hasCompletedTasks` and an `onClearCompleted` callback and passes both up to the app-level floating bar through `TasksFloatingBarState`. Three things depend on that flag there:

- `TaskActionButtons` (portrait) and `VerticalTaskActionButtons` (rail) show `ClearCompletedButton`.
- The single-tab portrait bar sizes its task-action slot from it (`TASK_ACTIONS_WIDTH` vs one button).
- `TempoBottomNavigation` gets `hasContextualActions = isTasksRoute && hasCompletedTasks`, which makes the running-session chip compact because the bar is otherwise short of room.

`TasksContent` draws the `CompletedTasksSeparator` (toggle pill, optional `WavyDivider`, optional inline `firstGroupLabel`) and skips the header for the first completed group because the separator already showed it.

## Goals / Non-Goals

**Goals:**
- Put the clear action in the separator and take everything that only existed for it out of the floating bar.

**Non-Goals:**
- Reworking the floating bar's motion or spacing beyond removing the clear button.
- Changing the confirmation dialog, the ViewModel events, or the delete use case.

## Decisions

**The separator raises the existing event.** `TasksContent` already receives `onEvent`, so the separator's "Clear" sends `UiEvent.RequestDeleteCompletedTasks` directly. No new `UiEvent`, and the `TasksScreen` callback goes away. Visibility needs no new flag either: the separator only renders when the category has completed tasks.

**A text button in the error color.** The issue asks for a text button instead of an icon. It keeps `colorScheme.error` content color as the destructive cue the red FAB used to give. The label is a new short "Clear" string, since "Clear completed" next to a "Completed" pill repeats itself. Once `ClearCompletedButton` is gone, `clear_completed` and `delete_all_completed_tasks` have no users and are deleted from every locale.

**The first group gets its own header instead of an inline label.** With the button at the trailing end, an inline group label between divider and button doesn't fit on narrow phones with long date labels. Giving every group a `CompletedGroupHeader` makes the list uniform and keeps the separator row the same whether collapsed or expanded. Alternatives considered: moving the button below the last completed task when expanded (the button changes place on toggle and ends up far down long lists), and keeping the inline label (crowded). The user chose this option.

**In manual sort, a weighted spacer stands in for the divider** so "Clear" still sits at the trailing end.

**Floating bar simplification.** Remove `hasCompletedTasks` and `onClearCompleted` from `TasksFloatingBarState`, the clear button from both action layouts, `TASK_ACTIONS_WIDTH` (the single-tab slot is always one button wide), and `hasContextualActions` from `TempoBottomNavigation` / `isSessionChipCompact`. With sort alone, its own KDoc already says the bar has room for the countdown, so the parameter would always be false. Keeping a parameter that is always false was the alternative; dropping it is simpler.

## Risks / Trade-offs

- [The running-session chip on the Tasks tab now always shows its countdown in portrait] → this is the "sort alone" case the existing code already treats as roomy; check on a narrow device.
- [Portrait floating-bar motion tests assert the clear button's gap and movement] → drop the clear-specific cases; keep the pill-stays-put and sort/pill/add gap checks.
- [Error-colored text could read as alarming] → the confirmation dialog and undo still guard it, and it matches the old red FAB.
