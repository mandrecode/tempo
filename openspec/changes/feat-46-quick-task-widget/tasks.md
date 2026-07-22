## 1. Dependencies & Manifest

- [x] 1.1 Add `androidx.glance` (`glance`, `glance-appwidget`, `glance-material3`) versions to `gradle/libs.versions.toml` and wire the libraries into `app/build.gradle.kts`.
- [x] 1.2 Add `res/xml/quick_task_widget_info.xml` (`appwidget-provider`: minimal fixed size, `resizeMode="none"`, `android:previewImage`, `android:widgetCategory="home_screen"`).
- [x] 1.3 Add `<receiver>` for the Glance widget receiver and `<activity>` for the trampoline quick-add screen to `AndroidManifest.xml` (transparent/dialog theme, `excludeFromRecents="true"`, `launchMode="singleTask"`).
- [x] 1.4 Add a launcher/widget preview drawable/icon asset if one doesn't already exist that fits the widget picker tile. (Reused `@mipmap/ic_launcher` as the widget picker preview — no bespoke widget screenshot asset exists to compose one from.)

## 2. Glance Widget Chrome

- [x] 2.1 Create `features/widget/presentation/TempoGlanceColorScheme.kt` bridging `TempoLightColorScheme`/`TempoDarkColorScheme` (or the raw `TempoLight*`/`TempoDark*` tokens) from `core/ui/theme/Color.kt` into a Glance `ColorProviders` (day/night), per design.md decision 4.
- [x] 2.2 Create `features/widget/presentation/QuickAddTaskWidget.kt` (`GlanceAppWidget`): renders icon + "Quick add task" label wrapped in `GlanceTheme(colors = TempoGlanceColorScheme)`, with an `actionStartActivity` to `QuickAddTaskActivity`.
- [x] 2.3 Create `features/widget/presentation/QuickAddTaskWidgetReceiver.kt` (`GlanceAppWidgetReceiver`) exposing `QuickAddTaskWidget`.

## 3. Quick-Add Surface (Trampoline Activity)

- [x] 3.1 Define MVI contract `QuickAddTaskContract.kt` (`UiState` with title/selected category/categories list/validation error/saving flag, `UiEvent`, `UiEffect`) per `docs/agents/UI_UX.md` conventions.
- [x] 3.2 Implement `QuickAddTaskViewModel.kt` (Hilt `@HiltViewModel`), injecting `CreateTaskUseCase` and `CategoryRepository`; loads categories, validates title, calls `CreateTaskUseCase`, maps `Result.ValidationError` to `UiState` error, emits a "close" `UiEffect` on `Result.Success` or cancel.
- [x] 3.3 Implement `QuickAddTaskScreen.kt` (owns ViewModel, collects state/effects, finishes the Activity on close effect) and `QuickAddTaskContent.kt` (pure Compose: title `TextField`, category chips, Save/Cancel actions), wrapped in `TempoTheme`.
- [x] 3.4 Implement `QuickAddTaskActivity.kt` (`@AndroidEntryPoint`, transparent theme, hosts `QuickAddTaskScreen` as its content, finishes itself on the close effect).
- [x] 3.5 Add required string resources to `strings.xml` (and every locale-specific `values-<locale>/strings.xml`, per AGENTS.md localization rule) for widget label, quick-add title/hint, validation error, save/cancel actions. (Validation/save/cancel strings already existed and are reused; only the three new widget-specific strings were added.)

## 4. Tests

- [x] 4.1 Unit test `QuickAddTaskViewModel` (MockK + Turbine): empty title validation, successful save calls `CreateTaskUseCase` and emits close effect, cancel emits close effect without calling the use case.
- [x] 4.2 UI test for `QuickAddTaskContent` covering: typing a title, validation error display, category selection, Save/Cancel emit the expected events.

## 5. Polish & Release Bookkeeping

- [x] 5.1 Verify widget preview tile, label, and light/dark rendering manually via `run` skill / emulator (add widget from picker, toggle system dark mode, confirm colors track `TempoLightColorScheme`/`TempoDarkColorScheme`). Verified on a Pixel emulator (Android 17): installed the debug build, placed "Quick Add Task" from the widget picker, tapped it to open the quick-add dialog with "Inbox" pre-selected as default category, saved a "Buy milk" task, and confirmed it appeared in the Tasks list. Widget and dialog colors matched `TempoLightColorScheme` in light mode and `TempoDarkColorScheme` after toggling system dark mode. Also confirmed the new What's New entry shows correctly on first launch.
- [x] 5.2 Replace `WhatsNewRegistry.latest` (`features/whatsnew/presentation/WhatsNewRegistry.kt`) with a new `WhatsNewEntry` announcing the quick-add widget, per AGENTS.md checklist item 5; recheck `versionCode`/`versionName` against `version.txt` right before merging. (This replaces the #28 encryption-at-rest entry that was occupying the same v1.3.0 slot — flagged for reviewer awareness.)

## 6. Verification

- [x] 6.1 Run `./gradlew ktlintFormat` then `./gradlew ktlintCheck`. (Both clean.)
- [x] 6.2 Run `./gradlew :app:detekt` and confirm the detekt baseline count does not increase. (Clean; baseline still 188 < 189 ceiling, file untouched.)
- [x] 6.3 Run `./gradlew testDebugUnitTest`. (All unit tests pass, including new `QuickAddTaskViewModelTest`; `koverVerifyDebug`, `compileDebugKotlin`, `compileDebugAndroidTestKotlin`, `assembleDebug`, and `lintDebug` also verified clean.)
- [x] 6.4 Run `openspec validate feat-46-quick-task-widget` and resolve any reported issues. (Valid.)
