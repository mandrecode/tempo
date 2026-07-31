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

### Requirement: The agenda's rows move out of each other's way

When a row of the Focus agenda changes height — a chain folding out, a task's subtasks unfolding —
the rows below it SHALL slide to their new positions rather than being relocated in a single frame,
matching how the Routines and Tasks lists already behave.

#### Scenario: Expanding a chain pushes the rows below it

- **WHEN** the user expands a habit chain that has rows beneath it in the agenda
- **THEN** those rows travel to their new positions over the course of the expansion
- **AND** no single frame carries the majority of that travel

#### Scenario: A card honours the modifier it is given

- **WHEN** a habit card or a habit chain card is given a modifier by its caller
- **THEN** that modifier applies to the card's root, so a list can animate the card's placement

### Requirement: A chain's habits read in the chain's own order

The Focus agenda SHALL list a chain's habits in the order the chain holds them, which is the order
its editor was left in — the same order the Routines tab shows. A habit named by a chain but no
longer present SHALL simply be absent rather than displacing the rest.

#### Scenario: Chain order wins over habit order

- **WHEN** a chain holds its habits in an order different from the order the habits themselves are stored in
- **AND** the user expands that chain in Focus
- **THEN** the habits are listed in the chain's order
- **AND** the order matches what the Routines tab shows for the same chain

#### Scenario: A missing habit is skipped

- **WHEN** a chain names a habit that no longer exists
- **THEN** the remaining habits are listed in the chain's order, without a gap

### Requirement: The empty day looks like the app's other empty days

When the Focus agenda has nothing on it, the screen SHALL present its headline and explanation in
the same style Tasks and Routines use for theirs — the shared empty-state headline, the same two
levels of muting, and the same position above the vertical middle.

#### Scenario: The empty state sits where the others do

- **WHEN** the Focus agenda is empty
- **THEN** its headline sits above the vertical middle of the content area, as the Tasks and Routines empty states do

#### Scenario: It does not restate what the hero already said

- **WHEN** the Focus agenda is empty
- **THEN** the hero states that nothing is scheduled, and the empty state below it does not say the same thing again
- **AND** the empty state's second line says where work comes from instead
