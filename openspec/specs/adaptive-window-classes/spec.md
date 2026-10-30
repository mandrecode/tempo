# adaptive-window-classes Specification

## Purpose
Defines how window width maps to the app's layout tiers and how the layout moves between them.

## Requirements

### Requirement: Official width classes decide every layout tier
The app SHALL classify window width with the Material 3 / WindowManager width classes (Compact < 600 dp ≤ Medium < 840 dp ≤ Expanded < 1200 dp ≤ Large < 1600 dp ≤ Extra-large). The floating rail SHALL replace the bottom bar from Medium up, and editors SHALL dock beside content from Large up, both derived from the same classification.

#### Scenario: Large desktop window
- **WHEN** the window is 1280 dp wide
- **THEN** navigation is a floating rail and editors dock beside the content

#### Scenario: Extra-large display
- **WHEN** the window is 1920 dp wide
- **THEN** it behaves as Large

#### Scenario: Boundary
- **WHEN** the window is 1199 dp wide
- **THEN** editors open as bottom sheets, and the rail is shown

### Requirement: Layout changes glide
When the rail clearance of screen content changes, because the window crosses a breakpoint or an editor docks beside the list, the content SHALL move to its new position with an animation, not in a single frame.

#### Scenario: Window resized across Medium
- **WHEN** a desktop window is widened from 500 dp to 700 dp
- **THEN** the rail appears and the content slides to clear it

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
