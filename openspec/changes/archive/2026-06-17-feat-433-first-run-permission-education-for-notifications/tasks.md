## 1. Permission Education UI

- [x] 1.1 Update `HandleReminderPermissions` to show reminder education before requesting Android notification permission when notifications are missing.
- [x] 1.2 Add explicit education actions for continuing to the system prompt and deferring without opening the prompt.
- [x] 1.3 Keep notification-granted and pre-Android-13 paths flowing directly to exact alarm checks.
- [x] 1.4 Keep denied and permanently disabled notification states explicit, including settings navigation for permanently disabled permission.
- [x] 1.5 Re-check notification permission on lifecycle resume while education is visible and proceed when permission has become granted.

## 2. Copy, Localization, and Preview

- [x] 2.1 Add English notification education and updated rationale strings.
- [x] 2.2 Add matching Spanish translations for every new or changed translatable string.
- [x] 2.3 Add debug previews for the reminder permission education/rationale UI where practical.

## 3. Verification

- [x] 3.1 Run `openspec validate feat-433-first-run-permission-education-for-notifications`.
- [x] 3.2 Run `./gradlew ktlintFormat`.
- [x] 3.3 Run `./gradlew testDebugUnitTest`.
- [x] 3.4 Run `./gradlew lintDebug` if string resources were changed.
