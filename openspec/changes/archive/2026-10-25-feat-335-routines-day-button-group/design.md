# Design

The bar is a `ButtonGroup` with seven `customItem`s, each a `ToggleButton`:

- Shapes: `ButtonGroupDefaults.connectedLeading/Middle/TrailingButtonShapes()`. The group reads as one segmented control, and the checked segment becomes fully rounded.
- Spacing: `ButtonGroupDefaults.ConnectedSpaceBetween`. Each segment uses `weight(1f)` and is 56 dp tall, with zero content padding so "Wed" plus "28" fit about 44 dp.
- Tapping the selected segment does nothing, which makes it single-select.
- Colours match the repeat-day picker from #115: `surfaceContainerHighest` when off, the primary container pair when on.
- Today's marker is a 4 dp dot drawn on every segment (transparent except today), so text baselines line up across the row.

## Alternatives considered

- **Keeping relative labels ("Yesterday", "Tomorrow") on the segments.** These do not fit in a seven-way split on a 360 dp phone. They are still announced to accessibility services.
- **Separate (unconnected) toggles like the repeat-day picker.** A date filter is a single choice among neighbours, which the connected segmented style communicates. The repeat-day picker is a multi-select of independent days.
