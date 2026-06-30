## Why

Issue [#90](https://github.com/mandrecode/tempo/issues/90) reports that the bottom navigation bar can visually interfere with the last real card in scrollable task and habit lists. Adding end-of-list clearance lets users scroll the final item into a comfortable reading position instead of leaving it crowded at the bottom edge.

## What Changes

- Add non-content scroll clearance at the end of task lists so the last task card can scroll above the bottom navigation area.
- Apply the same end-of-list clearance behavior to habit lists.
- Keep the clearance invisible/non-interactive so it does not look like a real task or habit and does not affect list item actions or empty states.
- No breaking changes.

## Capabilities

### New Capabilities

- `list-bottom-scroll-clearance`: Scrollable task and habit lists provide invisible bottom clearance after the final real item.

### Modified Capabilities

- None.

## Impact

- Affected code: Compose task and habit list content/screens.
- APIs/dependencies: No new public APIs or dependencies expected.
- Systems: UI layout only; no domain, data, persistence, scheduling, notification, or navigation changes expected.
