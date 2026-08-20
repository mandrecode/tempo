## Context

`rememberViewportHold` (`core/ui/util/ViewportHold.kt`) is the one mechanism both the Tasks list and
the plan sheet use to keep the view still while a row moves between sections. It works in two
halves:

1. The call site calls `holdViewport()` from a click handler, which records
   `firstVisibleItemIndex to firstVisibleItemScrollOffset` into snapshot state.
2. A `LaunchedEffect` keyed on the section counts fires once those counts change, and calls
   `state.scrollToItem(index, offset)` to put the view back.

Half 2 is the problem. Everything about it happens after the fact:

- A lazy list anchors on the *key* of its first visible row. When the row acted on is that row, the
  list finds the key at its new index during measure and follows it — the chase the hold exists to
  undo. That measure has already happened by the time anything can be restored.
- `scrollToItem` is a suspend function on a scroll session. Its body runs when the effect's
  coroutine is dispatched, which on device is `AndroidUiDispatcher` — the same frame if the
  continuation resumes inside the frame's trampoline, a later one if it does not. Nothing about the
  code decides which; a mutex already held by a settling fling, or an extra recomposition between
  the click and the state landing, is enough to push it over.
- It ends in `forceRemeasure()`, a second measure pass in the same frame at a different anchor. The
  row's `animateItem` placement animation is set up from the first pass and re-targeted by the
  second.

The visible result, from the video on #372: sometimes the card slides and the view holds; sometimes
the view jumps by a card and stops; sometimes the card is gone in a single frame with no slide.

Compose has an API for exactly this shape of problem. `requestScrollToItem(index, scrollOffset)`,
on both `LazyListState` and `LazyGridState`, is non-suspend, applies during the *next* measure
rather than forcing one, and — the part that matters most here — forgets the last known first-item
key, which is what disables the chase.

## Goals / Non-Goals

**Goals:**

- The held position is the position the next measure lays out at, so no frame is ever drawn at the
  chased position.
- One measure pass per change, so `animateItem` animates from one consistent layout to the next.
- The behaviour no longer depends on coroutine dispatch timing.
- The call sites and the public shape of `rememberViewportHold` are unchanged.

**Non-Goals:**

- Changing the animation itself — spec, duration or spring. The slide is Compose's; this change is
  about not disturbing it.
- Animating a row to a position off screen. Compose does not animate items into or out of the
  viewport, and nothing here changes that.
- Any behaviour change in Routines or Focus, which have no section to cross.

## Decisions

### `requestScrollToItem` in the commit phase, not `scrollToItem` in a coroutine

The restore moves from a `LaunchedEffect` to a `SideEffect`, and from `scrollToItem` to
`requestScrollToItem`.

`SideEffect` runs when the composition that produced the new sections is applied — after
composition, before that frame's measure and layout. A position requested there is the position the
LazyGrid/LazyColumn measures itself at, on the same frame the new content arrives. There is no
window in which the list is laid out anywhere else, so there is nothing to correct and nothing to
correct it with.

Alternatives considered:

- **Keep `LaunchedEffect`, swap in `requestScrollToItem`.** Removes the forced second measure and
  the scroll session, but not the dispatch: the effect still runs after the frame that chased, so a
  chased frame can still be drawn. Half a fix.
- **Call `requestScrollToItem` in `holdViewport()` itself,** at click time, instead of recording a
  position at all. Tempting — it is one line, and the request would be pending when the data
  arrives. But the request is consumed by the *next* measure, whatever that measure is for, and the
  measure after it re-learns the anchor key. Any remeasure between the click and the data landing
  (a ripple, a chip's selected state, the sheet resizing) silently spends it, and the chase comes
  back for the measure that mattered.
- **Anchor the list on a key that never moves,** as the Tasks list did before #371 with a
  zero-height first item. Only works while the list is scrolled to the top; anywhere else the first
  visible row is a real row again. This is the approach #371 deliberately removed.

### The recorded position stops being snapshot state

`restoreTo` was `mutableStateOf`, which was never needed: it is written from a click handler and
read from the restore, and nothing should recompose because of it. Keeping it as snapshot state and
reading it from a `SideEffect` also invites a write-during-composition tangle for no benefit. It
becomes a plain `var` on a remembered holder, alongside the previous section keys — which have to be
compared by hand now, because a `SideEffect` runs on every recomposition and only the ones where a
row actually crossed should restore anything.

### The section keys stay the contract

Nothing changes about what a caller passes or when it calls. `sectionKey` plus `moreSectionKeys`
still has to change by value when, and only when, a row crosses between sections; `holdViewport()`
is still called immediately before the event. Only the machinery behind them changes.

## Risks / Trade-offs

- **A `SideEffect` runs on every recomposition, where `LaunchedEffect(keys)` ran on key changes
  only.** → The body is a comparison of a small array against the previous one and an early return.
  The keys are counts; the comparison is two integers in both call sites today.

- **Comparing keys by hand can go wrong where `LaunchedEffect` could not.** → `contentEquals`, and
  the same "changes by value or not at all" contract the KDoc already states and both call sites
  already honour, since they pass sizes rather than the collections themselves.

- **A stale record can outlive the action that made it.** A `holdViewport()` before an action that
  turns out to move nothing leaves a position recorded, which the next section change — possibly
  from somewhere else entirely, a sync or an undo — would restore to. This is pre-existing and
  unchanged by this design; it is called out here because the restore is now exact enough that a
  wrong position would be held perfectly rather than approximately. Left alone rather than guessed
  at: the plan sheet's re-date of an already-planned task is the only known way to reach it, and
  bounding the record by time or by frames would trade a rare wrong hold for a common missed one.

- **`requestScrollToItem` cancels a scroll in progress.** → It is what `scrollToItem` did too, via
  the scroll mutex. A fling settling when a row is checked off was already interrupted.

- **No automated test can tell the two implementations apart.** Both compose test rules run effects
  on a test dispatcher that resolves them before the frame's layout — verified by writing the
  frame-stepping test and watching it pass against the broken implementation, on the v1 rule and on
  the v2 rule with `StandardTestDispatcher`. → The existing instrumented tests keep the contract
  they always did ("the view is held", "without the hold it is not"), and the frame-level guarantee
  is verified on a device, by recording the interaction and comparing frames.
