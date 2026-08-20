## Context

Tempo's custom modal sheet observes predictive-back progress and maps it directly to the sheet offset. The recordings for issue #229 show the sheet receiving predictive-back progress during the system back action used to hide Gboard. That progress translates a short new-task sheet fully off-screen before it restores. The taller edit sheet does not expose the same rebound in the supplied working path.

Compose already exposes IME visibility through `WindowInsets`, while the shared sheet owns all modal and docked predictive-back registration. The fix can therefore remain in the presentation primitive without adding task-specific state or changing editor behavior.

## Goals / Non-Goals

**Goals:**

- Let the IME consume back while it is visible without moving or dismissing the sheet.
- Restore sheet predictive-back dismissal as soon as the IME is closed.
- Apply the policy consistently to bottom, top, side, and docked sheets.
- Preserve adaptive placement: compact and medium modal sheets use the fix; expanded docked panes retain their layout and guarded-dismiss behavior.

**Non-Goals:**

- Changing task form content, sizing, scrolling, focus order, validation, or persistence.
- Replacing the custom sheet implementation or changing its gesture threshold and animation tokens.
- Adding a dependency or changing window-size breakpoints.

## Decisions

### Disable sheet predictive-back handling while the IME is visible

`TempoModalSheetPredictiveBackHandler` will register `PredictiveBackHandler` with `enabled = false` whenever `WindowInsets.isImeVisible` is true. This preserves Android back priority: the first back hides the focused editor's keyboard, and a subsequent back dismisses or guards the sheet.

This belongs in the shared handler rather than `TaskBottomSheetContent` because the conflict is between two system/back consumers, not task state. It also prevents the same fault in other editors using the shared primitive.

Alternatives considered:

- Resetting the offset when IME visibility changes: this would fight the back animation after it starts and could flash for one or more frames.
- Special-casing new tasks or multiline descriptions: the recordings make the short creation sheet easier to observe, but the invalid interaction is generic to sheet back handling.

### Keep IME padding inside the bottom-sheet surface

The bottom sheet retains internal IME padding, keeping its outer bottom edge anchored while focus moves between fields with different IME configurations. The effective inset uses the current animated value plus the overlap of the animation source and target. That overlap remains non-zero during a keyboard-to-keyboard focus handoff, but does not interfere with the normal show or hide animation.

Alternatives considered:

- Applying IME padding to the host: this prevents the keyboard from covering the footer but makes the whole sheet follow transient IME inset changes during field focus handoff.
- Using only the current IME inset: Gboard can briefly report zero while switching editor configurations, shrinking the sheet for a frame even though the keyboard remains open.
- Removing IME padding and relying on dialog resizing: the edge-to-edge dialog explicitly owns its insets, so this risks the keyboard covering the form footer.

### Cover the back-routing policy and verify the real animation on a device

A focused unit test will lock the IME/back-handler enablement policy. Build, static-analysis, and screenshot gates will protect the shared primitive, and a Pixel 10 AVD smoke test will repeat the supplied create-task sequence with a multiline description and screen recording.

## Risks / Trade-offs

- [IME visibility is exposed as Compose inset state] → Read the platform-provided visibility only for back routing and retain the established surface-local inset layout.
- [Shared behavior changes for every sheet] → The policy matches expected Android back ordering, and unit/device checks cover both keyboard hiding and subsequent sheet dismissal.
- [An automated Compose test cannot reliably assert a transient physical-keyboard frame] → Retain a deterministic policy test and verify the animation with the repository's prescribed AVD smoke-test path.
