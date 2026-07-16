## ADDED Requirements

### Requirement: Editors dock as a supporting pane on very large windows
On windows at least 1200dp wide, task and habit editors SHALL present as a persistent, non-modal pane docked at the end edge, with the main content remaining visible and interactive.

#### Scenario: Editor docks beside the list
- **WHEN** the user opens a task or habit editor in a window at least 1200dp wide
- **THEN** the editor appears as a docked pane without a scrim and the list remains scrollable and tappable

#### Scenario: Pane survives configuration change
- **WHEN** the window rotates or resizes while a docked editor pane is open (still ≥1200dp)
- **THEN** the pane and its draft content are preserved

#### Scenario: Crossing the breakpoint preserves the draft
- **WHEN** an open editor's window resizes across the 1200dp boundary in either direction
- **THEN** the editor re-presents in the appropriate container (pane or modal side sheet) with the draft intact

### Requirement: Docked panes participate in back navigation
The docked pane SHALL close via the system back affordance using the same dismiss path as modal sheets, including the unsaved-changes confirmation.

#### Scenario: Back closes a clean pane
- **WHEN** the user presses back with a clean docked editor open
- **THEN** the pane closes and the main content keeps its state

#### Scenario: Back on a dirty pane asks for confirmation
- **WHEN** the user presses back with unsaved changes in the docked pane
- **THEN** the discard-changes confirmation is shown and the pane stays open until confirmed

### Requirement: Navigation engine migration is behavior-preserving
Migrating to the new navigation runtime SHALL preserve existing navigation behavior: tab switching restores each tab's state, notification deep-links open the correct screen and item, and onboarding hand-off transitions are unchanged.

#### Scenario: Tab state restores after switching
- **WHEN** the user scrolls Tasks, switches to Routines, and returns to Tasks
- **THEN** the Tasks scroll position and UI state are restored as before the migration

#### Scenario: Notification deep-link still lands correctly
- **WHEN** the user taps a task reminder notification
- **THEN** the app opens Tasks and surfaces the referenced task exactly as it did before the migration

### Requirement: Adaptive navigation follows M3 breakpoint tiers
Navigation SHALL use a bottom bar below 600dp, a collapsed rail from 600dp through 1199dp, and a labeled rail at 1200dp and wider.

#### Scenario: Expanded width keeps the collapsed rail
- **WHEN** the window width is between 840dp and 1199dp
- **THEN** navigation uses the collapsed icon rail rather than the labeled sidebar

#### Scenario: Settings is a large-window destination
- **WHEN** the window is at least 1200dp wide and the user selects Settings
- **THEN** Settings replaces the content pane in place while the rail remains visible and selected

### Requirement: Sort adapts from sheet to anchored menu
Sort SHALL use a bottom sheet below 600dp and an anchored menu at widths of 600dp and above.

#### Scenario: Medium window opens anchored sort
- **WHEN** the user invokes Sort in a window at least 600dp wide
- **THEN** an M3 Expressive menu opens anchored to the rail Sort action
