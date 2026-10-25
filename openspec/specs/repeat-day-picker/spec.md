# repeat-day-picker Specification

## Purpose
Defines how repeat days are picked for habits, habit chains and weekly tasks.

## Requirements

### Requirement: Alarm-style day toggles
The repeat-day picker SHALL show the seven days of the week, Monday first, as individual toggle buttons in an expressive button group, each labelled with the day's initial and announced with the day's full name. The picker SHALL sit beside the editor row's icon, like the editor's other rows. When each day can be at least 40 dp wide there, all seven SHALL share the row without scrolling; otherwise each day SHALL keep a full 48 dp width and the row SHALL scroll.

#### Scenario: Weekdays only
- **WHEN** a habit repeats Monday to Friday
- **THEN** the Monday to Friday toggles are on and Saturday and Sunday are off

#### Scenario: A narrow phone
- **WHEN** the habit editor is opened on a 360 dp wide phone
- **THEN** the days keep a full 48 dp width and the row scrolls to reach Sunday

### Requirement: Every day is all days on for habits and chains
For habits and habit chains, "every day" SHALL be shown as all seven toggles on, with no separate every-day option. Turning one day off SHALL keep the other six. The last day left on SHALL NOT turn off. A full week SHALL be stored as "every day".

#### Scenario: Drop the weekend
- **WHEN** an every-day habit has Saturday and Sunday turned off
- **THEN** it repeats Monday to Friday

#### Scenario: Last day
- **WHEN** a habit repeats only on Monday and the user taps Monday
- **THEN** Monday stays on and the habit still repeats on Monday

### Requirement: Weekly tasks keep their default
For a weekly task, a picker with no day chosen SHALL show no toggle on, and the task SHALL keep repeating on its reminder's day.

#### Scenario: No day picked
- **WHEN** a weekly task has no repeat days chosen
- **THEN** no day toggle is on
