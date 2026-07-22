## Why

Adding a task today requires opening Tempo, navigating to the task list, and tapping the add-task control. For a fast capture action ("jot this down before I forget"), that's more friction than the action deserves. A home-screen widget lets the user go from "I need to remember this" to "saved" in one tap, without leaving their launcher. GitHub issue #46 ("feat: create quick task widget") requests exactly this.

## What Changes

- Add a new Android home-screen widget ("Quick Add Task") implemented with Jetpack Glance.
- Tapping the widget opens a minimal quick-add surface (title input + optional category picker) rendered in a transparent trampoline activity, not the full app.
- Saving calls the existing `CreateTaskUseCase` directly; on success the surface closes back to the home screen. No task list, checklist, or agenda view is rendered in the widget — quick-add only.
- Widget visuals (background, text, icon tint) use Glance's `GlanceTheme`/color providers wired to Tempo's existing Material color tokens so the widget matches the app's light/dark theme and follows Android 12+ dynamic color when available.
- New Gradle dependency: `androidx.glance:glance-appwidget`.
- New manifest entries: an `AppWidgetProvider` `<receiver>` plus `appwidget-provider` metadata (min size, resize mode, preview image).
- New "What's New" entry (per AGENTS.md checklist) announcing the widget once it ships.

## Capabilities

### New Capabilities
- `quick-task-widget`: Home-screen Glance widget that opens a quick-add surface, creates a task via the existing task creation use case, and reflects Tempo's light/dark theme colors.

### Modified Capabilities
(none — no existing capability's requirements change)

## Impact

- **Affected code**: new `features/widget/` (or similar) module tree — Glance `GlanceAppWidget` + `GlanceAppWidgetReceiver`, a trampoline `Activity` hosting the quick-add Compose/Glance UI, DI wiring for `CreateTaskUseCase` access from a non-Compose entry point.
- **Manifest**: new `<receiver>` for the widget provider, new `<activity>` for the quick-add trampoline (`excludeFromRecents`, transparent theme), new `res/xml/quick_task_widget_info.xml`.
- **Dependencies**: adds `androidx.glance:glance-appwidget` (and `androidx.glance:glance-material3` if used for color scheme bridging) to `gradle/libs.versions.toml` and `app/build.gradle.kts`.
- **Design system**: no changes to `core/ui/theme` — the widget reads existing color tokens, it does not introduce new ones.
- **What's New**: replaces `WhatsNewRegistry.latest` per AGENTS.md checklist item 5.
