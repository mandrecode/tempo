## Context

The #229 fix disables `PredictiveBackHandler` while `WindowInsets.isImeVisible` is true so Android can consume the first back action. The supplied Pixel 7 recording for #393 shows a remaining transition race: as soon as the IME reports hidden, the shared sheet re-registers predictive back while the description field still owns focus. The dialog/editor reconnects to the IME and Gboard reappears, so the user must dismiss it repeatedly.

The same shared sheet primitive serves compact and medium modal sheets plus expanded docked panes. Existing stable IME padding already distinguishes a real hide (`imeAnimationTarget` reaches zero) from a keyboard-to-keyboard focus handoff (the target remains non-zero).

## Goals / Non-Goals

**Goals:**

- Make a user-initiated IME dismissal settle after one system back or keyboard-hide action.
- Keep the active task sheet visible and stationary throughout the transition.
- Clear stale text focus only for a completed IME hide, not for field-to-field handoffs.
- Re-enable guarded sheet predictive back only after the dismissal transition is settled.
- Preserve compact, medium, and expanded placement and sizing.

**Non-Goals:**

- Changing title/description editing, validation, persistence, or task submission.
- Replacing the custom sheet or stable IME inset implementation.
- Changing adaptive breakpoints or adding dependencies.
- Changing keyboard behavior outside shared sheet-hosted editors.

## Decisions

### Treat IME dismissal as a short transition state

The shared sheet handler will remember whether the IME was visible. When visibility falls to false, it will keep predictive-back handling disabled until the transition is classified and settled. This avoids re-registering the sheet back callback during the exact frame in which a focused editor is disconnecting from Gboard.

Alternatives considered:

- Continue deriving handler enablement only from current visibility: this is the behavior demonstrated to fail in the supplied recording.
- Add a fixed delay before re-enabling back: timing would vary by IME, animation scale, and device.
- Special-case only the task description field: the race belongs to the shared dialog/back/focus boundary and could affect any sheet editor.

### Clear focus only when the IME animation target confirms a real hide

When the sheet observed a visible IME, current visibility becomes false, and `WindowInsets.imeAnimationTarget` has a zero bottom inset, the shared handler will clear Compose focus with `force = true`. It then marks the transition settled and allows predictive back to register on the next composition. Clearing focus removes the editor/IME connection that causes Gboard to reopen.

A transient zero current inset with a non-zero animation target is a keyboard-to-keyboard handoff, so focus remains untouched and sheet back stays disabled until the IME is visible again. The target inset is read only when visibility changes, avoiding per-frame composition reads.

Alternatives considered:

- Clear focus on every visible-to-hidden observation: this can interrupt title/description focus handoffs that momentarily report a hidden current IME.
- Call only `SoftwareKeyboardController.hide()`: the recording already shows that hiding without releasing the active editor connection permits the IME to reopen.
- Keep focus indefinitely and suppress future keyboard requests: this would require task-field-specific state and could prevent intentional refocus.

### Keep the fix and deterministic policy tests in the shared UI primitive

The transition predicates will remain pure functions next to `stableImeInset`, with unit tests for real dismissal, handoff, and predictive-back enablement. Device validation remains necessary because Compose unit tests cannot reproduce a physical IME/window focus race.

Across compact and medium widths, the modal task sheet uses the corrected shared handler. At expanded widths, the docked task editor calls the same handler and receives identical focus settlement without placement changes.

## Risks / Trade-offs

- [Some IMEs report unusual animation targets] → Keep the previous handler state during non-zero handoffs and validate the zero-target dismissal path on Gboard/Pixel 7 or the repository AVD fallback.
- [Clearing shared sheet focus could affect a non-task sheet after its IME closes] → Limit the action to a completed visible-to-hidden transition; losing text focus after an explicit keyboard dismissal matches the requested interaction and existing Done behavior.
- [Predictive back is briefly disabled after the IME becomes visually hidden] → The transition lasts only until focus is cleared and state is recomposed, preventing the more disruptive callback-registration race.

## Migration Plan

No data or API migration is required. The change is a presentation-only replacement of the transition policy and can be rolled back by reverting the shared handler and its tests.

## Open Questions

None. The supplied recording identifies Gboard on the Pixel 7 as the acceptance path; automated policy tests and device smoke testing cover the implementation boundary.
