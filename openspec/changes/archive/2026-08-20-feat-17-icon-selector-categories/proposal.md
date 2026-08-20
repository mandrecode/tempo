## Why

The habit/category icon picker (`IconPicker`) always shows the same first five icons (enum declaration order) in its collapsed row, and its "expand" affordance inlines the entire ~36-icon grid into the form instead of helping users browse by topic. [Issue #17](https://github.com/mandrecode/tempo/issues/17) asks for a randomized default row that samples across topics, and a right-arrow that opens a modal with icons organized into categories, so the picker stays compact while still being easy to browse. This proposal covers that UI/data-model rework; new icon assets and their category placement are a follow-up once the user supplies them.

## What Changes

- Introduce a formal `IconCategory` grouping for `TempoIcon` (replacing the plain `// Section` comments in the enum) so category membership is queryable at runtime by the picker UI and is easy to extend as new icons are added later.
- Randomize `IconPicker`'s default (collapsed) row: instead of always showing the first N icons in enum order, fill the row by drawing one random icon per category (wrapping to a second pass if there are more slots than categories) so the default row samples variety instead of repeating the same section.
- Replace the collapse/expand chevron (`ExpandButton`, `ic_expand_more`/`ic_expand_less`) with a trailing right-arrow (`ic_chevron_right`) that opens a modal bottom sheet (reusing the existing `TempoModalBottomSheet`) showing every icon grouped under its category heading.
- Selecting an icon inside the modal behaves the same as selecting one in the row: it calls `onSelectIcon` and dismisses the modal.
- **BREAKING** (internal only): `IconPicker`'s inline expand-to-grid behavior is removed in favor of the modal. No public signature change — `IconPicker` already only exposes `selectedIconName`, `onSelectIcon`, `onClearIcon`.

### Non-goals

- Adding new icon assets or expanding the icon set — the user will supply new icons in a follow-up change, at which point they get sorted into these categories (or new categories are added).
- Changing `TempoIcon.suggestIcon()` / keyword-based word-detection behavior — that was handled separately in #17's word-detection PR.
- Reworking `CategoryEditSheet`'s or `HabitBottomSheetFormSections`' own layout beyond consuming the updated `IconPicker` as-is.

## Capabilities

### New Capabilities
- `icon-category-picker`: icon categorization data model (each `TempoIcon` belongs to exactly one `IconCategory`) plus the picker UI behavior built on it — randomized default row sampling one icon per category, and a category-grouped modal reachable via a right-arrow trigger.

### Modified Capabilities
(none — no existing `openspec/specs/` capability currently documents `IconPicker` behavior)

## Impact

- `app/src/main/java/com/mandrecode/tempo/core/ui/theme/TempoIcon.kt`: add `IconCategory` enum and a `category` property per `TempoIcon` entry.
- `app/src/main/java/com/mandrecode/tempo/core/ui/components/IconPicker.kt`: randomized-row selection logic, replace `ExpandButton` with a right-arrow trigger, add the category-grouped modal composable (new file or same file).
- `app/src/main/res/values/strings.xml` and `values-es/strings.xml`: new strings for the modal title / category section headers / trigger content description (the existing `expand`/`collapse` strings become unused by this component).
- `app/src/test/java/com/mandrecode/tempo/core/ui/theme/TempoIconTest.kt`: coverage for category assignment (every icon has exactly one category, every category has at least one icon).
- New unit tests for the randomized-row selection logic (kept as a pure, non-Compose-dependent function so it's testable without Compose UI test infra, which this component currently has none of).
- Consumers `HabitBottomSheetFormSections.kt` and `CategoryEditSheet.kt`: no code changes expected since `IconPicker`'s public parameters are unchanged.
