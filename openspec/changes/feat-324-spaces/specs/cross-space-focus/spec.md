## ADDED Requirements

### Requirement: The Focus agenda lists the active space first, then other spaces below a divider
The system SHALL order the Focus agenda with the active space's items first in their normal priority order, followed by a labelled divider, followed by items from other spaces ordered among themselves.

#### Scenario: Active space items precede other spaces
- **WHEN** more than one space has items due today and the user opens Focus
- **THEN** the active space's items appear first in normal priority order, then a divider labelled as being from other spaces, then the remaining items

#### Scenario: A single space renders no divider
- **WHEN** only one space exists
- **THEN** the Focus agenda shows no divider and no other-space items

#### Scenario: No other-space items renders no divider
- **WHEN** more than one space exists but no other space has items due today
- **THEN** the Focus agenda shows no divider

### Requirement: The other-spaces block's initial state reflects how full the active space is
The system SHALL start the other-spaces block collapsed with a count when the active space has at least the density threshold of items due today, and SHALL start it expanded when the active space has fewer. The threshold SHALL be derived from the current window size class rather than being a single fixed value.

#### Scenario: A full active space collapses the block
- **WHEN** the active space has at least the density threshold of items due today and other spaces also have items
- **THEN** the other-spaces block is collapsed and shows how many items it holds

#### Scenario: A sparse active space expands the block
- **WHEN** the active space has fewer than the density threshold of items due today and other spaces have items
- **THEN** the other-spaces block is expanded and its items are shown

#### Scenario: Threshold follows the window size class
- **WHEN** the same set of items is shown at a compact width and at an expanded width
- **THEN** the threshold applied reflects how many items the current window can show, so a larger window tolerates more active-space items before collapsing the block

#### Scenario: Collapsed block reflects its contents
- **WHEN** items are added to or completed in another space while the block is collapsed
- **THEN** the count updates to match

### Requirement: An imminent item expands the block regardless of density
The system SHALL expand the other-spaces block when any item below the divider has a reminder falling within two hours, irrespective of how full the active space is.

#### Scenario: Imminent item overrides a full active space
- **WHEN** the active space is full enough to collapse the block, and an item below the divider has a reminder within two hours
- **THEN** the block is expanded so that item is visible without user action

#### Scenario: Distant reminders do not override density
- **WHEN** the active space is full enough to collapse the block, and the earliest reminder below the divider is more than two hours away
- **THEN** the block remains collapsed

### Requirement: Manual expansion and collapse are respected while Focus is open
The system SHALL honour a user's manual expand or collapse of the other-spaces block for as long as Focus remains open, and SHALL re-evaluate the automatic rules when Focus is next entered.

#### Scenario: Manual choice is not overridden by activity
- **WHEN** a user expands the block and then completes items in the active space
- **THEN** the block stays expanded

#### Scenario: Rules re-evaluate on re-entry
- **WHEN** a user manually collapses the block, leaves Focus, and returns
- **THEN** the block's state is determined by the density and imminence rules again

### Requirement: Up next is scoped to the active space
The system SHALL draw Up next only from the active space's items.

#### Scenario: Up next excludes other spaces
- **WHEN** items from several spaces are due today
- **THEN** Up next offers only items belonging to the active space

### Requirement: A running session is scoped to the active space at start and survives switching
The system SHALL allow a focus session to be started only for an item in the active space, and SHALL continue an already running session when the active space changes.

#### Scenario: Session continues across a space switch
- **WHEN** a session is running and the user switches to another space
- **THEN** the session continues and its remaining time is unaffected

#### Scenario: Session surface identifies its space when it differs
- **WHEN** a session is running for an item belonging to a space that is not active
- **THEN** the session surface and its notification identify that item's space

### Requirement: Space is shown only where an item can differ from the active space
The system SHALL display a space indicator on Focus items below the other-spaces divider and on a running session, and SHALL NOT display one on items that necessarily belong to the active space.

#### Scenario: Items below the divider are labelled
- **WHEN** the Focus agenda shows items from other spaces
- **THEN** each of those items indicates which space it belongs to

#### Scenario: Active space items are not labelled
- **WHEN** the Focus agenda shows the active space's items above the divider
- **THEN** those items carry no space indicator

### Requirement: Focus activity and streak span all spaces
The system SHALL record focus activity and calculate its streak across every space rather than per space.

#### Scenario: Sessions in different spaces contribute to one streak
- **WHEN** focus sessions are completed in different spaces on consecutive days
- **THEN** the streak counts those days continuously
