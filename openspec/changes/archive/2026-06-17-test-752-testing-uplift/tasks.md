## 1. Coverage filter narrowing

- [x] 1.1 Remove mapper and scheduler-impl blanket exclusions from `app/build.gradle.kts`.
- [x] 1.2 Keep Android-entry-point exclusions (receivers/workers/permissions/framework-only paths).

## 2. Unit-test uplift

- [x] 2.1 Ensure mapper tests for Habit, HabitChain, Task, and Category assert field-level mapping behavior.
- [x] 2.2 Replace no-op `HabitReminderSchedulerTest` with behavior verification against scheduler collaborators.
- [x] 2.3 Add/strengthen scheduler implementation tests for both habit and task reminder schedulers.
- [x] 2.4 Tighten preference repository tests with explicit interaction verification and Turbine for flow-backed APIs.

## 3. Repository integration tests

- [x] 3.1 Add in-memory Room integration coverage for `HabitRepositoryImpl`.
- [x] 3.2 Add in-memory Room integration coverage for `HabitChainRepositoryImpl`.
- [x] 3.3 Add in-memory Room integration coverage for `TaskRepositoryImpl`.
- [x] 3.4 Add in-memory Room integration coverage for `CategoryRepositoryImpl`.

## 4. Time-based de-flaking

- [x] 4.1 Make `HabitChainUtilTest` deterministic with fixed time fixtures.
- [x] 4.2 Make `TaskReminderDateUtilTest` deterministic with fixed-time `now` in assertions.

## 5. Validation and OpenSpec completion

- [x] 5.1 Run `openspec validate test-752-testing-uplift`.
- [x] 5.2 Run `./gradlew ktlintFormat`.
- [x] 5.3 Run `./gradlew ktlintCheck`.
- [x] 5.4 Run `./gradlew :app:detekt`.
- [x] 5.5 Run `./gradlew testDebugUnitTest`.
- [x] 5.6 Run `./gradlew koverVerifyDebug`.
