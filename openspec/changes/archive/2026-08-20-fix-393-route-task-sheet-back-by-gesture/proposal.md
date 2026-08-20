## Why

The current issue #393 fix reacts to global IME visibility changes and force-clears focus, which makes normal title-to-description focus handoffs close and reopen the keyboard on the Pixel 7. Keyboard-first back behavior must instead be decided for the back gesture that is actually in progress, without treating unrelated IME transitions as dismissal.

## What Changes

- Restore the task sheet's pre-#229 standard IME padding behavior.
- Keep the sheet predictive-back callback stable rather than enabling and disabling it as keyboard visibility changes.
- Route each back gesture from the keyboard state captured when that gesture begins: a keyboard-first gesture does not move or dismiss the sheet, while a sheet-first gesture retains the existing predictive animation and guarded dismissal.
- Clear editor focus only when a completed back gesture was explicitly routed to keyboard dismissal.
- Add regression coverage for gesture routing, cancellation, and title/description focus handoffs.
- Non-goals: changing task-editor layout, field behavior, discard confirmation, navigation, or introducing a new keyboard animation system.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `task-editor-keyboard-dismissal`: Define gesture-scoped keyboard-first routing that preserves task-sheet position and normal editor focus handoffs.

## Impact

- Affects the shared Compose task-sheet predictive-back handling and its focused unit tests.
- Applies to compact/medium modal sheets and expanded docked sheets without changing their layout contract.
- Adds no dependencies and changes no domain, data, persistence, or public API behavior.
- Originating issue: https://github.com/mandrecode/tempo/issues/393
