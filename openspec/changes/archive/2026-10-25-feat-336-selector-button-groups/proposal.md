# Proposal

## Why

Originating issue: [#336](https://github.com/mandrecode/tempo/issues/336) asks to replace the current rows of selector buttons with button groups, naming periodicity, priority and the categories selector. These rows use a hand-rolled connected chip (`ExpressiveChip`) inside scrolling `LazyRow`s. The repeat-day picker (#115) and the Routines day bar (#335) are now Material 3 Expressive button groups, and the editors' other choices should match.

## What Changes

- A shared `ConnectedSelectorButton` (a Material 3 Expressive `ToggleButton` with the connected leading/middle/trailing button-group shapes, a 48 dp minimum height, the radio-button role, and haptics) and a `SelectorButtonGroup` (a fill-width `ButtonGroup` of them, with press-to-widen) are added in `core/ui/components`.
- **Task editor**
  - **Priority** (High/Medium/Low) is a fill-width `SelectorButtonGroup`. The selected option keeps its priority colour, and tapping it again clears the priority. The per-option flag icon is dropped because the row's icon is the flag and three labels need the room.
  - **Periodicity** (Hourly…Yearly, with interval-aware labels) and **category** (any number of categories) keep their scrolling rows and auto-scroll to the selection, but each option is now a `ConnectedSelectorButton`. They can outgrow the width, which `ButtonGroup` would hide in an overflow menu.
  - **Monthly day option** (same day / first / last) is a fill-width `SelectorButtonGroup` in the tertiary colours.
- **Habit editor:** the Habit / Habit chain switch is a `SelectorButtonGroup`.
- The test tags `taskPriorityChip_*` and `taskPeriodicityChip_*` are kept.
- What's New: the entry becomes `434-380-115-335-336` (en, es).

### Non-goals

- Settings' selectors and the single "Add reminder" chip are unchanged (`ExpressiveChip` stays for them).
- No change to what any option does.

## Capabilities

### New Capabilities
- `editor-selector-groups`: how the task and habit editors present their single-choice options.

### Modified Capabilities

None.

## Impact

- UI: new `core/ui/components/SelectorButtonGroup.kt`; `TaskBottomSheetFormSections.kt`; `HabitBottomSheetFormSections.kt`; debug previews.
- Strings: What's New (en, es).
- Tests: new instrumented `SelectorButtonGroupTest`; `TaskBottomSheetTest` and `HabitBottomSheetTest` unchanged and passing.
