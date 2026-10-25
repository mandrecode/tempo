# Design

## Control

The control is a `ButtonGroup` with seven `customItem`s, each a `ToggleButton`:

- `shapesFor(48.dp)`: round off, rounded-square on, with the standard pressed morph.
- `Modifier.weight(1f).animateWidth(interactionSource)`: the expressive "pressed item widens" behaviour.
- `ButtonGroupDefaults.ConnectedSpaceBetween` spacing, so the seven read as one control.
- Zero content padding, so a single initial is never clipped.
- **Placement and touch targets.** In both editors the row spans the sheet's full width, below a "Repeat on" label (habits) or the weekly chip (tasks), rather than sitting beside the 48 dp icon column. On a 360 dp phone that is 312 dp, so each day is about 43 dp wide and 48 dp tall; beside the icon it was under 38 dp. Seven 48 dp-wide targets plus gaps (≥ 348 dp) cannot fit inside the sheet's 24 dp side padding. Compose's minimum interactive size extends each toggle's touch area to 48 dp, and overlapping touch areas go to the nearest bounds, so a tap always lands on the day under the finger. `DayOfWeekSelectorTest.onACompactSheet_everyDayIsItsOwnTarget` checks this at 312 dp.
- Colours: checked uses the caller's container pair (primary container for habits, tertiary container for tasks, as before); unchecked uses `surfaceContainerHighest` so it stands out from the sheet.
- Each toggle has `Role.Checkbox` and the full day name as its content description; the visible label is the first letter of the localized short name.

## Rules

The rules are pure functions, unit-tested on the JVM:

- `displayedRepeatDays(selected, nullMeansEveryDay)`: with `nullMeansEveryDay`, `null` or empty shows all seven; otherwise `null` shows none.
- `toggledRepeatDays(...)` returns `Changed(days)` or `Refused`. A full or empty week is `Changed(null)`, which matches the existing normalisation. Turning off the last day of an every-day picker is `Refused`.

## Alternatives considered

- **Keeping an "Every day" chip next to the group.** This was rejected because the alarm idiom expresses "every day" as all days on, and the chip pushed the row past the screen width.
- **A connected segmented group (`connectedLeading/Middle/TrailingButtonShapes`).** With seven segments this reads as a single bar. The alarm-style separate circles were what the issue's screenshot shows.
