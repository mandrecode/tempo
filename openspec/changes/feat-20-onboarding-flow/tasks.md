## 1. Completion persistence and startup

- [x] 1.1 Add and bind a SharedPreferences-backed onboarding completion repository with reactive state and idempotent completion
- [x] 1.2 Include onboarding completion in `MainUiState`/`MainViewModel` and select onboarding as the incomplete first-run destination
- [x] 1.3 Add unit tests for completion persistence and startup state

## 2. Onboarding state and behavior

- [x] 2.1 Add the onboarding MVI contract and ViewModel backed by the existing theme, navigation, and retention preferences
- [x] 2.2 Implement ordered page navigation, Skip/Finish effects, Tempo color initialization, tab invariants, valid default-tab handling, and retention updates
- [x] 2.3 Add ViewModel unit tests covering paging, preference changes, invariants, completion, and replay outcomes

## 3. Onboarding UI

- [x] 3.1 Build the pure four-page onboarding Content with accessible controls, progress, and responsive edge-to-edge layout
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
