## Why

Expanded task descriptions currently cause the completion control and trailing actions to drift vertically toward the middle of the card, making the card harder to scan and interact with. GitHub issue [#44](https://github.com/mandrecode/tempo/issues/44) calls for a compact collapsed description and stable top alignment while task content expands.

## What Changes

- Keep collapsed task descriptions to a single ellipsized line.
- Allow overflowing descriptions to expand without moving the completion control or trailing actions away from the top content row.
- Add focused Compose UI coverage and preview coverage for collapsed and expanded long-description states.
- Non-goals: changing task editing, subtask expansion behavior, metadata contents, or the visual styling of habit and habit-chain cards.

## Capabilities

### New Capabilities

- `task-card-presentation`: Defines collapsed and expanded task-description layout behavior and alignment of task-card controls.

### Modified Capabilities

None.

## Impact

- Affects the task-card Compose implementation, its debug previews, and instrumented Compose tests in the `:app` module.
- No domain, data, persistence, navigation, API, or dependency changes.
