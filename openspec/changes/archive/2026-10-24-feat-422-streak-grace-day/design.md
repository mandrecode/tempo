## Context

`CompletionHistoryUtil.getCurrentStreak` walks back from today and stops at the first required (scheduled, not paused) day without a completion. Today pending therefore reads as streak 0 until it is checked.

## Decisions

- **Lone-miss grace, not a per-streak budget.** A walk back from today tracks consecutive missed required days; reaching two ends the streak. Unscheduled days are skipped and paused days stay optional, so neither resets nor advances the miss counter. Chosen with the user over "one miss per streak" and "recent miss only".
- **Today is still open.** An unchecked today is neither a completion nor a miss, so the streak through yesterday keeps showing. Previously a pending today read as streak 0.
- **Encouragement trigger.** `getStreakStatus` returns the count plus `isRecovering`: true when the streak is positive and the most recent required day *before today* has no completion. That covers "missed yesterday, today pending" and "missed yesterday, checked today", and stays quiet on ordinary mornings when only today is pending.
- `getCurrentStreak` stays as a thin wrapper so existing call sites keep compiling.

## Risks

- A habit done every other day keeps an ever-growing streak (at half speed). Accepted per the chosen rule.
