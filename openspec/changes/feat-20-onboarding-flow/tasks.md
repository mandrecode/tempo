## 1. Completion persistence and startup

- [x] 1.1 Add and bind a SharedPreferences-backed onboarding completion repository with reactive state and idempotent completion
- [x] 1.2 Include onboarding completion in `MainUiState`/`MainViewModel` and select onboarding as the incomplete first-run destination
- [x] 1.3 Add unit tests for completion persistence and startup state

## 2. Onboarding state and behavior

- [x] 2.1 Add the onboarding MVI contract and ViewModel backed by the existing theme, navigation, and retention preferences
- [x] 2.2 Implement ordered page navigation, Skip/Finish effects, Tempo color initialization, tab invariants, valid default-tab handling, and retention updates
- [x] 2.3 Add ViewModel unit tests covering paging, preference changes, invariants, completion, and replay outcomes

## 3. Onboarding UI

- [x] 3.1 Build the pure five-page onboarding Content with accessible controls, progress, and responsive edge-to-edge layout
- [x] 3.2 Add the onboarding Screen/effect handling and typed route for first-run handoff and Settings replay
- [x] 3.3 Replace the Settings placeholder with onboarding navigation
- [x] 3.4 Add debug light/dark previews and Compose UI tests for content, navigation actions, configuration controls, and Skip availability

## 4. Resources and integration quality

- [x] 4.1 Add matching English and Spanish onboarding strings and reuse existing drawables/design tokens
- [x] 4.2 Run `openspec validate feat-20-onboarding-flow`, `./gradlew ktlintFormat`, `./gradlew lintDebug`, `./gradlew testDebugUnitTest`, `./gradlew ktlintCheck`, and `./gradlew :app:detekt`
- [x] 4.3 Perform a first-run and Settings-replay smoke test on the available Pixel target
- [x] 4.4 Keep the active onboarding route stable when theme, tab, or default-tab preferences change
- [x] 4.5 Animate onboarding button shapes and replace linear progress with Didi-style animated segments
- [x] 4.6 Adapt onboarding across compact, medium, expanded, and short window sizes using Android MediaQuery guidance
- [x] 4.7 Move the segmented onboarding progress above page content to match Didi's hierarchy
- [x] 4.8 Keep onboarding button colors stable while preserving press-driven shape animation
- [x] 4.9 Give the final Start using Tempo action enough width for compact screens and localized labels
- [x] 4.10 Add a centered Tempo logo and app-name welcome page as the final onboarding step
- [x] 4.11 Animate the welcome-to-app handoff with a route-scoped fade-through scale transition
- [x] 4.12 Keep automatic completed-task removal disabled when no preference has been saved
- [x] 4.13 Render the unmodified default app launcher logo on the final welcome page
- [x] 4.14 Remove the redundant Skip action from the final welcome page
- [x] 4.15 Keep completed-task retention controls exclusively in Settings
- [x] 4.16 Sharpen the English and Spanish education pages around user benefits
- [x] 4.17 Match onboarding concept-card contrast to the app's neutral `surfaceContainer` card role
- [x] 4.18 Keep a stable top-level back-stack anchor after first-run onboarding removes the graph start destination
- [x] 4.19 Commit onboarding completion synchronously before the navigation handoff
- [x] 4.20 Resolve independently emitted navigation preferences to an enabled default before updating onboarding UI state
- [x] 4.21 Keep completed-task retention default tests tied to production constants
