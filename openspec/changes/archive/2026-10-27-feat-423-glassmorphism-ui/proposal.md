# Proposal

## Why

Issue [#423](https://github.com/mandrecode/tempo/issues/423) asks for a blur / glassmorphism treatment so the app feels polished before its production release. Today every surface is a flat, opaque tone: the floating bar is a solid pill laid over the list, sheets are solid slabs on a grey scrim, and the page behind it all is one flat colour, so nothing reads as being in front of anything else.

## What Changes

- A **glass material** in the theme (`TempoGlass`), derived from the active colour scheme (Tempo palette or dynamic colour, light and dark), in three tiers:
  - **Ambient backdrop**: each page paints soft glows of its scheme's primary, tertiary and secondary containers instead of a flat `background`. This is what the glass picks up.
  - **Panel**: the rounded content block (Tasks, Routines, Focus, and the docked editor pane) is slightly translucent over that backdrop, with a light rim along its top edge. No blur, because what is behind it is already a soft gradient.
  - **Chrome**: the floating navigation pill, the add/sort/settings controls and the expanded-rail settings button use real backdrop blur of the content beneath them, plus a fill, a sheen and a specular rim. Role colours (the add button's `primaryContainer`, an active sort's `secondaryContainer`) stay as tinted glass.
- **Modal glass**: modal sheets and alert dialogs use the platform's cross-window blur, so the app behind them goes out of focus (easing in with the sheet). While the blur shows, the sheet becomes translucent with a rim and the scrim/dim is lightened.
- Content stays solid: cards, text and inputs keep their opaque colours, so legibility never depends on what scrolls behind them.
- Graceful fallback: below API 31, on low-RAM devices, in previews, or when the system disables window blurs (battery saver, the developer setting), chrome uses a denser, unblurred fill, and modals keep today's opaque surface and full scrim.
- The "what's new" entry announces the new look alongside the other unreleased features.

### Non-goals

- No new library. Haze or similar would need approval under `TECH_STACK.md`; the effect is built on Compose `GraphicsLayer` and `RenderEffect` and the platform window blur.
- No user setting to turn glass off. The system blur toggle and battery saver already turn the modal blur off, and the in-window chrome fallback is the same UI with a denser fill.
- No change to layout, sizes, breakpoints, navigation or content.
- The home-screen widget (Glance) is unchanged; widgets cannot blur.

## Capabilities

### New Capabilities

- `glass-material`: the ambient backdrop, the panel, blurred chrome, modal window blur, and their fallbacks.

## Impact

- New: `core/ui/theme/TempoGlass.kt`; `core/ui/glass/` (`AmbientBackground`, `GlassBackdrop`, `GlassModifiers`, `WindowBlurBehind`).
- Theme: `TempoTheme` provides `LocalTempoGlass` and decides whether blur is supported.
- Navigation: `TempoNavHost` records the content plane (`GlassBackdropSource`) and provides it to the floating chrome; `TempoBottomNavigation`, `NavigationComponents`, `PersistentFloatingBar`, `TaskActionButtons`, `SettingsButton`.
- Screens: Tasks, Routines, Focus, Settings and Onboarding paint the ambient backdrop once at their root; inner layers (Scaffold, `TempoTopBar`, content roots, chip/day headers) are transparent; the content blocks use `glassPanel`.
- Modals: `TempoModalSheet` (blur, translucency, rim, scrim) and the docked sheet; `TempoConfirmDialog`, `TempoMessageDialog`, `PermissionRevokedDialog`, the reminder permission dialogs and `ImportModeDialog` call `DialogWindowGlass()`.
- Lint: `.editorconfig` allowlists `LocalTempoGlass` and `LocalGlassBackdrop`.
- Screenshot references: the Focus content and docked Tasks references gain the ambient backdrop (layoutlib renders the no-blur fallback by design).
- Adaptability: identical behaviour at every width class. The bottom pill (Compact), compact rail (Medium/Expanded) and labelled rail (Large+) all use the same chrome glass, sampling whatever is under them; the docked editor pane (Large+) uses the panel material like the content block beside it.
