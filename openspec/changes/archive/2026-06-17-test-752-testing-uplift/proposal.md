## Why

GitHub issue [#752](https://github.com/mandrecode/tempo/issues/752) (Phase 4 of [#461](https://github.com/mandrecode/tempo/issues/461)) identified that the current Kover filter excludes unit-testable logic (notably mappers and scheduler implementations), while several repository and time-based test paths remain fragile or indirect. Coverage gates currently pass, but they do not fully represent test confidence for production logic.

## What Changes

- Narrow Kover excludes in `app/build.gradle.kts` so pure Kotlin/pure logic code is measured (mappers and testable scheduler/preference implementation paths) while keeping Android entry points (receivers/workers/permissions/framework-only surfaces) excluded.
- Strengthen scheduler and preference repository tests to verify observable behavior and exact interactions (not fixture-only assertions).
- Add in-memory Room integration tests for repository implementations (`HabitRepositoryImpl`, `HabitChainRepositoryImpl`, `TaskRepositoryImpl`, `CategoryRepositoryImpl`) to validate mapper + DAO + repository wiring without DAO mocks.
- De-flake time-based tests by using deterministic fixed-time fixtures for `HabitChainUtilTest` and `TaskReminderDateUtilTest`.
- Tighten broad relaxed-mock usage in targeted tests by adding explicit `verify`/`coVerify(exactly = 1)` interaction assertions where behavior contracts matter.
- Keep Robolectric/work-testing uplift (D5) deferred as future work.

Non-goals:

- Do not add receiver/worker unit tests in this phase.
- Do not change product behavior for reminders/tasks/habits beyond testability refactors.

## Capabilities

### New Capabilities

- `testing-coverage-uplift`: Defines which code paths must be included in Kover verification and what deterministic/reliable tests are required for repository, scheduler, preference, mapper, and time-based logic.

### Modified Capabilities

- None.

## Impact

- `app/build.gradle.kts` (Kover filter narrowing).
- `app/src/main/.../infrastructure/reminders/scheduler/*` (testability seams if required).
- `app/src/test/...` mapper/scheduler/preferences/time-based tests.
- `app/src/androidTest/...` repository in-memory Room integration coverage.
- OpenSpec artifacts for this change.
