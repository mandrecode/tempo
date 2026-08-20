## Why

The fix for [GitHub issue #229](https://github.com/mandrecode/tempo/issues/229) lets the first back gesture hide Gboard, but the focused task-description editor immediately requests the keyboard again on the Pixel 7. [Issue #393](https://github.com/mandrecode/tempo/issues/393) needs keyboard dismissal to settle in one attempt without moving, dismissing, or refocusing the task sheet.

## What Changes

- Make a system keyboard-dismiss action clear the task editor's active text focus so the IME stays hidden.
- Keep the task sheet visible, anchored, and editable while the keyboard closes.
- Preserve guarded sheet dismissal on the next back action after the keyboard is hidden.
- Apply the same behavior to compact and medium modal task sheets and expanded docked task editors without changing their placement.
- Add focused regression coverage for the IME-visible-to-hidden transition and verify the supplied Pixel 7 sequence.
- Non-goals: redesigning the task form, changing validation or persistence, changing keyboard behavior in unrelated screens, or changing adaptive breakpoints.

## Capabilities

### New Capabilities

- `task-editor-keyboard-dismissal`: Defines stable, one-attempt software-keyboard dismissal from a focused task editor across adaptive placements.

### Modified Capabilities

None.

## Impact

The change affects the Compose task editor and/or shared modal-sheet IME handling plus focused tests. It adds no API, data-model, persistence, localization, or dependency changes. Compact, medium, and expanded layouts retain their established placement and sizing.
