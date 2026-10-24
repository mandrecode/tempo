# Proposal

## Why

Originating issue: [#351](https://github.com/mandrecode/tempo/issues/351).

"Clear completed" lives in the persistent floating bar as a red icon FAB, far from the completed tasks it acts on, and it crowds the bar (it is the reason the portrait bar is "short of room" on the Tasks tab). The action belongs next to the "Completed" section it clears.

## What Changes

- The Tasks list's "Completed" separator row gets a "Clear" text button at its right end, shown whenever the selected category has completed top-level tasks, whether the section is collapsed or expanded. Tapping it opens the existing confirmation dialog; the delete and undo flow is unchanged.
- The separator row no longer shows the first completed group's label inline. When the section is expanded under a grouping sort (date, priority, title), every completed group, including the first, gets its own group header row.
- The floating bar (portrait, single-tab portrait, and landscape rail) no longer shows a clear-completed button; on the Tasks tab it shows only sort. Its layout no longer depends on whether completed tasks exist.
- What's New announces the new placement.

### Non-goals

- No change to what "clear completed" deletes, the confirmation copy, undo, or automatic removal of completed tasks.
- No change to the collapse/expand behavior of the completed section or to active task grouping.

## Capabilities

### New Capabilities
- `task-list-completed-section`: how the Tasks list presents its completed section — the separator row, its clear action, and completed group headers.

### Modified Capabilities

None.

## Impact

- UI: `TasksContent.kt` (`CompletedTasksSeparator`, completed group headers), `TasksScreen.kt`, `TaskActionButtons.kt` (`ClearCompletedButton`).
- Floating bar: `FloatingBarState.kt`, `FloatingBarTaskActions.kt`, `PersistentFloatingBar.kt` and related layout code; their instrumented and screenshot tests.
- Strings: a short "Clear" label in every locale; What's New entry strings.
- No domain, data, or persistence changes.
