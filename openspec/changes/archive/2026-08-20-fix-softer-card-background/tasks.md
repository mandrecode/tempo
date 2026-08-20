## 1. Neutral Card Surface

- [x] 1.1 Move task and routine neutral card fallbacks from `surfaceContainerLow` to the adjacent `surfaceContainer` role while preserving animations, alpha, and explicit colors.
- [x] 1.2 Move Settings section cards to the same `surfaceContainer` role without changing other Settings components.

## 2. Verification

- [x] 2.1 Update focused tests to cover the softer neutral surface role in light and dark themes.
- [x] 2.2 Run `openspec validate fix-softer-card-background`, formatting, unit tests, lint, and static analysis.
