# Focus Feature

## Overview

Focus is Tempo's third tab. Where Tasks and Routines are lists of everything, Focus is a view of one
day: what is due, what is late, how far through it you are, and one place to start working from. It
also owns the focus-session timer — the only part of Tempo that counts time rather than dates.

Shipped in 1.11.0 ([#42](https://github.com/mandrecode/tempo/issues/42)).

## The day

### What the agenda contains

The agenda is built by `GetFocusAgendaUseCase` and holds four kinds of thing:

| Included | Not included |
|:--|:--|
| Tasks due today | Tasks dated in the future |
| Tasks overdue and still open | Tasks with no date (reported as a footer count instead) |
| Habits scheduled today | Habits outside their repeat days |
| Habit chains scheduled today | Habits that belong to a chain (the chain's card shows them) |

Membership is one rule, in `FocusDayMembership.kt`, shared with the day's counts so the summary and
the list can never disagree.

**Subtasks.** A subtask normally rides inside its parent's card. When the parent is *not* itself on
the day — undated, dated later, or an overdue task already ticked off — a dated subtask becomes its
own row and can be focused on directly
([#359](https://github.com/mandrecode/tempo/issues/359)).

### How it is ordered

Sections read **Up next → Today → Overdue**
([#353](https://github.com/mandrecode/tempo/issues/353)). Within a section: uncompleted work first,
timed items in clock order, untimed items after them, completed items last. Tasks, habits and chains
interleave in one list rather than sitting in per-type groups — each row identifies its own type.

### Up next

A horizontal row of at most five task cards, drawn from Today first and then Overdue. It is a
shortlist *over* the day rather than a slice taken out of it: the same tasks still appear in the
sections below.

Tasks only — a habit is not something you sit down and run a timer against. Completed work is never
a candidate. A card offers "Start N min", or, for a task that already has sessions behind it and no
tick, "Back to work" and "Complete". Tapping the card body opens the session screen as a preview,
without starting anything.

A card's metadata line reads priority, category, then time. Because the row mixes today's work with
what came before it, a card carried over from a previous day shows **OVERDUE** where the time would
be — a bare "8:00 AM" on last week's task read as something due this morning.

### The summary hero

Above the list: today's date, the scheduled/completed counts, a progress indicator, a headline band
that changes with how the day is going, a streak, and a heatmap of recent days. The counts come from
`RecordDailyActivityUseCase`, which recomputes today from current state on every relevant change —
past days keep whatever was last written while they were current, so history is never rewritten.

## Sessions

A focus session is a countdown against one task. There is at most one at a time; starting another
asks before replacing it.

- Length defaults to the Settings value and can be overridden per start. Breaks have their own
  length.
- Nothing ticks in storage. The session keeps banked time plus the instant the current run segment
  began, so remaining time is always derived from the clock and survives process death.
- The end is an `AlarmManager` alarm, so it fires whether or not the app is alive.
- A running session appears as a card pinned to the front of the Up next row, and has its own
  full-screen destination.
- Finishing offers another session, a break, or completing the task.

Per-task totals for the current day (`sessions`, `minutes`) live in `FocusSessionRepository`, backed
by `SharedPreferences` rather than Room — it is one small record with no history to query, and it has
to be readable from a broadcast receiver on a cold start without opening the encrypted database. The
record is stamped with its date and resets when the date moves.

### Reminders know about sessions

A task reminder notification is withheld for a task that has already had focus time today — a banked
session, minutes worked, or a timer running on it right now
([#357](https://github.com/mandrecode/tempo/issues/357)). This covers both the scheduled reminder
alarm and the daily missed-reminder catch-up sweep. Only the notification is withheld: periodic
rollover and the sweep's re-arm still happen.

## Where the code lives

```text
features/focus/
├── data/          FocusSessionRepositoryImpl (SharedPreferences-backed)
├── domain/
│   ├── model/     FocusAgenda, FocusAgendaItem, FocusSession, TaskFocusToday,
│   │              FocusHeadlineBand, FocusDayMembership
│   ├── repository/ FocusSessionRepository
│   ├── scheduler/  FocusSessionScheduler
│   └── usecase/   GetFocusAgenda, GetUpNextItem, GetFocusStreak, GetFocusHistory,
│                  RecordDailyActivity, HasFocusTimeToday, FocusSessionUseCases
└── presentation/  FocusScreen/Content/ViewModel/Contract, FocusSessionScreen,
                   components/ (UpNextCard, RunningSessionCard, SessionFinishedSheet, …)

infrastructure/focus/   AndroidFocusSessionScheduler, session end/action receivers
core/data/…/DailyFocusActivity*   the per-day counts behind the streak and heatmap
```

## Settings

`features/settings/presentation/FocusSection.kt` — default session length and break length. Changing
either never affects a session already running.

## Related documents

- [`openspec/changes/feat-42-focus-mode/`](../../openspec/changes/feat-42-focus-mode/) — the original
  proposal, design and specs
- [`openspec/changes/fix-343-focus-and-bar-polish/`](../../openspec/changes/fix-343-focus-and-bar-polish/)
  — agenda interaction and motion follow-ups
