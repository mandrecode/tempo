## Context

`TaskBottomSheetContent.kt` auto-saves task edits via a debounced snapshot comparison. `taskSnapshot` is `remember(task?.id)`-frozen to the task's values at the moment the sheet opened and never updates during the editing session. Both the debounced `onSnapshot` callback and `onSheetDismissRequest` gate persistence on `currentSnapshot != taskSnapshot`, so once a field is changed and saved, reverting it to the original value makes `currentSnapshot == taskSnapshot` again and the revert is never persisted — the DB keeps the intermediate (wrong) value. `lastDispatchedSnapshot` already exists and correctly tracks the last value that was actually sent to `onAutoSave`, but it's currently only used as a secondary de-dupe guard (`lastDispatchedSnapshot != snapshot`), not as the comparison baseline.

## Goals / Non-Goals

**Goals:**
- Every real field change — including one that reverts a field to a previously-visited value — is detected and persisted exactly once.
- No redundant saves: an unchanged snapshot (same as last persisted) still does not trigger a save.
- Animate the category count badge in `CategoryChipRow` for appear/disappear/value-change, consistent with existing `AnimatedContent` usage in `TaskCardMetadata.kt`.

**Non-Goals:**
- No change to the debounce timing, the auto-save trigger mechanism, or the ViewModel/repository/DAO layers (already correct).
- No change to other dirty-tracking consumers of `taskSnapshot` (e.g., `hasUnsavedChanges` for the unsaved-changes-on-dismiss prompt) beyond what's needed for the save-comparison fix — `hasUnsavedChanges` intentionally compares against the *original* opened state, which is correct semantics for "is this session dirty at all" and is out of scope.

## Decisions

**Compare against `lastDispatchedSnapshot ?: taskSnapshot` instead of always `taskSnapshot`.**
This makes the effective baseline "the last state we know is persisted" — initially the original loaded task, and after any save, the newly persisted snapshot. This is the minimal change: it reuses the existing `lastDispatchedSnapshot` state var already threaded through both call sites, requires no new state, and preserves the existing de-dupe semantics (a snapshot equal to the baseline is never re-sent).

Alternative considered: update `taskSnapshot` itself (make it a mutable var reassigned after each save) instead of introducing a baseline expression. Rejected because `taskSnapshot` is also used by `hasUnsavedChanges`, which must keep comparing to the original opened state to correctly answer "has this editing session touched anything" (e.g., for a confirm-discard prompt) — conflating the two would regress that unrelated behavior.

**Category badge animation: wrap the existing `Box` in `AnimatedContent(targetState = count)` with fade + `SizeTransform`, matching `TaskCardMetadata.kt`'s `subtaskBadge` pattern.**
Reuses an established, already-reviewed pattern in the same feature module rather than introducing a new animation style, keeping visual consistency.

## Risks / Trade-offs

[Baseline expression duplicated at two call sites (`onSnapshot`, `onSheetDismissRequest`)] → Both already independently construct `TaskFormSnapshot` and duplicate structure; the fix is a one-line change at each site, no shared helper needed for a change this small.

[Animating a `Box` that's conditionally composed by `if (count > 0)`] → Must switch to `AnimatedVisibility` (or keep the item always composed and drive visibility through `AnimatedContent`'s own enter/exit) so the appear/disappear itself animates, not just be limited to value changes while already visible.
