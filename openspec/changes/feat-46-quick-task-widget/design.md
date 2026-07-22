## Context

Tempo has no home-screen widget today. Task creation already has a full, well-tested MVI flow: `TasksScreen`'s "+" button dispatches `TasksContract.UiEvent.ShowTaskDialog()`, which opens a task-creation sheet backed by `CreateTaskUseCase` (`features/tasks/domain/usecase/CreateTaskUseCase.kt`). The app also already has a mechanism for "open specific content after an external launch": `MainActivity.handleIntent()` reads intent extras (from reminder notifications), calls `MainViewModel.setPendingNotificationAction(...)`, and bumps a navigation trigger to switch tabs; the target screen (e.g. `TasksScreen`) then observes `pendingNotificationAction` in a `LaunchedEffect` to open the right dialog/detail and calls back to consume it. The app's color system is fully defined as Material 3 tokens in `core/ui/theme/Color.kt` (`TempoLightColorScheme` / `TempoDarkColorScheme`), consumed everywhere through `MaterialTheme.colorScheme.*` — never hardcoded hex in composables.

Jetpack Glance is the modern, Compose-idiomatic way to build App Widgets and is what the ecosystem has converged on for widget UI (vs. classic `RemoteViews`/`AppWidgetProvider` XML layouts). It ships its own theming primitive, `GlanceTheme`, which needs an explicit `ColorProviders` mapping rather than inheriting `MaterialTheme` directly, since Glance renders through `RemoteViews` and can't share a live Compose composition with the app process.

## Goals / Non-Goals

**Goals:**
- One-tap path from home screen to the app's real task-creation sheet, pre-navigated to the Tasks tab.
- Widget chrome (background, icon) visually matches Tempo's current light/dark theme.
- Reuse the existing task-creation UI and `CreateTaskUseCase` as-is — no parallel task-creation code path, no bespoke widget-only dialog.
- Keep the widget itself simple/static (an icon + launch action) so it needs no live data binding, Hilt entry point, or background refresh logic.

