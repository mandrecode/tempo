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
