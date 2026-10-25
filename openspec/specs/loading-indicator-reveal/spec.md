# loading-indicator-reveal Specification

## Purpose
Defines when the shared full-size loading indicator becomes visible, so fast loads do not flash it.

## Requirements

### Requirement: Loading indicator waits before appearing
The shared loading indicator SHALL stay invisible for a grace period of 400 ms after it is first shown. If loading is still in progress after the grace period, it SHALL fade in. The indicator SHALL keep its layout space during the grace period, so surrounding content does not move when it appears.

#### Scenario: Fast load
- **WHEN** a screen's data finishes loading within the grace period
- **THEN** the loading indicator is never visible, and the content replaces the empty space

#### Scenario: Slow load
- **WHEN** a screen's data is still loading once the grace period has passed
- **THEN** the loading indicator fades in and stays visible until loading finishes

#### Scenario: Preview without delay
- **WHEN** the indicator is created with a reveal delay of zero
- **THEN** it is visible from the first frame

### Requirement: Cold start hands over to a finished screen
On a cold start, the launch splash SHALL stay up, within a 2.5 s cap, until the app's settings and the first screen's first content have loaded. The what's-new sheet SHALL NOT open until the splash has handed over.

#### Scenario: Cold start
- **WHEN** the app is launched cold onto Focus
- **THEN** the splash is followed directly by the complete Focus screen, with no frame of only its background, title and navigation bar

#### Scenario: What's new after an update
- **WHEN** a what's-new entry is pending on a cold start
- **THEN** the sheet opens over the complete first screen, after the splash has handed over

### Requirement: Tabs open fully drawn
Every top-level tab SHALL have its first data loaded in the background once the database is ready, so that a tab opened later appears complete on its first frame. A tab opened before its data is ready SHALL fade its content in over its loading state.

#### Scenario: First visit to another tab
- **WHEN** the user opens Routines for the first time in a session, after the app has started
- **THEN** it appears complete in its first frame, its data having been loaded in the background at startup

#### Scenario: Tab opened before its data is ready
- **WHEN** a tab is opened before its background load has finished
- **THEN** its content fades in, rather than appearing all at once over the bare background
