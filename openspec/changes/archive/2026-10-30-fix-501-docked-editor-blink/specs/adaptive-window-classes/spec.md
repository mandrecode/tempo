# Spec Delta

## ADDED Requirements

### Requirement: Docking keeps the screen in view
Opening or closing a docked editor SHALL NOT be presented as leaving the screen beside it. Throughout the pane's appearance and disappearance, the screen's content (its header, chips and list) SHALL stay drawn at full opacity, and the pane SHALL visibly grow in from, or shrink toward, the end edge. Navigation between different screens SHALL keep its own transitions.

#### Scenario: Open a task on a Large window
- **WHEN** the window is 1280 dp wide and the user opens a task in Tasks
- **THEN** no frame shows the list or category chips missing or faded, and the editor pane grows in from the end edge

#### Scenario: Close the docked editor
- **WHEN** the user closes the docked editor with Back or by tapping the open task again
- **THEN** no frame shows the list missing or faded, and the pane shrinks toward the end edge

#### Scenario: Open a habit on a Large window
- **WHEN** the window is 1280 dp wide and the user opens a habit in Routines
- **THEN** no frame shows the routines list missing or faded, and the editor pane grows in from the end edge

#### Scenario: Switch tabs with an editor docked
- **WHEN** an editor is docked in Tasks and the user switches to Routines
- **THEN** the tab switch behaves as it does without an editor docked
