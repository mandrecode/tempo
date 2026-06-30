## Context

Tempo uses a custom `TempoModalBottomSheet` shared by task, habit, and sort flows. It currently handles back with `BackHandler`, which only receives the completed back action and cannot update the sheet during an Android predictive-back gesture. The manifest already enables the platform back callback and the project already depends on `androidx.activity:activity-compose` 1.13.0, which includes `PredictiveBackHandler`.

## Goals / Non-Goals

**Goals:**

- Make custom bottom sheets visually follow predictive-back progress.
- Restore the sheet to its open position when a predictive-back gesture is cancelled.
- Preserve existing dismissal behavior for regular back, scrim tap, and sheets with unsaved changes.

**Non-Goals:**

- Replace custom bottom sheets with Material3 `ModalBottomSheet`.
- Change bottom-sheet layouts, content, sizing, or route ownership.
- Add new dependencies or change domain/data behavior.

## Decisions

- Use `PredictiveBackHandler` in `TempoModalBottomSheet`.
  - Rationale: it is the official Activity Compose API for receiving back gesture progress and completion, and the app already includes the dependency.
  - Alternative considered: keep `BackHandler`; rejected because it cannot animate gesture progress.

- Drive the existing `Animatable` offset from predictive-back progress.
  - Rationale: the sheet already expresses visibility as `offsetY`, so predictive progress can reuse the same source of truth as open and dismiss animations.
  - Alternative considered: add a separate transform or scale effect; rejected because the issue is about restoring sheet dismissal behavior and extra transforms would alter visual design.

- Continue using the existing dismiss gate for dirty forms.
  - Rationale: task and habit sheets rely on `hasUnsavedChanges` to prevent accidental data loss. Predictive back completion must request dismissal through the same guard rather than bypassing it.
  - Alternative considered: disable predictive progress when unsaved changes exist; rejected because users can still preview the back gesture and receive the existing confirmation when they commit.

## Risks / Trade-offs

- Predictive-back APIs are experimental in Activity Compose -> opt in at the component boundary and keep usage narrow.
- A user could cancel a gesture after the sheet has partially moved -> always animate back to the open offset on cancellation.
- If a dirty sheet shows the discard dialog after predictive completion, the sheet itself remains open behind the dialog -> route completion through the same `onRequestDismiss` path used by regular back.
