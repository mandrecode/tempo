## Why

Live habit-chain notifications can remain active past midnight, but tapping one currently opens the chain in today's routines context instead of the scheduled date that started the live notification. This breaks overnight chain continuation and is tracked by [GitHub issue #91](https://github.com/mandrecode/tempo/issues/91).

## What Changes

- Carry the live habit-chain notification's scheduled date through its tap intent.
- Preserve that scheduled date in the pending notification action that drives app navigation.
- When opening a habit chain from a notification, select the notification's scheduled date before showing the chain sheet.
- Keep existing behavior for notification actions that do not include a scheduled date.
- Non-goal: changing reminder scheduling, chain completion rules, or live notification progress aggregation.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `habit-chain-live-notification-continuation`: notification taps must restore the scheduled date associated with the live habit-chain session.

## Impact

- Affects live activity notification intent creation, pending notification action parsing/storage, and routines notification navigation.
- Adds focused unit coverage for scheduled-date propagation and view-model handling.
- No new dependencies, database schema changes, or public API changes.
