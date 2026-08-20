## 1. Settings Redesign

- [x] 1.1 Refactor `SettingsContent` around a large title, section labels, rounded Settings cards, and local helper composables.
- [x] 1.2 Restyle theme, color scheme, tab visibility, and default-tab controls to match the Didi segmented-card treatment while preserving emitted events.
- [x] 1.3 Restyle notification, language, review, and feedback rows with Didi-style icon containers, typography, trailing affordances, and existing click behavior.
- [x] 1.4 Keep version text and scroll spacing aligned with the redesigned screen hierarchy.

## 2. Previews and Verification

- [x] 2.1 Update Settings debug previews for redesigned light, dark, Tempo, and system states.
- [x] 2.2 Run `openspec validate feat-47-settings-didi-design`.
- [x] 2.3 Run `./gradlew ktlintFormat`.
- [x] 2.4 Run focused build/static checks relevant to this UI-only change.
- [x] 2.5 Create a draft PR for issue #47.
