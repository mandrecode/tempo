## Context

Settings currently follows the required `SettingsScreen`/`SettingsContent` split and uses the existing Settings MVI contract. The Didi reference attached to GitHub issue #47 keeps the same user-facing controls but changes the screen hierarchy: a large title, compact section labels, pill-like grouped cards, and larger centered row content with clear icon affordances.

This is a presentation-only Settings change. The screen is explicitly allowed to remain thin and presentation/data-light, so no domain layer or new use cases are needed.

## Goals / Non-Goals

**Goals:**

- Match the Didi Settings reference with a large page title, section labels, rounded cards, and component-led rows.
- Match Didi's page/card surface contrast locally in Settings, using the muted surface for the screen and the whitest/lightest surface for cards.
- Prepare a Settings onboarding row and click callback for the upcoming onboarding destination.
- Preserve all current Settings actions and persisted state updates.
- Keep strings resource-backed and previews in `src/debug/`.
- Keep reusable visual helpers local to Settings unless another feature starts sharing the same pattern.

**Non-Goals:**

- No full app-wide color-system rollout for issue #50.
- No changes to settings persistence, navigation, repositories, or domain models.
- No new dependencies or design-system migration.
- No changes to available Settings options beyond their layout and visual treatment.

## Decisions

1. Keep implementation inside `SettingsContent.kt`.
   - Rationale: The change is limited to one screen's presentation. Extracting shared design-system components before another caller exists would add indirection without reuse.
   - Alternative considered: Add core UI Settings card components. Rejected until issue #50 proves the pattern belongs app-wide.

2. Model the Didi reference with local composables for page header, sections, cards, rows, and segmented controls.
   - Rationale: These helpers make the layout readable while preserving the existing MVI contract and event flow.
   - Alternative considered: Continue composing directly inside each section. Rejected because the visual pattern repeats enough to justify small local helpers.

3. Use Didi's Material3 top app bar API and pin.
   - Rationale: The reference implementation uses `TwoRowsTopAppBar`, which is only available to Tempo when Material3 is explicitly pinned to Didi's `1.5.0-alpha19` artifact instead of relying on the Compose BOM version.
   - Alternative considered: Approximate with public `LargeTopAppBar` on the existing BOM. Rejected because it does not use the same API/behavior as Didi.

4. Use Material theme tokens and surface containers for light/dark card treatment.
   - Rationale: The reference depends on contrast between screen background and cards, but Compose code must remain theme-aware and avoid hardcoded colors.
   - Alternative considered: Hardcode dark reference colors. Rejected because Tempo supports dynamic and Tempo color schemes in light and dark modes.

5. Preserve existing external-intent behavior in the content layer for this change.
   - Rationale: Moving intents out of `SettingsContent` would be a behavioral architecture cleanup unrelated to the requested visual redesign.
   - Alternative considered: Introduce effects for notification, language, review, and feedback actions. Rejected as scope creep for the draft PR.

6. Add the onboarding row as a prepared navigation callback.
   - Rationale: Didi's Settings includes a View onboarding item. Tempo does not have the onboarding destination yet, but plumbing the callback now keeps Settings ready for the upcoming flow.
   - Alternative considered: Hide the row until onboarding exists. Rejected because the user asked to prepare the section flow now.

## Risks / Trade-offs

- Didi reference spacing may not map exactly to Material typography across devices -> Use responsive Compose layout, stable touch targets, and preview both light and dark states on Pixel 9.
- Local helper composables could become duplicated when issue #50 generalizes the card approach -> Keep names Settings-specific so extraction later is straightforward.
- Larger rows and title reduce visible content above the fold -> Keep section spacing compact enough that primary settings remain quickly scannable.
