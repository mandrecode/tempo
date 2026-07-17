## Why

When editing a task, the auto-save dirty-check in `TaskBottomSheetContent` compares the current form state against a snapshot frozen when the sheet opened, instead of against the last value actually persisted. If a user moves a task to category B (which persists) and then moves it back to the original category A, the new snapshot equals the frozen original snapshot again, so the "changed" check evaluates false and the category change is silently dropped — the task stays stuck in category B in the database while the UI shows A selected. This is GitHub issue #167.

Separately, the numeric count badge on unselected category chips (`CategoryChipRow`) appears/disappears/changes abruptly with no transition, unlike sibling chip properties (color, corner radius, height) which already animate. Issue #167 also asks for this badge to animate.

## What Changes

- Fix the auto-save and dismiss-save dirty-check in `TaskBottomSheetContent.kt` to compare the current form snapshot against the last **persisted** snapshot (`lastDispatchedSnapshot ?: taskSnapshot`) rather than always against the original `taskSnapshot`, so every real change — including reverting to a previously-visited value — is detected and saved exactly once.
- Animate the category count badge in `CategoryChipRow.kt` (`CategoryItem`) so it fades/transitions in, out, and between values instead of popping abruptly, following the existing `AnimatedContent` + `SizeTransform` pattern used in `TaskCardMetadata.kt`.

## Capabilities

### New Capabilities
(none)

### Modified Capabilities
(none — no existing `openspec/specs/` capability covers task category editing or category chip presentation; this is a targeted bug fix to implementation behavior already implicitly expected by users, not a documented spec requirement change)

## Impact

- `app/src/main/java/com/mandrecode/tempo/features/tasks/presentation/components/TaskBottomSheetContent.kt` (auto-save dirty-check logic, both the debounced `onSnapshot` callback and `onSheetDismissRequest`)
- `app/src/main/java/com/mandrecode/tempo/features/tasks/presentation/components/sections/CategoryChipRow.kt` (count badge rendering)
- No domain, data, or database changes. No public API changes. No migrations.
