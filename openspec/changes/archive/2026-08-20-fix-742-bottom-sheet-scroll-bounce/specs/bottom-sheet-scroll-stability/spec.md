## ADDED Requirements

### Requirement: Bottom sheets do not bounce when content is scrolled past its top
The system SHALL keep a modal bottom sheet visually anchored when the user scrolls or drags its content upward past the top of that content, so the sheet does not overshoot its resting position and spring back.

#### Scenario: Scrolling up over an open editor sheet
- **WHEN** an existing habit or task is opened in its editor bottom sheet and the user drags the content upward (including past the top of the content)
- **THEN** the sheet surface stays at its resting position and does not stretch up and bounce back

#### Scenario: Scrolling up over the sort sheet
- **WHEN** the task sort bottom sheet is open and the user drags its content upward
- **THEN** the sheet surface stays at its resting position and does not bounce

### Requirement: Bottom sheets remain dismissible without drag gestures
The system SHALL keep each affected bottom sheet dismissible through non-drag paths when sheet drag gestures are disabled.

#### Scenario: Dismissing via the scrim
- **WHEN** an affected bottom sheet is open and the user taps the scrim outside the sheet
- **THEN** the sheet dismisses (subject to any unsaved-changes confirmation)

#### Scenario: Dismissing via system back and explicit actions
- **WHEN** an affected editor bottom sheet is open
- **THEN** system back and the in-sheet Cancel/Save actions dismiss the sheet (subject to any unsaved-changes confirmation)
