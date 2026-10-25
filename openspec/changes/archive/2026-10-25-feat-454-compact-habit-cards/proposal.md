# Proposal

## Why

Originating issue: [#454](https://github.com/mandrecode/tempo/issues/454).

Habit cards take more room than they need. A habit is usually a quick, recurring check-off, clearer and more common than a task, yet a standalone habit row was 88dp tall (56dp checkbox and 16dp padding), taller than a task row. Chains hid their habits behind an expand arrow and collapsed again once finished. Since those habits are exactly what you tick off, that cost an extra tap on every visit.

## What Changes

- **Habits:** the row is more compact, about 72dp tall: a 48dp checkbox and 16/12dp padding. A first pass at 56dp (40dp checkbox) proved too small on device, so this is a middle ground between that and the original 88dp. Descriptions still collapse to one line with the expand toggle.
- **Chains:** they always list their habits; there is no expand/collapse. The card is rebuilt in zones so nothing overlaps:
  - Header: title and an optional one-line description, with nothing else on the right.
  - Progress: a short 6dp bar with the done/total count beside it. Once every habit is done, the two become one "All done · n/n" chip.
  - Habits: exactly the same row as a standalone habit (same checkbox, padding, title style and description behaviour). Only the colours come from the chain.
  - Add habit: the list's last row, a dashed circle where the checkbox would be plus an "Add habit" label, replacing the header icon button.
  - The faded chain icon stays top-end, now uncovered, at 120dp (was 180dp).
- Removed with the expansion: `RoutinesContract.UiState.expandedChainIds`, `UiEvent.ToggleChainExpanded`, the auto-collapse when a chain is completed, the future-date habit-icon summary (the habits are listed now), and the `expand_chain`/`collapse_chain` strings. New string: `habit_chain_card_all_done` (en, es).

### Non-goals

- No change to quit-habit cards, the timeline column, colours, or completion rules.
- No change to the habit or chain editors.

## Capabilities

### New Capabilities
- `habit-card-layout`: size and content of habit and habit-chain cards in Routines.

### Modified Capabilities

None.

## Impact

- UI: `HabitCards.kt` (`HabitItem` gains `checkboxSize`), `HabitChainCard.kt`, `RoutinesContent.kt`.
- Presentation: `RoutinesContract`, `RoutinesViewModel`, `RoutinesViewModelActions`.
- Resources: `expand_chain`/`collapse_chain` removed, `habit_chain_card_all_done` added (en, es).
- Tests: new `CompactHabitCardsTest`; `RoutinesViewModelTest`, `RoutinesContentTest`, `RoutineCardModifierTest` drop the expansion state.
- No domain, data, or persistence changes.
