## Context

`ModalBottomSheet` (material3 1.4.0) attaches its scroll handling to the sheet `Surface`:

```
if (sheetGesturesEnabled)
    Modifier.nestedScroll(ConsumeSwipeWithinBottomSheetBoundsNestedScrollConnection(...))
...
.draggable(..., enabled = sheetGesturesEnabled && sheetState.isVisible)
```

`ConsumeSwipeWithinBottomSheetBoundsNestedScrollConnection.onPreScroll` does, for an upward gesture:

```
if (delta < 0 && source == UserInput) anchoredDraggableState.dispatchRawDelta(delta)
```

Because the connection lives on the sheet `Surface` (the parent of the sheet content), it receives `onPreScroll` before the content's `verticalScroll`. With content shorter than the sheet, the sheet rests at the Expanded anchor; the upward delta then drives the sheet past that anchor, `verticalScaleUp` stretches the surface to hide the gap, and on release the sheet settles back with the expressive spatial spring (underdamped), which reads as a bounce.

The motion spec that produces the bounce (`anchoredDraggableMotionSpec`, `showMotionSpec`, `hideMotionSpec`) and `MotionScheme` are all `internal` in material3 1.4.0, so the spring cannot be swapped for a critically-damped one from app code.

## Goals / Non-Goals

**Goals:**

- Eliminate the scroll-up bounce on the task editor, habit editor, and sort bottom sheets.
- Keep a working, discoverable way to dismiss each sheet.
- Keep the change small and isolated to the sheet entry points.

**Non-Goals:**

- No reimplementation of `ModalBottomSheet`.
- No change to `TempoModalTopSheet` or to any sheet content/business logic.
- No new libraries or motion-scheme overrides.

## Decisions

1. **Disable sheet drag gestures on the affected sheets.**

   Set `sheetGesturesEnabled = false`. This removes the `ConsumeSwipe...` nested-scroll connection entirely, so upward scroll deltas stay within the content and can no longer overshoot the sheet's top anchor. It also disables the direct `.draggable` on the surface.

2. **Why not keep drag-to-dismiss.**

   `.nestedScroll` (the bounce source) and `.draggable` (drag-to-dismiss) are gated by the same `sheetGesturesEnabled` flag, and the connection runs in the sheet's outer `onPreScroll` — ahead of any connection the content could install. There is no public hook to intercept the overshoot while keeping drag-to-dismiss without copying the sheet's `internal` internals. Disabling gestures is the lowest-risk option.

3. **Preserve dismissal via non-drag paths.**

   The scrim's `onDismissRequest` and the predictive/system back path are independent of `sheetGesturesEnabled`, and the editor sheets already expose explicit Cancel/Save actions plus an unsaved-changes guard. Dismissal therefore remains available.

4. **Remove drag handles while gestures are disabled.**

   Since drag gestures are intentionally disabled, the drag-handle affordance is misleading. Set `dragHandle = null` on the affected sheets so behavior and affordance stay aligned.

5. **Leave `TempoModalTopSheet` untouched.**

   It uses `Animatable` + `tween` with `coerceAtMost(0f)` clamped drag and no nested-scroll routing, so it cannot overshoot or bounce.

## Risks / Trade-offs

- Swipe-down-to-dismiss is no longer available on these sheets. Accepted: they are form/selection sheets where the scrim, back, and explicit actions cover dismissal, and the editor sheets intentionally guard against accidental dismissal of unsaved changes.
