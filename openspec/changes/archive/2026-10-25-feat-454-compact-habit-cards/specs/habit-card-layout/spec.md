# Spec Delta

## Purpose

Defines the size and content of habit and habit-chain cards in Routines.

## ADDED Requirements

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
