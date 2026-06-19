## 1. Habit history layout stabilization

- [x] 1.1 Update `HabitHistoryView.kt` so completion toggles in full-capacity rows keep a stable horizontal allocation between dots and streak label.
- [x] 1.2 Preserve #681 streak-pill right-edge alignment and existing semantics/accessibility behavior.

## 2. Preview regression coverage

- [x] 2.1 Update `HabitHistoryViewPreviews.kt` with explicit full-row before/after toggle previews in bottom-sheet row context.
- [x] 2.2 Update `HabitBottomSheetPreviews.kt` if needed so related previews compile and reflect the fixed layout behavior.

## 3. Automated regression coverage

- [x] 3.1 Add/adjust `HabitHistoryViewTest.kt` for constrained-width full-capacity transition behavior (no truncation/layout flip on toggle).

## 4. Verification

- [x] 4.1 Run `./gradlew ktlintFormat`.
- [x] 4.2 Run `./gradlew :app:compileDebugKotlin`.
- [x] 4.3 Run relevant tests for touched code (including `HabitHistoryViewTest` path coverage).
- [x] 4.4 Run `openspec validate fix-687-history-dot-displacement`.
