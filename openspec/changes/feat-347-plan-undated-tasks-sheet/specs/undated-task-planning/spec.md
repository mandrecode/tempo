## ADDED Requirements

### Requirement: The undated footer opens the plan sheet instead of leaving Focus

The Focus screen's "N tasks without a date" control — in the agenda footer and in the empty-state —
SHALL open a modal "Plan your tasks" sheet over Focus. It SHALL NOT navigate to the Tasks tab.

#### Scenario: Tapping the footer opens the sheet

- **WHEN** the user taps the "N tasks without a date" control in the Focus agenda footer
- **THEN** the plan sheet opens over Focus
- **AND** the Focus agenda stays behind it, unchanged and unscrolled

#### Scenario: Tapping the empty-state link opens the sheet

- **WHEN** the day is empty, undated tasks exist, and the user taps the undated link in the empty state
- **THEN** the plan sheet opens over Focus

#### Scenario: Focus no longer hands off to Tasks

- **WHEN** the user taps the undated control
- **THEN** no tab change occurs and the Tasks tab is not shown

### Requirement: The sheet lists undated work using the app's own task component

The sheet SHALL list every incomplete, top-level task whose `reminderDate` is `null` at the moment
the sheet opened, rendered with the same `TaskItem` component the Tasks list uses. Each row SHALL
lead its metadata row with the task's category, shown as the category's icon followed by its name.

#### Scenario: Undated tasks are listed

- **WHEN** the sheet opens with three incomplete undated top-level tasks
- **THEN** all three appear, each rendered as a task card carrying its title, description and metadata

#### Scenario: Subtasks and completed tasks are excluded

- **WHEN** an undated task is a subtask of another task, or is already completed
- **THEN** it does not appear in the sheet

#### Scenario: A task with steps says how many are left

- **WHEN** a listed task has subtasks
- **THEN** its card carries the same done-of-total count badge the Tasks list uses
- **AND** the steps stay folded, so the row keeps asking about the day rather than the work

#### Scenario: Category leads the metadata row

- **WHEN** a listed task belongs to a category with an icon
- **THEN** that icon and the category name appear as the first entry of the task's metadata row,
  ahead of priority, recurrence and reminder

#### Scenario: A category without an icon still names itself

- **WHEN** a listed task belongs to a category that has no icon set
- **THEN** the category name is shown without an icon, still leading the metadata row

#### Scenario: The Tasks list is unaffected

- **WHEN** the same task is rendered in the Tasks list
- **THEN** its metadata row carries no category badge
- **AND** it still offers its add-subtask action

#### Scenario: The sheet does not offer adding a subtask

- **WHEN** a task is rendered in the plan sheet
- **THEN** no add-subtask action is shown on its card, because the sheet asks one question of every
  row and breaking a task down is a different job

### Requirement: Quick-plan chips assign a date in one tap

Every unplanned row in the sheet SHALL offer quick-plan chips for **Today**, **Tomorrow** and
**Pick a date**. Choosing one SHALL set that task's reminder immediately, with no per-task save
step, through the same update path that schedules reminders elsewhere in the app.

#### Scenario: Today plans the task for today

- **WHEN** the user taps "Today" on an unplanned row
- **THEN** the task's reminder is set to today at the default planning time
- **AND** the change is persisted immediately without any further confirmation

#### Scenario: Tomorrow plans the task for tomorrow

- **WHEN** the user taps "Tomorrow" on an unplanned row
- **THEN** the task's reminder is set to the following day at the default planning time

#### Scenario: Pressing the chosen chip again takes the date back off

- **WHEN** the user taps a quick-plan chip that is already selected on a row
- **THEN** that task's reminder is cleared
- **AND** it returns to the unplanned section

#### Scenario: Pressing a different chip re-plans rather than clearing

- **WHEN** a task is planned for today and the user taps its "Tomorrow" chip
- **THEN** the task is planned for tomorrow, and stays in the planned section

