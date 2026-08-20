## Context

`FocusViewModel.observeDay()` captures `today` once when the screen is created and keeps collecting agenda, history, and vacation state for that date. A habit toggle obtains `today` again at event time. After midnight, the toggle writes a completion for the new date but the still-open screen observes the previous date, leaving the card visually unchanged.

## Goals / Non-Goals

**Goals:**

- Keep the Focus agenda's observed date in sync before a user toggles a habit after midnight.
- Preserve existing completion behavior and reuse the ViewModel's injected clock for deterministic tests.
- Keep the behavior identical across all window-size classes because it changes data freshness only.

**Non-Goals:**

- Proactively redraw an idle Focus screen exactly at midnight.
- Change task completion, habit recurrence, persistence, or UI layout.

## Decisions

### Recreate the day observer when an interaction detects a new local date

Before handling a habit or habit-chain completion event, compare the current local date with `UiState.today`. If it differs, cancel the collector bound to the previous date and start one bound to the new date, then persist the requested completion against that same current date.

This is chosen over a perpetual midnight timer: the interaction is the point at which stale data causes the reported failure, needs no background wake-up while the screen is unused, and can be verified with a controllable clock. Retaining a single day-observer job prevents stale collectors from competing to update the state.

### Keep date selection in the ViewModel

The ViewModel already owns the injected clock and calls the habit completion use case. The UI remains a pure event source; no new domain or data API is required.

## Risks / Trade-offs

- [A screen left idle across midnight continues showing the previous date until the next relevant interaction] → The next habit or chain toggle refreshes the date before writing and rendering completion, eliminating the reported blocked interaction.
- [Replacing a collector can briefly expose the prior state while new flows emit] → Mark the state loading only if needed and update its date only from the new collector; StateFlow-backed repository flows emit promptly.
