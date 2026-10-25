# Tasks

## 1. Data and domain

- [x] 1.1 Add `TaskAttachment` / `AttachmentImportResult`, `TaskAttachmentRepository`, `TaskAttachmentEntity`, `TaskAttachmentDao`, mapper, and `TaskAttachmentRepositoryImpl`; bind it in `RepositoryModule`.
- [x] 1.2 Add `AttachmentFileStore` and `AndroidAttachmentFileStore` (copy in with a 25 MB cap, delete, list).
- [x] 1.3 Room v11: entity, `MIGRATION_10_11`, schema `11.json`, DAO provider.
- [x] 1.4 `PruneOrphanedTaskAttachmentsUseCase`, run from `TempoApp` after the database warm-up.
- [x] 1.5 Copy attachments to the next occurrence in `ToggleTaskCompletionUseCase` and `RollOverduePeriodicTaskUseCase`, for the parent and each cloned subtask.
- [x] 1.7 Prune only files written before the process started, listing files before reading referenced names.
- [x] 1.6 Clear `task_attachments` in backup replace import; exclude `files/attachments/` from Auto Backup and device transfer.

## 2. UI

- [x] 2.1 `FileProvider` and `attachment_paths.xml`.
- [x] 2.2 `TaskFormState.attachments`, `AddAttachments` / `RemoveAttachment` / `AttachmentOpenFailed` events, and `TasksViewModelAttachmentActions` (live rows for saved tasks, pending list for new ones, discard on a closed editor).
- [x] 2.3 `TaskAttachmentsSection` with a debug preview; thread `TaskAttachmentActions` from `TaskEditor` to the sheet body.
- [x] 2.4 Strings (en, es), drawables, What's New `434-380`.
- [x] 2.5 Key the attachment observation on the editor session; guard attach/remove/discard failures; count pending attachments as unsaved changes on a new task.
- [x] 2.6 Exclude `infrastructure.attachments.*` (Android file I/O only) from Kover, like the other Android-bound infrastructure.

## 3. Tests and verification

- [x] 3.1 Unit: `TaskAttachmentRepositoryTest`, attachment cases in `TasksViewModelTest`, copy-on-recurrence checks in `ToggleTaskCompletionUseCaseTest` and `RollOverduePeriodicTaskUseCaseTest`.
- [x] 3.2 Instrumented: `TaskAttachmentsSectionTest`, `MigrationTest.migrate10To11_addsTaskAttachmentsAndPreservesTasks`.
- [x] 3.3 Run `./gradlew ktlintCheck :app:detekt testDebugUnitTest koverVerifyDebug lintDebug kspDebugKotlin` and `openspec validate --strict`.
- [x] 3.4 Check by hand on the Pixel 10 AVD: attach an image to a new task, save, reopen, open it, remove it.
