## Why

The app currently follows Material's default surface elevation direction on its task and habit screens, so cards become darker in light mode and lighter in dark mode instead of matching the Didi-inspired visual hierarchy established in Settings. GitHub issue [#50](https://github.com/mandrecode/tempo/issues/50) calls for a consistent, muted screen background with cards that pop in the reverse tonal direction.

## What Changes

- Normalize Tempo's native Material color-scheme surface roles after selecting the Tempo, dynamic, or fallback palette so the reverse hierarchy is part of the app theme.
- Use the canonical `background` role for app pages and continuous top-level chrome.
- Use the canonical low surface-container role for neutral content cards so cards are lighter than the surrounding background in light mode and darker in dark mode.
- Preserve state and accent treatments, including completed-item transparency, category colors, and Material You/custom/default color schemes.
- Keep components on standard Material roles so dialogs, sheets, controls, navigation, and future screens inherit one coherent surface ladder without feature-specific aliases.

## Capabilities

### New Capabilities

- `reverse-surface-hierarchy`: Defines the app-wide tonal relationship between primary screen backgrounds and content cards in light and dark themes.

### Modified Capabilities

None.

## Impact

- Affects the Compose theme's app-wide surface-role mapping plus standard role usage in shared top-level chrome and content cards.
- Adds focused theme/color hierarchy verification without changing domain, data, navigation behavior, persistence, or dependencies.
