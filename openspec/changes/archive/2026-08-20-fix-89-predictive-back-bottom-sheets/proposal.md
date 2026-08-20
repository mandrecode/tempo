## Why

[GitHub issue #89](https://github.com/mandrecode/tempo/issues/89) reports that predictive back no longer works in the app's custom bottom sheets after migrating away from Material bottom sheets. Android users expect a back gesture to visibly preview dismissal, cancel cleanly when the gesture is aborted, and complete dismissal when committed.

## What Changes

- Restore predictive-back progress handling for shared custom modal bottom sheets.
- Keep the custom bottom-sheet primitive because it keeps keyboard movement visually synced
  with the sheet, unlike the Material3 wrapper experiment.
- Constrain bottom-sheet drag to downward dismissal and show the drag handle only because
  dragging is supported.
- Keep existing bottom-sheet dismiss behavior, including scrim taps, regular back presses, and the discard-changes confirmation path.
- Non-goal: revert to the official Material bottom sheet component as the final
  implementation.
- Non-goal: redesign bottom-sheet content, sizing, drag behavior, or navigation destinations.

## Capabilities

### New Capabilities

- `modal-sheet-dismissal`: Shared modal sheet dismissal behavior, including predictive back gestures and unsaved-change confirmation.

### Modified Capabilities

- None.

## Impact

- Affects `TempoModalBottomSheet` and all feature sheets that use it, including task, habit, and sort sheets.
- Uses the existing `androidx.activity:activity-compose` dependency; no new library is required.
- No domain, data, persistence, or navigation model changes.
