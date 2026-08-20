## ADDED Requirements

### Requirement: A list holds its place when one of its own rows settles

A list that reorders a row into another section when it is acted on SHALL keep the view where it
was, rather than following the row to where it went. The row moves; the reader does not.

This is one mechanism, shared, so that a list added later inherits the behaviour instead of picking
between precedents.

#### Scenario: Checking a task off in the Tasks list
- **WHEN** a task is checked off and moves to the completed section
- **THEN** the list stays where it was looking
- **AND** the next task takes the place the checked one left, so a run of them can be worked
  through without scrolling back after each

#### Scenario: Acted on from part-way down the list
- **WHEN** the row that moves is the first one visible and the list is scrolled part-way down
- **THEN** the view is still held, because a lazy list otherwise anchors on that row's key and
  follows it to its new section

#### Scenario: A row that does not change section
- **WHEN** a row is acted on but stays in the section it was already in
- **THEN** nothing is scrolled or restored, because nothing moved
