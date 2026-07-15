## 1. Implementation

- [x] 1.1 Add BOM-managed `androidx.compose.material3.adaptive:adaptive` to the version catalog and `:app`, and note it in `docs/agents/TECH_STACK.md`.
- [x] 1.2 Reimplement `isFloatingNavigationRailLayout()` with `currentWindowAdaptiveInfo().windowSizeClass` and the medium width breakpoint.
- [x] 1.3 Derive rail metrics from a single source of truth (start padding, surface width, content clearance) and align `PersistentLandscapeFloatingBar`, `LandscapeBottomRail`, and the shell clearance to it.
- [x] 1.4 Grow `floatingRailContentPadding` into `adaptiveScreenContentLayout` (rail clearance + centered 840dp readable width) and apply it on Routines, Tasks, and Settings scaffolds.
- [x] 1.5 Apply horizontal safe-drawing insets at the `TempoNavHost` root.
- [x] 1.6 Covered by 1.4 (readable width applied per screen).
- [x] 1.7 Size `TempoModalSheet` from `LocalWindowInfo.containerSize` and cap sheet width at 640dp.
- [x] 1.8 Add `PreviewFormFactors` (phone, foldable, tablet, desktop) in `src/debug` and apply it to Routines/Tasks content previews.

## 2. Verification

- [x] 2.1 Unit-test the rail clearance derivation alongside the existing floating-navigation padding tests.
- [x] 2.2 Run `./gradlew testDebugUnitTest`.
- [x] 2.3 Run `./gradlew ktlintFormat`, `./gradlew ktlintCheck`, and `./gradlew :app:detekt`.
- [ ] 2.4 Run `openspec validate fix-142-adaptive-layouts` (CLI unavailable on this machine at proposal time — run when available).
- [x] 2.5 Smoke-verify on the Pixel 10 AVD in landscape: Routines, Tasks, Settings, task sheet, and portrait regression.
