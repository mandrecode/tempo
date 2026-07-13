## Why

The app currently follows Material's default surface elevation direction on its task and habit screens, so cards become darker in light mode and lighter in dark mode instead of matching the Didi-inspired visual hierarchy established in Settings. GitHub issue [#50](https://github.com/mandrecode/tempo/issues/50) calls for a consistent, muted screen background with cards that pop in the reverse tonal direction.

## What Changes

- Use a muted surface-container role for primary app screen backgrounds and their top-level chrome.
- Use the lowest surface-container role for task, habit, and settings cards so cards are lighter than the surrounding background in light mode and darker in dark mode.
- Preserve state and accent treatments, including completed-item transparency, category colors, and Material You/custom/default color schemes.
- Keep dialogs, sheets, controls, and navigation emphasis on their existing semantic roles unless they form part of the primary screen background or card hierarchy.

## Capabilities

### New Capabilities

- `reverse-surface-hierarchy`: Defines the app-wide tonal relationship between primary screen backgrounds and content cards in light and dark themes.

### Modified Capabilities

None.

## Impact

- Affects Compose theme-role usage in shared top-level chrome and the Tasks, Routines, and Settings presentation components.
- Adds focused theme/color hierarchy verification without changing domain, data, navigation behavior, persistence, or dependencies.
