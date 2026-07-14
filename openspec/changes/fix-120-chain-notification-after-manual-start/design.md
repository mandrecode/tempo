## Context

`HabitReminderReceiver` currently checks a standalone habit's completion history before posting its reminder, but the chain branch always posts after loading the chain. Manual chain progress is already persisted on each member habit for the selected date, so the receiver has enough durable state to determine whether the scheduled occurrence has begun.

The alarm receiver must remain responsible for recurrence rollover: whether or not the current notification is visible, it advances the chain reminder and schedules the next occurrence.

## Goals / Non-Goals

**Goals:**

- Prevent a redundant chain reminder once any member has been completed for the scheduled occurrence date.
- Preserve the next recurring reminder when the current notification is suppressed.
- Make the delivery decision testable without requiring an Android alarm integration test.

**Non-Goals:**

- Changing how habit or chain completion is stored.
- Changing live-activity creation or dismissal.
- Cancelling alarms proactively from the UI toggle path.
- Changing chain recurrence calculations.

## Decisions

1. **Gate notification delivery in the receiver using member completion history.** After loading the chain, the receiver loads its member habits and posts only when none records completion for the occurrence date. This is resilient to an alarm already being dispatched and uses the final durable state at delivery time. Proactive alarm cancellation was considered, but it cannot fully eliminate receiver races and would require carefully preserving the next recurrence from a separate path.

2. **Carry the occurrence date in the chain alarm intent and prefer it at delivery.** Completion history is date-specific, so the alarm scheduler stores the trigger's local date in `EXTRA_SCHEDULED_DATE`, matching standalone habit alarms. The receiver prefers that immutable occurrence date over the chain's stored reminder, which may already have advanced before a delayed delivery.

3. **Keep recurrence advancement unconditional after a valid chain is loaded.** Suppression changes presentation only. The existing `rescheduleHabitChain` call remains outside the notification condition so each delivered alarm advances at most once through the existing date comparison and repository update.

4. **Extract a visible-for-testing pure predicate.** Unit tests cover no progress, partial progress, and date-specific delayed occurrences. The receiver performs repository reads and scheduler side effects outside any Room transaction; this change adds no new write transaction boundary.

5. **Skip the repository member lookup for empty chains.** Empty chains are valid and have no progress that can suppress a notification. Returning an empty member list directly avoids passing an empty `IN` argument to Room while preserving the existing reminder behavior.

## Risks / Trade-offs

- **[A stale or missing member record is absent from the repository result]** → Evaluate available members; any persisted matching completion suppresses the prompt, while no matching completion preserves the existing reminder behavior.
- **[A chain has no members]** → Skip the Room lookup and treat the occurrence as unstarted.
- **[Progress changes concurrently with receiver delivery]** → Reading immediately before notification construction narrows the race; Android may still interleave a toggle after the read, but the live-activity path dismisses a posted chain notification when progress begins.
- **[Suppressed notification could accidentally stop future reminders]** → Keep recurrence rescheduling independent from the display predicate and cover that invariant in the implementation structure and tests.

## Migration Plan

No data or schema migration is required. Deploy as an application update; rollback restores unconditional chain reminder delivery without affecting stored reminder dates.

## Open Questions

None.
