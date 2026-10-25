# Proposal

## Why

Originating issue: [#115](https://github.com/mandrecode/tempo/issues/115) asks to rework habit periodicity "with alarm-style weekdays, using buttonGroup", like the day picker in the Clock app's alarms. The current picker is a horizontally scrolling row of chips with full day names and a separate "Every day" chip. On a phone it does not fit, and it reads as a list rather than a week.

## What Changes

- The shared repeat-day picker (`DayOfWeekSelector`) becomes a Material 3 Expressive `ButtonGroup` of seven toggle buttons, one per day, labelled with the day's initial (full name for accessibility, role checkbox). An off day is a circle; an on day morphs to a rounded square; a pressed day widens and squeezes its neighbours. It fits the editor row without scrolling.
- **Habits and chains** (alarm semantics): "every day" is all seven days on, and the separate "Every day" chip is gone. Turning one day off from every day leaves the other six. The last day left on cannot be turned off, because an empty week would mean "every day" again. Storage is unchanged: a full or empty week is stored as `null`.
- **Weekly tasks** use the same control. Nothing picked still means "the reminder's day", shown with no day on.
- What's New: the entry becomes `434-380-115` (en, es). The now-unused `everyday` string is removed.

### Non-goals

- No change to how repeat days are stored, scheduled, or shown on cards.
- The Routines day bar and the other selector rows are handled by #335 and #336.

## Capabilities

### New Capabilities
- `repeat-day-picker`: how repeat days are picked for habits, chains and weekly tasks.

### Modified Capabilities

None.

## Impact

- UI: `core/ui/components/DayOfWeekSelector.kt` (rewritten; `showAllDaysOption` replaced by `nullMeansEveryDay`), the repeat row in `HabitBottomSheetFormSections.kt` and the weekly section in `TaskBottomSheetFormSections.kt` (beside the icon column, scrolling when too narrow), debug previews.
- Strings: What's New (en, es); `everyday` removed.
- Tests: new unit `DayOfWeekSelectorTest` (toggle rules) and instrumented `DayOfWeekSelectorTest`.
