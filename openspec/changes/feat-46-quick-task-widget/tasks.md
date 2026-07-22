## 1. Dependencies & Manifest

- [x] 1.1 Add `androidx.glance` (`glance`, `glance-appwidget`, `glance-material3`) versions to `gradle/libs.versions.toml` and wire the libraries into `app/build.gradle.kts`.
- [x] 1.2 Add `res/xml/quick_task_widget_info.xml` (`appwidget-provider`: minimal fixed size matching the actual content, `resizeMode="none"`, `android:previewImage`, `android:widgetCategory="home_screen"`).
- [x] 1.3 Add `<receiver>` for the Glance widget receiver to `AndroidManifest.xml`, with `android:label` set to the widget's own string so it's identifiable in the widget picker (not the app's label).
- [x] 1.4 Add a static widget-picker preview drawable (`widget_quick_add_task_preview.xml`, a rounded tile + centered icon, with `-night` color variants) accurately approximating the placed widget's default appearance, replacing the earlier `@mipmap/ic_launcher` placeholder. (Glance 1.1.1 has no generated-preview API — see design.md risk list.)

## 2. Glance Widget Chrome

- [x] 2.1 Create `features/widget/presentation/TempoGlanceColorScheme.kt`: a static Tempo `ColorProviders` bridge from the raw `TempoLight*`/`TempoDark*` tokens in `core/ui/theme/Color.kt`, plus `resolveGlanceColorProviders()`/`shouldUseTempoStaticColors()` mirroring `TempoTheme`'s useTempoColors-vs-dynamic-color decision, per design.md decision 4.
- [x] 2.2 Create `features/widget/presentation/QuickAddTaskWidget.kt` (`GlanceAppWidget`): renders an icon-only tile (no label text) wrapped in `GlanceTheme(colors = TempoGlanceColorScheme)`, with an `actionStartActivity<MainActivity>()` passing an `EXTRA_OPEN_NEW_TASK_DIALOG` parameter.
- [x] 2.3 Create `features/widget/presentation/QuickAddTaskWidgetReceiver.kt` (`GlanceAppWidgetReceiver`) exposing `QuickAddTaskWidget`.

## 3. Reuse the existing task-creation sheet

- [x] 3.1 Add `PendingNotificationAction.OpenNewTaskDialog` (no-payload case) to `core/ui/navigation/Navigation.kt`, documented as triggered by the widget rather than a notification.
- [x] 3.2 `MainActivity.handleIntent()`: read `QuickAddTaskWidget.EXTRA_OPEN_NEW_TASK_DIALOG`, call `mainViewModel.setPendingNotificationAction(PendingNotificationAction.OpenNewTaskDialog)`, and bump `tasksNavigationTrigger` so `TempoNavHost` switches to `TasksRoute`; also clear the extra in `clearNotificationExtras()`.
- [x] 3.3 `TasksScreen.kt`: extend the existing `LaunchedEffect(pendingNotificationAction)` to also match `OpenNewTaskDialog`, dispatching `TasksContract.UiEvent.ShowTaskDialog()` (the same event the in-app "+" button dispatches) and consuming the action.

## 4. Tests

- [x] 4.1 No new ViewModel/Content unit or UI tests are needed for the widget module itself — the widget adds no new UI-layer code beyond `QuickAddTaskWidget`/`QuickAddTaskWidgetReceiver` (excluded from coverage like other Android entry points), and it triggers the app's existing, already-tested `ShowTaskDialog()`/`CreateTaskUseCase` flow rather than a new one.
- [x] 4.2 Add `TempoGlanceColorSchemeTest.kt` unit-testing `shouldUseTempoStaticColors()`'s three branches (Tempo preference on; preference off with dynamic supported; preference off with dynamic unsupported, falling back to Tempo).

## 7. Theme Parity (Tempo colors / dynamic color)

- [x] 7.1 `QuickAddTaskWidget` reads the user's "use Tempo colors" preference (`ThemePreferencesRepository.getUseTempoColors()`) via a small Hilt `@EntryPoint` (`ThemePreferencesEntryPoint`) each time `provideGlance()` runs, since `GlanceAppWidget` isn't `@AndroidEntryPoint`-compatible.
- [x] 7.2 `resolveGlanceColorProviders()` picks static Tempo colors or dynamic (`dynamicColorScheme()`, the same util `TempoTheme` uses) accordingly, always falling back to Tempo's static colors rather than unbranded Material defaults when dynamic color isn't supported.
- [x] 7.3 `SettingsViewModel` calls `QuickAddTaskWidget().updateAll(appContext)` right after `setUseTempoColors()` so an already-placed widget reflects a preference change immediately; updated `SettingsViewModelTest` for the new `@ApplicationContext Context` constructor param.

## 5. Polish & Release Bookkeeping

- [x] 5.1 Verify widget tile, light/dark rendering, dynamic-vs-Tempo colors (both toggle states), the immediate widget refresh on preference change, the widget-picker preview drawable, and the full tap → Tasks tab → task-creation sheet → save flow manually on an emulator.
- [x] 5.2 Replace `WhatsNewRegistry.latest` (`features/whatsnew/presentation/WhatsNewRegistry.kt`) with a new `WhatsNewEntry` announcing the quick-add widget, per AGENTS.md checklist item 5; recheck `versionCode`/`versionName` against `version.txt` right before merging. (This replaces the #28 encryption-at-rest entry that was occupying the same v1.3.0 slot — flagged for reviewer awareness.)

## 6. Verification

- [x] 6.1 Run `./gradlew ktlintFormat` then `./gradlew ktlintCheck`. (Clean, re-verified after the redesign.)
- [x] 6.2 Run `./gradlew :app:detekt` and confirm the detekt baseline count does not increase. (Clean; baseline still 188 < 189 ceiling.)
- [x] 6.3 Run `./gradlew testDebugUnitTest`. (All pass, including `koverVerifyDebug`, `compileDebugAndroidTestKotlin`, `assembleDebug`, `lintDebug`, and the new `TasksScreenTest` case run on-device.)
- [x] 6.4 Run `openspec validate feat-46-quick-task-widget` and resolve any reported issues. (Valid.)
