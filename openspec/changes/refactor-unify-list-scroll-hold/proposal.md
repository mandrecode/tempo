# Unify how lists hold their place when a row settles

## Why

Two screens keep the view still while a row moves between sections, and they do it two different
ways. Tasks puts a zero-height `scroll_anchor` item at the head of its list so the key the list
anchors on is one that never moves; the plan sheet, added in #347, records the scroll index before
the change and restores it after. Both were arrived at separately for the same problem, and the
next list that needs it has two precedents to choose between and no reason to prefer either.

The anchor is also the weaker of the two. It only works while the list is scrolled to the top,
because anywhere else the first visible item is a real row again; and being a real item in a list
with `spacedBy(8.dp)`, it costs 8dp of space above the first card that nobody chose.

## What Changes

- Lift the plan sheet's `rememberViewportHold` into `core/ui` as the shared mechanism, with
  overloads for `LazyListState` and `LazyGridState`.
- Adopt it in the Tasks list, so checking a task off holds the view from anywhere in the list
  rather than only from the top.
- Remove the `scroll_anchor` item. The first card in the Tasks list moves 8dp closer to the top of
  the panel — the only visible change, and the 8dp was a side effect of the item rather than a
  spacing decision.
- Reword the comment on `CompletedTasksSeparator.isFirstVisibleItem`, which cites the anchor as the
  reason the flag is not index-based. The flag is computed from `!hasActiveTasks` and never was
  index-based, so only the explanation changes.

### Non-goals

- No change to what either list *does* when a row settles — only to how the view is held.
- No change to the plan sheet's behaviour at all; it already uses the mechanism being shared.
- Not extended to any other list. Routines and Focus have no section a row crosses on completion,
  so there is nothing there to hold.

## Capabilities

- `task-list-scrolling` — new spec, covering what a list does with the view when one of its rows
  moves between sections.
