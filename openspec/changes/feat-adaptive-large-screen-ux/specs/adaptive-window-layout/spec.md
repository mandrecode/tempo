## ADDED Requirements

### Requirement: Rail actions are ordered by importance
In rail layouts the navigation rail SHALL be a top-start anchored column ordered: primary add action first, navigation tabs second, contextual secondary actions (sort, clear completed) last.

#### Scenario: Add action reads as primary on top
- **WHEN** a rail layout is shown on Routines or Tasks
- **THEN** the add button is the topmost rail element, above the navigation tabs

#### Scenario: Tasks contextual actions sit below the tabs
- **WHEN** the Tasks screen is shown in a rail layout
- **THEN** the sort button (and clear-completed when available) render below the navigation tabs, styled as secondary actions

#### Scenario: Contextual actions leave with their screen
- **WHEN** the user navigates from Tasks to Routines in a rail layout
- **THEN** the sort and clear-completed actions are absent and the rail keeps its anchor position without offset jumps

### Requirement: The rail expands with labels on large windows
When the window is at least 840dp wide and at least 480dp tall, the rail SHALL present navigation tabs as icon-plus-label rows and the add action as an extended button with its label; below that tier the compact icon rail is used.

#### Scenario: Tablet shows labeled rail
- **WHEN** the app is shown in a window at least 840dp wide and 480dp tall
- **THEN** each tab shows its icon and name, the selected tab is highlighted as a full row, and the add button shows its label

#### Scenario: Landscape phone keeps the compact rail
- **WHEN** the app is shown in a window at least 600dp wide but under 480dp tall
- **THEN** the rail remains icon-only with the same importance ordering

#### Scenario: Expanded rail reserves matching clearance
- **WHEN** the labeled rail is shown
- **THEN** screen content is laid out entirely outside the expanded rail footprint, with clearance derived from the same source of truth as the rail metrics
