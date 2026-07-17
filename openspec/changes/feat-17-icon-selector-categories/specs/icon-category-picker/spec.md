## ADDED Requirements

### Requirement: Icon categorization
Every `TempoIcon` SHALL belong to exactly one `IconCategory`, and every `IconCategory` SHALL contain at least one `TempoIcon`, so the picker UI can group and sample icons without hardcoding per-icon groupings.

#### Scenario: Every icon has a category
- **WHEN** any `TempoIcon` entry is inspected
- **THEN** it resolves to exactly one non-null `IconCategory`

#### Scenario: Every category is non-empty
- **WHEN** all `IconCategory` values are enumerated
- **THEN** each one has at least one `TempoIcon` assigned to it

### Requirement: Randomized default icon row
`IconPicker`'s collapsed row SHALL sample icons across categories rather than always showing the same icons in enum declaration order, so the default row surfaces variety instead of repeating one topic.

#### Scenario: Row fills from distinct categories first
- **WHEN** the number of visible row slots is less than or equal to the number of categories
- **THEN** each icon shown in the row comes from a distinct category, with no category represented twice

#### Scenario: Row wraps once every category has contributed
- **WHEN** the number of visible row slots exceeds the number of categories
- **THEN** after one icon from every category has filled a slot, remaining slots are filled with additional random icons (no duplicate icons within the row)

#### Scenario: Selected icon is always shown
- **WHEN** `selectedIconName` refers to an icon that would not otherwise be sampled into the row
- **THEN** the row still shows that icon (taking the first slot), same as the picker's existing selected-icon-first behavior

#### Scenario: Row stays stable across recompositions
- **WHEN** the surrounding form recomposes for an unrelated reason (e.g. the user types in another field) while the picker instance is unchanged
- **THEN** the previously sampled row order does not reshuffle

### Requirement: Category-grouped icon modal
`IconPicker` SHALL expose a trailing right-arrow trigger that opens a modal listing every icon grouped under a visible heading for its `IconCategory`, replacing the previous inline expand-to-grid behavior.

#### Scenario: Opening the modal
- **WHEN** the user taps the right-arrow trigger
- **THEN** a modal bottom sheet opens showing all `TempoIcon` entries, grouped and headed by their `IconCategory`

#### Scenario: Selecting an icon inside the modal
- **WHEN** the user taps an icon inside the modal
- **THEN** the picker invokes `onSelectIcon` with that icon's name and the modal dismisses

#### Scenario: Dismissing without selecting
- **WHEN** the user dismisses the modal (tapping outside, back gesture, or system back) without tapping an icon
- **THEN** the modal closes and the previous selection is unchanged
