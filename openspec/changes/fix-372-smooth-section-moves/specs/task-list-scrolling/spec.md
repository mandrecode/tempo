## ADDED Requirements

### Requirement: The hold is invisible

Holding the view SHALL be something the reader never sees happen. The view SHALL NOT pass through
the position the list would have taken had it followed the row, and no frame SHALL be drawn at that
position before it is corrected.

A hold applied after the list has already laid itself out against the new sections is a correction,
and a correction is visible: one frame at the chased position, one frame back. The held position
has to be the position the next layout is measured at, not one restored afterwards.

#### Scenario: Checking a task off, or giving one a day
- **WHEN** a row is acted on and moves to another section
- **THEN** every frame from the action onwards is drawn at the held position
- **AND** the list is never drawn at the position it would have followed the row to

#### Scenario: The row that moved
- **WHEN** the row's new place is within the part of the list on screen
- **THEN** it slides there, rather than disappearing from one place and appearing in the other
- **AND** the rows it moved past slide into the space it left

#### Scenario: Repeated without looking
- **WHEN** several rows are settled one after another, each from the same place on screen
- **THEN** each behaves the same as the first
- **AND** none of them depends on how long the previous one took to arrive
