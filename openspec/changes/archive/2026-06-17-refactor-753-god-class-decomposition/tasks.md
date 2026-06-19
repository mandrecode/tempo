## 1. Immutable MVI contracts

- [x] 1.1 Convert routines/tasks `UiState` collection fields to immutable persistent types.
- [x] 1.2 Update ViewModel state construction to produce persistent lists/maps.
- [x] 1.3 Update previews/screens affected by immutable type migration.

## 2. Null-safety hardening

- [x] 2.1 Replace presentation `!!` usages in Routines/Tasks screens with safe handling.
- [x] 2.2 Remove unsafe parent-task grouping `!!` in tasks loading pipeline.

## 3. Exception handling hardening

- [x] 3.1 Replace routines `catch (Exception)` blocks with explicit exception branches.
- [x] 3.2 Keep cancellation propagation (`CancellationException`) intact.

## 4. God-file decomposition

- [x] 4.1 Decompose HabitBottomSheet into focused content/section/helper files.
- [x] 4.2 Decompose TaskBottomSheet into focused content/section/helper files.
- [x] 4.3 Decompose HabitCards and TaskCard into focused card helper files.
- [x] 4.4 Decompose TasksViewModel and RoutinesViewModel into focused helper/action files.

## 5. Shared logic extraction

- [x] 5.1 Extract task data loading/grouping into dedicated helper file.
- [x] 5.2 Extract routines actions/load logic into dedicated helper file(s).

## 6. Architecture decision documentation

- [x] 6.1 Document D3 Settings thin-layer decision in `AGENTS.md`.

## 7. Verification

- [x] 7.1 Run `./gradlew ktlintFormat`.
- [x] 7.2 Run `./gradlew ktlintCheck`.
- [x] 7.3 Run `./gradlew :app:detekt`.
- [x] 7.4 Run `./gradlew testDebugUnitTest`.
- [x] 7.5 Run `./gradlew koverVerifyDebug`.
- [x] 7.6 Run `./gradlew :app:compileDebugAndroidTestKotlin`.
- [x] 7.7 Run `openspec validate refactor-753-god-class-decomposition --strict`.
