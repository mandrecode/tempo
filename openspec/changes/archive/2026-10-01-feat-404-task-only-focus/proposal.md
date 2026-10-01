## Why

Habits and chains clutter Focus and distract from choosing task work across categories. [Issue #404](https://github.com/mandrecode/tempo/issues/404) makes Focus a task-only view of the day.

## What Changes

- Remove habits and chains from the Focus agenda and its editing/completion interactions.
- Keep Today, unfinished Overdue, task sessions, and the existing undated-task planning picker.
- Count only agenda tasks in progress and newly recorded daily activity; preserve historical aggregate records.
- Update empty-state copy, onboarding announcement, documentation, fixtures, and regression coverage.
- Carry the task-only contract into the active Spaces proposal.

## Capabilities

### New Capabilities
- `task-only-focus`: task membership, task-only progress/activity, and deliberate planning of undated work.

### Modified Capabilities
- `focus-agenda-day-freshness`: remove obsolete habit-completion requirements while retaining one current-day observer for tasks.

## Impact

Focus domain and presentation, the daily activity recorder, routine completion's recorder dependency, tests/previews, localized resources, What's New, and Focus documentation. No new dependencies or database migration. Historical totals remain snapshots of the behavior at recording time. Spaces implementation and undated session support are outside scope.
