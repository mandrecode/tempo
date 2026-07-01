## 1. Implementation

- [x] 1.1 Add a reusable animated Settings icon action that matches Didi's shape, border, and pressed/unpressed color behavior.
- [x] 1.2 Replace the current route top-bar Settings `IconButton` with the animated Settings action while preserving navigation behavior and content description.
- [x] 1.3 Add or update previews/tests where useful for the changed top-bar affordance.

## 2. Verification

- [x] 2.1 Run `openspec validate feat-75-settings-icon-style`.
- [x] 2.2 Run focused tests or checks for the changed Compose/navigation code.
- [x] 2.3 Run `./gradlew ktlintFormat` and any blocking static checks needed before handoff.
