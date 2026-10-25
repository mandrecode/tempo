# Spec Delta

## Purpose

Defines the order of the app's top-level tabs and which tab takes over when the default is disabled.

## ADDED Requirements

### Requirement: Focus is the centre tab
The top-level tabs SHALL be ordered Routines, Focus, Tasks in the navigation bar, the navigation rails, the Settings tab toggles and the onboarding tab choices. When some tabs are disabled, the remaining ones SHALL keep this relative order.

#### Scenario: All tabs enabled
- **WHEN** all three tabs are enabled
- **THEN** the navigation bar shows Routines, Focus, Tasks, with Focus in the middle

### Requirement: Default-tab fallback prefers home
When the stored default tab is disabled, the app SHALL open on Focus if it is enabled, and otherwise on the first enabled tab in display order.

#### Scenario: Tasks default disabled
- **WHEN** Tasks is the default tab and the user disables it, leaving Routines and Focus
- **THEN** Focus becomes the default tab

#### Scenario: Focus disabled too
- **WHEN** the default is Focus and only Routines and Tasks are enabled
- **THEN** Routines becomes the default tab
