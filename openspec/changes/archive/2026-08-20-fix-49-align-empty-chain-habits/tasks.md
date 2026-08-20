## 1. Alignment Fix

- [x] 1.1 Adjust the habit-chain selection icon inset for the empty selector state while preserving populated-state alignment.
- [x] 1.2 Add Compose UI regression coverage for empty and populated habit-chain selections.

## 2. Verification

- [x] 2.1 Run the focused Compose UI tests on the default local AVD.
- [x] 2.2 Run `./gradlew ktlintFormat`, `./gradlew ktlintCheck`, and `./gradlew :app:detekt`.
- [x] 2.3 Run `openspec validate fix-49-align-empty-chain-habits` and confirm the change remains apply-ready.
