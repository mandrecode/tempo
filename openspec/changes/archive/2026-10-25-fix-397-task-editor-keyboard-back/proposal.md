# Proposal

## Why

Issue [#397](https://github.com/mandrecode/tempo/issues/397): dismissing the keyboard with a back
gesture from a focused task field can make the task sheet move or rebound (#229). Two earlier fixes
(#388, #394) routed back gestures from `WindowInsets.isImeVisible` and regressed ordinary title ↔
description focus handoffs, so #396 rolled back to the baseline. A safer fix must key off explicit
editor focus, which the task editor knows exactly, instead of delayed IME visibility.

## What Changes

- The task editor tracks which of its fields (title, description, or none) currently owns focus.
- While a task editor field owns focus, a task-editor back handler owns the back gesture: it
  consumes predictive-back progress without moving the sheet, preserves focus on cancel, and clears
  editor focus once on completion so the keyboard closes and the sheet stays open.
- When no task editor field owns focus, the shared sheet keeps its existing predictive animation,
  unsaved-change guard, and dismissal path.
- The shared sheet (`TempoModalSheet`, `TempoDockedSheet`) is unchanged; no IME-visibility routing,
  inset calculations, transition timers, or shared-sheet focus mutation are introduced.

### Non-goals

- Changing habit, category, sort, or other shared-sheet consumers.
- Changing sheet placement or sizing in compact, medium, or expanded windows.
- The backup-export password field keyboard flicker reported in a comment on #397 (separate issue).

## Capabilities

### New Capabilities

_None._

### Modified Capabilities

- `task-editor-keyboard-dismissal`: keyboard-first back routing is decided from explicit task
  editor field focus instead of keyboard visibility, and field handoffs must never close the
  keyboard.

## Impact

- `features/tasks/presentation/components/TaskBottomSheetContent.kt`,
  `TaskBottomSheetFormSections.kt`, `TaskBottomSheetModels.kt`, plus a new task-editor back
  handler file in the same package.
- Unit and instrumented tests for focus tracking and back routing.
- Supersedes the requirement removals staged by the unarchived
  `fix-393-restore-task-sheet-keyboard-baseline` change; that change should be archived without
  syncing its spec delta.
