## Context

Settings already demonstrates the desired hierarchy by using `surfaceContainer` for its scaffold and `surfaceContainerLowest` for grouped cards. Tasks and Routines still rely on default scaffold surfaces and higher container roles for cards. In Material 3 schemes, `surfaceContainerLowest` is the lightest container in a light scheme and the darkest container in a dark scheme, including platform dynamic color schemes, so it directly expresses issue #50 without theme-specific branching.

## Goals / Non-Goals

**Goals:**

- Apply one semantic surface hierarchy to the primary Tasks, Routines, and Settings screens.
- Keep the hierarchy correct for Tempo colors, platform dynamic colors, and Material fallback colors.
- Preserve existing item-state animations and category/accent color overrides.

**Non-Goals:**

- Redesign dialogs, modal sheets, chips, buttons, or navigation selection states.
- Change brand/accent palette values or add a new setting.
- Change screen structure, navigation, or business behavior.

## Decisions

1. Primary screens and matching top-level chrome use `surfaceContainer` as the muted background. This matches the completed Settings design and avoids custom color arithmetic. Using `background` was rejected because its extreme tone produces the opposite hierarchy with lowest containers.
2. Neutral content cards use `surfaceContainerLowest`. Material 3 defines this role at the correct extreme in both brightness modes, so no `LocalIsDarkTheme` branch or hardcoded color is needed. Using `surfaceContainerHighest`/`surfaceContainerHigh` was rejected because those roles reverse the requested relationship.
3. Existing state overlays remain layered on the new card role. Completed tasks retain their alpha treatment, while explicitly category-colored habit cards continue to use the category color; only their neutral fallback changes.
4. Verification covers semantic role selection and both light/dark rendered relationships. Existing previews remain the main visual inspection surface, while focused tests guard against role regressions.

## Risks / Trade-offs

- [Risk] Dynamic palettes can have subtler tonal contrast than the bundled schemes. → Use Material's ordered surface-container roles rather than calculated luminance offsets.
- [Risk] Applying the lowest role to modal surfaces could flatten or confuse elevation. → Limit the change to primary content cards and screen chrome identified in the requirement.
- [Risk] Category-colored cards do not necessarily follow the neutral hierarchy. → Preserve them as intentional accent exceptions and change only neutral fallbacks.
