## Context

Didi implements the desired hierarchy at the theme boundary: after selecting its brand or dynamic palette, it remaps the native Material page and container roles with `ColorScheme.copy`. Screens and components then consume standard roles such as `background` and `surfaceContainerLow`; they do not depend on Didi-specific page/card aliases. Tempo currently selects among Tempo, dynamic, and fallback schemes but exposes the selected scheme unchanged, which leaves Material's default elevation direction in control.

## Goals / Non-Goals

**Goals:**

- Make the reverse surface hierarchy a first-class property of Tempo's app-wide theme.
- Keep the hierarchy correct for Tempo colors, platform dynamic colors, and Material fallback colors.
- Keep screens and reusable components expressed through standard Material color roles.
- Preserve existing item-state animations and category/accent color overrides.

**Non-Goals:**

- Add feature-specific page/card aliases or theme exceptions.
- Redesign component shape, spacing, typography, or navigation selection states.
- Change brand/accent palette values or add a new setting.
- Change screen structure, navigation, or business behavior.

## Decisions

1. Apply `withPageSurfaceContrast(darkTheme)` to the selected `ColorScheme` inside `TempoTheme`, following Didi's theme architecture. Both modes map `background` to the palette's original `surfaceContainer` and `surface`/`surfaceContainerLow` toward the original `surfaceContainerLowest`. The remaining low-to-high container roles are shifted using original palette values to retain a coherent elevation ladder. Mapping happens after palette selection so Tempo, dynamic, and fallback schemes behave identically without hardcoded colors.
2. Screens and continuous chrome use the canonical `background` role; neutral cards use `surfaceContainerLow`. App-specific accessors were rejected because they would make the core visual language look optional and force every future component to learn a Tempo-only exception.
3. Existing state overlays remain layered on the normalized card role. Completed tasks retain their alpha treatment, while explicitly category-colored habit cards continue to use the category color; only their neutral fallback changes.
4. Verification exercises the normalized role ladder for both light and dark base schemes, including luminance direction and preservation of non-surface palette roles. A dedicated debug-only light/dark surface-ladder preview complements the existing paired screen and card previews, while an instrumented theme integration test verifies the normalized scheme is actually provided through `MaterialTheme`.

## Risks / Trade-offs

- [Risk] Dynamic palettes can have subtler tonal contrast than the bundled schemes. → Derive every normalized role from the selected palette's existing ordered surface roles rather than calculated or hardcoded colors.
- [Risk] Theme-wide remapping changes every component that already uses surface roles. → Preserve the full role ladder instead of replacing all surfaces with one page/card pair, and validate representative light/dark schemes.
- [Risk] Category-colored cards do not necessarily follow the neutral hierarchy. → Preserve them as intentional accent exceptions and change only neutral fallbacks.
