# Timezone & DST Handling

This document describes how the Tempo & Habits app handles timezones, Daylight Saving Time (DST),
and date/time operations throughout the codebase.

## Technology

All date/time logic uses **kotlinx-datetime** and **kotlin.time**. No `java.util.Date`,
`java.util.Calendar`, or `java.time` APIs are used in the app logic.

### Core Types

| Type                | Usage                                                  |
|---------------------|--------------------------------------------------------|
| `LocalDateTime`     | Reminder dates (stored in DB), scheduling triggers     |
| `LocalDate`         | Habit completion tracking, "today" calculations        |
| `Instant`           | Conversion to epoch millis for AlarmManager            |
| `TimeZone`          | Always `TimeZone.currentSystemDefault()`               |
| `kotlin.time.Clock` | Current time in ViewModels, receivers, and use cases   |

## Storage

Reminder dates are stored in Room as **ISO 8601 strings** via `LocalDateTime.toString()`.

```kotlin
// Converters.kt
fun fromLocalDateTime(date: LocalDateTime): String = date.toString()
fun toLocalDateTime(string: String): LocalDateTime = LocalDateTime.parse(string)
```

This stores the **wall-clock time** (e.g., `2025-03-09T08:00`) without timezone offset.
The timezone is resolved at scheduling time, not at storage time.

## Scheduling Flow

When a reminder is scheduled:

1. **User picks a time** → stored as `LocalDateTime` (wall-clock time)
2. **Scheduler converts to epoch millis**:
   ```kotlin
   reminderDate.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
   ```
3. **AlarmManager receives epoch millis** via `setExactAndAllowWhileIdle(RTC_WAKEUP, triggerAtMillis, ...)`

### Rescheduling Periodic Tasks

```kotlin
currentReminder
    .toInstant(currentSystemZone)
    .plus(period, currentSystemZone)
    .toLocalDateTime(currentSystemZone)
```

The conversion through `Instant` ensures that DST transitions are handled correctly by
kotlinx-datetime. For example, adding 1 day across a DST boundary will correctly produce
the next day at the same wall-clock time.

## DST Behavior

Since `TimeZone.currentSystemDefault()` is used at scheduling time:

- **Spring forward**: If a reminder is set for 2:30 AM and clocks skip from 2:00 to 3:00,
  `toInstant()` will resolve to the next valid time (3:00 AM). The alarm triggers at 3:00 AM.
- **Fall back**: If a reminder is set for 1:30 AM and clocks repeat 1:00-2:00,
  `toInstant()` resolves to the first occurrence. The alarm triggers at the first 1:30 AM.
- **Periodic reminders**: The `Instant.plus(period, timeZone)` arithmetic is DST-aware when
  using `DatePeriod` or `DateTimePeriod`. Adding 1 day preserves the wall-clock time.

## Timezone Change Behavior

If the user changes their device timezone:

- **Already-scheduled alarms** will fire at the original epoch millis (which may be a different
  wall-clock time in the new timezone).
- **Newly-scheduled alarms** will use the new timezone for conversion.
- **Stored `LocalDateTime` values** remain unchanged (they represent wall-clock time without
  timezone context).

This is standard Android alarm behavior and matches user expectations for most cases.

## Key Locations

| Component                      | File                                                    | Usage                              |
|--------------------------------|---------------------------------------------------------|------------------------------------|
| Task scheduler                 | `infrastructure/reminders/scheduler/TaskReminderSchedulerImpl.kt`  | `toInstant(currentSystemDefault())` |
| Habit scheduler                | `infrastructure/reminders/scheduler/HabitReminderSchedulerImpl.kt` | `toInstant(currentSystemDefault())` |
| Task rescheduling              | `infrastructure/reminders/receivers/TaskReminderReceiver.kt`       | `plus(period, currentSystemZone)`   |
| Habit completion               | `infrastructure/reminders/receivers/MarkHabitAsCompletedReceiver.kt` | `Clock.System.todayIn(...)`       |
| Toggle habit completion        | `features/routines/domain/usecase/ToggleHabitCompletionUseCase.kt` | `Clock.System.todayIn(...)`       |
| Toggle task completion         | `features/tasks/domain/usecase/ToggleTaskCompletionUseCase.kt`     | `Clock.System.todayIn(...)`       |
| Room TypeConverters            | `core/data/entity/Converters.kt`                                    | ISO 8601 string ↔ `LocalDateTime` |

## Guidelines

1. **Never use `java.time` or `java.util.Date`** — always use `kotlinx-datetime`.
2. **Always use `TimeZone.currentSystemDefault()`** — never hardcode UTC or other timezones.
3. **Store wall-clock time** as `LocalDateTime` strings — resolve timezone at scheduling time.
4. **Use `kotlin.time.Clock.System`** for "now" — enables testing with fake clocks.
5. **Use `Instant` arithmetic with timezone** for period calculations — ensures DST correctness.
