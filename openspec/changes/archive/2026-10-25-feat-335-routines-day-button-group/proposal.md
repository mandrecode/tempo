# Proposal

## Why

Originating issue: [#335](https://github.com/mandrecode/tempo/issues/335) asks to use button groups for the weekdays bar in the Routines screen. The bar is a horizontally scrolling row of separate chips labelled "Yesterday", "Today", "Tomorrow" and full weekday names. On a phone, only three or four days are visible at once, and the selection is a chip style that matches nothing else in the app since #115 moved repeat days to expressive toggles.

## What Changes

- The Routines day bar becomes a single-select, **connected** Material 3 Expressive `ButtonGroup` of seven segments (yesterday through five days ahead, unchanged). Each segment shows the short weekday over the day of the month. All seven fit the width without scrolling.
- The selected day uses the checked toggle shape and the primary container colour. Pressing a segment widens it as in other expressive groups. Today carries a small dot, in the same place on every segment so the labels stay aligned.
- Each segment is announced with its relative name and date ("Today, Sunday 25") and the radio-button role. Haptics on selection are kept.
- What's New: the entry becomes `434-380-115-335` (en, es).

### Non-goals

- No change to which days are offered or to what selecting a day does.
- The other selector rows are handled by #336.

## Capabilities

### New Capabilities
- `routines-day-filter`: how the Routines screen's day bar looks and behaves.

### Modified Capabilities

None.

## Impact

- UI: `features/routines/presentation/components/sections/DayFilterRow.kt` (rewritten; the private `DayFilterChip` is removed), its debug previews.
- Tests: new instrumented `DayFilterRowTest`.
- Strings: What's New (en, es).
