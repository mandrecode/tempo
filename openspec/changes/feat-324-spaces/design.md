## Context

Tempo stores one undifferentiated set of categories, habits and chains. `TaskEntity.categoryId` points at a `CategoryEntity`, and a reserved row — `DEFAULT_INBOX_CATEGORY_ENTITY`, with `id = -1` — is the fallback category for tasks created without one. Enabled tabs and the default tab are global `SharedPreferences` values. Nothing in the model expresses "which part of my life is this".

Two pieces of recent work constrain the design. PR #315 generalised navigation from two hardcoded tabs to an N-tab registry and added Focus with `daily_focus_activity`, which makes per-space tab configuration cheap. The adaptive work (`feat-adaptive-large-screen-ux`, `feat-151-nav3-supporting-pane`) established a floating rail rather than a bottom navigation bar, a labeled rail at ≥1200dp, a docked supporting pane above that, and a planned `pointer-keyboard-input` capability.

Milestone 2.0 will give every entity sync identity ([#30](https://github.com/mandrecode/tempo/issues/30)) and specify a sync contract ([#31](https://github.com/mandrecode/tempo/issues/31)). Spaces add an entity above the one that work is built around, which is why this lands first.

## Goals / Non-Goals

**Goals:**

- A context the user is only ever in one of at a time, which excludes the others rather than merely grouping them
- Zero visible change for anyone who never creates a second space
- One Room migration, verified against real pre-migration data
- Close the entity model before #30 and #31, so the sync work covers spaces in a single pass

**Non-Goals:**

- Sharing a whole space. Category sharing ([#9](https://github.com/mandrecode/tempo/issues/9)) is unaffected
- Per-space cloud sync opt-in
- Space colour theming the app
- Cross-space search or an "all spaces" listing
- Sections within categories — that is [#61](https://github.com/mandrecode/tempo/issues/61), a separate layer
- Any backend work

## Decisions

### Partition, not tagging

A category belongs to exactly one space, enforced by a non-null `spaceId`.

*Alternative considered:* many-to-many, letting a category appear in several spaces. Rejected because it makes "what am I not seeing right now?" an item-by-item question, which defeats the purpose. Partition is also the reversible direction — partition to tagging is an additive change later, tagging to partition is destructive.

The known cost is the user who wants one list visible everywhere. That is accepted; adding an "always visible" flag would quietly turn partition back into tagging.

### Task space is derived, not denormalised

A task's space is reached through its category. No `spaceId` on `TaskEntity`.

*Alternative considered:* denormalising `spaceId` onto tasks to avoid a join when filtering. Rejected as premature — it introduces a second source of truth that can disagree with the category, and the join is indexed. Revisit only if profiling shows it matters.

### Remove the sentinel rather than seed one per space

`DEFAULT_INBOX_CATEGORY_ENTITY` uses `id = -1`, referenced in `Task.kt`, `TaskEntity.kt`, `CategoryEntity.kt` and `BackupRepositoryImpl.kt`. With several spaces there must be several default categories, so a single reserved id cannot work.

Default status moves entirely to the existing `isDefault` metadata, scoped by `spaceId`. Resolution becomes "the default category of this space" rather than "the row with id −1".

*Alternative considered:* keeping the sentinel for the default space only, and using metadata for the rest. Rejected — two mechanisms for one concept, and the sentinel would keep leaking into call sites.

This is the most invasive part of the change and the most likely source of regressions, because the sentinel is currently load-bearing in code that predates it being questionable.

### Active space is device-local

Stored in `SharedPreferences`, never in the database, and explicitly excluded from backup and from any future sync payload.

The motivating case is a user with a phone on Personal and a laptop on Work at the same time. Syncing the selection would make switching context on one device yank the other out from under them. Space *definitions* are shared state; the *current selection* is a per-device UI concern.

### Tab configuration moves from global preferences to the space

`enabledTabs` and `defaultTab` become columns on `SpaceEntity` rather than global preference keys, so they travel with the space definition.

*Alternative considered:* a separate per-space preferences store keyed by space id. Rejected as more moving parts for values that are small, few, and conceptually part of what a space *is*.

Migration copies the current global values onto the seeded default space, then leaves the legacy keys in place rather than deleting them, matching the rollback approach PR #315 used.

### Focus is cross-space, with the active space first

The Focus agenda lists the active space in normal priority order, then a "From other spaces" divider, then everything else. Up next and any running session stay scoped to the active space.

*Alternatives considered:*
- **Space-scoped Focus.** Rejected: Focus is sold as "what today actually is", and showing a partial day while claiming to be the whole one is dishonest. It would also force constant space switching to see what is left in the day.
- **Sectioning the agenda by space.** Rejected: it fractures the existing screen structure, which already sections by item type.

Keeping the *actionable* surfaces scoped while the *agenda* stays complete gives awareness without distraction. With one space the divider never renders, so the invisibility rule needs no special casing, and Focus can remain a per-space configurable tab.

### Chain and habit space is an enforced invariant

`HabitChainMemberEntity` links chains to habits. A chain in Work holding a habit in Personal is incoherent, so habit selection is filtered to the chain's space and any operation that would break the invariant is rejected at the repository boundary rather than guarded only in the UI.

## Risks / Trade-offs

**Removing the sentinel breaks something subtle** → It is referenced in four files and its behaviour is covered by the existing `default-category-persistence` spec. Treat that spec's scenarios as the regression suite, extend them for the per-space case, and land the sentinel removal as its own reviewable step rather than inside a larger change.

**A missed query leaks another space's content** → Filtering has to be applied at the repository boundary, not per call site. Any query returning categories, habits or chains without a space predicate is a defect; tests should assert absence of other-space content, not just presence of the active space's.

**The migration runs over real user data** → Additive columns and a seeded default space, with no destructive step. Verify against a real pre-migration database, not only a synthetic one, following the approach already used for the database encryption migration.

**The feature makes the app feel heavier** → Mitigated by the invisibility rule: with one space there is no switcher, no badge, no onboarding mention, and no settings entry beyond creating a second space. This has to hold strictly or the mitigation fails.

**Widget and notification entry points have no active space** → The quick-task widget must resolve a target space explicitly, chosen when the widget is placed. Notification taps switch to the item's space rather than assuming the current one.

**Scope creep toward a Workspace product** → The scoping sentence is "categories organise, spaces exclude". Space sharing, per-space sync and cross-space views are all recorded as non-goals precisely because each is individually reasonable and collectively a different app.

## Migration Plan

1. Add `SpaceEntity` and its DAO; add nullable `spaceId` to categories, habits and chains
2. Seed one default space, adopting the current global tab configuration; assign every existing row to it; make `spaceId` non-null
3. Convert default-category resolution from the `id = -1` sentinel to `isDefault` scoped by space
4. Apply space filtering at the repository boundary
5. Add the switcher and management surfaces, which only become reachable once a second space exists
6. Extend the backup schema with spaces and `spaceId`, keeping older backups importable into the default space

Rollback: every schema step is additive, and the legacy tab preference keys are left in place so a reverted build reads them again. A reverted install sees its data unchanged because everything is in one space.

## Resolved Questions

**When a space is deleted, does its content move or go with it? → The user decides, per deletion.**

Neither default is right for everyone: deleting a finished project's space should take its content, while retiring a space you no longer switch to should not lose the tasks in it. Rather than pick one and be wrong half the time, the deletion flow offers both — move the content to a chosen space, or delete it along with the space — and states what each will do before the user commits. Deleting content is not reversible beyond the existing undo window, so the destructive option must never be the pre-selected one.

**Is the "From other spaces" block collapsed or expanded? → It depends on how full the active space already is, with an imminence override.**

Two orthogonal rules, each one line:

1. **Density sets the initial state.** If the active space has enough items today to fill the visible list, the block starts collapsed with a count. If the active space is sparse, it starts expanded.
2. **Imminence overrides toward expanded.** If any item below the divider has a reminder within **two hours**, the block is expanded regardless of density.

The density rule is the more interesting of the two, because it changes what the block *means*: not "the rest of your day, hidden by default", but "show me more when I am not already busy". On a packed work day the other spaces stay out of the way; on a quiet one they are worth seeing. That is a humane default and it needs no configuration.

Imminence still earns its override — a packed day is precisely when something two hours out is easiest to miss.

**Manual toggling wins while Focus is open**, and the rule recomputes next time Focus is entered. Recomputing continuously would snap a deliberately expanded block shut the moment a task was completed, which is the one behaviour guaranteed to feel broken.

The imminence window is two hours. The density threshold is **derived per window size class** rather than a single constant: "fills the visible list" is roughly five items on a compact phone and considerably more on an expanded window, and one number would collapse the block on a large screen that had ample room to show it — cutting against this feature being more useful the larger the window. Both values are provisional and cheap to change.

## Open Questions

None outstanding. The density threshold's per-size-class values are a tuning exercise for implementation rather than an open design question.
