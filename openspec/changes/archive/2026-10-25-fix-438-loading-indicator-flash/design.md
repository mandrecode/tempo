# Design

## Approach

The fix sits in the shared `TempoLoadingIndicator`, so all four loading surfaces get it at once and no ViewModel timing changes.

- A `revealed` flag starts `false` (or `true` when `revealDelayMillis <= 0`). A `LaunchedEffect` waits `revealDelayMillis` and then sets it.
- The indicator is always measured and laid out. Only its alpha animates, from 0 to 1 over 200 ms. A caller that wraps its height around it, like the plan sheet, therefore does not grow when it appears. An `AnimatedVisibility` would not reserve the 96 dp while hidden. Until the indicator is revealed it carries no content description, so accessibility services do not announce it either.
- When loading finishes, callers drop the composable. That cancels the effect, so a quick load never shows the indicator.

## Why 400 ms

Room-backed first loads on a Pixel 7 finish in roughly 50–250 ms. 400 ms covers them with margin, and it is still short enough that a slow load (cold database, migration) shows progress quickly.

## Alternatives considered

- **Delaying `isLoading` in each ViewModel.** This repeats the same timing logic in four places and mixes a presentation concern into state.
- **Starting with `isLoading = false`.** This would flash the empty state instead, which is worse.

## The whole first screen

Hiding the indicator did not remove the flash; it moved it. The first frames after the splash were the screen's background, title and navigation bar, followed by the content.

- **Cold start:** `MainActivity`'s splash condition now also holds while `MainUiState` is `Loading` and while `StartupContentGate.isHolding`. Tasks, Routines and Focus call `HoldSplashWhileFirstLoad(uiState.isLoading)` from their screens, which reports in a `SideEffect`, so the report is in place before the splash's next pre-draw check. The gate starts closed and opens for good on the first report of content. Onboarding reports content at once, since it loads nothing. The gate is reached through `LocalActivity` (`StartupContentGateHost`) rather than a new `CompositionLocal`, which the project's Compose rules forbid. The existing 1.5 s cap bounds the wait.
- **Later first visits:** the three contents switch on `FirstLoadCrossfade(isLoading)`, which fades the content in over 250 ms. Content that is ready on first composition (previews, screenshot tests, a warm tab) shows immediately; only a loading-to-loaded change fades.
- **What's new:** the sheet is a separate window, and opening it while the main window's drawing was still held ended the splash over an undrawn screen (a black frame). It now waits for `hasSplashHandedOver`, which the splash condition sets the first time it lets go.

Verified with screenshot bursts during cold starts on the Pixel 10 AVD. Before: splash, blank, bare Focus chrome, content. After: splash, then the complete Focus screen, and with a pending what's-new entry the sheet opens over it with no black frame.

Two follow-ups from review (Copilot):
- **No fade under the splash.** The first screen reports to the gate after the composition that shows its content, so `FirstLoadCrossfade` can still see the gate holding there (`isSplashCoveringFirstLoad`). For that load it switches with `snap()` rather than fading, so the splash never hands over to content that is still transparent. Later first visits still fade.
- **No indicator in the outgoing state.** While the content fades in, the outgoing loading slot stays composed but empty once `isLoading` is false. Otherwise a load ending just before the 400 ms reveal could reveal the indicator mid-fade over the loaded content. Covered by `aLoadEndingJustBeforeTheRevealDelay_neverShowsTheIndicator`.

