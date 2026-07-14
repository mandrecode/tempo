## Context

`TaskItem` renders its completion control, main content, and trailing actions in one Compose `Row`. That row currently centers children vertically, so when a long description expands, the fixed-size controls move toward the expanded card's vertical center. The collapsed description is already constrained to one line, but the behavior is not explicitly protected by focused UI tests.

## Goals / Non-Goals

**Goals:**

- Preserve a compact one-line collapsed description with ellipsis when it overflows.
- Anchor the completion control, title/content block, and trailing action block to the top of the task header while an overflowing description expands.
- Preserve the existing expand/collapse interaction, animation, metadata, and subtask behavior.
- Make the alignment behavior measurable in Compose UI tests and visible in debug previews.

**Non-Goals:**

- Redesign task-card colors, shapes, typography, metadata, or action icons.
- Change task, subtask, or description data models.
- Change habit or habit-chain cards.

## Decisions

1. **Top-align the existing task header row.** Use the row's vertical alignment to anchor all three header regions rather than introducing offsets or overlay positioning. The existing 48 dp minimum title container keeps short cards visually balanced while top alignment remains stable as the content grows. Absolute offsets were rejected because they would be brittle with font scaling and metadata changes.
2. **Retain the current local description expansion state.** Description expansion is presentation-only and does not need MVI or persistence changes. The current ellipsized single-line collapsed state and unlimited expanded state remain the source of behavior.
3. **Expose stable semantic test tags for header regions.** Focused Compose tests will compare the top bounds of the completion control, content block, and trailing actions before and after expanding a long description. Position assertions based on text or icon internals were rejected because those internals can change independently of the layout contract.
4. **Extend the existing long-description preview.** Preview coverage will show the relevant expanded state directly, without adding runtime-only controls or dependencies.

## Risks / Trade-offs

- [Top alignment could make short content appear offset] → Keep the existing 48 dp minimum content height and internal centering so a title-only card retains its current visual balance.
- [Pixel-exact UI assertions can be fragile] → Compare semantic-region top bounds with a small tolerance instead of hard-coding absolute coordinates.
- [Description overflow is width-dependent] → Constrain the test card width and use deterministic long text so the expand action is guaranteed to appear.
