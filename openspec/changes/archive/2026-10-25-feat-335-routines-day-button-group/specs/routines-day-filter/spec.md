# Spec Delta

## Purpose

Defines how the Routines screen's day bar presents and selects the day being shown.

## ADDED Requirements

### Requirement: The whole window fits as one connected group
The Routines day bar SHALL show yesterday through five days ahead as one connected, single-select button group whose segments all fit the screen width without scrolling. Each segment SHALL show the short weekday and the day of the month, and SHALL be announced with its relative name, where it has one, and its date.

#### Scenario: Phone width
- **WHEN** Routines is shown on a 360 dp wide phone
- **THEN** all seven days are visible at once

#### Scenario: Accessibility
- **WHEN** a screen reader focuses tomorrow's segment
- **THEN** it is announced as "Tomorrow" with its weekday and date

### Requirement: Selecting a day
Tapping a segment SHALL select that day and show its routines. The selected segment SHALL be visibly emphasised, and today SHALL be marked whether or not it is selected.

#### Scenario: Pick a later day
- **WHEN** the user taps Wednesday
- **THEN** Wednesday becomes the selected segment and Routines shows Wednesday's habits
