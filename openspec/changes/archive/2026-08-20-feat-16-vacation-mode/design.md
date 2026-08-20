## Context

Streaks are derived, not stored: `CompletionHistoryUtil.getCurrentStreak` walks backwards from today over a habit's `completionHistory` (a comma-separated ISO-date string on `HabitEntity`) and stops at the first *planned* day with no completion. `repeatDays` already introduces a "day not in scope" concept — unplanned days are stepped over without breaking the streak — and `CompletionHistoryUtil.isScheduledOn` is documented as the single source of truth shared by that math and the history dots in `HabitHistoryView`.

Vacation mode is therefore not a new streak mechanism: it is a second reason a day can be out of scope. The main constraints are (a) the pause must be resolvable for *past* dates forever, because the streak walk re-derives history on every render, and (b) habit reminders are exact `AlarmManager` alarms that reschedule themselves inside `HabitReminderReceiver`, so anything that cancels them takes on the job of restoring them — including re-checking a possibly-revoked exact-alarm permission.

Settings in this codebase is a deliberate thin exception to full Clean Architecture layering (AGENTS.md D3): preference-backed configuration goes through a domain interface with a `SharedPreferences` implementation in `core/data/preferences/`, bound in `PreferencesRepositoryModule`, with no use-case layer. `MissedReminderPreferences` (shipped in #257) is the closest precedent and this change follows its shape.

## Goals / Non-Goals

**Goals:**

- A single app-level pause the user controls from Settings, with an optional planned end date.
- Streak math that treats a paused planned day as optional: missing it never breaks the streak, completing it still counts.
- Habit and chain reminder notifications suppressed on paused days, with zero scheduler side effects — reminders must resume on their own.
- Paused days visually distinguishable in the history dots, so an unchanged streak pill next to a gap is self-explanatory.
- Vacation periods preserved across export/import so a restore cannot resurrect broken streaks.

**Non-Goals:**

- Per-habit pause, task-side pausing, automatic/geo/calendar-driven vacation detection.
- Retroactively editing already-recorded periods, or a period history UI beyond the current period.
- Any change to how completions themselves are stored.

## Decisions

### Model a pause as a list of closed date ranges, persisted in `SharedPreferences`

`VacationPeriod(start: LocalDate, endInclusive: LocalDate?)` lives in `core/domain/model/` — alongside `DayOfWeek` and `Periodicity`, which `CompletionHistoryUtil` and multiple features already share — because routines, settings, backup, and infrastructure all read it. `null` end means open-ended.

`VacationModeRepository` (interface in `core/domain/repository/`, in keeping with the settings exception: no use-case layer) exposes `StateFlow<List<VacationPeriod>>` plus `start()`, `stop()`, `setPlannedEnd(LocalDate?)`, and `replaceAll(List<VacationPeriod>)` for restore. The implementation is `VacationModePreferencesImpl` in `core/data/preferences/`, keeping the whole list in one `SharedPreferences` string as `start..end` entries joined by `;` (empty end = open-ended), mirroring the codebase's existing "dates as a delimited string" convention (`completionHistory`). It is bound in `PreferencesRepositoryModule`.

*Alternative rejected — a Room `vacation_periods` table:* correct in the abstract, but it costs an entity, DAO, migration, schema regeneration, and backup-mapper wiring for a handful of rows per year that are never queried relationally. The `StateFlow` also matters: `getCurrentStreak` is called from composition (`HabitHistoryView`, `HabitQuitCards`) and a synchronous in-memory snapshot avoids threading a suspend read through the UI. Room can replace the storage later without touching the domain interface.

*Alternative rejected — store only a boolean "on vacation":* cannot answer "was 2026-03-12 paused?", so every past streak would silently un-freeze the moment the trip ended.

### Data invariants and idempotency

The repository normalizes on every write and is the only place these hold:

1. Periods are sorted ascending by `start` and are **non-overlapping and non-adjacent** — writes merge any ranges that touch or overlap.
2. At most one period may be open-ended, and it must be the last one.
3. `endInclusive >= start` always; a `setPlannedEnd` earlier than `start` is rejected and leaves state unchanged.
4. `stop()` closes the active period on the day *before* today, and drops it entirely when the pause also started today. Closing with `endInclusive = today` would have been more forgiving, but the switch is derived from "is today paused" — so it would spring straight back on after the user turned it off, which no amount of forgiveness justifies.

Idempotency: `start()` while today is already covered is a no-op; `stop()` with no active period is a no-op; `replaceAll` with a malformed or empty restore payload yields an empty list rather than throwing. Both `start()` and `stop()` are pure preference writes — **no scheduler, alarm, or notification calls happen on toggle**, which is what makes them safely repeatable.

### Expire planned end dates by comparison, never by a timer

"Vacation ends on the 14th" is enforced by `isPausedOn(date, periods)` comparing against the stored range at read time. No `WorkManager` job, no alarm, no midnight receiver, and nothing to recover after a reboot or a force-stop. The switch in Settings derives its checked state from `isPausedOn(today, periods)`, so it flips itself off the day after the period ends.

### Extend the existing scope predicate rather than the streak algorithm

`CompletionHistoryUtil` gains:

- `isPausedOn(date, periods)` — the paused counterpart of `isScheduledOn`.
- `isRequiredOn(date, repeatDays, periods) = isScheduledOn(date, repeatDays) && !isPausedOn(date, periods)`.

`getCurrentStreak` gains a `vacationPeriods: List<VacationPeriod> = emptyList()` parameter, and exactly one line changes semantics: the "no completion on this day" branch breaks only when `isRequiredOn` is true instead of `isScheduledOn`. The "completion found on this day" branch keeps using `isScheduledOn`, which is what delivers the asymmetry the spec asks for — a paused day you *did* complete still increments, a paused day you skipped is stepped over. The default argument keeps every existing call site and test compiling unchanged.

`HabitHistoryView` reads the same two predicates to pick the dot style, so the dots and the streak label are structurally incapable of disagreeing. Periods reach the composables as a parameter from `RoutinesContract.UiState` (`ImmutableList<VacationPeriod>`, collected from the repository in `RoutinesViewModel`), not by injecting a repository into a composable.

### Suppress reminders at delivery, not by cancelling alarms

`HabitReminderReceiver` already gates delivery on `shouldShowHabitReminder(habit, scheduledDate)`. That gate gains the vacation periods; `rescheduleHabit` / `rescheduleHabitChain` stay unconditional, so the alarm chain keeps advancing through the pause and the first post-vacation reminder fires with no restore step.

*Alternative rejected — cancel all habit alarms on toggle-on and reschedule on toggle-off:* the restore path would have to re-derive every habit's next reminder date, survive the app being killed mid-vacation, handle a reboot while paused (`RescheduleRemindersWorker` would rebuild alarms that "should" be cancelled), and cope with `SCHEDULE_EXACT_ALARM` having been revoked in the meantime. A silent gate on the delivery path has none of those failure modes, at the cost of alarms still waking the device while paused — an acceptable trade for a feature measured in days per year.

**Transaction boundary vs. Android side effects:** the only persistent write on toggle is the `SharedPreferences` commit; alarm scheduling, cancellation, and notification posting are untouched by the toggle and remain owned by the existing receiver/scheduler path. The receiver reads the periods `StateFlow` value inside its existing `goAsync()` coroutine before deciding to post.

### Backup: an additive settings field under the same schema version

`SettingsBackupDto` gains `vacationPeriods: List<VacationPeriodBackupDto> = emptyList()` (`start`, nullable `end`, ISO-8601 local dates), which the documented evolution rules in `BackupFileDto` permit without bumping `schemaVersion`. `BackupSettings`, `BackupMapper`, and `BackupSettingsDataSource` follow the pattern already used for retention settings, including "settings apply on REPLACE only, never on merge". Restored periods go through the same normalization as user input, so a hand-edited file cannot install overlapping or inverted ranges.

### Settings UI mirrors `RemindersSection`

A new `VacationModeSection` in `features/settings/presentation/` uses `SettingsSwitchItem` plus an `AnimatedVisibility` block revealing an end-date row when the switch is on, exactly as `RemindersSection` reveals the catch-up time. The row opens the existing `TempoDatePickerDialog` with the period start as its lower bound and offers a clear action for "no end date". New `UiState` fields and `UiEvent`s go in `SettingsContract`; `SettingsViewModel` collects the repository flow and derives `isVacationModeActive` from today.

## Risks / Trade-offs

- **Alarms still fire while paused (device wakeups, no user-visible output)** → Accepted deliberately; the alternative is a cancel/restore path with several genuine failure modes (reboot, permission revocation, process death). Documented in the receiver.
- **A user could turn vacation mode on retroactively-in-spirit to rescue a streak they already broke** → Periods only ever start on the day they are declared, never in the past, so a streak already broken yesterday stays broken.
- **Preference storage means periods accumulate unboundedly** → Ranges merge on write and each is ~24 bytes; even a decade of monthly trips stays trivially small. If it ever matters, periods older than the longest history window (21 days) plus a margin can be pruned without changing the interface.
- **`getCurrentStreak` gains a parameter that is easy to forget at a call site** → The default is `emptyList()`, i.e. exactly today's behavior, so a missed call site degrades to "vacation ignored here" rather than a crash; the two production call sites (`HabitHistoryView`, `HabitQuitCards`) are covered by tests asserting the frozen value.
- **Timezone/travel edge case: crossing timezones changes what "today" is** → All comparisons use `TimeZone.currentSystemDefault()` and whole local dates, matching how the rest of the app resolves "today"; a day boundary shifting by a few hours can at worst pause or unpause one extra day, which is harmless for this feature.
- **Streak-tier colouring (7/14/21-day tiers) now reachable while inactive** → Intended: the tier reflects the streak, and the streak is explicitly frozen, not earned, during the pause.

## Migration Plan

No data migration. Absent preferences read as an empty period list, which makes every existing streak and reminder behave exactly as before; older backup files decode with `vacationPeriods = emptyList()`. Rollback is removing the feature — the stored preference is inert to any older build.

## Open Questions

None blocking. Deferred by choice: whether the routines screen should carry a persistent "on vacation" banner (left out per the agreed scope — Settings is the only surface for now), and whether per-habit pause should later reuse `VacationPeriod` with a habit id.
