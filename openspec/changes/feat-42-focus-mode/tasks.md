Each numbered group is one child PR onto the `feat/42-focus-mode` integration branch. Every group
must end green on `./gradlew ktlintFormat && ./gradlew ktlintCheck && ./gradlew :app:detekt &&
./gradlew testDebugUnitTest` before its PR is opened.

## 1. Tab registry and navigation generalisation

- [ ] 1.1 Add a `TempoTab` enum (`FOCUS`, `ROUTINES`, `TASKS`) carrying route, selected/unselected
      icon, title resource and preference key, and derive `navigationItems` from it
- [ ] 1.2 Add `FocusRoute` as a `NavKey`, its back stack in `TempoNavigator`, and its
      `TempoNavigator.Section` entry with `toSection()` coverage
- [ ] 1.3 Replace `isRoutinesTabEnabled()`/`isTasksTabEnabled()` on `NavigationPreferencesRepository`
      with `enabledTabs(): Flow<Set<TempoTab>>` and `setTabEnabled(tab, enabled)`
- [ ] 1.4 Implement the read-time preference migration seeding the tab set from the legacy
      `routines_tab_enabled`/`tasks_tab_enabled` keys with Focus enabled, leaving legacy keys in place
- [ ] 1.5 Extract the "at least one tab enabled" and "default tab must be enabled" rules into one
      shared policy used by both `SettingsViewModel` and `OnboardingViewModel`
- [ ] 1.6 Update `MainActivity.rememberStartDestination` to resolve the start tab from the registry
      and enabled set
- [ ] 1.7 Make `TabsAndNavigationSection` and `DefaultTabSection` registry-driven, adding the Focus
      switch and Focus chip
- [ ] 1.8 Add the Focus tab option to onboarding's tab and default-tab steps
- [ ] 1.9 Extend `BackupSettingsDataSource` and the backup DTO with the tab set and default tab as
      optional versioned fields defaulting to Focus enabled
- [ ] 1.10 Register a placeholder Focus destination rendering an empty scaffold so navigation is
      exercisable before the screen exists
- [ ] 1.11 Add `ic_focus`/`ic_focus_outlined` drawables and `focus` string in `values` and `values-es`
- [ ] 1.12 Unit-test the preference migration, the enablement policy, start-destination resolution,
      and backup round-trip including a pre-change export
- [ ] 1.13 Update existing navigation and settings tests for the registry shape

## 2. Daily focus activity persistence

- [ ] 2.1 Add `DailyFocusActivityEntity` (date primary key, `scheduledCount`, `completedCount`,
      `focusMinutes`) and its DAO
- [ ] 2.2 Add the additive Room migration and regenerate `app/schemas/` via `./gradlew kspDebugKotlin`
- [ ] 2.3 Add the `DailyFocusActivity` domain model, repository interface, mapper and implementation,
      and bind it in `core/di/RepositoryModule.kt`
- [ ] 2.4 Add `RecordDailyActivityUseCase` writing today's counts, invoked from task and habit
      completion toggles
- [ ] 2.5 Add `GetFocusHistoryUseCase` returning the last N days with the three-state classification
      derived from counts
- [ ] 2.6 Add `GetFocusStreakUseCase` counting days with any completion and skipping days with
      nothing scheduled
- [ ] 2.7 Include daily activity in backup export/import with an optional field defaulting to empty
- [ ] 2.8 Unit-test state classification thresholds, streak forgiveness, unscheduled-day skipping,
      and that history is unaffected by completed-task deletion

## 3. Focus screen — summary and agenda

- [ ] 3.1 Create `features/focus/` with `FocusContract` (`UiState`/`UiEvent`/`UiEffect`) using
      `kotlinx.collections.immutable` collections
- [ ] 3.2 Add `GetFocusAgendaUseCase` merging tasks due today, overdue tasks, habits and chains
      scheduled today into ordered sections, excluding future and undated items
- [ ] 3.3 Add `GetUpNextItemUseCase` applying priority-then-due-time ranking over uncompleted items
- [ ] 3.4 Add `FocusViewModel` as a thin orchestrator over those use cases
- [ ] 3.5 Add `resolveHeadlineBand` selecting the four completion bands plus the unscheduled-day
      case, unit-tested at every boundary (0, just under 1/3, exactly 1/3, just under 2/3, exactly
      2/3, one item left, all complete, nothing scheduled)
