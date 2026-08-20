## Context

The reverse-surface restyle normalized the app's page background to `background` and neutral cards to `surfaceContainerLow`. After normalization, `surfaceContainerLow` represents the most contrasting card-side tone in both light and dark themes. Task, habit, habit-chain, quit-habit, and Settings section cards therefore stand farther from the page than desired.

## Goals / Non-Goals

**Goals:**

- Reduce neutral card-to-page contrast by exactly one normalized surface role.
- Keep the reverse hierarchy: neutral cards remain lighter than the page in light mode and darker in dark mode.
- Apply the same semantic role consistently to all neutral content cards touched by the prior restyle.
- Preserve state animations, completed-task alpha, and explicit category/accent overrides.

**Non-Goals:**

- Change the theme's surface-role normalization or page background.
- Restyle buttons, sheets, dialogs, navigation, chips, or category-colored cards.
- Change shapes, elevation, spacing, typography, or app architecture.

## Decisions

1. Neutral cards will use normalized `MaterialTheme.colorScheme.surfaceContainer` instead of `surfaceContainerLow`. Within the remapped ladder this is the adjacent role toward `background`, so it reduces contrast without introducing hardcoded colors or a Tempo-specific alias. Changing alpha or blending colors was rejected because it would vary contrast against dynamic palettes and weaken semantic role usage.
2. The token replacement will be limited to the five neutral-card paths changed by the prior color restyle: task, habit, habit-chain, quit-habit, and Settings section cards. Other components using `surfaceContainerLow`, such as the Settings button, retain their independent visual semantics.
3. Existing focused surface tests will be extended to assert that the adjacent normalized role remains on the intended side of the page tone in light and dark schemes. Existing previews provide visual coverage without adding new preview-only component structure.

## Risks / Trade-offs

- [Risk] Some dynamic palettes have very subtle differences between adjacent surface roles. → Use the palette's own adjacent semantic role so the platform remains responsible for a coherent tonal ladder.
- [Risk] A broad search-and-replace could unintentionally flatten non-card components. → Change only the known neutral-card call sites and assert the expected role in focused tests.
- [Risk] Reduced contrast could make card boundaries too subtle on a palette. → Preserve existing shapes and content colors, and visually inspect light and dark previews or the running app in addition to automated role assertions.
