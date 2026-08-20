## ADDED Requirements

### Requirement: Interactive elements indicate hover
When a pointer device is present, rail items, task and habit cards, and action buttons SHALL show a hover indication.

#### Scenario: Hovering a rail tab
- **WHEN** the user moves a mouse pointer over a rail tab or the add button
- **THEN** the element shows a hover state before any click

### Requirement: Escape dismisses transient surfaces
Pressing Escape SHALL dismiss the topmost transient surface (menu, modal sheet, or docked editor pane) through the same path as the back affordance.

#### Scenario: Escape closes a sheet like back
- **WHEN** the user presses Escape while an editor sheet or pane is open
- **THEN** it dismisses exactly as a back press would, including the unsaved-changes confirmation when dirty

### Requirement: Keyboard focus traverses logically
Focus traversal SHALL follow the visual hierarchy: rail top-to-bottom (add, tabs, contextual actions), then screen content, then any open editor.

#### Scenario: Tab key walks the rail in importance order
- **WHEN** the user presses Tab repeatedly from the rail's first element
- **THEN** focus moves add → tabs → contextual actions → screen content without dead ends
