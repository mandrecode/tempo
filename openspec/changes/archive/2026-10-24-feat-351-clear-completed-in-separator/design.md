# Design

## Context

`TasksScreen` worked out `hasCompletedTasks` and an `onClearCompleted` callback and passed both up to the app-level floating bar through `TasksFloatingBarState`. There, `TaskActionButtons` (portrait) and `VerticalTaskActionButtons` (rail) showed `ClearCompletedButton`, and the single-tab portrait bar sized its task-action slot from the flag.

## Goals / Non-Goals

**Goals:**
- Put the clear action with the completed tasks and take everything that only existed for it out of the floating bar.

**Non-Goals:**
- Reworking the floating bar's motion or spacing beyond removing the clear button.
- Changing the confirmation dialog, the ViewModel events, or the delete use case.

## Decisions

**A footer item after the expanded completed tasks.** `TasksContent` adds a `completed_clear` lazy item after the last completed group, only when `showCompletedTasks` is true. It sends the existing `UiEvent.RequestDeleteCompletedTasks` through `onEvent`, so no new event or screen callback is needed.

Alternatives tried or considered:
- *A "Clear" button at the trailing end of the separator row* (first implementation in this PR). The first group's label had to move to its own header row to make room, which stacked two wavy dividers and read as clutter. It was rejected after review on device.
- *Clear in the separator while collapsed, at the bottom while expanded.* The button would change place on toggle. The user chose to show it only when expanded.

**Error-colored text button with the remove-done icon.** It keeps the destructive cue the red FAB gave, and the icon ties it to the old action. The label is "Clear completed", since at the bottom of the list it has no "Completed" pill beside it.

**Floating bar simplification.** Remove `hasCompletedTasks` and `onClearCompleted` from `TasksFloatingBarState`, the clear button from both action layouts, and `TASK_ACTIONS_WIDTH` (the single-tab slot is always one button wide).

**Session chip compactness keys off the Tasks route.** Previously `hasContextualActions` was `isTasksRoute && hasCompletedTasks`, on the assumption that sort alone left room for the countdown. Review showed that a 360dp portrait window with all tabs needs 352dp but has 328dp, which squeezes the add button. The chip is now compact whenever Tasks is shown. A width-based rule was considered, but the route already identifies the only tab with an extra action, and it stays deterministic and testable through `isSessionChipCompact`.

## Risks / Trade-offs

- [Clear is hidden while the section is collapsed] → intentional: you only clear what you can see. Expanding is one tap.
- [Wide phones lose the countdown on Tasks] → it is still shown on Routines and in the rail layouts, and tapping the chip opens the session.
- [Portrait floating-bar motion tests asserted the clear button's gap and movement] → the clear-specific cases are dropped; the pill-stays-put and sort/pill/add gap checks remain.
