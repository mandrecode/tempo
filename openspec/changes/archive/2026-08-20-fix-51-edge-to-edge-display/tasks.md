## 1. Edge-to-edge layout

- [x] 1.1 Configure the Tasks and Routines scaffolds to pass zero top-level content window insets while preserving top-app-bar padding.
- [x] 1.2 Include the current navigation-bar inset in Tasks and Routines list bottom-clearance calculation without duplicating overlay-control padding.

## 2. Regression coverage

- [x] 2.1 Add focused tests for portrait and rail bottom-clearance calculations with and without navigation-bar insets.
- [x] 2.2 Run the relevant unit and Compose UI tests for the changed screen layout behavior.

## 3. Quality gates

- [x] 3.1 Run `openspec validate fix-51-edge-to-edge-display`, `./gradlew ktlintFormat`, `./gradlew ktlintCheck`, and `./gradlew :app:detekt`.
- [x] 3.2 Run `./gradlew testDebugUnitTest`, `./gradlew assembleDebug`, and manually smoke-test edge-to-edge layout on the preferred connected device or fallback AVD.
