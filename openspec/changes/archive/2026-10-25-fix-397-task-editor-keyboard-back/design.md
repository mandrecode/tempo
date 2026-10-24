# Design

## Context

`TempoModalSheet` and `TempoDockedSheet` register one continuously composed
`PredictiveBackHandler` that animates the sheet and routes to the guarded dismissal path. It is
registered before the sheet's content composes. AndroidX dispatches back to the most recently added
enabled callback, so a handler composed inside the content takes priority while enabled and falls
through to the sheet handler while disabled. See proposal.md - Why.

## Goals / Non-Goals

**Goals:** route back from explicit field focus; keep the shared sheet byte-for-byte unchanged.

**Non-Goals:** reacting to IME insets; altering other sheet consumers.

## Decisions

- **Focus is tracked per field, not as one enum mutated in callback order.** Title and description
  each report `onFocusChanged` into a small `TaskEditorFocusState`; the focused field is derived
  (title, description, or none). During a title → description handoff both callbacks fire in the
  same frame; deriving from two flags avoids a transient "none" overwriting the destination.
  Alternative: a single enum updated by each callback — rejected because the loser's
  `isFocused = false` can land after the winner's `true`.
- **Inner `PredictiveBackHandler(enabled = focusedField != None)` inside the task content.** It is
  always composed (so its registration order stays behind the sheet's handler) and only toggles
  `enabled`. Progress is collected and ignored; completion calls `focusManager.clearFocus()` once;
  cancellation rethrows without side effects. Alternative: `BackHandler` — rejected because a
  non-predictive callback would let the system play the default back preview.
- **Pre-Android 13 back key is taken before the keyboard.** Without predictive back the keyboard
  consumes the first back key and hides while the field keeps focus, which would cost an extra back
  press. On API < 33 the task fields intercept the back key before the soft keyboard and clear focus
  themselves. API 33+ is left to the predictive handler so a 3-button back key is never handled twice.
- **No change to the shared sheet.** Habit, category, and sort sheets therefore keep baseline
  behavior.

## Risks / Trade-offs

- [The IME window may itself consume the back gesture before the app sees it] → the editor handler
  then never runs, which matches baseline; when it does run, clearing focus is idempotent.
- [Registration order could invert if the sheet handler were recreated after the content] → both
  handlers are composed unconditionally for the sheet's lifetime; verified by instrumented test and
  manual Pixel 7 validation.

## Migration Plan

Pure UI change; rollback is reverting the PR.
