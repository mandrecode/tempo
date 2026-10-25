# Proposal

## Why

Originating issue: [#440](https://github.com/mandrecode/tempo/issues/440) asks to move the Focus tab to the centre of the navigation bar. Focus is the app's home surface (`TempoTab.DEFAULT`) but sits at the leading end. The centre is the most prominent position and the easiest to reach with either thumb.

## What Changes

- Tab order becomes **Routines · Focus · Tasks** (was Focus · Routines · Tasks). `TempoTab`'s declaration order is the display order everywhere: the portrait bar, both rails, the Settings tab toggles and the onboarding tab choices, so they all follow.
- When the stored default tab has been disabled, the fallback now prefers the home tab (Focus) when it is enabled, then the first enabled tab in display order. Without this, the reorder would have silently changed the fallback from Focus to Routines.
- Nothing stored changes: tabs persist by `preferenceValue` / name, never by position.
- What's New: the entry becomes `434-380-115-335-336-440` (en, es).

### Non-goals

- No change to which tab the app opens on, to single-tab mode, or to the session chip (which still takes the Focus slot while a session runs).

## Capabilities

### New Capabilities
- `tab-order`: the order of the top-level tabs and the default-tab fallback.

### Modified Capabilities

None.

## Impact

- Domain: `TempoTab` (order), `TabPreferencesPolicy.resolveDefaultTab` (home-first fallback).
- Tests: `TabPreferencesPolicyTest` (home-first fallback, display-order fallback without home, order pinned).
- Strings: What's New (en, es).
