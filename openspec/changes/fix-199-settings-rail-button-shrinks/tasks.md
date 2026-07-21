## 1. Implementation

- [x] 1.1 In `app/src/main/java/com/mandrecode/tempo/core/ui/navigation/PersistentFloatingBar.kt`, add `Modifier.verticalScroll(rememberScrollState())` to the `Column` in `PersistentLandscapeFloatingBar` so its content scrolls instead of overflowing/clipping when it exceeds the available height.
- [x] 1.2 Verify the required `androidx.compose.foundation.verticalScroll` / `rememberScrollState` imports are added and unused imports are not left behind.

## 2. Verification

- [x] 2.1 Run `./gradlew ktlintFormat` then `./gradlew ktlintCheck` and `./gradlew :app:detekt`.
- [x] 2.2 Run `./gradlew testDebugUnitTest` to confirm no regressions.
- [x] 2.3 Manually verify in a rail-layout window (e.g. resized/landscape tablet emulator or AVD) on the Tasks route with completed tasks present (Sort + Clear-completed both visible): confirm the Settings button renders at full size and is reachable, both when content fits and when the window is short enough that content would previously overflow.
- [x] 2.4 Manually verify no visual regression in the common case (content fits: no scroll indicator, Settings still pinned to the bottom) on a standard tablet/desktop window and in the expanded rail tier.
- [x] 2.5 Run `openspec validate fix-199-settings-rail-button-shrinks` (strict) before considering implementation ready.
