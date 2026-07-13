## Context

`HabitChainSelectionSection` anchors its leading list icon with a fixed 16 dp top inset while `HabitMultiSelector` changes its first visible row by state. A populated selector starts with the taller selected-habit row, where that inset aligns correctly; an empty selector starts with compact filter chips, leaving the icon visibly too low.

## Goals / Non-Goals

**Goals:**

- Keep the leading icon centered against the selector's first visible row in both empty and populated states.
- Preserve the populated-state layout and all selection/reordering behavior.
- Cover both states with a coordinate-based Compose UI regression test.

**Non-Goals:**

- Redesign the selector or selected-habit rows.
- Change dimensions outside the chain habits property row.
- Change domain, persistence, scheduling, or localization behavior.

## Decisions

1. Compute the icon's top inset from whether `selectedHabitIds` is empty. The empty state uses a 12 dp inset that centers a 24 dp icon against the filter chip within its 48 dp touch-target row; the populated state keeps the existing 16 dp inset that anchors it to the selected-habit row. This is preferred over vertically centering the entire parent `Row`, which would move the icon as the selector grows and break its relationship to the first row.
2. Keep the state-specific layout decision in `HabitChainSelectionSection`, where the icon and selector are composed together. `HabitMultiSelector` remains responsible only for selector content and behavior.
3. Verify alignment through Compose semantics bounds in the existing bottom-sheet instrumented test suite. This tests the user-visible geometry rather than duplicating the padding value in a unit test.

## Risks / Trade-offs

- [Risk] Material component sizing changes could make a hardcoded inset imperfect. → Mitigation: assert visible center alignment in UI tests and scope the value to this property row.
- [Risk] A conditional inset could regress the already-correct populated state. → Mitigation: add coverage for both empty and populated chain selections.
