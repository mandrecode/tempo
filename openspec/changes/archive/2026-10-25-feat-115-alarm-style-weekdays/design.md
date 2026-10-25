# Design

## Control

The control is a `ButtonGroup` with seven `customItem`s, each a `ToggleButton`:

- `shapesFor(48.dp)`: round off, rounded-square on, with the standard pressed morph.
- `Modifier.weight(1f).animateWidth(interactionSource)`: the expressive "pressed item widens" behaviour.
- `ButtonGroupDefaults.ConnectedSpaceBetween` spacing, so the seven read as one control.
- Zero content padding, so a single initial is not clipped on a 360 dp phone (each button is about 36 dp wide in the editor row).
- Colours: checked uses the caller's container pair (primary container for habits, tertiary container for tasks, as before); unchecked uses `surfaceContainerHighest` so it stands out from the sheet.
- Each toggle has `Role.Checkbox` and the full day name as its content description; the visible label is the first letter of the localized short name.

## Rules

The rules are pure functions, unit-tested on the JVM:

- `displayedRepeatDays(selected, nullMeansEveryDay)`: with `nullMeansEveryDay`, `null` or empty shows all seven; otherwise `null` shows none.
- `toggledRepeatDays(...)` returns `Changed(days)` or `Refused`. A full or empty week is `Changed(null)`, which matches the existing normalisation. Turning off the last day of an every-day picker is `Refused`.

## Alternatives considered

- **Keeping an "Every day" chip next to the group.** This was rejected because the alarm idiom expresses "every day" as all days on, and the chip pushed the row past the screen width.
- **A connected segmented group (`connectedLeading/Middle/TrailingButtonShapes`).** With seven segments this reads as a single bar. The alarm-style separate circles were what the issue's screenshot shows.
