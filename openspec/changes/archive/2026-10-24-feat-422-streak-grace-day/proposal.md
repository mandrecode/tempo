## Why

A single missed day resets a habit's streak to zero, which feels unfair and discouraging. [Issue #422](https://github.com/mandrecode/tempo/issues/422) asks that missing one day keeps the streak alive, with the streak label cheering the user on instead.

## What Changes

- The streak survives a lone missed required day; only two consecutive missed required days break it. Missed days never add to the count.
- Today is still open: leaving it unchecked is not a miss, so it no longer zeroes a streak that was alive yesterday.
- When the most recent required day before today was missed and the streak survived, the streak label switches to encouraging "keep going" copy (build and quit habits, habits and chains).
- What's New announces the change.

## Capabilities

### Modified Capabilities
- `habit-history-display`: streak calculation tolerates a lone missed day and the label shows encouraging copy after one.

## Impact

`CompletionHistoryUtil` streak math, `HabitHistoryView` streak label, quit-habit streak badge (same count), localized strings (en, es), What's New, unit tests. No data or schema change.
