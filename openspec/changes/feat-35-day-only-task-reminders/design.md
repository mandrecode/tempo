## Context

Task reminder creation currently requests notification permission, opens `TempoDatePickerDialog`, and always continues to `TempoTimePickerDialog`. Tasks persist a concrete `LocalDateTime`, and reminder schedulers consume that value without needing to know how the user selected it. Settings already observes lightweight SharedPreferences-backed contracts and is intentionally allowed to remain presentation/data-light.

The new behavior spans task presentation, Settings presentation, and preference persistence, but it does not require a Room or scheduler model change. The key invariant is that every saved task reminder remains a concrete local date and time; “date only” describes the selection experience, not a nullable or partially specified persisted timestamp.

## Goals / Non-Goals

**Goals:**

- Offer a date-only completion action directly from task reminder date selection.
- Resolve date-only selections with a persisted default time whose initial value is 09:00.
- Preserve exact-time reminder selection.
- Keep the active default time reactive across Tasks and Settings.
- Prevent the date-only action from creating a reminder that is already in the past.

**Non-Goals:**

- Do not change the Task Room entity, database schema, alarm scheduler, delivery receiver, recurrence, or rollover behavior.
- Do not distinguish date-only and exact-time reminders after they are saved.
- Do not retroactively move existing reminders when the default preference changes.
- Do not add a separate Settings domain/use-case layer; the documented thin Settings exception remains appropriate for a single preference update.

## Decisions

1. **Persist one normalized `LocalTime` preference behind a pure task-domain contract.** `TaskReminderPreferences` will expose a `StateFlow<LocalTime>`, default to 09:00, and normalize persisted hour/minute data before publishing it. A SharedPreferences-backed implementation under `core/data/preferences` will bind through `RepositoryModule`. This follows the existing completed-task-retention preference pattern while keeping Android APIs out of task domain code. Storing a single concrete time was chosen over separate UI-only hour/minute state so Tasks and Settings share one source of truth.

2. **Resolve date-only input before it enters the existing task form event.** Tasks UI state will observe the preference and pass the default time into the bottom sheet. The date picker will expose two completion actions: use the displayed default time or continue to exact time selection. Both routes call the existing reminder event with year, month, day, hour, and minute, so task persistence and scheduling retain their current concrete-`LocalDateTime` invariant. Adding a nullable time to `Task` was rejected because it would require a schema migration and force every scheduler/formatter/recurrence path to resolve an otherwise presentation-level choice.

3. **Keep existing reminders stable when the setting changes.** Updating the preference affects only later date-only selections. No database transaction or alarm rescheduling occurs, and no existing task rows are rewritten. This makes the preference update idempotent: saving the same normalized time repeatedly produces the same published value and future selection behavior.

4. **Make past date-only combinations unavailable.** The date picker alternative action will be enabled only when the selected date combined with the default time is later than the current local date-time. Exact-time selection remains available for the same day, preserving the existing path. This prevents a common post-09:00 default selection from silently producing an alarm the scheduler must skip.

5. **Use the shared time picker for Settings.** The Notifications section will show the localized active default time and open `TempoTimePickerDialog` when tapped. `SettingsViewModel` observes the preference flow and forwards user changes directly to the preference contract, consistent with the D3 thin-Settings decision. A new use case was rejected because the update contains no multi-repository orchestration or reusable business policy.

6. **Extend shared picker APIs without changing routine behavior.** `TempoDatePickerDialog` will gain optional secondary-confirm configuration used only by tasks. Existing callers retain their current OK/Cancel behavior. Labels come from localized resources, and Compose tests will lock the task-specific actions and Settings event emission.

## Risks / Trade-offs

- [Users cannot later tell whether 09:00 was selected explicitly or through the date-only action] → Treat date-only as an input shortcut, clearly show the resolved time in the reminder chip, and avoid unnecessary persistence complexity.
- [Three date-dialog actions may be tight on narrow devices or in Spanish] → Use concise localized labels and Material text buttons; verify the picker on the project’s phone-size previews/instrumented UI checks.
- [The current day’s default may already be past] → Disable only the default-time action for that selection while leaving exact-time selection available.
- [Corrupt stored preference values could produce an invalid `LocalTime`] → Clamp hours to 0–23 and minutes to 0–59 before constructing or publishing the value.
- [Preference writes and task saves are not transactional] → They are intentionally independent: a task save receives a fully resolved time, and changing the default never mutates task records or Android alarms.

## Migration Plan

- Ship the preference with a read-time default of 09:00; no one-time migration or Room migration is required.
- Existing tasks and scheduled alarms remain untouched.
- Rollback can remove the new UI and preference binding while leaving the unused SharedPreferences file harmlessly in place.

## Open Questions

None.
