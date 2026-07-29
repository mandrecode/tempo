## Why

Tempo is strong at item-level interaction but has no surface that answers "what should I do right
now?". Tasks and Routines each show one half of the day, split by category and day-of-week chrome,
so orienting means visiting two tabs and mentally filtering both. Issue
[#42](https://github.com/mandrecode/tempo/issues/42) asks for a Focus mode that hides everything not
due today; its sub-issues [#22](https://github.com/mandrecode/tempo/issues/22) and
[#19](https://github.com/mandrecode/tempo/issues/19) ask for a day summary built from the completion
state the app already tracks. Together they describe one screen, and that screen is the app's
clearest differentiator.

A guiding constraint agreed with the maintainer: **the summary reports the day, it does not grade
it.** The goal is flowing with the work, not accumulating productivity scores. This rules out
gradients, grades, shaming empty states, and celebration copy.

## What Changes

- **New Focus tab**, leftmost of three, enabled by default and disableable like its peers. This
  requires generalising a two-tab model that is currently hardcoded in five places into an N-tab
  registry — the largest piece of non-visible work in this change.
- **Hero summary card** on `primaryContainer`: greeting, focus streak, last-7-days dots, and a
  `CircularWavyProgressIndicator` showing today's completion. Answers #22 and #19.
- **Today-only agenda** below the existing two-block seam, ordered **Up next → Overdue → Today**.
  Habits, chains and tasks interleave in one list with type and category as pills; no category
  grouping, no day-of-week row.
- **Up next card** on `tertiaryContainer`: the single highest-ranked item, carrying the only
  session-start button on the screen. Collapses entirely when nothing qualifies.
- **Focus sessions (Pomodoro)**: start from Up next, run in place, expand to an immersive view on
  tap, and report completion in a modal sheet. Backed by an exact alarm and an ongoing
  chronometer notification — **no foreground service**.
- **New `DailyFocusActivity` persistence** so streak and focus-minute history survives the completed
  task retention purge that would otherwise erase it.
- **New Settings entries**: a Focus tab switch, Focus in the default-tab picker, and a default
  session length.
- **Focus becomes everyone's default tab**, existing installations included, applied once by the
  preference migration. The #210 what's-new sheet states that the start tab changed and points at
  the Settings entry that reverses it, so the change is announced rather than silent.
- Tasks with no date remain excluded per the issue's today-only rule, surfaced only as a footer
  count linking to Tasks.

Non-goals for this change:

- No new task/habit editing capability — Focus reuses the existing editor sheets.
- No break-length setting (fixed at 5 minutes) and no session history view beyond the daily totals.
- No change to the deliberate hard-cut tab transition established in #245.
- No multi-session or background session queueing: exactly one session exists at a time.

## Capabilities

### New Capabilities

- `focus-mode-navigation`: Focus as a third peer tab — tab registry, per-tab enablement with the
  "at least one enabled" rule, default-tab selection, start destination resolution, backup of tab
  preferences, and the one-time what's-new start-tab prompt.
- `focus-day-summary`: the hero card's streak, three-state day history, and completion ring, plus
  the `DailyFocusActivity` record that makes them durable across retention purges.
- `focus-today-agenda`: what Focus shows and in what order — Up next ranking, Overdue and Today
  sections, interleaving of tasks/habits/chains, and the undated-task footer count.
- `focus-session-timer`: starting, pausing, ending and completing a focus session; its in-place,
  immersive, floating-bar and notification surfaces; recovery after force-close or reboot; and the
  configurable default session length.

### Modified Capabilities

None. No existing spec's requirements change: `presentation-architecture` guardrails are followed by
the new screen rather than altered, and `habit-history-display`,
`habit-chain-live-notification-continuation` and `notification-permission-education` keep their
current behavior. Focus reuses their patterns without redefining them.

## Impact

**Navigation (largest blast radius).** `TempoBottomNavigation.navigationItems`,
`TempoNavigator.Section` and its four back stacks, `Navigation.kt` entries and destinations,
`PersistentFloatingBar`, `NavigationPreferencesRepository` (paired booleans → per-tab set),
`MainActivity.rememberStartDestination`, `OnboardingViewModel`/`OnboardingContract`, and
`BackupSettingsDataSource`. The "at least one tab enabled" rule is currently duplicated across
Settings and Onboarding and is consolidated.

**Data.** New `DailyFocusActivityEntity`, DAO, migration and exported schema; new domain model,
repository and use cases; backup DTO gains a versioned field with a default so older exports still
import.

**UI.** New `features/focus/` package following the Screen/Content split, with previews in
`src/debug/`. Reuses `WavyDivider`, `TempoModalSheet`, `ExpressiveChip`, `TaskItem`, habit cards and
`adaptiveScreenContentLayout`.

**Infrastructure.** New focus-session alarm scheduler, boot/force-close recovery receiver, and
notification builder using `setUsesChronometer` + `setChronometerCountDown`. No new manifest
permission — `SCHEDULE_EXACT_ALARM`, `POST_NOTIFICATIONS` and `RECEIVE_BOOT_COMPLETED` already exist.

**Settings.** New Focus section; `TabsAndNavigationSection` and `DefaultTabSection` become
registry-driven rather than two-tab literals.

**Delivery.** Integration branch `feat/42-focus-mode` with stacked child PRs — one per capability
above, plus a final adaptivity/polish/what's-new PR — squash-merged to `main` as a single
`feat(#42)` commit.
