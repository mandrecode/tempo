## ADDED Requirements

### Requirement: Task cards form connected groups
In the Tasks list, the cards under one header (or the whole list when it has no headers) SHALL form one connected run: the run's first and last cards keep their round outer corners, the joins between its cards are tight, and the cards stand a narrow seam apart. The first item in the list SHALL keep the list's top inset whatever it is.

#### Scenario: Tasks sorted by priority
- **WHEN** the list is grouped under priority headers
- **THEN** each priority's tasks form their own run, rounded at its two ends

### Requirement: Settings options form connected groups
Each settings section's options SHALL stand as one connected group of cards, rounded at the group's two ends and joined tightly in between, without dividers. An option that hides SHALL fold away with its seam, and the group SHALL re-round to its new ends. Rows SHALL be at least 72dp tall, like Android's own Settings.

#### Scenario: Turning repeat reminders off
- **WHEN** the user turns off repeating missed reminders
- **THEN** the time option folds away and the switch's card rounds at both ends

### Requirement: The selected tab shows its name
On the bottom navigation bar the selected tab SHALL be filled with the primary colour and widen to show its name beside the icon, easing between tabs as the selection moves; the others SHALL stay icons. Rails SHALL fill the selected item with the same colour.

#### Scenario: Switching tabs
- **WHEN** the user goes from Tasks to Routines
- **THEN** the Tasks pill shrinks back to an icon while the Routines one widens to show its name
