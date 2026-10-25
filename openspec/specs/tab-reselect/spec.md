# tab-reselect Specification

## Purpose
Defines what tapping the already-open top-level tab does.

## Requirements

### Requirement: Reselecting Tasks shows the default category
When the Tasks tab is open and the user taps it again in the navigation bar or rail, the Tasks screen SHALL select the default category. If no category is marked default, it SHALL select the first category. Tapping another tab SHALL navigate to it as before.

#### Scenario: Browsing another category
- **WHEN** Tasks shows a category other than the default and the user taps the Tasks tab
- **THEN** the default category is selected and its chip is scrolled into view

#### Scenario: Already on the default category
- **WHEN** Tasks already shows the default category and the user taps the Tasks tab
- **THEN** nothing changes

#### Scenario: No default category
- **WHEN** no category is marked default and the user taps the open Tasks tab
- **THEN** the first category is selected

### Requirement: Reselecting other tabs does nothing
Tapping the open Routines or Focus tab SHALL NOT navigate or change what the tab shows.

#### Scenario: Reselect Routines
- **WHEN** Routines is open and the user taps the Routines tab
- **THEN** the screen is unchanged
