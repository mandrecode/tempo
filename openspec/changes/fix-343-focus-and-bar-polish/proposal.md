## Why

Three defects reported against the Focus tab shipped in 1.11.0 ([#343](https://github.com/mandrecode/tempo/issues/343), [#344](https://github.com/mandrecode/tempo/issues/344), [#345](https://github.com/mandrecode/tempo/issues/345)). Each one makes Focus feel like a lesser copy of the tabs it draws from: a chain card that answers a tap with nothing, a floating bar that lands its tab transition and then twitches, and a footer that hands you off to Tasks without looking or behaving like anything else in the app that does.

## What Changes

- **#343 — Habit chains open from Focus.** Tapping a chain card in the Focus agenda opens the chain editor, the way tapping a task or a habit already opens theirs. Focus currently wires `onEdit = { }` on `HabitChainCard`, so the chain is the one agenda row whose body is dead to a tap.
- **#344 — The floating bar stops twitching at the end of a tab transition.** The bar's controls are laid out with `Arrangement.spacedBy`, which keeps allocating a gap for an `AnimatedVisibility` child that has already shrunk to zero width, then drops that gap in a single frame when the node is disposed. Measured from the reported video: the centred group settles, holds ~300 ms, then jumps ~10 px. The spacing moves inside the animated children so it collapses with them and the group re-centres continuously.
- **#343 (follow-ups) — The agenda moves like the lists it draws from.** Expanding a card relocated the rows below it in a single frame instead of sliding them, because `AgendaRow` never passed `Modifier.animateItem()` — and could not have, since `HabitCard` and `HabitChainCard` both declared a `modifier` parameter and dropped it. Separately, a chain's habits were listed in the habits list's order rather than the chain's own, so Focus showed the same steps in a different sequence from Routines.
- **#345 — The "N tasks without a date" footer follows Tempo's style.** It becomes a rounded, bounded-ripple button carrying the `ic_open_in_new` icon, matching the "Open in Tasks" affordance the session screen already uses for the same kind of hand-off. Today it is a bare centred `Text` with a full-width rectangular ripple and no sign that it leaves the screen.

Non-goals:

- No change to what the Focus agenda contains, orders, or counts.
- No change to what the chevron does, or to chain completion. The chain card itself gains no delete affordance; the editor's own delete button works from Focus exactly as the habit editor's already does.
- No change to the rail/landscape floating bar layout or to single-tab mode; #344 is scoped to the portrait bar's centred group.
- No new navigation destination for the undated list — it keeps handing off to the Tasks tab.

## Capabilities

### New Capabilities

- `focus-agenda-interaction`: what each row of the Focus agenda does when tapped, and how Focus hands work off to the tab that owns it.
- `floating-bar-motion`: how the persistent floating bar's controls join, leave, and re-centre as the active tab changes.

### Modified Capabilities

<!-- None: no existing spec in openspec/specs/ covers Focus or the floating bar. -->

## Impact

- `features/focus/presentation/FocusContent.kt` — chain card tap target, undated footer, item placement animation.
- `features/focus/domain/usecase/GetFocusAgendaUseCase.kt` — a chain's habits follow the chain's order.
- `features/routines/presentation/components/cards/HabitCards.kt`, `HabitChainCard.kt` — the `modifier` parameter reaches the card root.
- `features/focus/presentation/FocusContract.kt`, `FocusViewModel.kt` — an `EditChain` event and the state that drives the editor.
- `features/focus/presentation/FocusEditors.kt`, `FocusScreen.kt` — a `FocusChainEditor` alongside the task and habit editors, driven by `RoutinesViewModel` exactly as `FocusHabitEditor` is.
- `core/ui/navigation/PortraitFloatingBarLayout.kt`, `FloatingBarTaskActions.kt` — spacing ownership moves into the animated children.
- `core/ui/components/` — the session screen's "Open in Tasks" button is promoted to a shared composable so Focus and the session screen cannot drift apart.
- No domain, data, Room, or scheduling changes; no new strings beyond reusing `focus_session_open_in_tasks` if the footer keeps its own plural label.
