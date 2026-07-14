## Why

New users currently land in Tempo without an explanation of how tasks, categories, routines, and reminders fit together. For the public release, [GitHub issue #20](https://github.com/mandrecode/tempo/issues/20) calls for a short, skippable first-run flow that teaches those concepts and lets users choose the most important defaults before entering the app.

## What Changes

- Add a lightweight, paged onboarding flow that introduces tasks and categories before routines and reminders.
- Show onboarding automatically on first launch, while allowing users to skip it at any point.
- Let users choose Tempo's signature colors or dynamic colors, with Tempo selected by default for new installations.
- Let users enable or disable the Tasks and Routines tabs while guaranteeing that at least one remains enabled.
- Let users choose an enabled default tab.
- Let users configure automatic removal and retention days for completed tasks.
- Finish or skip onboarding by handing off to the selected default tab, and persist completion so onboarding does not block later launches.
- Make onboarding replayable from Settings without resetting first-run state or changing the user's saved choices unless they explicitly edit them.
- Keep notification permission requests out of this flow; onboarding explains reminders, while the existing contextual permission-education behavior remains responsible for requesting access.

## Capabilities

### New Capabilities

- `first-run-onboarding`: First-run gating, concept education, preference configuration, skipping, completion, replay, and handoff behavior.

### Modified Capabilities

None.

## Impact

- Adds an onboarding presentation feature, route, ViewModel, preference persistence, localized resources, previews, and tests in `:app`.
- Integrates app startup and Settings navigation with the onboarding route.
- Reuses existing theme, navigation-tab, default-tab, and completed-task-retention preferences; no new external dependency, Room schema change, or permission flow is required.