#### Scenario: Pick a date opens the date picker

- **WHEN** the user taps "Pick a date" on an unplanned row
- **THEN** the app's date picker opens, offering today and later dates only
- **AND** confirming a date sets the task's reminder to that date at the default planning time

#### Scenario: The default planning time avoids the past

- **WHEN** the chosen day is today and the default planning hour has already passed
- **THEN** the reminder is set to a later time on that same day rather than to an instant already gone

#### Scenario: A reminder is actually scheduled

- **WHEN** a task is planned from the sheet
- **THEN** its reminder is scheduled through the same reminder scheduler the task editor uses

### Requirement: Planning is gated by reminder permissions

Setting a reminder from the sheet SHALL pass through the same notification and exact-alarm
permission flow the task editor uses, so the sheet cannot promise a reminder the system will not
deliver.

#### Scenario: Permissions are requested before the first plan

- **WHEN** the user taps a quick-plan chip and the required reminder permissions are not granted
- **THEN** the permission education flow is shown before any date is applied

#### Scenario: Declining permissions leaves the task unplanned

- **WHEN** the user dismisses that permission flow without granting
- **THEN** the task keeps no reminder and stays in the unplanned section

### Requirement: The sheet splits into planned and unplanned sections

Once at least one listed task has a date, the sheet SHALL group its rows under **Planned** and
**Unplanned** headers drawn in the Tasks list's group-header style. While nothing is planned, the
sheet SHALL show one plain, unheaded list.

#### Scenario: No headers before anything is planned

- **WHEN** the sheet opens and no listed task has a date
- **THEN** the rows appear as a single list with no section headers

#### Scenario: Planning a task splits the list

- **WHEN** the user plans one of several listed tasks
- **THEN** an "Unplanned" header and a "Planned" header appear
- **AND** the planned task moves under "Planned" while the rest stay under "Unplanned"

#### Scenario: A planned task keeps its place in the sheet

- **WHEN** a task has been planned from the sheet
- **THEN** it remains visible under "Planned" for as long as the sheet is open, rather than
  disappearing from a list of undated work

#### Scenario: A completed task stops asking for a day

- **WHEN** the user ticks a task off from inside the sheet
- **THEN** it stops counting towards the tasks that still need a day
- **AND** it moves under "Planned", shown struck through, because it is settled either way

#### Scenario: Completing alone is not a change to undo

- **WHEN** the user only ticks tasks off and closes the sheet
- **THEN** no undo snackbar is shown, because no reminder was moved

#### Scenario: Everything planned leaves only the planned section

- **WHEN** every listed task has been planned
- **THEN** only the "Planned" section is shown

### Requirement: A task can still be opened in the full editor from the sheet

The sheet SHALL let the user open a listed task in the app's full task editor for anything the
chips do not cover.

#### Scenario: Tapping the card opens the editor

- **WHEN** the user taps the body of a task card in the sheet
- **THEN** the full task editor opens over the sheet, editing that task

#### Scenario: A date set in the editor is reflected in the sheet

- **WHEN** the user sets a reminder in that editor and closes it
- **THEN** the task appears under "Planned" in the sheet

### Requirement: One way out, with batch undo

The sheet SHALL offer a single footer action, **Done**, always enabled and carrying a leading check
icon. It SHALL NOT offer a Cancel or Close alongside it: nothing is staged, so there is nothing such
a button could throw away.

Closing the sheet by any means — Done, the drag handle, back, the scrim, or Escape — SHALL behave
identically: the planning stays, and a snackbar offers to undo the whole batch. The snackbar SHALL
be raised only when the sheet actually changed something.

#### Scenario: Done is the only footer action

- **WHEN** the sheet is open
- **THEN** Done is shown and enabled, and no Cancel or Close action is present

#### Scenario: Done stays available with nothing planned

- **WHEN** the sheet opens and nothing has been planned yet
- **THEN** Done is still enabled, because it is the way out rather than a confirmation

