## Why

GitHub issue [#75](https://github.com/mandrecode/tempo/issues/75) asks Tempo to adopt the more expressive Settings icon treatment from Didi. Tempo's current Settings affordance is a plain icon button, so it lacks the shaped surface, border, and press-responsive color animation that make the entry point feel polished and consistent with the newer interaction style.

## What Changes

- Replace the Settings action in Tempo route top bars with an expressive settings icon button modeled after Didi's implementation.
- Animate the Settings button shape when pressed.
- Animate the Settings button container, content, and border colors when pressed.
- Preserve the existing Settings navigation behavior and accessibility label.
- Non-goal: redesign the Settings screen content or change Settings persistence/domain behavior.

## Capabilities

### New Capabilities
- `settings-entry-affordance`: Covers the visible Settings entry point styling and press feedback used to open Settings.

### Modified Capabilities
- None.

## Impact

- Affected code: Compose UI/navigation components for the Settings entry point and any supporting reusable UI utilities.
- APIs: No external API changes.
- Dependencies: No new dependencies expected.
- Verification: focused unit/static checks where applicable, plus Compose/UI test coverage if an accessible behavior assertion changes.
