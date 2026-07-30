Each numbered group is one child PR onto the `feat/42-focus-mode` integration branch. Every group
must end green on `./gradlew ktlintFormat && ./gradlew ktlintCheck && ./gradlew :app:detekt &&
./gradlew testDebugUnitTest` before its PR is opened.

## 1. Tab registry and navigation generalisation

- [x] 1.1 Add a `TempoTab` enum (`FOCUS`, `ROUTINES`, `TASKS`) carrying route, selected/unselected
      icon, title resource and preference key, and derive `navigationItems` from it
- [x] 1.2 Add `FocusRoute` as a `NavKey`, its back stack in `TempoNavigator`, and its
      `TempoNavigator.Section` entry with `toSection()` coverage
- [x] 1.3 Replace `isRoutinesTabEnabled()`/`isTasksTabEnabled()` on `NavigationPreferencesRepository`
      with `enabledTabs(): Flow<Set<TempoTab>>` and `setTabEnabled(tab, enabled)`
- [x] 1.4 Implement the read-time preference migration seeding the tab set from the legacy
      `routines_tab_enabled`/`tasks_tab_enabled` keys with Focus enabled and Focus as the default
      tab, applied exactly once, leaving legacy keys in place
- [x] 1.5 Extract the "at least one tab enabled" and "default tab must be enabled" rules into one
      shared policy used by both `SettingsViewModel` and `OnboardingViewModel`
- [x] 1.6 Update `MainActivity.rememberStartDestination` to resolve the start tab from the registry
      and enabled set
- [x] 1.7 Make `TabsAndNavigationSection` and `DefaultTabSection` registry-driven, adding the Focus
      switch and Focus chip
- [x] 1.8 Add the Focus tab option to onboarding's tab and default-tab steps
- [x] 1.9 Extend `BackupSettingsDataSource` and the backup DTO with the tab set and default tab as
      optional versioned fields defaulting to Focus enabled
- [x] 1.10 Register a placeholder Focus destination rendering an empty scaffold so navigation is
      exercisable before the screen exists
- [x] 1.11 Add `ic_focus`/`ic_focus_outlined` drawables and `focus` string in `values` and `values-es`
- [x] 1.12 Unit-test the preference migration, the enablement policy, start-destination resolution,
      and backup round-trip including a pre-change export
- [x] 1.13 Update existing navigation and settings tests for the registry shape

## 2. Daily focus activity persistence

- [x] 2.1 Add `DailyFocusActivityEntity` (date primary key, `scheduledCount`, `completedCount`,
      `focusMinutes`) and its DAO
- [x] 2.2 Add the additive Room migration and regenerate `app/schemas/` via `./gradlew kspDebugKotlin`
- [x] 2.3 Add the `DailyFocusActivity` domain model, repository interface, mapper and implementation,
      and bind it in `core/di/RepositoryModule.kt`
- [x] 2.4 Add `RecordDailyActivityUseCase` writing today's counts, invoked from task and habit
      completion toggles
- [x] 2.5 Add `GetFocusHistoryUseCase` returning the last N days with the three-state classification
      derived from counts
- [x] 2.6 Add `GetFocusStreakUseCase` counting days with any completion and skipping days with
      nothing scheduled
- [x] 2.7 Include daily activity in backup export/import with an optional field defaulting to empty
- [x] 2.8 Unit-test state classification thresholds, streak forgiveness, unscheduled-day skipping,
      and that history is unaffected by completed-task deletion

## 3. Focus screen — summary and agenda

- [x] 3.1 Create `features/focus/` with `FocusContract` (`UiState`/`UiEvent`/`UiEffect`) using
      `kotlinx.collections.immutable` collections
- [x] 3.2 Add `GetFocusAgendaUseCase` merging tasks due today, overdue tasks, habits and chains
      scheduled today into ordered sections, excluding future and undated items
- [x] 3.3 Add `GetUpNextItemUseCase` selecting uncompleted tasks in the agenda's own order
      (revised: it does no ranking of its own — a row that disagreed with the list beneath it
      made one of the two look wrong, and habits were dropped as candidates entirely)
- [x] 3.4 Add `FocusViewModel` as a thin orchestrator over those use cases
- [x] 3.5 Add `resolveHeadlineBand` selecting the four completion bands plus the unscheduled-day
      case, unit-tested at every boundary (0, just under 1/3, exactly 1/3, just under 2/3, exactly
      2/3, one item left, all complete, nothing scheduled)
- [x] 3.6 Build `FocusSummaryHero` on `primaryContainer`: header line with weekday and streak, the
      band-selected headline, seven-day row, and a determinate `CircularWavyProgressIndicator`
- [x] 3.7 Build `UpNextCard` on `tertiaryContainer`, collapsing entirely when nothing qualifies
      (revised: a swipeable row of up to five cards, drawn from the sections rather than
      lifted out of them, each card as tall as its own content — a running session, a task
      already worked on and an untouched one carry different controls, so one height for all
      of them only padded the short ones out)
- [x] 3.8 Build `FocusContent` with the two-block seam, `WavyDivider` section headers for Overdue and
      Today with counts, and interleaved task/habit/chain rows carrying type and category pills
