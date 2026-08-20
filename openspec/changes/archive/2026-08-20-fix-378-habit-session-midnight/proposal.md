## Why

After midnight, the Focus session screen continues to observe the previous calendar day's agenda. Habit completion updates are written for the new day, so the visible habit state does not refresh and appears stuck ([#378](https://github.com/mandrecode/tempo/issues/378)).

## What Changes

- Refresh the Focus agenda and its derived day state when the local calendar day changes while the screen remains open.
- Ensure habit toggles after midnight update the currently displayed day's completion state.
- Add regression coverage for a Focus screen that spans midnight.

Non-goals: changing task-completion behavior, habit scheduling rules, or the Focus session UI layout.

## Capabilities

### New Capabilities

- `focus-agenda-day-freshness`: Keeps the open Focus agenda aligned with the current local calendar day.

### Modified Capabilities

- None.

## Impact

- Affects Focus presentation state observation and its unit tests.
- No API, persistence schema, dependency, or adaptive-layout changes.