- [ ] 3.6 Build `FocusSummaryHero` on `primaryContainer`: header line with weekday and streak, the
      band-selected headline, seven-day row, and a determinate `CircularWavyProgressIndicator`
- [ ] 3.7 Build `UpNextCard` on `tertiaryContainer`, collapsing entirely when no item qualifies
- [ ] 3.8 Build `FocusContent` with the two-block seam, `WavyDivider` section headers for Overdue and
      Today with counts, and interleaved task/habit/chain rows carrying type and category pills
- [ ] 3.9 Add the undated-task footer count navigating to the Tasks tab
- [ ] 3.10 Wire completion, edit and add actions to the existing task and habit editor sheets
- [ ] 3.11 Replace the placeholder destination with `FocusScreen` and wire its floating-bar add action
- [ ] 3.12 Add all Focus strings to `values` and `values-es`
- [ ] 3.13 Add `@Preview` composables in `src/debug/` for the hero, Up next card and full content in
      light and dark, including empty and all-complete states
- [ ] 3.14 Unit-test agenda assembly (exclusions, section membership, ordering) and Up next ranking
      including ties and completed items
- [ ] 3.15 UI-test `FocusContent` for section order, Up next collapse, and the undated footer

## 4. Focus session timer

- [ ] 4.1 Add the `FocusSession` domain model and a single-nullable-session preferences-backed
      repository
- [ ] 4.2 Add start, pause, end and complete use cases, with starting a new session replacing any
      running one and banking its elapsed minutes
- [ ] 4.3 Add the exact-alarm scheduler and receiver for session end, following the existing reminder
      scheduler shape
- [ ] 4.4 Build the ongoing notification with `setUsesChronometer(true)` and
      `setChronometerCountDown(true)`, reusing the dismissal-persistence behavior from the habit chain
      live activity
- [ ] 4.5 Add boot and app-start reconciliation restoring or completing a persisted session, treating
      a passed end time as complete
- [ ] 4.6 Transform the Up next card into the running session card in place, with its own remaining
      time state holder that the agenda does not observe
- [ ] 4.7 Build the immersive session view with the large ring, task title and subtask checklist,
      reachable by tapping the session card and collapsing back with the session intact
- [ ] 4.8 Build the session-complete `TempoModalSheet` offering break, another session and stop as
      equal choices, with no streak or celebration content
- [ ] 4.9 Add the timer chip to `PersistentFloatingBar` for non-Focus tabs, returning to Focus on tap
- [ ] 4.10 End the session and show the completion sheet when its task is completed mid-session
- [ ] 4.11 Record elapsed minutes into the daily activity record on every session end
- [ ] 4.12 Add the Focus settings section with the default session length chips (15/20/25/30/45/60,
      default 25) and its preferences repository binding
- [ ] 4.13 Add all session strings to `values` and `values-es`
- [ ] 4.14 Add `@Preview` composables in `src/debug/` for the running card, immersive view and
      completion sheet
- [ ] 4.15 Unit-test session replacement, elapsed-minute banking, passed-end-time completion, and that
      changing the default length leaves a running session untouched

## 5. Adaptivity, what's-new and release polish

- [ ] 5.1 Apply `adaptiveScreenContentLayout` and rail clearance to Focus, verifying no rail underlap
      at the medium tier
- [ ] 5.2 Implement the expanded-tier two-pane layout for summary and agenda, resolving the pane-width
      open question from the design
- [ ] 5.3 Verify the Focus tab switch is an instant cut with no transition, matching Routines and Tasks
- [ ] 5.4 Add an optional action label and callback to `WhatsNewEntry`, leaving actionless entries
      rendering unchanged
- [ ] 5.5 Replace `WhatsNewRegistry.latest` with the Focus entry offering the one-time start-tab
      switch, and remove the superseded `settings-source-code-link` strings
- [ ] 5.6 Verify on device that an existing user's default tab is unchanged by the update
- [ ] 5.7 Run `./gradlew lintDebug` and confirm no `MissingTranslation` or `ExtraTranslation`
- [ ] 5.8 Run `./gradlew koverVerifyDebug` and close any coverage gaps in new domain code
- [ ] 5.9 Confirm `app/detekt-baseline.xml` has not grown beyond 189 entries
- [ ] 5.10 Run `:app:connectedDebugAndroidTest` on the Pixel 10 AVD
- [ ] 5.11 Run `openspec validate feat-42-focus-mode` and open the final squashed `feat(#42)` PR to
      `main` with `Closes #42`, `Closes #22`, `Closes #19`
