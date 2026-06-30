## ADDED Requirements

### Requirement: Bottom sheets support predictive back dismissal
Shared modal bottom sheets SHALL visually follow Android predictive-back gesture progress and use the existing dismissal path when the gesture is committed.

#### Scenario: Gesture previews sheet dismissal
- **WHEN** a user starts and progresses a predictive-back gesture while a shared modal bottom sheet is visible
- **THEN** the bottom sheet moves toward its dismissed position according to gesture progress

#### Scenario: Gesture cancellation restores the sheet
- **WHEN** a user cancels a predictive-back gesture before committing it
- **THEN** the bottom sheet returns to its fully visible position without dismissing its content

#### Scenario: Gesture completion dismisses a clean sheet
- **WHEN** a user completes a predictive-back gesture on a sheet without unsaved changes
- **THEN** the sheet dismisses through its standard dismiss animation and callback

### Requirement: Unsaved changes remain protected during predictive back
Shared modal bottom sheets with unsaved changes SHALL continue to require discard confirmation when a predictive-back gesture is committed.

#### Scenario: Dirty sheet asks for confirmation
- **WHEN** a user completes a predictive-back gesture on a sheet with unsaved changes
- **THEN** the sheet remains visible and the discard-changes confirmation is shown

#### Scenario: Dirty sheet stays open when confirmation is cancelled
- **WHEN** a discard-changes confirmation opened by predictive back is cancelled
- **THEN** the sheet remains fully visible with its content intact
