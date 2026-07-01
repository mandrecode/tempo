## ADDED Requirements

### Requirement: Didi-style Settings hierarchy
The Settings screen SHALL present settings with a large page title, visible section labels, and rounded grouped cards that mirror the Didi reference design while retaining the existing Settings controls.

#### Scenario: User opens Settings
- **WHEN** the Settings screen is displayed
- **THEN** the screen shows a prominent Settings title followed by sectioned Settings content in rounded cards

#### Scenario: User changes visual settings
- **WHEN** the user selects theme, color scheme, tab visibility, or default tab options
- **THEN** the existing Settings events are emitted and the controls reflect the selected state

### Requirement: Settings rows keep existing actions
The Settings screen SHALL keep notification settings, language settings, review, feedback, and version presentation available through the redesigned rows without changing their current behavior.

#### Scenario: User taps an external settings row
- **WHEN** the user taps notification settings, language settings, review app, or send feedback
- **THEN** the screen launches the same external destination or fallback behavior as before the redesign

### Requirement: Settings redesign remains theme-aware
The Settings screen SHALL use Material theme tokens for card, background, text, icon, and selected-control colors so the redesigned layout adapts to light, dark, dynamic, and Tempo color modes.

#### Scenario: User previews light and dark modes
- **WHEN** Settings content is rendered in light or dark previews
- **THEN** cards, selected controls, icons, and text remain legible without hardcoded composable strings or hardcoded UI colors

### Requirement: Settings exposes onboarding entry point
The Settings screen SHALL include an onboarding entry point in the About section and route its tap through a navigation callback.

#### Scenario: User taps onboarding row
- **WHEN** the user taps the onboarding entry in Settings
- **THEN** the screen invokes the onboarding callback without changing existing Settings state
