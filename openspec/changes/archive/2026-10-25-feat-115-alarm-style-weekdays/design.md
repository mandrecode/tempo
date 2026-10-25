# Design

## Control

The control is a `ButtonGroup` with seven `customItem`s, each a `ToggleButton`:

- `shapesFor(48.dp)`: round off, rounded-square on, with the standard pressed morph.
- `Modifier.weight(1f).animateWidth(interactionSource)`: the expressive "pressed item widens" behaviour.
- `ButtonGroupDefaults.ConnectedSpaceBetween` spacing, so the seven read as one control.
- Zero content padding, so a single initial is never clipped.
- **Placement and touch targets.** In both editors the row sits beside the 48 dp icon column like every other row (after review, a full-width row below a "Repeat on" label stood out from the rest of the sheet). Beside the icon a 412 dp phone leaves about 311 dp, so each day is about 43 dp wide and 48 dp tall. When a day would be narrower than 40 dp (a 360 dp phone leaves 264 dp, under 38 dp each, or a large display size), the days keep a full 48 dp width in a row that scrolls instead of being squeezed. In the shared row, Compose's minimum interactive size extends each toggle's touch area to 48 dp and overlapping touch areas go to the nearest bounds, so a tap always lands on the day under the finger. `DayOfWeekSelectorTest` checks both widths.
- Colours: checked uses the caller's container pair (primary container for habits, tertiary container for tasks, as before); unchecked uses `surfaceContainerHighest` so it stands out from the sheet.
- Each toggle has `Role.Checkbox` and the full day name as its content description; the visible label is the first letter of the localized short name.

## Rules

The rules are pure functions, unit-tested on the JVM:

- `displayedRepeatDays(selected, nullMeansEveryDay)`: with `nullMeansEveryDay`, `null` or empty shows all seven; otherwise `null` shows none.
- `toggledRepeatDays(...)` returns `Changed(days)` or `Refused`. A full or empty week is `Changed(null)`, which matches the existing normalisation. Turning off the last day of an every-day picker is `Refused`.

## Alternatives considered

- **Keeping an "Every day" chip next to the group.** This was rejected because the alarm idiom expresses "every day" as all days on, and the chip pushed the row past the screen width.
- **A connected segmented group (`connectedLeading/Middle/TrailingButtonShapes`).** With seven segments this reads as a single bar. The alarm-style separate circles were what the issue's screenshot shows.
