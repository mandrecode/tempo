## 1. Domain model and repository contract

- [x] 1.1 Add `VacationPeriod(start: LocalDate, endInclusive: LocalDate?)` in `core/domain/model/`, pure Kotlin with `kotlinx-datetime`, documenting that a null end means open-ended
- [x] 1.2 Add `covers(date)` / period helpers on `VacationPeriod` plus a companion `normalize(List<VacationPeriod>)` implementing the invariants from design.md (sorted, merged, non-overlapping, at most one open-ended and it must be last, `endInclusive >= start`)
- [x] 1.3 Add `VacationModeRepository` in `core/domain/repository/` exposing `periods: StateFlow<List<VacationPeriod>>`, `start()`, `stop()`, `setPlannedEnd(LocalDate?)`, `replaceAll(List<VacationPeriod>)`, with KDoc stating the idempotency rules (repeat `start()`/`stop()` are no-ops, no scheduler side effects)
- [x] 1.4 Unit-test `normalize` and the period helpers: merging touching/overlapping ranges, rejecting inverted ranges, collapsing duplicates, open-ended ordering

## 2. Preferences-backed persistence

- [x] 2.1 Implement `VacationModePreferencesImpl` in `core/data/preferences/` with its own prefs file, serializing the list as `start..end` entries joined by `;` (empty end = open-ended) and exposing it as a `StateFlow`
- [x] 2.2 Make reads tolerant: malformed, inverted, or unparsable entries are dropped rather than throwing, and every write goes through `VacationPeriod.normalize`
- [x] 2.3 Bind `VacationModeRepository` in `core/di/PreferencesRepositoryModule.kt`
- [x] 2.4 Unit-test the implementation with Robolectric-free fakes where possible: `start()` when already paused is a no-op, `stop()` closes with today inclusive, `setPlannedEnd` before start is rejected, clearing the end reopens the period, round-trip through the serialized string, corrupt string reads as empty

## 3. Streak math

- [x] 3.1 Add `isPausedOn(date, periods)` and `isRequiredOn(date, repeatDays, periods)` to `CompletionHistoryUtil`, and document them as the shared scope predicate next to `isScheduledOn`
- [x] 3.2 Add `vacationPeriods: List<VacationPeriod> = emptyList()` to `getCurrentStreak` and switch only the "no completion on this day" branch to `isRequiredOn`, leaving the completion branch on `isScheduledOn`
- [x] 3.3 Unit-test the frozen-streak scenarios from `specs/vacation-mode/spec.md`: streak survives a seven-day gap inside a period, reads unchanged during an active period, a completion recorded while paused increments, a missed planned day after the period still breaks, a QUIT habit freezes identically, and the default empty-list argument reproduces current behavior exactly

## 4. Reminder suppression

- [x] 4.1 Inject `VacationModeRepository` into `HabitReminderReceiver` and pass the current periods into `shouldShowHabitReminder` / `shouldShowHabitChainReminder`, keeping both `@VisibleForTesting` companion functions pure
- [x] 4.2 Leave `rescheduleHabit` / `rescheduleHabitChain` unconditional and add a comment stating why alarms are never cancelled for a pause (reboot, process death, and revoked exact-alarm permission all self-heal)
- [x] 4.3 Unit-test the gate: paused date suppresses habit and chain notifications, unpaused date keeps existing behavior including the already-completed rule, and rescheduling still happens while paused

## 5. History rendering

- [x] 5.1 Add a `vacationPeriods` parameter to `HabitHistoryView` and render a paused dot style distinct from completed, missed, and unscheduled, with a completion inside a period still rendering as completed
- [x] 5.2 Feed the periods into `HabitHistoryView` and `HabitQuitCards` from `RoutinesContract.UiState` (`ImmutableList<VacationPeriod>`) collected in `RoutinesViewModel`; do not inject a repository into a composable
- [x] 5.3 Add `@Preview`s under `src/debug/` covering a history window spanning a vacation period, with and without completions inside it
- [x] 5.4 Add instrumented tests in `HabitHistoryViewTest` for the paused dot style and for the streak pill staying unchanged across a paused window

## 6. Settings UI

- [x] 6.1 Add vacation-mode fields and events to `SettingsContract` (active flag, current period, planned end date) and collect the repository flow in `SettingsViewModel`, deriving the switch state from `isPausedOn(today, periods)`
- [x] 6.2 Add `VacationModeSection.kt` modeled on `RemindersSection`: switch plus an `AnimatedVisibility` end-date row opening `TempoDatePickerDialog` bounded below by the period start, with a clear action for "no end date"
- [x] 6.3 Wire the section into `SettingsContent` and add the strings to `values/strings.xml` **and** `values-es/strings.xml` in the same change
- [x] 6.4 Unit-test `SettingsViewModel`: toggling on/off, setting and clearing the end date, rejecting an end date before the start, and the switch reading off once a planned end date has passed

## 10. Routines title badge

- [x] 10.1 Add the `ic_beach_access` palm drawable and a `vacation_mode_active` content-description string in `values/` and `values-es/`
- [x] 10.2 Give `TempoTopBar` an optional `titleBadge` slot rendered next to the title
- [x] 10.3 Derive `isVacationModeActive` in `RoutinesViewModel` and hand it to the Routines top-bar slot, so navigation never reaches for the repository
- [x] 10.4 Unit-test the derived flag (active period vs a period that already ended) and instrument-test the `TempoTopBar` badge slot

## 7. Backup

- [x] 7.1 Add `VacationPeriodBackupDto(start, end?)` and `vacationPeriods: List<VacationPeriodBackupDto> = emptyList()` to `SettingsBackupDto`, keeping `schemaVersion` at 1 per the additive-field rule
- [x] 7.2 Extend `BackupSettings`, `BackupSettingsMapper`/`BackupMapper`, and `BackupSettingsDataSource` (snapshot + apply), routing restored periods through `VacationPeriod.normalize`
- [x] 7.3 Update `docs/BACKUP_FORMAT.md` with the new settings field
- [x] 7.4 Unit-test the round-trip, a file with no `vacationPeriods` decoding to an empty list, a hand-edited overlapping/inverted payload being normalized, and merge-mode imports leaving stored periods untouched

## 8. What's New

- [x] 8.1 Replace `WhatsNewRegistry.latest` with a `WhatsNewEntry(id = "vacation-mode", ...)` and update `whats_new_title`/`whats_new_description` in `values/` and `values-es/`, removing the now-unused missed-reminder strings

## 9. Verification

- [x] 9.1 `./gradlew ktlintFormat` then `./gradlew ktlintCheck` and `./gradlew :app:detekt` (baseline must not grow past 189)
- [x] 9.2 `./gradlew compileDebugKotlin` and `./gradlew testDebugUnitTest`
- [x] 9.3 `./gradlew lintDebug` to catch `MissingTranslation` / `ExtraTranslation` after the strings changes
- [x] 9.4 `./gradlew koverVerifyDebug` for the 80% line / 70% branch thresholds
- [x] 9.5 `./gradlew connectedDebugAndroidTest` against the Pixel 10 AVD for the history-view instrumented tests
- [x] 9.6 `openspec validate feat-16-vacation-mode --strict`
- [x] 9.7 Manual smoke test on device: turn vacation mode on, confirm the streak pill holds and paused dots render, confirm no habit reminder fires, turn it off and confirm the next reminder arrives
