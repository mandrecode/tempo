## Why

GitHub issue [#726](https://github.com/mandrecode/tempo/issues/726) reports that the habit-chain live notification can become stale when a chain was started from a reminder notification and the user continues checking habits from inside the app (including yesterday's date). The live notification should keep reflecting progress for that active session.

## What Changes

- Keep updating an already-active habit-chain live activity when the user toggles completion in-app for non-today dates.
- Restrict this behavior to chains that currently have an active live activity session, so unrelated historical edits preserve current behavior.
- Add repository regression tests for active-vs-inactive past-date updates.

Non-goals:

- Do not make all historical in-app edits update live activities.
- Do not change reminder rescheduling rules for habits or chains.
- Do not change notification UI text, actions, channels, or receiver wiring.

## Capabilities

### New Capabilities

- `habit-chain-live-notification-continuation`: Defines how an active habit-chain live activity continues to sync when progress is updated from inside the app.

### Modified Capabilities

- None. No existing OpenSpec specs are present.

## Impact

- `features/routines/data/repository/HabitRepositoryImpl.kt` past-date live activity gating.
- `app/src/test/.../features/routines/data/repository/HabitRepositoryTest.kt` regression coverage.
- No Room schema, migrations, domain model, or navigation changes.
