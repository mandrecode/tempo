# Tasks

- [x] 1.1 Rewrite `DayOfWeekSelector` as a `ButtonGroup` of seven `ToggleButton`s (initials, full-name descriptions, checkbox role, expressive shapes and press widening).
- [x] 1.2 Extract `displayedRepeatDays` / `toggledRepeatDays` with `nullMeansEveryDay`; replace `showAllDaysOption` at the task call site; remove the unused `everyday` string.
- [x] 1.3 Unit `DayOfWeekSelectorTest` and instrumented `DayOfWeekSelectorTest` (including the shared-row target check and the scrolling fallback).
- [x] 1.6 Give the row the editors' full width: below a "Repeat on" label in the habit sheet, and without the icon-column spacer in the task sheet.
- [x] 1.7 After review, put the row back beside the icon column in both editors, with the days scrolling at full size when they would be narrower than 40 dp.
- [x] 1.4 What's New `434-380-115` (en, es).
- [x] 1.5 Run `./gradlew ktlintCheck :app:detekt testDebugUnitTest lintDebug` and `openspec validate --strict`; check by hand in the habit sheet on the Pixel 10 AVD.
