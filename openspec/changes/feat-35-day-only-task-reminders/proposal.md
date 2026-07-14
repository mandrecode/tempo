## Why

Task reminders currently force users to choose an exact time after choosing a day, even when the day itself is all they care about. [GitHub issue #35](https://github.com/mandrecode/tempo/issues/35) calls for a faster date-only path backed by a sensible, user-configurable default time.

## What Changes

- Let users finish task reminder selection after choosing a date by using the configured default reminder time instead of manually choosing a clock time.
- Preserve the existing exact-time path for users who need a specific reminder time.
- Add a task reminder default-time setting, initialized to 09:00, and use it for subsequent date-only reminder selections.
- Show the active default time in Settings and allow users to change it with the existing time-picker experience.
- Keep stored task reminders and Android alarm scheduling based on a concrete local date and time; this change does not alter reminder delivery, recurrence rollover, or existing reminders when the preference changes.

## Capabilities

### New Capabilities

- `day-only-task-reminders`: Defines date-only task reminder selection, configurable default-time behavior, and exact-time selection.

### Modified Capabilities

None.

## Impact

- Task reminder picker UI and Tasks MVI state wiring.
- Settings UI, state, and preference observation.
- A new pure task preference contract with a SharedPreferences-backed implementation and Hilt binding.
- Localized Settings and reminder-picker strings in English and Spanish.
- Unit tests for preference persistence and ViewModel state/events, plus Compose UI coverage for the new Settings control and reminder selection actions.
- No new dependencies, Room schema changes, or reminder scheduler changes.
