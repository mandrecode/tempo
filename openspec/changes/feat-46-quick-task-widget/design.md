## Context

Tempo has no home-screen widget today. Task creation is a MVI Compose flow reached through `CreateTaskUseCase` (`features/tasks/domain/usecase/CreateTaskUseCase.kt`), which validates title/description, computes sort order, persists via `TaskRepository`, and schedules reminders via `TaskReminderScheduler`. The app's color system is fully defined as Material 3 tokens in `core/ui/theme/Color.kt` / `Theme.kt` (`TempoLightColorScheme` / `TempoDarkColorScheme`, plus Android 12+ dynamic color), consumed everywhere through `MaterialTheme.colorScheme.*` — never hardcoded hex in composables.

Jetpack Glance is the modern, Compose-idiomatic way to build App Widgets and is what the ecosystem has converged on for widget UI (vs. classic `RemoteViews`/`AppWidgetProvider` XML layouts). It ships its own theming primitive, `GlanceTheme`, which needs an explicit `ColorProviders` mapping rather than inheriting `MaterialTheme` directly, since Glance renders through `RemoteViews` and can't share a live Compose composition with the app process.

## Goals / Non-Goals

**Goals:**
- One-tap path from home screen to "task saved" for a short title, optionally tagged with a category.
- Widget chrome (background, icon, label) visually matches Tempo's current light/dark theme, including Android 12+ dynamic color when enabled system-wide.
- Reuse `CreateTaskUseCase` as-is — no parallel task-creation code path.
- Keep the widget itself simple/static (an icon + label + launch action) so it needs no live data binding, Hilt entry point, or background refresh logic.

**Non-Goals:**
- No task list, agenda, or completion checkboxes in the widget surface (that's a different, larger widget — out of scope for issue #46).
- No widget configuration screen (size/category defaults) in this iteration.
- No multi-size responsive layouts beyond Android's default minimum widget cell — a single fixed small size is sufficient for a launch-icon-style widget.
- No offline queuing/retry UI for save failures beyond a simple inline error state.

## Decisions

**1. Glance for the widget surface, not classic `RemoteViews`/XML layouts.**
Glance is Compose-based, is the AndroidX-recommended path since 2022, and lets the widget module stay Kotlin/Compose-idiomatic like the rest of the app. Alternative (hand-rolled `RemoteViews` + XML) was rejected — more boilerplate, no real benefit given Glance is already GA and used industry-wide.

**2. The widget itself is a static launcher tile, not a live Glance form.**
Because the feature is quick-add only (no list, no per-item actions), the `GlanceAppWidget` content is just an icon + "Quick add task" label with a single `actionStartActivity` action. This avoids needing a Hilt `EntryPoint` inside the widget receiver (Glance widgets aren't `@AndroidEntryPoint`-compatible out of the box) and avoids any widget-side data loading/refresh concerns. All actual work — text input, category pick, save — happens in a normal `@AndroidEntryPoint` trampoline `Activity`, which can inject `CreateTaskUseCase` and `CategoryRepository` exactly like any other Hilt Android entry point.

**3. Trampoline `Activity` with a transparent/dialog theme hosts the real Compose UI.**
Tapping the widget launches `QuickAddTaskActivity` (`android:theme` = transparent, `excludeFromRecents="true"`, `launchMode="singleTask"`), which shows a small Compose bottom-sheet-style surface (title `TextField` + category chips + Save/Cancel) wrapped in the existing `TempoTheme`. On save it calls `CreateTaskUseCase`, shows a short confirmation (or inline validation error, reusing `CreateTaskUseCase.Result.ValidationError`), and finishes. This reuses 100% of the app's existing Compose theming and the real use case — no new validation or persistence logic.

**4. Glance chrome colors are bridged from Tempo's existing M3 tokens via `GlanceTheme.colors`.**
`androidx.glance:glance-material3` provides `ColorProviders(lightColorScheme, darkColorScheme)`, built directly from the existing `TempoLightColorScheme` / `TempoDarkColorScheme` objects in `Theme.kt` (exposing them, or the raw `TempoLight*`/`TempoDark*` token vals, for reuse rather than redefining colors). The widget wraps its content in `GlanceTheme(colors = TempoGlanceColorScheme) { ... }` so surface/onSurface/primary map 1:1 to the app's tokens; day/night is resolved automatically by Glance the same way Compose resolves `isSystemInDarkTheme()`. Dynamic color (Android 12+) is intentionally *not* threaded into the widget in this iteration — `GlanceTheme.colors` defaults to `GlanceTheme.colors = ColorProviders(dayNightColorScheme)` built from static brand tokens, keeping the widget deterministic and independent from the app's `dynamicColor` toggle; this can be revisited later if wallpaper-matching for the widget specifically is requested.

**5. New `features/widget/` feature module tree, following existing feature layout.**
- `features/widget/presentation/QuickAddTaskWidget.kt` — `GlanceAppWidget` (static content + launch action).
- `features/widget/presentation/QuickAddTaskWidgetReceiver.kt` — `GlanceAppWidgetReceiver`.
- `features/widget/presentation/QuickAddTaskActivity.kt` — trampoline Activity + Compose content, Contract (`UiState`/`UiEvent`/`UiEffect`) + ViewModel per UI_UX.md conventions, injecting `CreateTaskUseCase` and category listing.
- `res/xml/quick_task_widget_info.xml` — `appwidget-provider` metadata (resizeMode="none", fixed minimal size, `android:previewImage`).
No domain or data layer changes are needed — `Task`, `Category`, `CreateTaskUseCase`, and `TaskRepository` are reused unmodified.

## Risks / Trade-offs

- **[Risk]** Glance is a newer, smaller-surface-area library than core Compose; some `MaterialTheme` semantics (e.g. shape tokens, elevation) don't map 1:1 into `GlanceTheme`. → **Mitigation**: keep the widget's own Glance UI intentionally minimal (icon + label only), so only a handful of color roles (primary, onPrimary, surface, onSurface) need mapping; the visually rich UI (text input, category chips) lives in the trampoline Activity under full `TempoTheme`/Compose, sidestepping Glance's styling limits entirely.
- **[Risk]** Some OEM launchers restrict or delay widget-triggered activity launches (background activity start limits on Android 10+). → **Mitigation**: use `actionStartActivity` (Glance's supported, allow-listed launch path for widget taps) rather than a raw `PendingIntent` broadcast-then-start, which is the officially sanctioned mechanism and exempt from background-activity-launch restrictions.
- **[Trade-off]** No widget-side list/data means the user always leaves the widget context (briefly) to type a title — accepted per scope decision (issue #46 = quick-add only, not an agenda widget).
- **[Risk]** Adding `androidx.glance:glance-appwidget` grows APK size and build surface. → **Mitigation**: it's a small, AndroidX-maintained artifact already common in production apps; no reason to expect it to threaten the detekt/ktlint baseline or build time meaningfully.

## Migration Plan

No data migration. Purely additive: new Gradle dependency, new manifest `<receiver>`/`<activity>`, new resource files, new feature module. Rollback is a plain revert (remove the module, manifest entries, and dependency) with no persisted-state cleanup required, since the widget stores no state of its own (each Home-screen placement is just a `GlanceAppWidget` instance).

## Open Questions

- Should the quick-add surface expose reminder date/time, or stay title+category only for v1? (Design assumes title+category only, matching "quick" framing; can be extended later.)
