# habit-card-layout Specification

## Purpose
Defines the size and content of habit and habit-chain cards in Routines.

## Requirements

### Requirement: Habit rows are compact
A standalone habit card SHALL be no taller than 72dp when its title fits on one line and it has no description, more compact than the original 88dp row.

#### Scenario: Single-line habit
- **WHEN** a habit with a one-line title and no description is shown in Routines
- **THEN** its card is at most 72dp tall

### Requirement: Chains always list their habits
A habit-chain card SHALL always show its habits, each with its own completion toggle, without an expand or collapse control. Each listed habit SHALL use the same row as a standalone habit. Completing every habit SHALL NOT hide them.

#### Scenario: Habit inside a chain
- **WHEN** a habit is shown inside a chain
- **THEN** its row has the same height, checkbox and title style as a standalone habit

#### Scenario: Chain shown
- **WHEN** a chain with habits is shown in Routines
- **THEN** each of its habits is visible inside the card without any extra tap

#### Scenario: Chain completed
- **WHEN** the last habit of a chain is completed
- **THEN** the chain's habits stay visible, shown as completed

### Requirement: Chain card layout
A habit-chain card SHALL show, top to bottom: its title and optional description, its progress as a short bar with the done/total count beside it, its habits, and an "Add habit" row as the last row of the list. Once every habit is done, the bar and count SHALL be replaced by a single "All done" chip.

#### Scenario: Chain in progress
- **WHEN** some of a chain's habits are done for the selected day
- **THEN** a short progress bar and the done/total count are shown under the title

#### Scenario: Chain all done
- **WHEN** every habit in the chain is done for the selected day
- **THEN** an "All done" chip with the total count is shown instead of the bar and count

#### Scenario: Adding a habit
- **WHEN** the user taps the "Add habit" row at the end of the chain's list
- **THEN** the habit editor opens for a new habit in that chain
