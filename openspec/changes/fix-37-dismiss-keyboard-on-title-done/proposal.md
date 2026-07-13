## Why

Pressing the keyboard Done action in Tempo's title and name fields clears Compose focus but leaves the software keyboard visible, making the action appear ineffective. [GitHub issue #37](https://github.com/mandrecode/tempo/issues/37) requests the smooth dismissal behavior already used by Didi.

## What Changes

- Dismiss the software keyboard and clear field focus when Done is pressed in task, habit, habit-chain, and category editors.
- Remove the unreachable `QuickTaskEntryBar` and tests that mounted it outside the production screen hierarchy.
- Add regression coverage for Done-action behavior on the affected fields.
- Keep existing title wrapping, validation, submission, and persistence behavior unchanged.

Non-goals:

- Changing title fields to single-line inputs or changing their length limits.
- Changing description-field Enter behavior.
- Reintroducing or redesigning quick task entry.
- Introducing a new shared input component or dependency.

## Capabilities

### New Capabilities

- `editor-keyboard-dismissal`: Defines Done-action keyboard and focus behavior for primary editor text fields.

### Modified Capabilities

None.

## Impact

- Affects Compose presentation code for task, habit, habit-chain, and category inputs in `:app`.
- Removes an orphaned task-entry component and its dedicated test coverage.
- Extends existing Compose instrumented tests for the affected editors.
- Does not affect domain/data layers, navigation, storage, public APIs, or dependencies.
