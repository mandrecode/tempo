# Tasks

## 1. Habits

- [x] 1.1 `HabitItem`: `checkboxSize` parameter (default 40dp), icon and radius derived from it, 12/8dp padding; `HabitCard` corner radius 28/18dp.

## 2. Chains

- [x] 2.1 `HabitChainCard`: always list habits (32dp checkbox, 4dp vertical padding); compact header with title, count pill, add button, one-line description and thin progress bar; smaller background icon.
- [x] 2.2 Remove `expandedChainIds`, `ToggleChainExpanded`, auto-collapse on completion and the `expand_chain`/`collapse_chain` strings.

## 3. Tests and verification

- [x] 3.1 `CompactHabitCardsTest` (instrumented): habit card at most 56dp tall; chain lists its habits without expanding. Update `RoutinesViewModelTest`, `RoutinesContentTest`, `RoutineCardModifierTest`.
- [x] 3.2 Run `./gradlew ktlintCheck :app:detekt testDebugUnitTest koverVerifyDebug lintDebug` and `openspec validate --strict`.
- [x] 3.3 Run the Routines card and content instrumented tests on an emulator and check the Routines screen visually.
