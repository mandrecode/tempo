# Proposal

## Why

Originating issue: [#476](https://github.com/mandrecode/tempo/issues/476), a spike on removing the tab options (which tab is the default, and enabling or disabling tabs). The three tabs are the app, and the options cost more than they give: a Settings section and an onboarding page to explain them, invariants kept in four places (Settings, Onboarding, Backup restore and the repository), a single-tab layout of the floating bar that exists only for that case, and a default-tab migration. Reopening on the tab the user left is the behaviour people expect anyway.

## What Changes

- All three tabs are always shown; the Settings "Tabs & Navigation" and "Default tab" sections are removed.
- The onboarding "set up your first session" page (the tab choices) is removed: onboarding has 5 pages, and finishing opens on Focus.
- After onboarding the app reopens on the tab last used (the saved last route, written on every tab change and notification open), and on Focus when there is none.
- `TabPreferencesPolicy`, the enabled/default-tab preference keys and their migration, and the single-tab floating-bar layout (with the compact solo action and the scroll-from-top plumbing that only fed it) are removed.
- Backup: `routinesTabEnabled`, `tasksTabEnabled`, `enabledTabs` and `defaultTab` are no longer written; files that carry them still import (the fields are ignored).

### Non-goals

- No change to tab order, the reselect behaviour, or what a tab shows.
- No in-app notice: users who had hidden a tab will see it again after updating; this is covered in the release notes.

## Capabilities

### Modified Capabilities
- `tab-order`: all tabs always shown; the default-tab fallback is replaced by reopening on the last-used tab.

### Removed Capabilities
- `tab-preferences`: enabling or disabling tabs and choosing a default tab.

## Impact

- Domain/data: `TabPreferencesPolicy` removed; `NavigationPreferencesRepository` keeps only the last route; backup DTO/mapper/data source drop the tab fields.
- UI: Settings, onboarding, `TempoBottomNavigation`, `PersistentFloatingBar`, `Routines`/`TasksScreen` (solo action removed), `MainActivity` (start destination).
- Compatibility: an older build importing a new backup falls back to its own default tab (Routines) because the field is absent.
