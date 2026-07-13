## Why

The neutral cards introduced by the recent reverse-surface restyle contrast too strongly with their page backgrounds, making the cards visually dominate the content. The card tone should move one step toward the page tone while preserving the intended lighter-in-light and darker-in-dark hierarchy.

## What Changes

- Use the adjacent normalized surface-container role for neutral task, habit, habit-chain, quit-habit, and Settings section cards.
- Preserve the reverse tonal direction in light and dark themes while reducing card-to-page contrast by one surface step.
- Preserve completed-task transparency, animations, and explicit category/accent card colors.
- Keep page backgrounds, theme palette remapping, shapes, spacing, and typography unchanged.

## Capabilities

### New Capabilities

- `neutral-card-surface-contrast`: Defines the reduced-contrast neutral card role and its behavior across supported themes and card states.

### Modified Capabilities

None.

## Impact

The change affects only Compose container-color selection for neutral cards in Tasks, Routines, and Settings, plus focused UI/theme tests and previews. It introduces no API, dependency, persistence, domain, data, or navigation changes.
