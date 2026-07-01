## Why

GitHub issue [#47](https://github.com/mandrecode/tempo/issues/47) asks for the Settings screen to match the Didi reference design. The current Settings surface exposes the right controls, but its visual hierarchy and grouped components do not match the large-title, card-led design direction that will later inform the broader app coloring work in issue #50.

## What Changes

- Redesign Settings around the Didi reference: oversized page title, section labels, rounded cards, and component-centered setting rows.
- Apply the Didi-style light/dark card treatment to Settings without changing persisted settings behavior.
- Keep theme, color scheme, notification, language, tab/navigation, default-tab, feedback, review, and version behavior intact.
- Add or update previews for the redesigned Settings content and new internal presentation components.

## Capabilities

### New Capabilities

- `settings-visual-design`: Covers the visual presentation and interaction affordances of the Settings screen.

### Modified Capabilities

- None.

## Impact

- Affected code: `app/src/main/java/com/mandrecode/tempo/features/settings/presentation/SettingsContent.kt` and matching debug previews.
- Affected resources: Settings strings or drawables only if required by the redesign.
- No domain, data, persistence, navigation route, or dependency changes are planned.
