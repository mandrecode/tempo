## 1. Dependencies & Manifest

- [x] 1.1 Add `androidx.glance` (`glance`, `glance-appwidget`, `glance-material3`) versions to `gradle/libs.versions.toml` and wire the libraries into `app/build.gradle.kts`.
- [x] 1.2 Add `res/xml/quick_task_widget_info.xml` (`appwidget-provider`: minimal fixed size, `resizeMode="none"`, `android:previewImage`, `android:widgetCategory="home_screen"`).
- [x] 1.3 Add `<receiver>` for the Glance widget receiver to `AndroidManifest.xml`, with `android:label` set to the widget's own string so it's identifiable in the widget picker (not the app's label).
- [x] 1.4 Add a launcher/widget preview drawable/icon asset if one doesn't already exist that fits the widget picker tile. (Reused `@mipmap/ic_launcher` as the widget picker preview.)

## 2. Glance Widget Chrome

- [x] 2.1 Create `features/widget/presentation/TempoGlanceColorScheme.kt` bridging the raw `TempoLight*`/`TempoDark*` tokens from `core/ui/theme/Color.kt` into a Glance `ColorProviders` (day/night), per design.md decision 4.
- [x] 2.2 Create `features/widget/presentation/QuickAddTaskWidget.kt` (`GlanceAppWidget`): renders an icon-only tile (no label text) wrapped in `GlanceTheme(colors = TempoGlanceColorScheme)`, with an `actionStartActivity<MainActivity>()` passing an `EXTRA_OPEN_NEW_TASK_DIALOG` parameter.
- [x] 2.3 Create `features/widget/presentation/QuickAddTaskWidgetReceiver.kt` (`GlanceAppWidgetReceiver`) exposing `QuickAddTaskWidget`.

## 3. Reuse the existing task-creation sheet

- [x] 3.1 Add `PendingNotificationAction.OpenNewTaskDialog` (no-payload case) to `core/ui/navigation/Navigation.kt`, documented as triggered by the widget rather than a notification.
- [x] 3.2 `MainActivity.handleIntent()`: read `QuickAddTaskWidget.EXTRA_OPEN_NEW_TASK_DIALOG`, call `mainViewModel.setPendingNotificationAction(PendingNotificationAction.OpenNewTaskDialog)`, and bump `tasksNavigationTrigger` so `TempoNavHost` switches to `TasksRoute`; also clear the extra in `clearNotificationExtras()`.
- [x] 3.3 `TasksScreen.kt`: extend the existing `LaunchedEffect(pendingNotificationAction)` to also match `OpenNewTaskDialog`, dispatching `TasksContract.UiEvent.ShowTaskDialog()` (the same event the in-app "+" button dispatches) and consuming the action.

## 4. Tests

- [x] 4.1 No new ViewModel/Content unit or UI tests are needed for the widget module itself — the widget adds no new UI-layer code beyond `QuickAddTaskWidget`/`QuickAddTaskWidgetReceiver` (excluded from coverage like other Android entry points), and it triggers the app's existing, already-tested `ShowTaskDialog()`/`CreateTaskUseCase` flow rather than a new one.

## 5. Polish & Release Bookkeeping

- [x] 5.1 Verify widget tile, light/dark rendering, and the full tap → Tasks tab → task-creation sheet → save flow manually on an emulator.
- [x] 5.2 Replace `WhatsNewRegistry.latest` (`features/whatsnew/presentation/WhatsNewRegistry.kt`) with a new `WhatsNewEntry` announcing the quick-add widget, per AGENTS.md checklist item 5; recheck `versionCode`/`versionName` against `version.txt` right before merging. (This replaces the #28 encryption-at-rest entry that was occupying the same v1.3.0 slot — flagged for reviewer awareness.)

## 6. Verification

- [x] 6.1 Run `./gradlew ktlintFormat` then `./gradlew ktlintCheck`. (Clean, re-verified after the redesign.)
- [x] 6.2 Run `./gradlew :app:detekt` and confirm the detekt baseline count does not increase. (Clean; baseline still 188 < 189 ceiling.)
- [x] 6.3 Run `./gradlew testDebugUnitTest`. (All pass, including `koverVerifyDebug`, `compileDebugAndroidTestKotlin`, `assembleDebug`, `lintDebug`, and the new `TasksScreenTest` case run on-device.)
- [x] 6.4 Run `openspec validate feat-46-quick-task-widget` and resolve any reported issues. (Valid.)
