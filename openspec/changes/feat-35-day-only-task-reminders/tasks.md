## 1. Preference contract and persistence

- [x] 1.1 Add the pure `TaskReminderPreferences` contract with a 09:00 default and normalization rules.
- [x] 1.2 Implement SharedPreferences persistence, bind it with Hilt, and cover defaults, updates, and invalid stored values with unit tests.

## 2. Task reminder selection

- [x] 2.1 Observe the default reminder time in Tasks MVI state and pass it through the task bottom-sheet API.
- [x] 2.2 Extend the shared date picker with optional localized date-only and exact-time actions without changing existing callers.
- [x] 2.3 Resolve date-only task selections to the configured default, disable past default-time combinations, and preserve exact-time selection.
- [x] 2.4 Add focused unit or Compose tests for default-time state propagation, date-only selection, exact-time selection, and the past-combination guard.

## 3. Settings control

- [x] 3.1 Add Settings state/events and ViewModel preference observation/update handling with unit tests.
- [x] 3.2 Add the localized default task reminder time control to Notifications Settings with time-picker interaction, English/Spanish resources, and debug previews.
- [x] 3.3 Add Compose UI coverage for displaying the default time and emitting a changed-time event.

## 4. Verification

- [x] 4.1 Run `openspec validate feat-35-day-only-task-reminders` and `./gradlew testDebugUnitTest`.
- [x] 4.2 Run `./gradlew ktlintFormat`, `./gradlew ktlintCheck`, `./gradlew :app:detekt`, and `./gradlew lintDebug`.
