## 1. Icon categorization data model

- [x] 1.1 Add `IconCategory` enum in `TempoIcon.kt` (or a sibling file in the same package) with one entry per existing section comment (`FITNESS_SPORTS`, `HEALTH_WELLNESS`, `FOOD_NUTRITION`, `PRODUCTIVITY_WORK`, `HOME_DAILY_LIFE`, `COMMUNICATION_SOCIAL`, `ENTERTAINMENT_HOBBIES`, `PERSONAL_DEVELOPMENT`, `MISCELLANEOUS`), each with a `@StringRes val labelRes: Int`.
- [x] 1.2 Add the 9 category label strings to `app/src/main/res/values/strings.xml` and `values-es/strings.xml`.
- [x] 1.3 Add a `category: IconCategory` constructor parameter to `TempoIcon` and assign it per entry, following the current section boundaries exactly (no icon moves categories from its existing comment grouping).
- [x] 1.4 Remove the now-redundant `// Section` comments once every entry carries an explicit `category` (or keep them as a visual aid — decide during implementation, not a behavior change either way).

## 2. Randomized row sampling

- [x] 2.1 Add `TempoIcon.sampleAcrossCategories(icons: List<TempoIcon>, slotCount: Int, random: Random = Random.Default): List<TempoIcon>` in the `TempoIcon` companion: group by `category`, shuffle each group with the injected `random`, round-robin one icon per category per pass until `slotCount` is reached or icons are exhausted.
- [x] 2.2 Unit test `sampleAcrossCategories` (new `TempoIconTest` cases): distinct categories when `slotCount <= categoryCount`, no duplicate icons when wrapping, deterministic output with a seeded `Random` for a given input.
- [x] 2.3 Unit test the categorization invariants: every `TempoIcon.entries` value has a non-null `category`; every `IconCategory.entries` value has at least one `TempoIcon` assigned to it.

## 3. IconPicker row + trigger rework

- [x] 3.1 In `IconPicker.kt`, replace the `allIcons.take(firstRowIconsCount)` branch with a `remember { }`-cached call to `sampleAcrossCategories`, keeping the existing selected-icon-first behavior layered on top.
- [x] 3.2 Remove `isExpanded` state, `animateContentSize`, and the inline full-grid `FlowRow` branch — the row is always the collapsed/sampled view.
- [x] 3.3 Replace `ExpandButton` with a right-arrow trigger button using the existing `ic_chevron_right` drawable; wire it to open the new modal (task 4) instead of toggling `isExpanded`.
- [x] 3.4 Add/verify content-description and modal-title strings in both locales.

## 4. Category-grouped modal

- [x] 4.1 Add a modal composable (in `IconPicker.kt` or a new `IconCategoryPickerSheet.kt` in the same package) built on `TempoModalBottomSheet`, listing `TempoIcon.getAllIcons().groupBy { it.category }` iterated in `IconCategory.entries` order, each section with a `Text` header (`stringResource(category.labelRes)`) followed by that category's icons.
- [x] 4.2 Reuse the existing `IconOption` composable for icons inside the modal (share it within the file rather than duplicating).
- [x] 4.3 Wire icon taps inside the modal to call `onSelectIcon` and dismiss the modal; wire outside-tap/back-gesture dismissal to close without changing selection.

## 5. Verification

- [x] 5.1 `./gradlew testDebugUnitTest --tests "com.mandrecode.tempo.core.ui.theme.TempoIconTest"` — new categorization + sampling tests pass.
- [x] 5.2 `./gradlew ktlintFormat` and `./gradlew :app:detekt` — clean, no new baseline suppressions.
- [x] 5.3 `./gradlew lintDebug` — no `MissingTranslation`/`ExtraTranslation` for the new strings.
- [x] 5.4 Manual verification via the `run` skill / emulator: open the habit creation sheet a few times and confirm the row samples different icons per open but stays stable while typing; open the modal and confirm all icons appear grouped under category headings; select an icon from the modal and confirm it becomes the picker's selection.
- [x] 5.5 `openspec validate feat-17-icon-selector-categories` before considering the change ready to archive.
