## Context

Tempo already persists theme colors, tab visibility, the default tab, and completed-task retention, and Settings exposes all four controls. Startup currently resolves only those preferences and enters the selected Tasks or Routines destination. Settings contains a placeholder "View onboarding" action, but there is no onboarding route or persisted completion state. Completed-task retention is an advanced, potentially destructive preference that is more appropriate after users understand the app.

The change crosses startup, navigation, preferences, and Compose presentation. It must preserve the app's Screen/Content split, use existing repositories as the source of truth, keep onboarding short, and avoid coupling reminder education to Android permission requests.

## Goals / Non-Goals

**Goals:**

- Gate the normal start destination until a first-run user finishes or skips onboarding.
- Explain the benefits and core concepts in four short pages, followed by a focused Tempo welcome page.
- Apply preference selections through the same repositories used by Settings and the rest of the app.
- Persist completion and support replay from Settings.
- Guarantee at least one visible tab and an enabled default tab.
- Hand first-run users directly to the selected default tab.

**Non-Goals:**

- Creating sample tasks, categories, routines, or reminders on the user's behalf.
- Requesting notification permission or replacing contextual permission education.
- Adding analytics, remote-configured copy, illustrations, or a new dependency.
- Changing the underlying task, category, routine, reminder, or retention behavior.

## Decisions

### Use a dedicated onboarding feature with MVI presentation

Add `features/onboarding/presentation` with a contract, ViewModel, Screen, and pure Content composable. The UI state mirrors the existing preferences needed by onboarding and tracks the current page. This follows the existing presentation architecture and makes the behavior unit- and UI-testable.

Alternative: implement a one-off composable in `MainActivity`. Rejected because it would mix startup, preference orchestration, navigation, and UI state while preventing the established Screen/Content split.

### Use four content pages plus a final welcome page and apply choices immediately

The pages are: (1) the benefits of tasks and categories, (2) the benefits of routines and reminders, (3) light/dark/system mode plus Tempo versus dynamic colors, (4) tab visibility and default tab, and (5) a centered final welcome with the unmodified default Tempo launcher logo and app name. The welcome renders the launcher resource directly so Compose does not replace or augment its background. The appearance and setup pages reuse the same section composables as Settings so behavior and styling cannot drift. Completed-task retention remains exclusively in Settings, keeping the first-run configuration focused and avoiding an advanced destructive control before the user has completed a task. Preference edits are persisted as the user makes them, matching Settings behavior and allowing the global theme to preview appearance changes immediately. The completion action appears on the welcome page so configuration and celebration remain distinct steps; Skip is omitted there because it would duplicate completion behavior.

Alternative: stage all choices and commit only on Finish. Rejected because replay would need a parallel draft model and appearance could not provide an immediate whole-app preview. Skip therefore preserves any explicit edits already made.

### Persist only completion as new onboarding data

Add a small SharedPreferences-backed `OnboardingPreferencesRepository` that exposes completion as a reactive flow, an idempotent `setCompleted()` operation, and a one-time started marker. Existing repositories remain authoritative for all configurable choices. When onboarding starts for the first time and the theme repository has no saved color preference, Tempo colors are selected once. This satisfies the new-install default without overwriting an upgrading user's explicit choice or changing it after recomposition, process recreation, or replay.

No database transaction is required: each preference is an independent SharedPreferences write. Completion is written only after preference events have already been applied; repeating Finish or Skip is safe because setting the same completion boolean is idempotent.

Alternative: store a single onboarding configuration object. Rejected because it would duplicate existing preference state and introduce synchronization failure modes.

### Make onboarding a typed Navigation route

Add `OnboardingRoute(isReplay: Boolean)` to the existing NavHost. `MainViewModel` includes onboarding completion in startup state, and `MainActivity` selects onboarding as the initial route when incomplete. First-run Finish/Skip marks completion and replaces onboarding with the resolved enabled default route. Settings replay navigates to the same route with `isReplay = true`; leaving replay pops back to Settings while completion remains true.

The NavHost start destination is captured only when the successful app composition is first created. Reactive preference changes update the active screen and theme, but they do not rebuild the navigation graph or replace an active onboarding replay.

Alternative: render onboarding outside the NavHost. Rejected because Settings replay and back-stack behavior would require a second routing mechanism.

### Match established expressive motion

Onboarding actions use Tempo's existing press-driven shape animation language while keeping their normal colors stable during presses. The progress indicator follows Didi's segmented pill treatment: one segment per page, completed and current segments use the primary color, future segments use a muted surface color, and segment colors animate when the page changes. It sits near the top of the flow, directly below Skip and above the current page content, while the footer contains only navigation actions.

Completing the welcome page uses a route-scoped fade-through: the centered welcome fades while scaling slightly outward, and the resolved Tasks or Routines destination fades in while scaling to full size. Both halves use Tempo's long motion duration. The transition is scoped to onboarding-to-app navigation, so Settings replay and ordinary tab navigation keep their existing motion.

Alternative: keep the stock linear progress indicator and static button shapes. Rejected because they visually diverge from the interaction feedback used throughout Tempo and make discrete onboarding steps less legible.

### Adapt onboarding to the live window size

Enable Compose MediaQuery integration and derive onboarding layout from the current window width and height. Compact and medium windows keep a centered single-column flow; windows at least 840 dp wide place education or configuration beside the page introduction in two balanced columns. Short landscape windows reduce outer and section spacing while preserving scrollability, safe drawing insets, and 48 dp actions. Phone, foldable, tablet, and desktop previews cover the major form factors without adding a new dependency.

The final footer gives the longer Start using Tempo action twice the horizontal weight of Back, keeping the localized completion label readable on compact phones without changing the proportions of ordinary Back/Next navigation.

Alternative: infer layout from the physical device type or a startup-only configuration value. Rejected because split-screen, fold/unfold, desktop resizing, and rotation can all change the usable window while the app remains active.

### Keep permission education contextual

The reminder page explains that reminders can notify at useful times, but it does not request permission. Existing notification permission education remains the sole permission-sensitive entry point, so onboarding copy cannot get ahead of the user's first reminder action.

## Risks / Trade-offs

- [Users upgrading from an older release are technically "incomplete"] → Treat the first release containing onboarding as their first guided launch; Skip is always visible, and completion is persisted immediately.
- [Immediate preference writes during replay can change settings before Finish] → Controls are explicit and reflect the same immediate-save model already used in Settings.
- [Two ViewModels could drift in tab invariants] → Cover the same invariants with onboarding tests and keep repository constants as the shared persisted representation.
- [Users may look for retention during onboarding replay] → Keep the control in its established Settings section and keep onboarding focused on first-session choices.
- [Localized copy can diverge as behavior evolves] → Keep copy factual, reference existing concepts, and add English and Spanish resources in the same change.

## Migration Plan

1. Add the completion preference with a default of incomplete.
2. Add the onboarding route and include completion in startup state.
3. Ship the flow; existing and new installations see it once and can skip.
4. Rollback is safe: older builds ignore the standalone onboarding SharedPreferences file, while all selected settings use already-supported repositories.

## Open Questions

None.
