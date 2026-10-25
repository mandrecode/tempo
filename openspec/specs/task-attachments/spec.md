# task-attachments Specification

## Purpose
Defines how files are attached to tasks, opened, removed, and kept or cleaned up across deletion, recurrence and backup.

## Requirements

### Requirement: Attach files from the task editor
The task editor SHALL let the user pick one or more files of any type and attach them to the task. Each attached file SHALL be listed by name with an icon for its kind (image, PDF, other) and a remove action. Files larger than 25 MB SHALL be refused with a message, and files that cannot be read SHALL be reported.

#### Scenario: Attach to a saved task
- **WHEN** the user picks a file while editing an existing task
- **THEN** the file is attached to the task immediately and listed in the editor

#### Scenario: Attach to a new task
- **WHEN** the user picks a file in the editor of a task that has not been saved yet and then saves the task
- **THEN** the saved task has the file attached

#### Scenario: Too large
- **WHEN** the user picks a file larger than 25 MB
- **THEN** it is not attached and the app says the file is too large

### Requirement: Attachments are private copies opened in other apps
An attached file SHALL be a private copy kept by the app, so it remains available if the original is moved or deleted. Tapping an attachment SHALL open it in an app that handles its type, granting that app read access to that file only. If no app can open it, the app SHALL say so.

#### Scenario: Open an attachment
- **WHEN** the user taps an attached PDF and the device has a PDF viewer
- **THEN** the PDF opens in that viewer

#### Scenario: Original deleted
- **WHEN** the original file is deleted after it was attached
- **THEN** the attachment still opens

### Requirement: Attachments follow the task's lifecycle
Removing an attachment SHALL delete its file unless another task still shares it. Undoing a task deletion SHALL restore its attachments. Attachments of tasks that are permanently gone, and files picked into tasks that were never saved, SHALL be cleaned up when the app next starts. The next occurrence of a recurring task SHALL keep the same attachments.

#### Scenario: Undo a deletion
- **WHEN** the user deletes a task with attachments and taps Undo
- **THEN** the restored task has the same attachments

#### Scenario: Recurring task completed
- **WHEN** the user completes a recurring task with attachments
- **THEN** its next occurrence has the same attachments

#### Scenario: Deleted for good
- **WHEN** a task with attachments was deleted and its undo expired, and the app starts again
- **THEN** the attachment rows and their files are removed

### Requirement: Attachments stay on the device
Attachment files SHALL be excluded from Android Auto Backup and device transfer, like the database that references them. Backup export SHALL NOT include them. A backup import that replaces all data SHALL leave no attachment rows behind.

#### Scenario: Replace import
- **WHEN** the user imports a backup in replace mode
- **THEN** no task in the imported data shows an attachment from before the import
