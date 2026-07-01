## Why

GitHub issue [#47](https://github.com/mandrecode/tempo/issues/47) asks for the Settings screen to match the Didi reference design. The current Settings surface exposes the right controls, but its visual hierarchy and grouped components do not match the large-title, card-led design direction that will later inform the broader app coloring work in issue #50.

## What Changes

- Redesign Settings around the Didi reference: oversized page title, section labels, rounded cards, and component-centered setting rows.
- Apply Didi's page-surface contrast theme treatment so the muted surface becomes the screen background and the whitest/lightest surface becomes card background.
- Add the Settings onboarding row and callback flow so the future onboarding destination can be enabled without redesigning Settings again.
- Keep theme, color scheme, notification, language, tab/navigation, default-tab, feedback, review, and version behavior intact.
- Add or update previews for the redesigned Settings content and new internal presentation components.

## Capabilities

### New Capabilities

- `settings-visual-design`: Covers the visual presentation and interaction affordances of the Settings screen.

### Modified Capabilities

- None.

## Impact

- Affected code: Settings presentation, shared theme surface mapping, and matching debug previews.
- Affected resources: Settings strings or drawables only if required by the redesign.
- Affected dependencies: pin Compose Material3 to the same version Didi uses so `TwoRowsTopAppBar` is available.
- No domain, data, persistence, or navigation route changes are planned.
