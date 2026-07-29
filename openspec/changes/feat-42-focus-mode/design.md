## Context

Focus is the first destination added to the app since navigation was built, and the navigation layer
assumes exactly two tabs in five independent places: `navigationItems` in `TempoBottomNavigation`,
`TempoNavigator.Section` with one back stack per constant, the paired
`isRoutinesTabEnabled`/`isTasksTabEnabled` flows on `NavigationPreferencesRepository`,
`MainActivity.rememberStartDestination`, and `BackupSettingsDataSource`. The "at least one tab must
stay enabled" rule is implemented twice, once in `SettingsViewModel` and once in
`OnboardingViewModel`, with slightly different shapes. Adding a third tab by copying that pattern
would triple the duplication, so this change generalises first and adds Focus second.

The screen itself is comparatively cheap: the two-block seam, wavy section headers, task rows, habit
cards, adaptive rail clearance and modal sheets all exist. Two new composables carry the design — a
summary hero and an Up next card.

Two constraints shape the rest. First, completed tasks are deletable by
`DeleteExpiredCompletedTasksUseCase`, so any history derived from `Task.completedAt` is lossy and a
durable record is required. Second, the app has never run a foreground service and declares no
`FOREGROUND_SERVICE` permission; since Android 14 every foreground service needs a declared type and
none of the types describe a Pomodoro timer, so the session timer must be built without one.

## Goals / Non-Goals

**Goals:**

- One today-only screen that centralises tasks, habits and chains, per issue #42.
- A day summary that is durable across retention purges, per issues #22 and #19.
- A navigation layer where adding a fourth tab is a registry entry, not a refactor.
- A focus session timer that survives force-close and reboot without a foreground service.
- One announced change to the default tab, reversible in Settings — not a silent one.

**Non-Goals:**

- The twelve-week history grid behind the streak. The hero ships seven days; the full grid is a
  follow-up so this change does not grow a second history surface.
- Break-length configuration, session history browsing, and per-task session lengths.
- Any change to how Routines and Tasks render their own content.
- Widget or notification surfaces for the day summary.

## Decisions

### Tabs become a registry, not a third pair of booleans

A single `TempoTab` enum (`FOCUS`, `ROUTINES`, `TASKS`) with route, icons, title and preference key
becomes the source of truth. `navigationItems` derives from it, `TempoNavigator` keys its back stacks
by it, and `NavigationPreferencesRepository` exposes `enabledTabs: Flow<Set<TempoTab>>` plus
`setTabEnabled(tab, enabled)`. The "at least one enabled" rule and the "default tab must be enabled"
rule move into one place that Settings and Onboarding both call.

*Alternative considered:* adding `isFocusTabEnabled()` alongside the existing two. Rejected — it
scales the existing duplication by 50% and leaves four call sites that must each be taught the new
tab, which is exactly the bug surface this change should remove.

### Preference migration is read-time, not a Room migration

Tab enablement lives in `SharedPreferences`, so the existing `routines_tab_enabled` and
`tasks_tab_enabled` keys are read once when the new per-tab set is first materialised, then written
in the new shape. Focus defaults to enabled. No data is destroyed if the user downgrades.

### Daily activity stores counts and derives state

`DailyFocusActivityEntity` holds `date`, `scheduledCount`, `completedCount` and `focusMinutes`. The
three display states are computed from the counts, so retuning thresholds later needs no migration.
Today's row is recomputed whenever the Focus screen loads or a completion toggles — items can be
added or rescheduled during the day, so `scheduledCount` is only final once the day ends. Past rows
are never recomputed; they keep the last value written while that day was current.

*Alternative considered:* deriving everything from `Task.completedAt` and habit
`completionHistory`. Rejected for tasks because retention deletes the evidence; kept for habits,
whose history is already durable and is folded into the same counts.

### The session timer is an alarm plus a chronometer notification

