## 1. Locate List Surfaces

- [x] 1.1 Identify the Compose task list implementation that renders the final real task card.
- [x] 1.2 Identify the Compose habit list implementation that renders the final real habit card.
- [x] 1.3 Read the UI/UX agent guidance before editing UI files.

## 2. Implement Bottom Clearance

- [x] 2.1 Add invisible trailing scroll clearance to populated task lists without adding a fake task item.
- [x] 2.2 Add invisible trailing scroll clearance to populated habit lists without adding a fake habit item.
- [x] 2.3 Confirm empty task and habit states do not show new visible or interactive placeholders.

## 3. Verification

- [x] 3.1 Run `openspec validate feat-90-ghost-scroll-card`.
- [x] 3.2 Run `./gradlew ktlintFormat`.
- [x] 3.3 Run focused tests or checks relevant to the touched UI files.
- [x] 3.4 Run `./gradlew ktlintCheck` and `./gradlew :app:detekt` if implementation touches Kotlin source.
