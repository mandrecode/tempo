## ADDED Requirements

### Requirement: Tapping a Focus agenda row opens the item it names

Every row of the Focus agenda SHALL open its own item's editor when its card body is tapped, without leaving the Focus tab. Tasks, habits and habit chains SHALL behave alike in this respect: the editor is the owning tab's own editor, hosted by Focus as a bottom sheet.

#### Scenario: Tapping a habit chain card opens the chain editor

- **WHEN** the user taps the body of a habit chain card in the Focus agenda
- **THEN** the habit chain editor opens as a bottom sheet over the Focus tab
- **AND** the editor is populated with that chain

#### Scenario: The chain editor closes back to Focus

- **WHEN** the chain editor is open over Focus and the user dismisses it
- **THEN** the sheet closes and the Focus agenda is still the visible screen
- **AND** tapping the same chain card again reopens the editor

#### Scenario: Expanding a chain is not opening it

- **WHEN** the user taps the expand chevron on a habit chain card in the Focus agenda
- **THEN** the chain's habits are revealed or hidden
- **AND** no editor opens

#### Scenario: The editor's own delete is answerable from Focus

- **WHEN** the chain editor is open over Focus and the user chooses to delete the chain
- **THEN** the delete confirmation appears over Focus, as it already does for a habit deleted from the habit editor

#### Scenario: The chain card itself carries no delete

- **WHEN** the user looks at a chain card in the Focus agenda
- **THEN** the card offers no delete affordance of its own

### Requirement: The undated-tasks footer reads as a hand-off to Tasks

The Focus screen SHALL present its "N tasks without a date" affordance as a rounded button carrying an open-in-new icon, so that it is recognisable as leaving Focus for the Tasks tab. Its touch feedback SHALL be bounded to the button's rounded shape rather than spanning the full width of the list.

#### Scenario: The footer names its destination

- **WHEN** the Focus agenda has at least one undated task
- **THEN** the footer shows the undated-task count alongside an open-in-new icon
- **AND** the footer is drawn as a rounded, self-contained control

#### Scenario: Tapping the footer opens the Tasks tab

- **WHEN** the user taps the undated-tasks footer
- **THEN** the app navigates to the Tasks tab

#### Scenario: The footer is absent when there is nothing undated

- **WHEN** the Focus agenda has no undated tasks
- **THEN** no undated-tasks footer is shown

#### Scenario: The empty day offers the same control

- **WHEN** the day is empty and undated tasks exist
- **THEN** the empty state shows the same rounded open-in-Tasks control as the populated agenda
