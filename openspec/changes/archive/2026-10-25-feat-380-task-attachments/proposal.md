# Proposal

## Why

Originating issue: [#380](https://github.com/mandrecode/tempo/issues/380) asks for attachments in tasks: pictures, documents, and so on. Tasks often depend on a file: a lease to sign, a receipt to file, a photo of what to buy. Today that file lives somewhere else and has to be found again.

## What Changes

- The task editor gets an **Attachments** row, below reminder and repeat. It lists each attached file (an icon for its type, and its name) with a remove button, plus an **Add attachment** button that opens the system document picker for any file type, with multiple selection.
- Tapping an attachment opens it in whatever app the device has for that type, through a read-only, per-file grant. If no app can open it, a snackbar says so.
- Picked files are **copied** into the app's private storage, so they stay readable after the original is moved or deleted and the app needs no lasting permission on someone else's storage. Files over 25 MB are refused with a snackbar.
- On a saved task, attachments are added and removed immediately, like the editor's other auto-saved edits. On a new task they wait in the form and are attached when the task is saved.
- Deleting a task keeps its attachments until nothing can be undone any more, so **undo restores them**. Rows and files left behind (expired deletions, picks into tasks that were never saved) are pruned when the app starts.
- A recurring task's next occurrence keeps the same attachments, sharing the files.
- Attachments are excluded from Auto Backup, like the encrypted database they belong to. Backup export/import does not carry them, and a "replace" import clears attachment rows so imported tasks reusing old ids cannot inherit stale files.
- What's New: the entry becomes `434-380`, announcing both #434 and #380 (en, es).

### Non-goals

- No thumbnails or in-app viewer. Files open in the system's viewer apps.
- No attachment indicator on task cards in the list.
- No attachments in backup export/import files.
- No encryption of the attachment files beyond app-private storage. The database stays encrypted; the files are protected by Android's per-app sandbox.

## Capabilities

### New Capabilities
- `task-attachments`: attaching, opening and removing files on tasks, and their lifecycle.

### Modified Capabilities

None.

## Impact

- Data: Room v10 → v11 (`task_attachments`, additive migration `MIGRATION_10_11`, schema `11.json`), `TaskAttachmentEntity`, `TaskAttachmentDao`, `TaskAttachmentRepositoryImpl`, `AttachmentFileStore` (`AndroidAttachmentFileStore` in infrastructure).
- Domain: `TaskAttachment`, `AttachmentImportResult`, `TaskAttachmentRepository`, `PruneOrphanedTaskAttachmentsUseCase`. `ToggleTaskCompletionUseCase` and `RollOverduePeriodicTaskUseCase` copy attachments to the next occurrence.
- App: `TempoApp` prunes orphans after the database warm-up.
- Manifest: a `FileProvider` (`${applicationId}.attachments`, `res/xml/attachment_paths.xml`). Backup rules exclude `files/attachments/`.
- Backup: "replace" import clears `task_attachments`.
- UI: `TaskAttachmentsSection` and `TaskAttachmentActions`, threaded through `TaskBottomSheet` → `TaskBottomSheetContent` → `TaskBottomSheetBody`; `TasksContract`; `TasksViewModelAttachmentActions`.
- Strings (en, es); four new drawables.
