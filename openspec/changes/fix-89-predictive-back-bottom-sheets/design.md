## Context

Tempo uses a custom `TempoModalBottomSheet` shared by task, habit, and sort flows. It currently handles back with `BackHandler`, which only receives the completed back action and cannot update the sheet during an Android predictive-back gesture. The manifest already enables the platform back callback and the project already depends on `androidx.activity:activity-compose` 1.13.0, which includes `PredictiveBackHandler`.

## Goals / Non-Goals

**Goals:**

- Make custom bottom sheets visually follow predictive-back progress.
- Restore the sheet to its open position when a predictive-back gesture is cancelled.
- Preserve existing dismissal behavior for regular back, scrim tap, and sheets with unsaved changes.
- Preserve the existing keyboard/sheet synchronization provided by the custom bottom-sheet
  implementation.
- Fix the bottom-sheet upward-drag edge case by allowing only downward drag from the resting
  position.

**Non-Goals:**

- Replace custom bottom sheets with Material3 `ModalBottomSheet`.
- Change bottom-sheet layouts, content, sizing, or route ownership.
- Add new dependencies or change domain/data behavior.

## Decisions

- Use `PredictiveBackHandler` in `TempoModalBottomSheet`.
  - Rationale: it is the official Activity Compose API for receiving back gesture progress and completion, and the app already includes the dependency.
  - Alternative considered: keep `BackHandler`; rejected because it cannot animate gesture progress.
  - Implementation note: register it inside the custom `Dialog` content and disable the
    dialog's default back dismissal so the dialog back dispatcher does not consume the event
    before the sheet handler can animate progress.

- Drive the existing `Animatable` offset from predictive-back progress.
  - Rationale: the sheet already expresses visibility as `offsetY`, so predictive progress can reuse the same source of truth as open and dismiss animations.
  - Alternative considered: add a separate transform or scale effect; rejected because the issue is about restoring sheet dismissal behavior and extra transforms would alter visual design.

- Continue using the existing dismiss gate for dirty forms.
  - Rationale: task and habit sheets rely on `hasUnsavedChanges` to prevent accidental data loss. Predictive back completion must request dismissal through the same guard rather than bypassing it.
  - Alternative considered: disable predictive progress when unsaved changes exist; rejected because users can still preview the back gesture and receive the existing confirmation when they commit.

- Keep `TempoModalBottomSheet` custom rather than wrapping Material3 `ModalBottomSheet`.
  - Rationale: the Material3 wrapper correctly owns several platform behaviors, but its dialog
    applies IME handling at the root level, which causes the keyboard to displace the sheet after
    the sheet animation starts. Tempo's custom sheet applies IME padding to the moving sheet
    surface, keeping both animations visually synchronized.
  - Alternative considered: tune the Material3 wrapper with disabled gestures, no handle, and
    adjusted focus timing; rejected because it still produced a visible keyboard jump on Pixel 7.

- Show the bottom-sheet drag handle and support constrained drag.
  - Rationale: a visible handle should correspond to a real interaction. Bottom sheets only drag
    downward from rest and clamp upward movement at the open position, mirroring top sheets'
    one-way dismissal behavior in the opposite direction.

- Share the custom modal-sheet primitive between top and bottom sheets.
  - Rationale: `TempoModalTopSheet` and `TempoModalBottomSheet` own the same dialog, scrim,
    dismissal guard, drag threshold, and back behavior. Keeping one internal implementation with
    opposite directions reduces custom-maintenance risk.

- Mirror Material3's predictive-back transform shape.
  - Rationale: Material3 eases back progress and applies subtle sheet scaling during predictive
    back. Tempo should keep that visual language while preserving custom IME placement.

- Let vertical drag start from anywhere on the sheet.
  - Rationale: the handle communicates drag affordance, but users naturally pull on the visible
    sheet surface. The sheet steals vertical gestures only after touch slop so normal taps still
    reach child controls.

- Cap bottom-sheet maximum height below the top system area.
  - Rationale: large habit chains can make the bottom sheet fill the display and place the handle
    against the status/top-bar region. Reserving top air keeps the sheet visually modal without
    pushing content padding into normal-height sheets.

## Risks / Trade-offs

- Predictive-back APIs are experimental in Activity Compose -> opt in at the component boundary and keep usage narrow.
- A user could cancel a gesture after the sheet has partially moved -> always animate back to the open offset on cancellation.
- If a dirty sheet shows the discard dialog after predictive completion, the sheet itself remains open behind the dialog -> route completion through the same `onRequestDismiss` path used by regular back.
- Owning custom sheet behavior means Tempo must keep parity-sensitive details such as dialog
  window flags, scrim handling, IME placement, drag thresholds, and back handling covered by
  focused manual checks.
