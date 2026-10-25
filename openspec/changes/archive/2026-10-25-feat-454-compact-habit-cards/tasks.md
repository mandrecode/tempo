# Tasks

## 1. Habits

- [x] 1.1 `HabitItem`: `checkboxSize` parameter (default 48dp), icon and radius derived from it, 16/12dp padding; `HabitCard` corner radius 32/24dp; `isInsideChain` removed so chain habits render identically.

## 2. Chains

- [x] 2.1 `HabitChainCard`: always list habits (same row as a standalone habit); compact header with title, count pill, add button, one-line description and thin progress bar; smaller background icon.
- [x] 2.2 Remove `expandedChainIds`, `ToggleChainExpanded`, auto-collapse on completion and the `expand_chain`/`collapse_chain` strings.

## 3. Tests and verification

- [x] 3.1 `CompactHabitCardsTest` (instrumented): habit card at most 72dp tall; chain lists its habits without expanding; a habit inside a chain is as tall as a standalone one. Update `RoutinesViewModelTest`, `RoutinesContentTest`, `RoutineCardModifierTest`.
- [x] 3.2 Run `./gradlew ktlintCheck :app:detekt testDebugUnitTest koverVerifyDebug lintDebug` and `openspec validate --strict`.
- [x] 3.3 Run the Routines card and content instrumented tests on an emulator and check the Routines screen visually.
