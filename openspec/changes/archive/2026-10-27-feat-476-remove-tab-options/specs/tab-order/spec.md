# Spec Delta

## Purpose

Defines the order of the app's top-level tabs and which tab the app opens on.

## MODIFIED Requirements

### Requirement: Focus is the centre tab
The top-level tabs SHALL be ordered Routines, Focus, Tasks in the navigation bar and the navigation rails. All three tabs SHALL always be shown; there is no setting to hide one.

#### Scenario: Navigation bar order
- **WHEN** the navigation bar is shown
- **THEN** it shows Routines, Focus, Tasks, with Focus in the middle

## REMOVED Requirements

### Requirement: Default-tab fallback prefers home
**Reason**: There is no default-tab setting to fall back from (#476); the app reopens on the last-used tab.

## ADDED Requirements

### Requirement: The app reopens on the last-used tab
After onboarding, the app SHALL open on the tab the user last navigated to, and on Focus when there is none.

#### Scenario: First launch after onboarding
- **WHEN** no tab has been visited yet
- **THEN** the app opens on Focus

#### Scenario: Reopening
- **WHEN** the user last visited Tasks
- **THEN** the app opens on Tasks
