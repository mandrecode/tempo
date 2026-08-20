## Why

Deep production testing found that the keyboard-handling changes delivered by
[PR #388](https://github.com/mandrecode/tempo/pull/388) and
[PR #394](https://github.com/mandrecode/tempo/pull/394) regress normal task editing: moving
between the title and description can close and reopen the keyboard. Restore the known pre-#229
baseline while issue [#393](https://github.com/mandrecode/tempo/issues/393) remains open for a
different solution.

## What Changes

- Restore the shared task sheet's standard IME padding and original continuously registered
  predictive-back handler from the pre-#229 baseline.
- Remove gesture-start keyboard routing and its focus-clearing behavior introduced by PR #394.
- Remove the now-invalid keyboard-dismissal behavior contract instead of claiming the original
  dismissal glitch is fixed.
- Preserve all unrelated releases, adaptive placements, sheet dismissal guards, and entered task
  content behavior.
- **Non-goal:** solve #393 or the earlier #229 keyboard-dismissal glitch in this rollback.
- **Non-goal:** change task editor layout, validation, persistence, or navigation.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `task-editor-keyboard-dismissal`: Remove the guarantees introduced by the two unsuccessful fixes;
  the restored baseline intentionally carries no dedicated keyboard-dismissal contract.

## Impact

- Affects the shared Compose task-sheet predictive-back implementation and its focused policy test.
- Removes no dependencies and changes no public APIs, data models, persistence, or localization.
- Behavior is identical at compact, medium, and expanded widths except for returning task-field and
  keyboard interaction to the pre-#229 baseline.
