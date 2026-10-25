# Tasks

- [x] 1.1 Rewrite `DayFilterRow` as a connected, single-select `ButtonGroup` of `ToggleButton`s (short weekday and date, today dot, relative names and radio-button role for accessibility, haptics kept).
- [x] 1.2 Remove `DayFilterChip` and its previews.
- [x] 1.3 Add instrumented `DayFilterRowTest` (including a 2× font-scale check that the date stays inside its segment).
- [x] 1.4 What's New `434-380-115-335` (en, es).
- [x] 1.5 Run `./gradlew ktlintCheck :app:detekt testDebugUnitTest koverVerifyDebug lintDebug` and `openspec validate --strict`; check by hand on the Pixel 10 AVD.
