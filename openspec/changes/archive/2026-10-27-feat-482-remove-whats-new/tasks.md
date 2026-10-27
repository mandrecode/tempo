# Tasks

- [x] 1.1 Remove the what's-new sheet, registry, entry model, preference repository and DI binding.
- [x] 1.2 Drop the what's-new state from `MainViewModel`/`MainUiState`; remove the onboarding-active and splash hand-over signals that only served it.
- [x] 1.3 Remove the what's-new strings (en, es), its tests and previews; update `MainViewModelTest`.
- [x] 1.4 Remove step 5 from the New Feature Checklist in AGENTS.md and the README mention.
- [x] 1.5 Run `./gradlew ktlintCheck :app:detekt testDebugUnitTest lintDebug` and compile the instrumented tests.
