## Why

Fixes [#372](https://github.com/mandrecode/tempo/issues/372).

Checking a task off, or giving it a day, moves its card to another section, and the card is meant to
slide there while the view stays put. Sometimes it does. Sometimes the card is simply gone from one
frame and drawn in its new section the next, with the whole list lurching under it — the video on
the issue shows both, a card away in a single frame with no slide at all, and a view that shifts by
a whole card and stops dead.

The hold that keeps the view still is the cause. It records the scroll position before the change
and puts it back afterwards from a coroutine — which runs *after* the list has already measured
itself against the new sections, and by then the list has moved: a lazy list anchors on the key of
its first visible row, so it follows the row that just left to wherever it went. The restore then
snaps the view back and forces a second measure in the same frame. Whether the user sees the chase
before the snap, and whether the row's `animateItem` slide survives being re-measured against a
different anchor mid-flight, comes down to which side of that frame's layout pass the coroutine
happens to resume on. Hence "sometimes".

## What Changes

- Hold the view by *requesting* the position for the next measure rather than scrolling to it after
  one. `LazyListState`/`LazyGridState.requestScrollToItem` also forgets the anchor key, which is
  what stops the list chasing the row in the first place — so there is no chase to undo, no second
  measure pass, and one clean layout for `animateItem` to animate.
- Apply the request in the commit phase of the composition that brings the new sections in, so it
  lands before that frame's measure instead of a coroutine dispatch later. The recorded position
  stops being snapshot state, which it never needed to be: nothing reads it during composition and
  nothing should recompose because of it.
- No change to either screen's call sites — both keep calling `holdViewport()` immediately before
  the event that can move a row.
- Give the Tasks list 16dp above its first card instead of 8dp — the same as its own sides and the
  same as Routines, which is the same block with the same 28dp corners. The other half of what #371
  left behind: removing the `scroll_anchor` item took the arrangement's 8dp with it, and the card
  has been sitting inside the corner curve ever since, which reads as shoved against the seam. Not
  a spec-level behaviour, so it carries no requirement of its own; recorded here because it is a
  visible change riding along with this one.

### Non-goals

- No change to which section a row lands in, to sorting, or to when a row moves at all.
- No change to the `animateItem` spec — the slide is the one Compose already provides; this is
  about not disturbing it.
- Not extended to other lists. Routines and Focus have no section a row crosses on completion.
- Not a fix for a row moving to a position outside the viewport, which Compose does not animate and
  this change does not attempt to.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `task-list-scrolling`: adds a requirement that the hold is invisible — the view never passes
  through the position it would otherwise have chased to, and the row that moved slides to its new
  place rather than jumping. The capability's spec currently lives in the not-yet-archived
  `refactor-unify-list-scroll-hold` change, so this delta is additive to it.

## Impact

- `app/src/main/java/com/mandrecode/tempo/core/ui/util/ViewportHold.kt` — the mechanism, and the
  only production file that changes behaviour.
- `app/src/androidTest/java/com/mandrecode/tempo/core/ui/util/ViewportHoldTest.kt` and
  `.../features/tasks/presentation/TasksViewportHoldTest.kt` — existing coverage that must keep
  passing unchanged, since the contract they pin is exactly what this preserves.
- `app/src/main/java/com/mandrecode/tempo/features/tasks/presentation/TasksContent.kt` — the list's
  top content padding only. `PlanTasksSheet` is untouched, and neither screen's hold wiring moves.
- No screenshot references cover the Tasks list, so none need regenerating; `Routines` and `Focus`
  are unchanged.
- No new dependencies. `requestScrollToItem` is stable API in the Compose Foundation already on the
  BOM in use.
