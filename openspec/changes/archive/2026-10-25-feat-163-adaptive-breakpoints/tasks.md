# Tasks

- [x] 1.1 Add `WindowWidthClass`, `windowWidthClass(widthDp)` and `currentWindowWidthClass()` on WindowManager 1.5's constants and `currentWindowAdaptiveInfoV2()`.
- [x] 1.2 Derive `sheetPlacement` / `rememberSheetPlacement` and `isFloatingNavigationRailLayout` from it; remove the hand-coded 1200 dp and its dp maths.
- [x] 1.3 Add `animatedRailClearance` and apply it in `floatingRailContentClearance`, `TasksScreen` and `RoutinesScreen`.
- [x] 1.4 Unit `WindowWidthClassTest`; instrumented `AnimatedRailClearanceTest`.
- [x] 1.4a Handle size, layout and orientation changes in place in `MainActivity` (`configChanges`), so animations survive them; instrumented `MainActivityConfigChangeTest`.
- [x] 1.5 What's New `434-380-115-335-336-440-163` (en, es).
- [x] 1.6 Run `./gradlew ktlintCheck :app:detekt testDebugUnitTest koverVerifyDebug lintDebug validateDebugScreenshotTest` (adaptive sheet references unchanged) and the navigation, tasks, routines, focus and settings instrumented packages on the Pixel 10 AVD.
