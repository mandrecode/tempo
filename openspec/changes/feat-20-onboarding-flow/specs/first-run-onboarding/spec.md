## ADDED Requirements

### Requirement: First-run onboarding gate
The app SHALL show onboarding before the normal Tasks or Routines destination until the user finishes or skips the flow, and SHALL persist completion across later launches.

#### Scenario: First launch
- **WHEN** the app starts without a persisted onboarding completion marker
- **THEN** the onboarding flow is the initial destination

#### Scenario: Later launch
- **WHEN** the app starts after onboarding was finished or skipped
- **THEN** the app opens the resolved enabled default tab without showing onboarding

### Requirement: Short ordered concept education
The onboarding flow SHALL present tasks and categories before routines and reminders, SHALL explain concepts and configuration in four pages, and SHALL end with a fifth welcome page.

#### Scenario: User advances through education
- **WHEN** the user moves forward from the first page
- **THEN** tasks and categories have been introduced before routines and reminders

#### Scenario: Reminder education
- **WHEN** the routines and reminders page is displayed
- **THEN** the app explains reminder behavior without requesting notification permission

#### Scenario: Final welcome
- **WHEN** the user advances from the configuration page
- **THEN** a final page displays the Tempo logo and app name centered in the available content area

### Requirement: Skippable and navigable flow
The onboarding flow SHALL expose Skip on every page, Back after the first page, and a forward action until the final page.

#### Scenario: Skip from any page
- **WHEN** the user selects Skip
- **THEN** onboarding is marked complete and the user is handed off without visiting the remaining pages

#### Scenario: Navigate backward
- **WHEN** the user selects Back after the first page
- **THEN** the immediately preceding onboarding page is displayed

#### Scenario: Page transition feedback
- **WHEN** the user presses an onboarding action or changes pages
- **THEN** action shapes animate using Tempo's established interaction treatment while their colors remain stable, and a Didi-style segmented progress indicator above the page content animates the completed-page colors

#### Scenario: Window size changes
- **WHEN** onboarding is shown on a phone, foldable, tablet, desktop, landscape, or resized multi-window surface
- **THEN** the content recomposes into a readable single- or two-column layout without clipping actions or losing scroll access

#### Scenario: Final action on a compact phone
- **WHEN** the final onboarding page is displayed in a compact window
- **THEN** the Start using Tempo action receives more horizontal space than Back so its localized label remains readable

### Requirement: Appearance selection
The onboarding flow SHALL reuse the Settings appearance controls to allow light, dark, or system mode and selection between Tempo signature colors and supported dynamic colors, with Tempo colors selected by default for users who have not completed onboarding.

#### Scenario: New user sees appearance choices
- **WHEN** an incomplete user reaches the appearance page
- **THEN** light, dark, and system mode choices are shown, Tempo colors are marked selected, and dynamic colors are available when supported by the device

#### Scenario: User changes appearance
- **WHEN** the user selects an available color scheme
- **THEN** the app persists and previews that scheme immediately

### Requirement: Tab visibility invariant
The onboarding flow SHALL allow Tasks and Routines tabs to be enabled or disabled while preventing both tabs from being disabled simultaneously.

#### Scenario: Disable one of two tabs
- **WHEN** both tabs are enabled and the user disables one tab
- **THEN** the selected tab is disabled and the other remains enabled

#### Scenario: Attempt to disable the only tab
- **WHEN** only one tab is enabled and the user attempts to disable it
- **THEN** the app keeps that tab enabled

### Requirement: Valid default tab
The onboarding flow SHALL allow the user to choose an enabled default tab and SHALL automatically move the default when its tab is disabled.

#### Scenario: Select an enabled default
- **WHEN** the user selects an enabled tab as the default
- **THEN** the app persists that tab as the default

#### Scenario: Disable the default tab
- **WHEN** the user disables the current default tab while the other tab is enabled
- **THEN** the app enables no additional tab and persists the other tab as the default

### Requirement: Completed-task retention configuration
The onboarding flow SHALL leave automatic completed-task removal disabled for users without a saved retention preference, and SHALL allow the user to enable automatic removal and choose a supported retention duration.

#### Scenario: New user sees retention defaults
- **WHEN** a user without a saved completed-task retention preference reaches configuration
- **THEN** automatic completed-task removal is disabled

#### Scenario: Configure retention
- **WHEN** the user enables automatic removal and selects a retention duration
- **THEN** the app persists the enabled state and normalized supported retention duration through the existing retention behavior

### Requirement: Successful handoff
Finishing or skipping first-run onboarding SHALL mark it complete and navigate to the resolved enabled default tab.

#### Scenario: Finish onboarding
- **WHEN** the user selects the final completion action
- **THEN** onboarding is removed from the active back stack and a fade-through scale transition reveals the selected enabled default tab

#### Scenario: Stored default is invalid
- **WHEN** onboarding exits while the stored default tab is disabled
- **THEN** the app displays the remaining enabled tab

### Requirement: Settings replay
Settings SHALL allow a completed user to replay onboarding without clearing completion or resetting saved choices.

#### Scenario: Open onboarding from Settings
- **WHEN** the user selects View onboarding in Settings
- **THEN** the onboarding flow opens with current preference values

#### Scenario: Finish replay
- **WHEN** the user finishes or skips onboarding opened from Settings
- **THEN** the app returns to Settings and preserves onboarding completion

#### Scenario: Edit navigation preferences during replay
- **WHEN** the user changes tab visibility or the default tab while replaying onboarding
- **THEN** the app keeps onboarding active until the user explicitly finishes or skips it
