# Tasks

- [x] 1.1 Glass material tokens (`TempoGlass`) derived from the colour scheme, provided by `TempoTheme` with blur support detection (API 31+, not edit mode, not low-RAM).
- [x] 1.2 `Modifier.ambientBackground()`; paint it once at the Tasks, Routines, Focus, Settings and Onboarding roots and make the layers inside transparent (Scaffold, `TempoTopBar`, content roots, chip and day headers).
- [x] 1.3 `Modifier.glassPanel()` for the content blocks and the docked editor pane.
- [x] 1.4 `GlassBackdropSource` / `ProvideGlassBackdrop` / `Modifier.glass()`; record the content plane in `TempoNavHost` and apply glass to the pill, add, sort and settings controls.
- [x] 1.5 Fix the blur showing stale content: a fresh blur layer per draw, plus a pre-draw invalidation that adds no frames (0 frames in 4 s idle, `gfxinfo`).
- [x] 1.6 Cross-window blur for modal sheets (eased, follows the system toggle; translucent sheet, rim, lighter scrim) and `DialogWindowGlass()` for alert dialogs.
- [x] 1.7 Extend the "what's new" entry (en, es) with the new look.
- [x] 1.8 Screenshot hosts paint the ambient backdrop; regenerate and review the references.
- [x] 1.9 Check on the API 31 emulator in light and dark: Focus, Tasks (list scrolling under the bar), task editor sheet, What's New sheet, Settings (collapsed bar over scrolled content).
- [x] 1.10 Run `./gradlew ktlintCheck :app:detekt testDebugUnitTest validateDebugScreenshotTest lintDebug`.