The composable helpers (`HoldSplashWhileFirstLoad`, `isSplashCoveringFirstLoad`, `StartupContentGateHost`) live in `core/ui/components/StartupContentGateHost.kt`. The plain `StartupContentGate` class stays in `core/ui` and is unit-tested.
- **Gate released with the splash.** When the 1.5 s cap runs out before the first content arrives, the splash condition also releases the gate. The load is then visible, so its content fades in over the loading state rather than snapping (Copilot).
- **Cap raised to 2.5 s.** Measured cold starts on the Pixel 10 AVD reach first content in 1.3–1.7 s (`am start -W`). The old 1.5 s cap sometimes lifted the splash onto the empty screen just before the content arrived. The cap is only a safety limit; the splash still lets go as soon as content is ready.
- **Gate starts closed.** The splash condition reads `MainUiState` from its `StateFlow`, which flips to Success a frame before Compose has composed the first screen. A gate that started open let the splash go in that gap, onto the bare chrome. This showed in about one cold start in five on the AVD. The gate now starts closed. Tabs open it when their data arrives, and onboarding opens it at once. The 2.5 s cap still bounds the wait.
- **No transition at all under the splash.** `Crossfade`, even with `snap()`, only switches on the next animation frame. In the frame where the content arrived, it was still at alpha 0 while the screen had already reported to the gate, and the splash lifted onto it. That was the remaining bare-chrome frame, in roughly 2 of 8 cold starts. `FirstLoadCrossfade` now composes the content directly. Its alpha is 1 in that same frame when the content arrives under the splash, and otherwise animates from 0 on an `Animatable`. The loading state is removed as soon as the load ends. Verified with 10 of 10 clean cold starts (splash, then the finished Focus screen) and with onboarding on a fresh install releasing the splash in about 1.3–1.7 s.

## Every tab warm before it is opened

The splash covers the first tab only. Opening another tab for the first time still showed its bare background, then faded the content in. Each tab's view model was created on the first visit, and its first database queries took about 100–300 ms after that.

- The three tab screens now take their view model from the activity (`tabViewModel()`) rather than from their navigation entry. A tab's entry already lives as long as the activity (the tabs keep their back stacks), so this changes when the view model is created, not how long it lives. The Focus session overlay and the Focus-hosted task editor keep their own entry-scoped view models, as before.
- `MainActivity.warmTabsWhenDatabaseIsReady()` creates all three view models as soon as the database warm-up completes, on the main thread like any view model but never before the database exists (creating one earlier would block on the warm-up). The one on screen is created by the warm-up or by its screen, whichever comes first.
- A tab opened afterwards composes with its data already loaded, so `FirstLoadCrossfade` shows the content in its first frame with no fade. The fade remains for a tab opened before its data arrives.

Verified on the Pixel 10 AVD with screenshot bursts of first visits to Routines and Tasks right after a cold start. Without the warm-up, 4 of 8 switches showed the bare screen; with it, 0 of 8. `MainActivityTabWarmupTest` looks up the never-opened tabs' view models once, after Focus has loaded, and checks they are already loaded. It fails without the warm-up.

Follow-ups from review (Copilot):
- **Dedicated keys.** The tab view models are held under `tabViewModelKey(...)`, not the default key. The Focus session overlay and the task editor it opens are composed outside the navigation entries and resolve their own `FocusViewModel` and `TasksViewModel` against the activity under the default key. With shared keys, the overlay would have driven the Focus tab's editor and shared the Tasks tab's form. `MainActivityTabWarmupTest.tabViewModels_areSeparateFromTheOverlaysOwn` covers this.
- **Catching up when shown.** Work that used to happen at view-model creation, which coincided with the first visit, now happens when the tab is shown. That means on entering it and on every return to the app, through an `ON_RESUME` observer, which also fires on composition. Focus re-checks the day and recounts the day's activity (`onTabShown`). Routines moves a timeline still on the old "today" to the new day but keeps a day the user picked (`onTabShownOn`). Unit-tested in `FocusViewModelTest` and `RoutinesViewModelTest`.
- **Failure-tolerant warm-up.** `DatabaseWarmupSignal.isReady` only means the warm-up attempt ended. If the database could not be opened, the view-model creation fails the same way, so the warm-up catches non-cancellation exceptions and gives up quietly. The screen that needs the database reports the failure through the usual path.