An exact alarm fires at the session end; the ongoing notification is built with
`setUsesChronometer(true)` and `setChronometerCountDown(true)` so the platform renders the countdown
and the app posts nothing per second. The active session is persisted in `SharedPreferences` — one
nullable record, no schema — and is reconciled on app start and on `BOOT_COMPLETED` using the same
receiver-and-scheduler shape as task and habit reminders. A session whose end time has already
passed is completed rather than resumed.

*Alternative considered:* a foreground service, which is the conventional Pomodoro implementation.
Rejected because Android 14+ would force the `specialUse` type, which requires a written
justification at every Play Console submission, for no behavioural gain over an exact alarm.

*Alternative considered:* a `WorkManager` periodic worker. Rejected — minimum interval is 15 minutes
and workers are not intended for user-visible timing precision.

### Ranking and agenda assembly are domain use cases

`GetFocusAgendaUseCase` merges task, habit and chain flows into today's sections, and
`GetUpNextItemUseCase` applies the priority-then-due-time rule. Both are pure Kotlin over
`kotlinx-datetime`, so the ordering rules are unit-testable without Compose or Room, and the
ViewModel stays a thin orchestrator per the project's MVI rules.

### The countdown does not recompose the agenda

Remaining time lives in its own state holder read only by the session card, the immersive view and
the floating-bar chip. The agenda list does not observe it, so a running session does not recompose
task rows once per second.

### What's-new gains an optional action

`WhatsNewEntry` grows an optional action label and callback; the Focus entry uses it to offer the
start-tab switch once. Entries without an action render exactly as they do today.

## Risks / Trade-offs

- **The navigation refactor touches backup, onboarding and start-up at once** → Land it as the first
  child PR with no Focus UI attached, so a regression is bisectable to a small diff. Round-trip
  tests cover old-format import.
- **Exact alarm permission can be denied or revoked** → The session still runs and displays
  correctly in-app; the end-of-session sheet appears on next foreground. The notification's
  chronometer is accurate regardless, since it counts against a fixed end timestamp.
- **Notification permission can be denied** → Sessions run in-app only. The existing first-run
  notification education (#433) already covers the request path; Focus adds no new prompt.
- **`scheduledCount` for today shifts as the user edits their day** → Accepted and documented: the
  hero's denominator can move during the day. Making it immutable would misreport a day where work
  was added.
- **Detekt baseline is frozen at 189 and Kover requires 80% line / 70% branch** → New domain use
  cases and the preference migration need tests in the same PR that introduces them; no new baseline
  suppressions.
- **Room schema export must stay in sync** → The daily-activity PR regenerates `app/schemas/` and
  commits it, or CI fails.
- **Five stacked PRs can drift from `main`** → The integration branch is rebased on `main` after each
  merge to it, and only the final squash lands on `main`.

## Migration Plan

1. **Preferences**: on first read of `enabledTabs`, seed from the legacy boolean keys, add Focus as
   enabled, set the default tab to Focus, and persist the new shape. Keyed on the enabled-tabs key
   being absent, so it applies exactly once and never overrides a later choice.
2. **Database**: additive migration creating the daily-activity table; no existing table is altered,
   so downgrade leaves the table orphaned but harmless.
3. **Backup schema**: bump the version and add tab-set, default-tab and daily-activity fields, all
   optional with defaults so pre-change exports import cleanly.
4. **Rollout**: the what's-new entry announces Focus, states that it is now the start tab, and
   points at the Settings entry that changes it back.
5. **Rollback**: reverting the final squash restores the two-tab UI; the orphaned table and the
   rewritten preference keys are inert, and legacy keys are left in place rather than deleted
   precisely so a revert reads them again.

## Open Questions

- Exact pane split at the expanded tier: whether the summary pane keeps a fixed width or shares the
  readable-width cap with the agenda. Deferred to the adaptivity PR, where it can be measured on a
  real window rather than guessed.
- Whether the undated-task footer should also count undated habits. Currently tasks only, since
  habits without repeat days are treated as daily and therefore always scheduled.
