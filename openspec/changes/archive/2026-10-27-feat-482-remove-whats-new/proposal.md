# Proposal

## Why

Originating issue: [#482](https://github.com/mandrecode/tempo/issues/482). The one-off "what's new" bottom sheet was a nice experiment, but it is not clearly worth its cost: it interrupts the user with a modal after an update, it needs a registry entry and strings kept in step with every feature (a step in the new-feature checklist), and it has already caused startup glitches (it had to wait for the splash and for onboarding to end). Release notes already reach users through the Play Store listing.

## What Changes

- The `WhatsNewBottomSheet`, `WhatsNewRegistry`, `WhatsNewEntry` and `WhatsNewPreferencesRepository` are removed, with their strings (en, es) and tests.
- `MainViewModel`/`MainUiState` no longer carry a what's-new entry or the app version for it.
- The onboarding-active signal that existed only so the sheet would not show over onboarding (`OnboardingActiveEffect`, `onOnboardingActiveChange`) and the splash hand-over flag are removed.
- AGENTS.md drops step 5 ("What's New") from the New Feature Checklist; README no longer lists it.

### Non-goals

- Play Store release notes (`distribution/whatsnew`, `scripts/generate-whatsnew.sh`) are unrelated to the in-app sheet and are untouched.
- No replacement announcement mechanism. The feature can be reconsidered later with a different design.

## Capabilities

### Removed Capabilities
- `whats-new-onboarding`: the once-per-feature announcement sheet.

## Impact

- UI: `MainActivity`, `Navigation.kt` (signal removed). Core: `MainViewModel`, `MainUiState`, DI binding.
- Data: the `whats_new_preferences` SharedPreferences file is no longer read; it stays on disk, harmless.
