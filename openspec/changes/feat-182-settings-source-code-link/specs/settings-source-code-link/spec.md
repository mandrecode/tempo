## ADDED Requirements

### Requirement: Source Code Entry In Settings About
The Settings About section SHALL include a source code entry that opens Tempo's public git repository, presented with the same row treatment as the other About entries.

#### Scenario: Source code entry is visible
- **WHEN** the user scrolls to the About section of Settings
- **THEN** a source code entry is shown after the feedback entry, with a localized title, a localized description, and the code icon

#### Scenario: Source code entry opens the repository
- **WHEN** the user taps the source code entry
- **THEN** the app issues a view intent for the configured Tempo repository URL so it opens in the user's browser

#### Scenario: Source code entry has no handler
- **WHEN** the user taps the source code entry and no installed app can handle the repository URL
- **THEN** the app shows the existing "no browser app found" message and remains on the Settings screen

### Requirement: Repository URL Is Build-Configured
The repository URL SHALL be provided as a build configuration value rather than hardcoded in UI code, alongside the existing feedback form URL.

#### Scenario: Repository URL is read from build config
- **WHEN** the source code entry builds its view intent
- **THEN** it uses the repository URL exposed by the app's build configuration

### Requirement: Source Code Entry Is Announced
The source code entry SHALL be announced once through the What's New sheet.

#### Scenario: What's New announces the source code entry
- **WHEN** the What's New sheet is shown for the release containing this change
- **THEN** its single entry describes the new Settings source code link
