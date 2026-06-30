## Context

Habit-chain live activity notifications are created by infrastructure code and opened through `MainActivity` intent extras that become `PendingNotificationAction`. The routines UI consumes that action, navigates to routines, and opens the matching chain sheet. Today the chain id is preserved, but the scheduled date represented by the live notification is not, so an overnight tap after midnight opens the chain while the routines screen is still selected on today.

## Goals / Non-Goals

**Goals:**

- Preserve the live notification's scheduled date across the activity intent and pending action boundary.
- Select the scheduled date before opening the habit-chain sheet from a notification tap.
- Keep behavior idempotent when the same pending action is restored after process recreation.
- Preserve compatibility for any existing `OpenHabitChain` action that does not include a date.

**Non-Goals:**

- Changing chain reminder scheduling or alarm request-code generation.
- Changing how habit and chain completion histories are computed.
- Adding persistence or schema changes for live notification sessions.

## Decisions

- Encode the scheduled date as an ISO-8601 string extra on the live notification content intent. This follows existing Android intent patterns and avoids introducing Android types into domain models.
- Extend `PendingNotificationAction.OpenHabitChain` with an optional `LocalDate`. This keeps the navigation contract explicit while allowing legacy paths to continue opening in the current selected-date context.
- Persist the optional date alongside the pending action in `MainViewModel` saved state. The pending action can survive activity recreation without losing the target date.
- Consume the date in `RoutinesViewModel` by selecting it before opening the chain sheet. This keeps the behavior near existing routines state handling instead of adding navigation-specific date mutations in the app shell.

## Risks / Trade-offs

- Date parsing failure could drop the action if handled too strictly -> treat invalid or absent date extras as `null` and preserve current open-by-id behavior.
- Selecting a date and opening the sheet must be ordered -> update routines state with the requested date before looking up/opening the chain.
- The live notification may be refreshed multiple times for the same chain/date -> intent extras are deterministic and `FLAG_UPDATE_CURRENT` keeps the latest date on the pending intent.

## Migration Plan

No data migration is required. Existing notifications without the new extra continue to open the chain using the current selected date.

## Open Questions

None.
