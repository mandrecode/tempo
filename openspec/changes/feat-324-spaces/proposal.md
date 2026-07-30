## Why

Tempo assumes one life. A user who wants to run work alongside personal life has no way to keep them apart: every category, habit and Focus agenda is mixed into a single list, so using the app at work means looking at chores, and using it at home means looking at the backlog.

Categories cannot solve this — they organise, they do not exclude. What is missing is a context you are only ever in one of at a time, which reconfigures what the app shows and which tools it offers.

Timing matters: this adds an entity above the one milestone 2.0 is built around. Landing it before [#30](https://github.com/mandrecode/tempo/issues/30) migrates the entity model and [#31](https://github.com/mandrecode/tempo/issues/31) specifies the sync contract means spaces are covered in one pass rather than retrofitted into a shipped API.

Tracked by [#324](https://github.com/mandrecode/tempo/issues/324).

## What Changes

- **A `Space` entity** with a name, icon, colour and its own tab configuration. Categories, habits and chains each belong to exactly one space (partition, not tagging).
- **The app filters to the active space.** Tasks and Routines show only the active space's content. The active space is device-local and never syncs.
- **A single space is invisible.** No switcher, no badge, no onboarding mention until a second space exists. An existing install migrates into one unnamed default space and looks identical to today.
- **A space switcher** in `TempoTopBar`'s actions slot at compact widths, as a chip above the navigation rail at medium and expanded widths, with `Cmd/Ctrl+1…9` on hardware keyboards.
- **BREAKING (internal):** the `DEFAULT_INBOX_CATEGORY_ENTITY` sentinel (`id = -1`) is removed. Each space owns exactly one default category, identified by persisted metadata rather than a magic id.
- **Enabled tabs and default tab become per space**, so a work space can hide Routines entirely.
- **Focus becomes cross-space.** The agenda lists the active space first in normal priority order, then a "From other spaces" divider with the rest. Up next and any running session stay scoped to the active space.
- **Chains are constrained** so a chain and its member habits always share a space.
- Backup schema carries spaces; older backups import into the default space.

Explicitly **not** in this change: sharing a whole space, per-space cloud sync, space colour theming the UI, cross-space search, and sections inside categories (that is [#61](https://github.com/mandrecode/tempo/issues/61)).

## Capabilities

### New Capabilities

- `space-context`: what a space is, the partition rules binding categories/habits/chains to exactly one space, the active-space concept and its device-local scope, and the rule that a single space is invisible.
- `space-switching`: how a user changes the active space across window size classes, including keyboard switching and what happens to an open editor pane.
- `space-management`: creating, renaming, reordering and deleting spaces, per-space tab configuration, and what happens to content when a space is deleted.
- `cross-space-focus`: how the Focus agenda presents items from other spaces, and which Focus surfaces stay scoped to the active space.

### Modified Capabilities

- `default-category-persistence`: the default category is currently a single row identified by a sentinel id. It becomes one default category **per space**, still user-renameable and still identified by persisted metadata rather than name text.

## Impact

- **Data**: new `SpaceEntity` and DAO; `spaceId` added to `CategoryEntity`, `HabitEntity`, `HabitChainEntity`; Room migration assigning all existing rows to a seeded default space; schema export regenerated.
- **Sentinel removal**: `DEFAULT_INBOX_CATEGORY_ENTITY` is referenced in `Task.kt`, `TaskEntity.kt`, `CategoryEntity.kt` and `BackupRepositoryImpl.kt`; all four change.
- **Repositories**: category, task, habit and chain queries gain active-space filtering.
- **UI**: `TempoTopBar` actions slot, navigation rail, a new space management surface in Settings, Focus agenda and Up next, the session sheet and its notification.
- **Preferences**: enabled-tabs and default-tab keys move from global to per-space; active space stored device-locally.
- **Backup**: `BackupFileDto` gains a spaces section and `spaceId` fields; schema version bumps.
- **Widget**: the quick-task widget must resolve a target space.
- **Downstream**: constrains [#298](https://github.com/mandrecode/tempo/issues/298) — space assignment is user-scoped metadata and must never travel inside a shared category's payload.
