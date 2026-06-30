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

### Requirement: Bottom-sheet drag is constrained to valid dismissal movement
Shared modal bottom sheets SHALL expose a drag handle only when drag-to-dismiss is enabled and SHALL prevent upward drag from moving the sheet beyond its fully visible resting position.

#### Scenario: Repeated upward handle drags keep the sheet stable
- **WHEN** a user drags the bottom-sheet handle upward multiple times while the sheet is fully visible
- **THEN** the sheet remains at its fully visible resting position

#### Scenario: Downward drag dismisses past threshold
- **WHEN** a user drags the bottom sheet downward past the dismiss threshold and releases
- **THEN** the sheet dismisses through its standard dismiss path

### Requirement: Bottom-sheet keyboard movement remains synchronized
Shared modal bottom sheets containing an initially focused text field SHALL keep keyboard appearance visually synchronized with sheet opening.

#### Scenario: New task or habit sheet opens with title focus
- **WHEN** a user opens a new task or habit bottom sheet
- **THEN** the sheet and keyboard appear without a delayed layout jump
