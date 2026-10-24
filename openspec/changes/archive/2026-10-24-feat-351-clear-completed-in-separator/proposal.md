# Proposal

## Why

Originating issue: [#351](https://github.com/mandrecode/tempo/issues/351).

"Clear completed" lives in the persistent floating bar as a red icon FAB, far from the completed tasks it acts on, and it crowds the bar. The action belongs with the completed tasks it clears.

## What Changes

- When the "Completed" section is expanded, a "Clear completed" text button appears at the bottom right, below the last completed task. It is not shown while the section is collapsed, so you always see what you are about to delete. Tapping it opens the existing confirmation dialog; delete and undo are unchanged.
- The separator row itself is unchanged (toggle, divider, inline first-group label).
- The floating bar (portrait, single-tab portrait, and landscape rail) no longer shows a clear-completed button; on the Tasks tab it shows only sort, and its layout no longer depends on whether completed tasks exist. The running-session chip stays compact on the Tasks tab, where sort and add still flank the bar.
- What's New announces the move, combined with the unreleased #379 and #422 entries.

### Non-goals

- No change to what "clear completed" deletes, the confirmation copy, undo, or automatic removal of completed tasks.
- No change to collapse/expand behavior or to how completed tasks are grouped.

## Capabilities

### New Capabilities
- `task-list-completed-section`: how the Tasks list presents its completed section and the action that clears it.

### Modified Capabilities

None.

## Impact

- UI: `TasksContent.kt` (new `ClearCompletedFooter`), `TasksScreen.kt`, `TaskActionButtons.kt` (`ClearCompletedButton` removed).
- Floating bar: `FloatingBarState.kt`, `FloatingBarTaskActions.kt`, `PersistentFloatingBar.kt`, `SingleTabFloatingBarLayout.kt`; their instrumented tests.
- Strings: What's New entry; `delete_all_completed_tasks` removed.
- No domain, data, or persistence changes.
