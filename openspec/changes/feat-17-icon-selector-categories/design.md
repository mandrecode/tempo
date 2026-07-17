## Context

`TempoIcon` (`app/src/main/java/com/mandrecode/tempo/core/ui/theme/TempoIcon.kt`) is a flat enum of ~36 icons. The only grouping today is nine `// Section` comments (Fitness & Sports, Health & Wellness, Food & Nutrition, Productivity & Work, Home & Daily Life, Communication & Social, Entertainment & Hobbies, Personal Development, Miscellaneous) with no runtime representation. `IconPicker` (`app/src/main/java/com/mandrecode/tempo/core/ui/components/IconPicker.kt`) shows `allIcons.take(5)` in its collapsed row (always the same five, in enum order) plus a chevron (`ExpandButton`) that toggles the whole ~36-icon `FlowRow` inline via `animateContentSize`. It's consumed by `HabitBottomSheetFormSections.kt` and `CategoryEditSheet.kt`, both of which only pass `selectedIconName` / `onSelectIcon` / `onClearIcon` — no other state leaks out of the picker today.

There is no persistence concern: `iconName` (a stable string like `"run"`) is what's stored on habits/categories, not the category. `IconCategory` is a pure UI/code grouping attribute with no database or domain-layer footprint.

## Goals / Non-Goals

**Goals:**
- Give every `TempoIcon` a runtime-queryable `IconCategory` so the picker (and the user's planned future icon additions) can group without touching UI code per icon.
- Make the collapsed row sample across categories, stable for the lifetime of one picker instance.
- Replace the inline expand-to-grid with a modal grouped by category, reusing existing modal infrastructure.

**Non-Goals:**
- Persisting or exposing `IconCategory` outside the UI layer — it's not part of the domain model and habits/categories keep storing `iconName` only.
- Building any picker/reorder UI for adding new icons to categories — the user will hand Claude new icon assets in a follow-up, and category assignment happens by editing the enum, same mechanism as today.
- Touching `TempoIcon.suggestIcon()` (word detection) — unrelated, already shipped separately.

## Decisions

**`IconCategory` as an enum with a `@StringRes` label, mirroring `TempoIcon`'s own pattern.**
`TempoIcon` already pairs an enum entry with a `@StringRes val keywordsRes: Int` resolved via `context.getString()`; giving `IconCategory` the same shape (`@StringRes val labelRes: Int`) keeps both types consistent and keeps every user-facing string in `strings.xml` per the no-hardcoded-strings rule. The nine categories become nine enum entries in the same order as today's section comments, so re-categorizing is a 1:1 mechanical mapping with no behavior change to worry about.
Alternative considered: a `Map<TempoIcon, IconCategory>` built separately. Rejected — it's an extra place that can drift out of sync when someone adds an icon and forgets the map entry; a constructor parameter on `TempoIcon` makes a missing category a compile error instead.

**`TempoIcon` gains a `category: IconCategory` constructor parameter**, assigned per icon following the existing section boundaries exactly (e.g. `FITNESS`, `RUN`, `WALK`, `SPORTS` → `IconCategory.FITNESS_SPORTS`). This is the minimal change: no icon moves, no keyword/drawable/name changes.

**Row sampling is a pure, non-Compose function**, e.g. `TempoIcon.sampleAcrossCategories(icons: List<TempoIcon>, slotCount: Int, random: Random = Random.Default): List<TempoIcon>` living next to `suggestIcon()`/`getAllIcons()` in the `TempoIcon` companion. It groups the input by `category`, shuffles each group independently (using the injected `Random` so tests are deterministic with a seeded instance), then round-robins one icon per category per pass until `slotCount` is reached or icons run out. Keeping this Compose-free lets it be unit-tested directly (this component currently has zero Compose UI tests, and adding that infra is out of scope for this change).
`IconPicker` calls it once inside `remember(selectedIconName == null) { ... }` — actually keyed on nothing extra beyond composable identity, so the sampled set is computed once per picker instance and does not reshuffle on unrelated recompositions (per the "Row stays stable across recompositions" requirement). The existing selected-icon-first behavior (icon already selected always takes the first slot) is layered on top of the sampled list, unchanged in spirit from today's code.

**Modal reuses `TempoModalBottomSheet`** (`core/ui/components/TempoModalBottomSheet.kt`), the same primitive already used elsewhere in the app for guarded-dismiss sheets with predictive back support — no new modal primitive needed. Content is `TempoIcon.getAllIcons().groupBy { it.category }`, iterated in `IconCategory.entries` order (not groupBy's incidental order) so sections always appear in the same, deliberate sequence. Each section renders a `Text` header (`stringResource(category.labelRes)`) followed by the icons in that category, reusing the existing `IconOption` composable (made internal/shared within the file rather than duplicated).

**Trigger is a plain right-arrow icon button using the existing `ic_chevron_right` drawable** (already present in `res/drawable/`, no new asset needed) instead of the `ExpandButton` chevron-up/down toggle. `isExpanded` state, `animateContentSize`, and the inline full-grid `FlowRow` branch are deleted from `IconPicker` — the row is always the collapsed/sampled view now, and "see everything" always means "open the modal."

**New strings**: a content description for the arrow trigger, a modal title, and one label per `IconCategory` (9 categories) — added to both `values/strings.xml` and `values-es/strings.xml` per the repo's localization rule. `IconPicker` stops referencing `R.string.expand`/`R.string.collapse`, but those strings stay in `strings.xml` unchanged — `TaskCard.kt` and `HabitCards.kt` both still use them for unrelated expand/collapse affordances.

## Risks / Trade-offs

- **[Risk]** Re-sampling on every fresh bottom-sheet open means the row's icons genuinely change each time a user opens "create habit," which could feel inconsistent → **Mitigation**: this is the explicitly requested behavior (issue #17 asks for randomization); stability is scoped to "one open session," not across sessions, matching the spec's "stays stable across recompositions" requirement.
- **[Risk]** Manually re-deriving each icon's category from the existing section comments could misclassify an icon → **Mitigation**: 1:1 mechanical mapping preserving current file order, plus a unit test asserting every icon has a category and every category is non-empty, plus a spot-check against the section comments during review.
- **[Trade-off]** Using `Column`/`FlowRow` per section in the modal instead of `LazyColumn` — acceptable given ~36 icons total (small, fixed-size content), keeps the implementation simpler and consistent with `IconPicker`'s existing non-lazy `FlowRow` usage.

## Migration Plan

No data migration — `IconCategory` is a compile-time UI attribute with no persisted representation. Rollout is a normal PR: implement, verify manually via the `run` skill / emulator (screenshot the collapsed row across a few recompositions and the modal), merge. Rollback is a plain revert since nothing is persisted.
