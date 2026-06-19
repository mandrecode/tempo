## Context

`HabitHistoryView` uses adaptive dot fitting inside a weighted slot and computes visible dots from the width available after the streak label. The streak label itself animates text/content transitions, so near a capacity boundary the measured remaining width can fluctuate during recomposition and trigger dot-count flips (for example introducing/removing truncation/ellipsis), which looks like the latest checked dot jumping.

The latest #681 alignment fix removed redundant end padding so the streak pill sits flush to the row end. That behavior must remain intact.

## Goals / Non-Goals

**Goals:**

- Keep dot placement stable when toggling completion in full-capacity/constrained rows.
- Preserve the current visual language (dot color transitions, streak pill style).
- Keep streak pill flush with the row end in bottom-sheet context.
- Validate through previews plus regression test coverage.

**Non-Goals:**

- No changes to streak business logic or completion-history data.
- No redesign of the habit history UI structure beyond layout stabilization.
- No localization/resource expansion unless required by compilation.

## Decisions

1. **Stabilize label width for transition states.**

   Ensure the streak label's measured width does not oscillate across the no-streak/streak transition used during completion toggles. This keeps dot fitting deterministic when the row is at capacity.

2. **Avoid container-level size animation that remeasures the whole row.**

   Remove or narrow animations that cause parent-row width reflow during the transition; keep content-level color/text animations where they do not alter horizontal allocation.

3. **Add explicit preview states for regression.**

   Add before/after toggle previews in the same row composition as the bottom sheet (icon slot + weighted history view), so visual drift is obvious during development.

4. **Guard constrained-width behavior in tests.**

   Add an instrumented test asserting that completion-state transitions do not trigger a truncation/layout flip in a constrained/full-capacity setup.

## Risks / Trade-offs

- Over-constraining label width may reduce horizontal room in some locales or very long streak values; this is acceptable for transition stability and will be checked with previews.
- Removing broad container-size animation can make layout transitions less "elastic," but prevents visible jitter/displacement in the bug path.
