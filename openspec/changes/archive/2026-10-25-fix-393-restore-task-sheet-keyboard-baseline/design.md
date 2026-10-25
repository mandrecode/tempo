## Context

PR #388 replaced standard `imePadding()` with a custom stable-inset policy and toggled the sheet's
predictive-back callback with IME visibility. PR #394 then removed that policy but added a
gesture-scoped route that clears focus after keyboard-routed back completion. Production testing
found both approaches worse than the implementation immediately before #388: title/description
focus handoffs can close and reopen the keyboard, making ordinary editing unusable.

The exact pre-change implementation is available at commit `f311e02`. The rollback only concerns
the shared sheet files changed by those PRs; later release commits and unrelated product work remain
intact.

## Goals / Non-Goals

**Goals:**

- Restore the task editor's known pre-#229 keyboard and field-focus behavior exactly.
- Remove the focus-clearing and gesture-routing policy introduced by PR #394.
- Keep existing adaptive sheet placement, predictive sheet motion, and guarded dismissal behavior.
- Record that the dedicated keyboard-dismissal guarantees are withdrawn.

**Non-Goals:**

- Fix issue #393 or the original #229 predictive-back visual glitch.
- Introduce another IME state machine, timing heuristic, or focus policy.
- Change task content, persistence, validation, or adaptive layout.

## Decisions

### Restore the pre-#229 files, not whole merge commits

`TempoModalSheet.kt` will match `f311e02`, and `TempoModalSheetBackRoute.kt` plus its unit test will
be removed. This is preferable to reverting the full merge commits because those commits also
contain already-archived OpenSpec history, while subsequent `main` commits contain unrelated
release metadata that must remain.

### Withdraw the failed behavior contract

The canonical `task-editor-keyboard-dismissal` requirements were created by the unsuccessful fixes.
Archiving this rollback removes those requirements rather than preserving promises that the restored
implementation does not satisfy. Issue #393 remains open as the source for a future, independently
designed solution.

### Keep adaptive behavior unchanged

The baseline uses standard IME padding for both modal and docked presentations. No placement rule,
size-class threshold, or sheet geometry changes, so compact, medium, and expanded layouts retain
their existing structure.

## Risks / Trade-offs

- [The original predictive-back keyboard glitch returns] → This is the accepted rollback trade-off;
  leave #393 open and do not present the rollback as a fix for dismissal.
- [A broad revert could remove unrelated history] → Restore only the three implementation/test paths
  whose behavior differs from `f311e02` and verify the scoped diff against that commit.
- [Automated tests cannot reproduce the physical IME interaction] → Run the static/unit/build gates
  and smoke-test the debug application on the connected Pixel 7 without replacing production.

## Migration Plan

1. Restore the scoped shared-sheet implementation to `f311e02`.
2. Remove the gesture-routing helper and test.
3. Verify code quality, build, and the pre-#229 file equivalence.
4. Install only the debug variant for Pixel 7 smoke testing.
5. Archive this OpenSpec change in the rollback PR.

Rollback of this rollback is a normal revert of the new PR; no data migration is involved.

## Open Questions

None for this rollback. A future #393 proposal must begin from observed device behavior rather than
reusing either rejected state machine.