#### Scenario: Closing offers the batch back

- **WHEN** the user closes the sheet after planning two tasks
- **THEN** the sheet closes
- **AND** a snackbar appears offering to undo the planning

#### Scenario: Dismissing without the button behaves the same

- **WHEN** the user closes the sheet with the drag handle, back, the scrim or Escape after planning
- **THEN** the same undo snackbar is offered, because leaving is leaving however it is done

#### Scenario: Undo restores every reminder the sheet set

- **WHEN** the user takes the snackbar's undo action
- **THEN** every task the sheet planned returns to having no reminder
- **AND** the reminders scheduled for them are cancelled

#### Scenario: Closing in the same breath as the tap still offers the undo

- **WHEN** the user taps a quick-plan chip and dismisses the sheet before the write has come back
- **THEN** the undo is still offered for that task, because the sheet is answerable for what it
  asked for, not only for what it has seen land

#### Scenario: An undo restores the batch it was offered for

- **WHEN** a second sheet is opened and closed while the first sheet's undo snackbar is still shown
- **THEN** taking that snackbar's undo restores only what the first sheet changed, whether the second
  sheet changed something else or nothing at all

#### Scenario: Closing without a change says nothing

- **WHEN** the user opens the sheet and closes it without planning anything
- **THEN** no undo snackbar is shown

#### Scenario: Planning then unplanning is not a change

- **WHEN** the user plans a task and then presses the same chip again before closing
- **THEN** no undo snackbar is shown, because the task is where it started

#### Scenario: Undo after further edits only touches what the sheet set

- **WHEN** a task planned in the sheet had its reminder changed again in the editor before closing
- **THEN** undo restores that task to the reminder it had when the sheet opened

### Requirement: The sheet adapts to the window it is shown in

The sheet SHALL derive its presentation from the current window rather than assuming a phone.

#### Scenario: Compact and medium windows get a bottom sheet

- **WHEN** the window is narrower than the large-window breakpoint
- **THEN** the sheet is presented as a modal bottom sheet, capped at the app's standard sheet width

#### Scenario: Large windows get the docked pane

- **WHEN** the window is at or beyond the large-window breakpoint
- **THEN** the sheet is presented as the app's docked pane, in line with the app's other adaptive
  sheets

#### Scenario: Column count follows available width

- **WHEN** the sheet is laid out at any width
- **THEN** the number of task columns is derived from the available width against a minimum card
  width, never from a hardcoded count

#### Scenario: Wide rows put the chips beside the task, not beneath it

- **WHEN** a row is laid out at 480dp or wider
- **THEN** its quick-plan chips sit to the right of the task's text, two across with the picker
  beneath them, rather than in a band under the whole card
- **AND** the checkbox, the task's text and the chips are centred on each other, because the card is
  then two columns of comparable height rather than one tall one
- **AND** the chips finish flush with the card's edge, after any expand control the row happens to
  carry and clear of it, so they line up down the list whether or not a given row has one
- **AND** the date picker spans the width of the two day chips above it, so the three read as one
  block rather than as two chips and a shorter third
- **AND** the chips stand on a surface one tone further from the page than the card carrying them,
  reaching the card's own edges and rounding only where the two halves meet, so a row reads as a
  card divided into the half that describes the task and the half that changes it
- **AND** the decision is made against the row's own width, not the window's, because the sheet caps
  its width and the grid may give a row more or less than the window suggests

#### Scenario: Chips reflow rather than truncate

- **WHEN** the quick-plan chips do not fit on one line at the current width
- **THEN** they wrap onto further lines, and no chip is clipped or ellipsised

#### Scenario: Escape dismisses on a window with a keyboard

- **WHEN** the user presses Escape while the sheet is open
- **THEN** the sheet dismisses, exactly as Close would

#### Scenario: Narrow windows stay usable

- **WHEN** the sheet is shown in a 360dp-wide window
- **THEN** every row's card, category badge and chips are laid out without horizontal overflow
