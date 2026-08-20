## Context

The shared `TempoModalSheet` runs inside a Compose `Dialog` with platform back dismissal disabled so the app can animate and guard sheet dismissal itself. The pre-#229 implementation keeps one `PredictiveBackHandler` registered and uses standard `imePadding()`. That baseline keeps title/description handoffs stable on the physical Pixel 7, and normal back hides Gboard before reaching the sheet callback, but predictive gesture progress can still reach the sheet while the IME is present.

The #229/#393 fixes made handler enablement and focus cleanup react to `WindowInsets.isImeVisible`. IME visibility also changes transiently during editor-to-editor handoffs, so the #393 cleanup can force-clear the destination editor even though no back gesture occurred. AndroidX additionally documents that an `enabled` update can be observed one frame late by a gesture.

## Goals / Non-Goals

**Goals:**

- Decide keyboard-first versus sheet-first behavior once, when a back callback begins.
- Keep a keyboard-first gesture from moving or dismissing the task sheet.
- Clear focus only after a completed gesture that the app explicitly routed to the keyboard.
- Preserve the existing sheet animation, cancellation restore, discard guard, and adaptive placement when the keyboard is not the target.
- Keep ordinary title/description focus handoffs continuously connected to the keyboard.

**Non-Goals:**

- Changing task-editor layout, sizing, fields, validation, or navigation.
- Replacing standard Compose IME padding or creating custom inset animation behavior.
- Changing domain, data, persistence, or task-save behavior.

## Decisions

### Keep one predictive-back handler active

`TempoModalSheetPredictiveBackHandler` will remain composed and enabled for the sheet lifetime. It will read the latest IME visibility when its `onBack` block starts and resolve an immutable route for that gesture.

This avoids handler enable/disable timing races and keeps routing stable even if IME visibility changes while progress is being collected. The alternative of disabling the handler whenever `isImeVisible` is true was rejected because AndroidX documents same-frame stale-enabled delivery and because it couples callback lifecycle to unrelated focus handoffs.

### Route progress and completion from the gesture snapshot

The route has two modes:

- Keyboard route: collect and consume progress without forwarding it to the sheet; on successful completion, force-clear editor focus; on cancellation, do nothing.
- Sheet route: forward progress to the existing sheet transform; on successful completion, invoke guarded dismissal; on cancellation, restore the sheet.

The route is captured before collecting progress and never changes during that gesture. A small pure policy type will expose these effects for deterministic unit coverage.

Clearing focus only on completed keyboard-routed callbacks was chosen over observing a later global IME-hidden transition. A direct keyboard `hide()` call alone was rejected because it leaves the focused editor eligible to request the IME again after dialog/back dispatch settles.

### Return to standard IME padding

The baseline `imePadding()` placement will be restored for bottom and docked sheets. Physical Pixel 7 validation shows continuous keyboard visibility during repeated title/description handoffs in this state. Custom source/target inset padding is a separate layout mechanism and is not needed to implement back routing.

### Keep adaptive behavior identical

The same route policy applies to modal bottom/side sheets and the expanded docked sheet. No breakpoint, dimension, placement, or screenshot reference changes are expected.

## Risks / Trade-offs

- **Risk: IME visibility changes in the same frame that a gesture begins.** → Capture the latest Compose inset state once at callback start and keep the result immutable; validate repeated physical-device gestures.
- **Risk: A keyboard-routed cancellation could leave partial sheet transforms.** → Never forward keyboard-route progress, so there is nothing to restore.
- **Risk: Standard back is consumed by the platform IME before the app callback.** → Preserve that behavior; the route is a fallback for predictive progress that reaches the dialog callback.
- **Risk: Clearing focus changes the focused-field state after an app-consumed predictive dismissal.** → Restrict it to successful keyboard-route completion; no focus mutation occurs during handoffs or canceled gestures.
