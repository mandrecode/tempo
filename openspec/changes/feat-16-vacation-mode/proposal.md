## Why

Tracks [#16](https://github.com/mandrecode/tempo/issues/16) — users who travel, get sick, or take a deliberate break currently have no way to step away from their routines without watching every streak reset to zero. The only workarounds are lying to the app (checking habits off without doing them) or accepting the loss, and both make the streak number meaningless. A global, user-declared pause lets people skip planned days honestly while the streak they earned stays intact.

## What Changes

- Add a **vacation mode**: an app-level pause the user turns on in Settings, with a start date (the day it is turned on) and an optional end date. Turning it off ends the pause on the current day.
- Persist **past vacation periods**, not just the active one, so historical streak math stays stable forever — a streak computed today over last month's trip must still skip those days next year.
- **Freeze streaks across paused days**: a planned day inside a vacation period never breaks the current streak. A completion recorded on a paused day still counts, so a user who keeps a habit going while away is credited rather than penalized.
- **Suppress habit and habit-chain reminder notifications** on paused days. Alarms keep rescheduling themselves as they do today, so reminders resume automatically after the pause without any restore step, reboot handling, or exact-alarm re-prompt.
- **Render paused days distinctly** in the habit history dots so a gap in the dots visibly reads as "paused", not "missed", and the unchanged streak pill makes sense next to it.
- Carry vacation periods in **encrypted backup export/import** alongside the other settings, so a restore does not resurrect broken streaks.
- Announce the feature through the existing **What's New** bottom sheet.

Non-goals (deliberately out of scope for this change):

- Per-habit pause/resume. This change ships the global switch only; per-habit pausing can build on the same period model later if it is ever wanted.
- Pausing tasks, task reminders, or the daily missed-reminder catch-up — those are task-side behaviors and stay exactly as they are.
- Suppressing habit-chain live activities, hiding or dimming paused habits on the routines list, and any scheduled/automatic vacation detection.

## Capabilities

### New Capabilities
- `vacation-mode`: a global, date-range pause of habit tracking — how periods are declared, stored, and resolved for a given day; how the pause affects streak math and habit reminder delivery; how it is surfaced and edited in Settings; and how it survives backup/restore.

### Modified Capabilities
- `habit-history-display`: the history view gains a third day state (paused) alongside scheduled and unscheduled, and the shared "is this day in scope" rule that history dots and streak math both read must now account for vacation periods.

## Impact

- **Domain** (`features/routines/domain/`): new `VacationPeriod` model and `VacationModeRepository` interface (pure Kotlin, `kotlinx-datetime`).
- **Streak math** (`util/CompletionHistoryUtil.kt`): `getCurrentStreak` takes the active/past vacation periods; new paused-day predicate shared with the history view.
- **Data** (`core/data/preferences/`): `SharedPreferences`-backed implementation and a `@Binds` entry in `core/di/PreferencesRepositoryModule.kt`. No Room entity, no migration, no schema regeneration.
- **Infrastructure** (`infrastructure/reminders/receivers/HabitReminderReceiver.kt`): reminder and chain-reminder delivery becomes conditional on the day not being paused; rescheduling is untouched.
- **UI** (`features/settings/presentation/`): new vacation-mode settings section, contract/ViewModel state and events, strings in `values/` and `values-es/`; (`features/routines/presentation/`): habit and chain history views and the routines UI state that feeds them.
- **Backup** (`features/backup/`): `BackupSettings` gains the vacation periods; mapper, DTO, and settings data source updated with the existing "null means an older file" tolerance.
- **Tests**: unit tests for period resolution, streak freezing, reminder suppression, preferences persistence, backup round-trip, and settings ViewModel; instrumented tests for the paused-day dot rendering.
