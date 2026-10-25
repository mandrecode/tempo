# Design

## Two forms of one control

- **Fits the width → `SelectorButtonGroup`**: priority (3), monthly day option (3), Habit / Habit chain (2). Every option has `weight(1f)` and `animateWidth`, so the pressed option widens into its neighbours as in #115 and #335.
- **May not fit → a `LazyRow` of `ConnectedSelectorButton`s**: categories (user-defined, unbounded) and periodicity (five options whose selected label grows, e.g. "Every 2 weeks on Mon, Wed"). A `ButtonGroup` would move what does not fit into an overflow menu, hiding a category behind "⋯". Keeping the `LazyRow` keeps horizontal scrolling and the existing auto-scroll to the selected item. The buttons carry the same connected shapes by index, so both forms look like one family.

`connectedButtonShapes(index, count)` picks `ButtonGroupDefaults.connectedLeading/Middle/TrailingButtonShapes()`, or the standalone toggle shapes for a single option (for example, only the Inbox category).

## Colours

Off: `surfaceContainerHighest` / `onSurfaceVariant`, the same as the day pickers. On: the caller's pair. Priority uses its own colour at 20% alpha with the full colour for content. Periodicity uses the primary container pair. The monthly option uses the tertiary container pair.

## Sizing

The minimum height is 48 dp and the horizontal content padding is 12 dp. With 16 dp padding plus a flag icon, "Medium" was ellipsized in the 3-way split on a 360 dp phone, so the icons were dropped (the row's own icon is the flag).
