## Why

In the habit-chain editor, the habits property icon and its selector are vertically misaligned before any habit has been selected. Issue [#49](https://github.com/mandrecode/tempo/issues/49) reports that the same components align correctly once selected habit rows appear, so the empty state should preserve that alignment.

## What Changes

- Align the chain habits property icon with the selector content when the selected-habit list is empty.
- Preserve the existing alignment and behavior once one or more habits are selected.
- Add focused UI coverage for the empty and populated chain-selection states.
- Non-goals: redesigning chain selection, changing selection/reordering behavior, or modifying spacing in unrelated bottom-sheet properties.

## Capabilities

### New Capabilities

- `habit-chain-selector-alignment`: Defines stable alignment for the habit-chain selector across empty and populated selection states.

### Modified Capabilities

None.

## Impact

- Affected UI: the habit-chain selection section in the routines bottom sheet.
- Affected verification: Compose UI tests and/or previews for chain selection states.
- No API, persistence, domain, dependency, or localization changes are expected.
