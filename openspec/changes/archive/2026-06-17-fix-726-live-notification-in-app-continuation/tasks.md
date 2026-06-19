## 1. Repository behavior

- [x] 1.1 Update `HabitRepositoryImpl.toggleHabitCompletion` to allow past-date in-app chain updates only when a chain has an active live activity.
- [x] 1.2 Preserve existing behavior for today and notification-triggered updates.
- [x] 1.3 Keep transaction boundaries and post-transaction live activity updates unchanged.

## 2. Regression coverage

- [x] 2.1 Add a test proving past-date in-app toggles update an active live activity chain.
- [x] 2.2 Add or adjust a test proving past-date in-app toggles still skip inactive chains.

## 3. Verification

- [x] 3.1 Run `openspec validate fix-726-live-notification-in-app-continuation`.
- [x] 3.2 Run `./gradlew ktlintFormat`.
- [x] 3.3 Run `./gradlew testDebugUnitTest`.
