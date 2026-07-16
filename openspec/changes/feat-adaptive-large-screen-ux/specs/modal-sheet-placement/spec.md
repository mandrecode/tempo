## ADDED Requirements

### Requirement: Sheet presentation follows the window size
Modal task, habit, and category editors SHALL be presented in the container appropriate to the window: a bottom sheet on portrait-phone-like windows, a modal side sheet anchored to the end edge when the window width is at least 840dp or the window height is below 480dp.

#### Scenario: Tablet width presents a side sheet
- **WHEN** an editor opens in a window at least 840dp wide
- **THEN** it is presented as a full-height modal side sheet anchored at the end edge

#### Scenario: Landscape phone presents a side sheet
- **WHEN** an editor opens in a window with height below 480dp
- **THEN** it is presented as a full-height modal side sheet rather than a bottom sheet

#### Scenario: Portrait phone keeps the bottom sheet
- **WHEN** an editor opens in a window below 840dp wide with height of at least 480dp
- **THEN** it is presented as the existing bottom sheet

### Requirement: Side sheets preserve modal sheet behavior
Side sheets SHALL provide the same modal behaviors as bottom sheets: scrim, drag-to-dismiss toward the end edge, predictive-back progress along the horizontal axis, and the unsaved-changes discard confirmation.

#### Scenario: Predictive back previews horizontal dismissal
- **WHEN** a user progresses a predictive-back gesture while a side sheet is visible
- **THEN** the sheet translates toward the end edge with gesture progress and restores if cancelled

#### Scenario: Dirty side sheet asks for confirmation
- **WHEN** a dismissal is requested on a side sheet with unsaved changes
- **THEN** the discard-changes confirmation is shown and the sheet stays visible until confirmed

#### Scenario: Keyboard does not displace the side sheet
- **WHEN** the keyboard opens while a side sheet is visible
- **THEN** the sheet keeps its position and size, and its content insets for the keyboard

### Requirement: Sort options present as a sheet everywhere
The sort control SHALL present its options as a sheet, following the same window placement rule as the editors (design review reverted an anchored-menu variant).

#### Scenario: Sort sheet on a wide window
- **WHEN** the user taps the sort button in a rail layout
- **THEN** the sort options appear in a sheet (side sheet on side-placement windows) with the same selection behavior as on phones

### Requirement: The rail stays visible under modal sheets
Opening a modal editor or sort sheet SHALL NOT hide the navigation rail; the rail remains visible beneath the scrim.

#### Scenario: Editor opens over the rail
- **WHEN** an editor side sheet opens in a rail layout
- **THEN** the rail stays in place under the scrim and reappears interactive when the sheet dismisses