- [x] 3.9 Add the undated-task footer count navigating to the Tasks tab
- [x] 3.10 Wire completion, edit and add actions to the existing task and habit editor sheets
- [x] 3.11 Replace the placeholder destination with `FocusScreen` and wire its floating-bar add action
- [x] 3.12 Add all Focus strings to `values` and `values-es`
- [ ] 3.13 Add `@Preview` composables in `src/debug/` for the hero, Up next card and full content in
      light and dark, including empty and all-complete states
- [x] 3.14 Unit-test agenda assembly (exclusions, section membership, ordering) and Up next ranking
      including ties and completed items
- [ ] 3.15 UI-test `FocusContent` for section order, Up next collapse, and the undated footer

## 4. Focus session timer

- [x] 4.1 Add the `FocusSession` domain model and a single-nullable-session preferences-backed
      repository
- [x] 4.2 Add start, pause, end and complete use cases, with starting a new session replacing any
      running one and banking its elapsed minutes
- [x] 4.3 Add the exact-alarm scheduler and receiver for session end, following the existing reminder
      scheduler shape
- [x] 4.4 Build the ongoing notification with `setUsesChronometer(true)` and
      `setChronometerCountDown(true)`, reusing the dismissal-persistence behavior from the habit chain
      live activity
- [x] 4.5 Add boot and app-start reconciliation restoring or completing a persisted session, treating
      a passed end time as complete
- [x] 4.6 Transform the Up next card into the running session card in place, with its own remaining
      time state holder that the agenda does not observe
- [x] 4.7 Build the session surface with the large ring, task title and subtask checklist,
      reachable by tapping the session card and dismissable with the session intact
      (revised: a bottom sheet rather than an immersive view or a slide-in screen, so it
      arrives the way every other single-item surface in the app does)
- [x] 4.8 Build the session-complete `TempoModalSheet` offering break, another session and stop as
      equal choices, with no streak or celebration content
- [x] 4.9 Add the timer chip to `PersistentFloatingBar` for non-Focus tabs, returning to Focus on tap
- [x] 4.10 End the session and show the completion sheet when its task is completed mid-session
- [x] 4.11 Record elapsed minutes into the daily activity record on every session end
- [x] 4.12 Add the Focus settings section with the session and break lengths and its preferences
      repository binding
      (revised: both lengths, set on a clock face in hours and minutes rather than a fixed
      row of chips, uncapped beyond the picker's own 23:59; a session can also start at a
      length chosen for that start alone)
- [x] 4.13 Add all session strings to `values` and `values-es`
- [ ] 4.14 Add `@Preview` composables in `src/debug/` for the running card, immersive view and
      completion sheet
- [x] 4.15 Unit-test session replacement, elapsed-minute banking, passed-end-time completion, and that
      changing the default length leaves a running session untouched

## 5. Adaptivity, what's-new and release polish

- [x] 5.1 Apply `adaptiveScreenContentLayout` and rail clearance to Focus, verifying no rail underlap
      at the medium tier
- [ ] 5.2 Implement the expanded-tier two-pane layout for summary and agenda, resolving the pane-width
      open question from the design
- [x] 5.3 Verify the Focus tab switch is an instant cut with no transition, matching Routines and Tasks
- [x] 5.4 Replace `WhatsNewRegistry.latest` with the Focus entry, whose copy states that Focus is
      now the start tab and where to change it, and remove the superseded
      `settings-source-code-link` strings
- [x] 5.5 Verify on device that an existing installation is moved to Focus once, and that setting
      the tab back to Routines survives a relaunch
- [x] 5.6 Run `./gradlew lintDebug` and confirm no `MissingTranslation` or `ExtraTranslation`
- [x] 5.7 Run `./gradlew koverVerifyDebug` and close any coverage gaps in new domain code
- [x] 5.8 Confirm `app/detekt-baseline.xml` has not grown beyond 189 entries
- [x] 5.9 Run `:app:connectedDebugAndroidTest` on the Pixel 10 AVD
- [x] 5.10 Run `openspec validate feat-42-focus-mode` and open the final squashed `feat(#42)` PR to
      `main` with `Closes #42`, `Closes #22`, `Closes #19`

## Not done

Left deliberately, and worth carrying into the archive rather than quietly dropping:

- [ ] 3.13 (partial) Previews cover `FocusContent` in light and dark and in its empty, mid-day and
      all-complete states. The hero and the Up next card have none of their own.
- [ ] 3.15 No UI test for `FocusContent`. Section order, row collapse and the undated footer are
      covered only by the unit tests behind them.
- [ ] 4.14 No previews for the running-session card, the session sheet or the completion sheet.
- [ ] 5.2 No expanded-tier two-pane layout. Focus uses `adaptiveScreenContentLayout` and clears the
      rail, and was checked at 360dp, 411dp, compact rail and expanded rail — but summary and agenda
      remain one column at every width, so the design's pane-width question is still open.

## Deviations from the plan

Decisions taken during the work that the groups above no longer describe on their own:

- **Focus is the default tab for every installation, not an offer.** The plan had the what's-new
  sheet asking; it now states the change and points at the Settings entry that reverses it.
- **The completion sheet belongs to expiry alone.** It had been raised on every ending, including
  the three the user had just chosen, and never on the one it was written for.
- **The session notification carries the app's controls**, at the same importance and promoted
  standing as a habit-chain live activity, rather than sitting silently in the shade.
- **Daily activity counts a chain once**, matching the agenda, after review found it counted every
  habit inside one and disagreed with the screen.
