## 1. Day membership, shared (#359)

- [x] 1.1 Add a pure-Kotlin membership helper in `features/focus/domain/model/` that answers whether a
      task is on a given day: due that day, or overdue and still open — and, when the task has a
      parent, only if the parent is not itself on the day.
- [x] 1.2 Use the helper in `GetFocusAgendaUseCase` to build the overdue and today sections from all
      tasks rather than from top-level tasks only, keeping undated tasks out and the undated footer
      counting top-level tasks only.
- [x] 1.3 Use the same helper in `RecordDailyActivityUseCase.countsTowards`, replacing its
      hand-copied rule, so the hero and heatmap count exactly the rows the agenda shows.
- [x] 1.4 Unit-test the helper directly: undated parent with a dated subtask, parent due today with a
      dated subtask, future-dated parent with an overdue subtask, completed overdue parent, undated
      subtask, and a promoted subtask carrying subtasks of its own.

## 2. Today before overdue (#353)

- [x] 2.1 In `GetFocusAgendaUseCase`, hand `todayItems + overdue` to `GetUpNextItemUseCase` so the
      shortlist fills from today first and falls back to overdue.
- [x] 2.2 In `FocusContent.FocusAgendaList`, emit the Today section and its header above the Overdue
      section and its header, leaving both headers, counts and contents otherwise unchanged.
- [x] 2.3 Update the KDoc on `GetUpNextItemUseCase` and `FocusAgenda.upNext` so "the order the day
      already puts them in" names the new order rather than the old one.
- [x] 2.4 Unit-test in `GetFocusAgendaUseCaseTest`: today's task leads Up next over an overdue one;
      overdue fills the remaining places once today runs out; an all-completed today still yields the
      overdue task.

## 3. Subtask order (#354)

- [x] 3.1 Sort `subtasksByParent` in `GetFocusAgendaUseCase` by `sortOrder` then `id`, the comparator
      `TasksViewModelDataLoading` already uses, so every consumer of a `TaskEntry` inherits it.
- [x] 3.2 Unit-test that a parent's subtasks come back in stored order even when the task list is in
      descending-id order, and that equal sort orders fall back to id.

## 4. Focus-aware reminders (#357)

- [x] 4.1 Add `focusOn(taskId, date)` to `FocusSessionRepository` and implement it in
      `FocusSessionRepositoryImpl` by reading preferences directly and returning an empty record when
      the stored day is not `date`.
- [x] 4.2 Add `HasFocusTimeTodayUseCase` in `features/focus/domain/usecase/`: true when today's record
      for the task has sessions or minutes, or a session (including a break) is running on it.
- [x] 4.3 Gate `taskReminderNotifier.notify` in `TaskReminderReceiver` on the new use case, leaving
      `shouldProcessTaskReminder` and the periodic rollover call outside the gate.
- [x] 4.4 Filter the same way in `MissedReminderCatchUpRunner` before its `forEach`, leaving the
      `finally` re-arm untouched.
- [x] 4.5 Unit-test `FocusSessionRepositoryImpl.focusOn` for the stored-day match, mismatch, and an
      unknown task id.
- [x] 4.6 Unit-test `HasFocusTimeTodayUseCase`: sessions recorded, minutes only, running session,
      running break, yesterday's record, and nothing at all.
- [x] 4.7 Unit-test the two callers: no notify when the task has focus time, notify when it does not,
      periodic rollover still runs while suppressed, and the sweep still re-arms.

## 5. UI coverage

- [x] 5.1 Extend `FocusContentTest` to assert the Today header renders above the Overdue header when
      both sections are populated.
- [x] 5.2 Extend `FocusContentTest` to assert a promoted subtask renders as an ordinary agenda row.

## 6. Docs (#358)

- [x] 6.1 Refresh `README.md`'s feature list, project layout and docs pointers to cover what shipped
      since it was last written: Focus (sessions, day summary, streak/heatmap), vacation mode,
      quick-add widget, encrypted backup export/import, missed-reminder catch-up, what's-new
      onboarding, notification-permission education, and adaptive large-screen layouts.
- [x] 6.2 Check `docs/` for pages the same features contradict or leave unmentioned, and update the
      index/pointers so a reader lands on something current.

## 7. Verification

- [x] 7.1 `./gradlew ktlintFormat`, then `./gradlew compileDebugKotlin compileDebugUnitTestKotlin`.
- [x] 7.2 `./gradlew testDebugUnitTest` and `./gradlew :app:detekt` and `./gradlew ktlintCheck`.
- [x] 7.3 `./gradlew koverVerifyDebug` for the coverage gate.
- [x] 7.4 `openspec validate fix-353-focus-day-priority-and-focus-aware-reminders --strict`.
- [x] 7.5 Run the connected `FocusContentTest` on the Pixel 10 AVD (11/11 green), then smoke-test Focus
      there against seeded data: Today above Overdue, today's task leading Up next, a promoted subtask
      as its own row offering "Start 25 min", a parent on the day keeping its steps nested, subtasks in
      stored order, and the undated footer unchanged.
- [x] 7.6 #357's suppression is not reachable by hand on these AVDs — they are production images with
      no root, so the clock cannot be moved to make an `AlarmManager` alarm fire early and a manual
      `am broadcast` is not delivered to a non-exported receiver. Covered by unit tests instead
      (`HasFocusTimeTodayUseCaseTest`, `TaskReminderReceiverTest`, `MissedReminderCatchUpRunnerTest`).
