## Why

Adding a task today requires opening Tempo, navigating to the task list, and tapping the add-task control. For a fast capture action ("jot this down before I forget"), that's more friction than the action deserves. A home-screen widget lets the user go from "I need to remember this" to "saved" in one tap, without leaving their launcher. GitHub issue #46 ("feat: create quick task widget") requests exactly this.

## What Changes

- Add a new Android home-screen widget ("Quick Add Task") implemented with Jetpack Glance: an icon-only tile with no label text, matching a launcher-icon-style widget rather than a labeled button.
- Tapping the widget launches the app's `MainActivity`, navigates to the Tasks tab, and opens the same task-creation sheet already used by the in-app "+" button — no bespoke widget-only UI. This reuses the existing `PendingNotificationAction`-based mechanism the app already uses to open specific content after a notification-triggered launch (a new `PendingNotificationAction.OpenNewTaskDialog` case).
- Widget visuals (background, icon tint) use Glance's `GlanceTheme`/color providers wired to Tempo's existing static light/dark Material color tokens, so the widget matches the app's light/dark theme. Android 12+ dynamic (wallpaper-based) color is intentionally not used for the widget — see design.md decision 4.
- New Gradle dependency: `androidx.glance:glance-appwidget` and `androidx.glance:glance-material3`.
- New manifest entry: an `AppWidgetProvider` `<receiver>` plus `appwidget-provider` metadata (min size, resize mode, preview image, distinct widget-picker label).
- New "What's New" entry (per AGENTS.md checklist) announcing the widget once it ships.

## Capabilities

### New Capabilities
- `quick-task-widget`: Home-screen Glance widget that opens the app's existing task-creation sheet and reflects Tempo's light/dark theme colors.

### Modified Capabilities
(none — no existing capability's requirements change; the widget reuses the existing task-creation flow and notification-launch-action mechanism as-is)

## Impact

- **Affected code**: new `features/widget/` module tree — Glance `GlanceAppWidget` + `GlanceAppWidgetReceiver`, a `TempoGlanceColorScheme` bridge. `MainActivity.handleIntent()` gains a branch reading a widget-launch intent extra; `PendingNotificationAction` gains an `OpenNewTaskDialog` case; `TasksScreen` gains a branch in its existing pending-action `LaunchedEffect` to dispatch `ShowTaskDialog()`.
- **Manifest**: new `<receiver>` for the widget provider (`android:label` set to the widget's own string, not the app label), new `res/xml/quick_task_widget_info.xml`. No new `<activity>` — the widget launches the existing `MainActivity`.
- **Dependencies**: adds `androidx.glance:glance-appwidget` and `androidx.glance:glance-material3` to `gradle/libs.versions.toml` and `app/build.gradle.kts`.
- **Design system**: no changes to `core/ui/theme` — the widget reads existing color tokens, it does not introduce new ones.
- **What's New**: replaces `WhatsNewRegistry.latest` per AGENTS.md checklist item 5.
