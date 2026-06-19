## Context

Current coverage verification excludes `*.mapper.*` and reminder scheduler implementation classes, which hides real logic from Kover metrics. Repository tests for the main data implementations are mostly DAO-mock based, and selected date/time tests depend on live wall-clock values (`Clock.System.now()`), introducing midnight/time-zone flakiness.

## Design decisions

### D1 — Kover filtering by runtime testability

- Remove blanket mapper exclusion and scheduler-impl exclusion from Kover.
- Keep exclusions for Android framework entry points that are not reliably unit-testable in the current stack (receivers, workers, permissions, Compose/tooling/generated classes, DI generated classes).
- If a scheduler path still needs Android-only mechanics (PendingIntent/AlarmManager interaction), isolate those mechanics behind narrow collaborators so scheduler orchestration can be unit-tested and counted by Kover.

### D2 — Repository confidence via real Room wiring

- Add in-memory Room integration tests for the four repository implementations named in #752.
- Use a real `TempoDatabase` + real DAOs + repository implementation (no DAO mocks in these tests).
- Validate key behaviors that require mapper + DAO + SQL interaction together (insert/read/update/delete, ordering/relations, and targeted query methods).

### D3 — Deterministic time fixtures

- Replace direct `Clock.System.now()` usage in `HabitChainUtilTest` and `TaskReminderDateUtilTest` assertions with fixed instants (`2026-06-17T12:00:00Z` in system-default zone conversion where needed).
- Preserve coverage of recurrence logic while making assertions independent from execution time.

### D4 — Relaxed mock tightening

- Keep `relaxed = true` where useful for setup speed, but add explicit verifications on contract-critical calls.
- Ensure rollover-specific behavior remains covered in `ToggleTaskCompletionUseCaseRolloverTest` and non-rollover behavior remains in `ToggleTaskCompletionUseCaseTest`; avoid duplicate intent.

### D5 — Deferred scope

- Robolectric + `androidx.work:work-testing` remains deferred for receiver/worker uplift in a follow-up change.

## Risks and mitigations

- **Risk:** Scheduler refactor changes behavior.  
  **Mitigation:** Keep public repository/scheduler interfaces unchanged and add behavior-focused tests for schedule/cancel flows.
- **Risk:** In-memory repository tests become flaky due to time defaults.  
  **Mitigation:** Use fixed `LocalDateTime` fixtures and explicit ordering assertions.
