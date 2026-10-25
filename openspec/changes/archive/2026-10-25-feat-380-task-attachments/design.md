# Design

## Storage

Files are copied into `files/attachments/<uuid>` and never referenced in place.

- **Why copy rather than keep a persisted URI permission?** Persisted SAF grants are capped per app, are lost when the source app is uninstalled or the file moves, and do not exist for every provider (camera/gallery shares). A copy always opens.
- **Size cap: 25 MB.** The provider's reported size is checked first, and the copied byte count is also checked, because providers do not always report a size. A partial copy is deleted.
- **Sharing out:** a `FileProvider` rooted only at `attachments/`, used with `ACTION_VIEW` and `FLAG_GRANT_READ_URI_PERMISSION`. The viewer gets one file, read-only, for that intent.

## Rows and lifecycle

`task_attachments(id, taskId, displayName, mimeType, sizeBytes, storedName)` is indexed on `taskId` and `storedName`.

There is **deliberately no foreign key** to `tasks`. Task deletion is undoable: the tasks are deleted and later re-inserted from a snapshot under the same ids. An `ON DELETE CASCADE` would drop the attachment rows at deletion time, and the undo would not bring them back. Without the foreign key, rows survive the pending undo and simply point at the re-inserted task again.

The cost is orphans: rows whose task is permanently gone, and files no row points to (including files picked into a new task that was never saved). `PruneOrphanedTaskAttachmentsUseCase` deletes both. It runs at app start, after the database warm-up and off the main thread. That is the one moment no undo can be pending, because undo state lives only in the process that offered it. Running it in the daily cleanup worker could race a visible undo snackbar.

The prune runs alongside the app rather than before it, so it must not delete a file the editor is copying in for a task that is about to be saved. It lists candidate files first and only those written **before the process started** (`keepFilesFromEpochMillis`). After that it reads the referenced names. A file copied in during this run is never a candidate, and a row inserted between the two reads is still seen.

Task ids come from `AUTOINCREMENT`, so a new task never reuses a deleted task's id. The one path that writes explicit old ids is backup "replace" import. It clears `task_attachments` in the same transaction, so an imported task cannot pick up a deleted task's files before the next prune.

`storedName` may be shared by several rows: the next occurrence of a recurring task gets copies of the rows, not of the files. That applies to the parent and to each cloned subtask, which is matched to its original by the ids `insertTasks` returns. A file is deleted only when the last row referencing it goes (`remove`), or by the prune.

## Editor flow

- **Saved task:** `TasksViewModel` follows `observeAttachments(editingTask.id)` with `flatMapLatest` on the edited task's id, whichever way the editor was opened. Add and remove write immediately.
- **New task:** imported attachments (`id = 0`) live in `TaskFormState.attachments`. `createOrUpdateTask` hands that list to `addTask`, which calls `attach(pending, newTaskId)` once `CreateTaskUseCase` succeeds. The list is captured before the form is reset, which happens synchronously.
- If the editor closed or another one opened while files were still copying, the copies are discarded.
- The observation is keyed on `(editorSessionId, editingTask.id)`. Reopening the task already being edited, for example from its notification, resets the form, and a fresh subscription refills it.
- Attaching, removing and discarding never throw at the user. A failed attach or remove shows a snackbar; a failed discard leaves an orphan for the next prune.
- For a new task, picked attachments count as unsaved changes, so dismissing the sheet asks before discarding them.
- The section is only shown when the host passes `TaskAttachmentActions`. `TasksScreen`'s `TaskEditor` does, so Tasks and Focus (which reuses `TaskEditor`) both get it. Other callers of `TaskBottomSheet` are unchanged.
