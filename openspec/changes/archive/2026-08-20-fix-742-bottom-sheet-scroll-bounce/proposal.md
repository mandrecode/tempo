## Why

GitHub issue [#742](https://github.com/mandrecode/tempo/issues/742) reports that bottom sheets "get buggy" if you try to scroll up: the sheet enters a bouncing glitch. It is most visible when opening an existing habit or task and dragging the already-top content upward — the whole sheet stretches up past its resting position and springs back.

The root cause is in Material3's `ModalBottomSheet`. When `sheetGesturesEnabled` is `true`, the sheet installs `ConsumeSwipeWithinBottomSheetBoundsNestedScrollConnection`. On an upward scroll (`delta < 0`) this connection routes the raw delta into `sheetState.anchoredDraggableState.dispatchRawDelta(...)` in `onPreScroll`/`onPostScroll`, before the content's own scrollable can react. With short content the sheet rests at its Expanded anchor, so the upward delta pushes the sheet past that anchor; the surface stretches (`verticalScaleUp`) and then settles back with the underdamped expressive spring, producing the visible bounce. This is independent of child overscroll visuals, which is why suppressing content overscroll alone did not fix it.

## What Changes

- Disable parent sheet drag gestures (`sheetGesturesEnabled = false`) on the affected modal bottom sheets so upward scroll deltas can no longer leak into the sheet's anchored-draggable:
  - `TempoModalBottomSheet` (shared wrapper used by the task and habit editor sheets).
  - `SortBottomSheet` (uses `ModalBottomSheet` directly).
- Remove drag handles on these sheets while gestures are disabled, so the UI does not imply drag-dismiss is available.
- Keep dismissal available through the scrim tap, system back, and explicit in-sheet actions (Cancel/Save), all of which are independent of `sheetGesturesEnabled`.

Non-goals:

- Do not reimplement `ModalBottomSheet`. The `.nestedScroll` and `.draggable` modifiers are gated by the same `sheetGesturesEnabled` flag, so drag-to-dismiss cannot be preserved without copying the sheet's internals (which rely on `internal` APIs).
- Do not change `TempoModalTopSheet`; it is a custom top sheet driven by `Animatable`/`tween` with clamped drag and no nested-scroll routing, so it does not exhibit the glitch.
- Do not change sheet content, domain, data, or navigation behavior.

## Capabilities

### New Capabilities

- `bottom-sheet-scroll-stability`: Defines the stability invariant for modal bottom sheets when content is scrolled past its top edge, and the dismissal paths that must remain available.

### Modified Capabilities

- None.

## Impact

- `app/src/main/java/com/mandrecode/tempo/core/ui/components/TempoModalBottomSheet.kt`
- `app/src/main/java/com/mandrecode/tempo/features/tasks/presentation/components/sections/SortBottomSheet.kt`