**Non-Goals:**
- No task list, agenda, or completion checkboxes in the widget surface (that's a different, larger widget — out of scope for issue #46).
- No widget configuration screen (size/category defaults) in this iteration.
- No multi-size responsive layouts beyond Android's default minimum widget cell — a single fixed small size is sufficient for a launch-icon-style widget.
- No label text on the widget tile itself — it's an icon-only launcher-style tile, identified in the widget picker by its `android:label`/`android:description`, not on-tile text.

## Decisions

**1. Glance for the widget surface, not classic `RemoteViews`/XML layouts.**
Glance is Compose-based, is the AndroidX-recommended path since 2022, and lets the widget module stay Kotlin/Compose-idiomatic like the rest of the app. Alternative (hand-rolled `RemoteViews` + XML) was rejected — more boilerplate, no real benefit given Glance is already GA and used industry-wide.

**2. The widget is a static, icon-only launcher tile with no bespoke UI of its own.**
The `GlanceAppWidget` content is just an icon with a single `actionStartActivity<MainActivity>()` click action — no label text on the tile, no live data binding, no Hilt `EntryPoint` inside the widget receiver (Glance widgets aren't `@AndroidEntryPoint`-compatible out of the box). All actual task-creation UI is the app's own, already-shipped Compose flow — nothing new is rendered by the widget module itself. An earlier iteration of this design routed the tap to a bespoke transparent trampoline `Activity` with its own minimal quick-add dialog; that has been replaced by decision 3 below, so no new Activity, Contract, ViewModel, or Content composable exists in `features/widget/`.

**3. Tapping the widget launches `MainActivity` and reuses the existing pending-action mechanism to open the real task-creation sheet.**
`QuickAddTaskWidget` passes a boolean `ActionParameters` key (`QuickAddTaskWidget.EXTRA_OPEN_NEW_TASK_DIALOG`) to `actionStartActivity<MainActivity>()`; Glance turns this into a plain `Intent` extra. `MainActivity.handleIntent()` reads it exactly like it already reads `TaskReminderReceiver.EXTRA_TASK_ID` etc.: it calls `MainViewModel.setPendingNotificationAction(PendingNotificationAction.OpenNewTaskDialog)` (a new, no-payload case) and bumps `tasksNavigationTrigger` so `TempoNavHost` switches to `TasksRoute`. `TasksScreen`'s existing `LaunchedEffect(pendingNotificationAction)` — which already handles `OpenTask` by reopening a specific task — gets one more branch: on `OpenNewTaskDialog` it dispatches `TasksContract.UiEvent.ShowTaskDialog()` (the same event the in-app "+" button dispatches) and consumes the action. This means the widget adds zero new UI code, zero new validation logic, and automatically inherits every future change to the task-creation sheet (reminder fields, category picker behavior, error handling) for free.

**4. Glance chrome colors are bridged from Tempo's existing M3 tokens via `GlanceTheme.colors`.**
`androidx.glance:glance-material3` provides `ColorProviders(lightColorScheme, darkColorScheme)`, built directly from the existing `TempoLight*`/`TempoDark*` token vals in `Color.kt`. The widget wraps its content in `GlanceTheme(colors = TempoGlanceColorScheme) { ... }` so background/primary map 1:1 to the app's tokens; day/night is resolved automatically by Glance. Dynamic color (Android 12+) is intentionally *not* threaded into the widget — this keeps the widget deterministic and independent from the app's `dynamicColor` toggle; this can be revisited later if wallpaper-matching for the widget specifically is requested.

**5. `features/widget/` feature module tree, reduced to just the Glance tile.**
- `features/widget/presentation/QuickAddTaskWidget.kt` — `GlanceAppWidget` (icon-only static content + launch action + the `EXTRA_OPEN_NEW_TASK_DIALOG` key).
- `features/widget/presentation/QuickAddTaskWidgetReceiver.kt` — `GlanceAppWidgetReceiver`.
- `features/widget/presentation/TempoGlanceColorScheme.kt` — Glance `ColorProviders` bridge.
- `res/xml/quick_task_widget_info.xml` — `appwidget-provider` metadata (resizeMode="none", fixed minimal size, `android:previewImage`).
No domain or data layer changes are needed — `Task`, `Category`, `CreateTaskUseCase`, and `TaskRepository` are reused unmodified, and no new UI-layer code beyond the widget tile itself and the `MainActivity`/`Navigation.kt`/`TasksScreen.kt` wiring described in decision 3.

## Risks / Trade-offs

- **[Risk]** Some OEM launchers restrict or delay widget-triggered activity launches (background activity start limits on Android 10+). → **Mitigation**: use `actionStartActivity` (Glance's supported, allow-listed launch path for widget taps) rather than a raw `PendingIntent` broadcast-then-start, which is the officially sanctioned mechanism and exempt from background-activity-launch restrictions.
- **[Trade-off]** Tapping the widget briefly shows the full app (Tasks tab + sheet) rather than staying in a minimal overlay — accepted in favor of reusing the real, fully-featured task-creation flow instead of maintaining a second, more limited one.
- **[Risk]** Adding `androidx.glance:glance-appwidget`/`glance-material3` grows APK size and build surface. → **Mitigation**: small, AndroidX-maintained artifacts already common in production apps; no reason to expect them to threaten the detekt/ktlint baseline or build time meaningfully.
- **[Risk]** `PendingNotificationAction.OpenNewTaskDialog` is not persisted across process death (unlike the other cases, which round-trip through `SavedStateHandle`), since it carries no id to reconstruct. → **Mitigation**: the window is a launch-to-first-composition race that's vanishingly unlikely to coincide with process death; if missed, the user simply taps the in-app "+" button once more. Not worth the added `SavedStateHandle` schema complexity for a no-payload, idempotent action.

## Migration Plan

No data migration. Purely additive: new Gradle dependencies, a new manifest `<receiver>`, new resource files, a new feature module, and small additions to already-existing files (`MainActivity`, `Navigation.kt`, `TasksScreen.kt`). Rollback is a plain revert with no persisted-state cleanup required, since the widget stores no state of its own (each home-screen placement is just a `GlanceAppWidget` instance) and the `PendingNotificationAction` addition is additive to an existing sealed interface.

## Open Questions

None outstanding — the widget now fully delegates to the existing task-creation sheet, so its future evolution (reminder fields, category picker changes, etc.) tracks that sheet automatically.
