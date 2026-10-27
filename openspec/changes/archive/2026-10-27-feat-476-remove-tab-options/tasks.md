# Tasks

- [x] 1.1 Remove the Settings tab sections and the onboarding setup page (5 pages); onboarding exits to Focus.
- [x] 1.2 Reduce `NavigationPreferencesRepository` to the last route; remove `TabPreferencesPolicy`, the tab preference keys and their migration.
- [x] 1.3 Show every tab in `TempoBottomNavigation`; start on the last-used tab (Focus when none); remove the single-tab bar layout and `compactSoloAction`.
- [x] 1.4 Backup: stop writing the tab fields, ignore them on import; update `docs/BACKUP_FORMAT.md`.
- [x] 1.5 Update the `tab-order` spec; update or remove the affected unit and instrumented tests.
- [x] 1.6 Run `./gradlew ktlintCheck :app:detekt testDebugUnitTest lintDebug` and compile the instrumented tests.
