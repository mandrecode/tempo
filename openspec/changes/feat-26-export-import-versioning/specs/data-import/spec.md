# data-import

## ADDED Requirements

### Requirement: User can import a Tempo export file in Replace or Merge mode
The system SHALL let the user pick a previously exported file via the system file picker and import it in one of two explicitly chosen modes: **Replace** (wipe current data and restore the file exactly, preserving original ids and the seeded Inbox category) or **Merge** (add the file's contents to existing data). Replace SHALL require an explicit confirmation with a destructive-action warning before running.

#### Scenario: Replace restores the file exactly
- **WHEN** the user imports a valid file in Replace mode and confirms the warning
- **THEN** all existing rows are deleted and the file's records are inserted with their original ids, and a success summary is shown

#### Scenario: Replace payload without a default category re-seeds Inbox
- **WHEN** a Replace import completes from a payload containing no default category
- **THEN** the seeded Inbox category (id -1) is recreated so the app invariant holds

#### Scenario: Merge adds new records with fresh ids
- **WHEN** the user imports a file in Merge mode containing records with no natural-key match locally
- **THEN** those records are inserted with newly generated ids and all internal references (category, parent task, next instance, chain memberships) are remapped consistently

### Requirement: Import validates schema version before anything else
The import SHALL read the file's `schemaVersion` first and reject files whose version is greater than the highest version the app supports, reporting both versions to the user. Files at a supported version with unknown extra JSON fields SHALL still decode (unknown keys are ignored).

#### Scenario: Newer schema version rejected clearly
- **WHEN** the user imports a file with `schemaVersion` greater than the app's supported version
- **THEN** the import is rejected before any database change, and the error names the file's version and the highest supported version

#### Scenario: Unknown fields tolerated
- **WHEN** a file at a supported schema version contains additional unknown JSON fields
- **THEN** the file imports successfully, ignoring the unknown fields

### Requirement: Import validates payload integrity before writing
Before any database mutation, the import SHALL verify the payload parses and is internally consistent: every task references a payload category, every subtask references a payload parent task, every chain membership references a payload chain and habit, and no entity collection contains duplicate ids. A payload that fails parsing or validation SHALL be rejected with a specific, user-readable reason and the local database left untouched. A dangling `nextInstanceId` reference SHALL be nulled rather than treated as fatal.

#### Scenario: Corrupt file rejected
- **WHEN** the user imports a file that is not valid JSON or does not match the expected structure
- **THEN** the import fails with a "corrupt or unrecognized file" error and no data changes

#### Scenario: Broken references rejected
- **WHEN** a payload contains a task whose `categoryId` matches no category in the payload
- **THEN** the import fails with a validation error identifying the problem and no data changes

#### Scenario: Import is atomic
- **WHEN** any error occurs while applying an import to the database
- **THEN** all changes from that import are rolled back and existing local data is intact

### Requirement: Merge detects duplicates and reports conflicts explicitly
In Merge mode the system SHALL match incoming records to local records by natural key (category by name; task by title within its resolved category and parent; habit by title; habit chain by title). An incoming record whose natural key and remaining content (ignoring ids and sort order) match a local record SHALL be skipped as a duplicate. An incoming record whose natural key matches but whose content differs SHALL NOT be imported and SHALL NOT modify the local record; it SHALL be reported as a conflict. Records depending on a conflicted record (e.g. subtasks of a conflicted task) SHALL be skipped and reported. After the import, the user SHALL be shown a summary of imported, skipped-duplicate, and conflicted records, with per-item detail for conflicts.

#### Scenario: Exact duplicate skipped silently into the summary
- **WHEN** a merged file contains a task identical (apart from id and sort order) to an existing local task
- **THEN** the task is not inserted again and the summary counts it as skipped

#### Scenario: Conflict reported, local data untouched
- **WHEN** a merged file contains a category named the same as a local category but with a different color
- **THEN** the local category is unchanged, the incoming category is not imported, references to it resolve to the local category, and the summary lists the conflict with its reason

#### Scenario: Re-importing the same file is a no-op
- **WHEN** the user merges the same export file twice
- **THEN** the second merge inserts nothing and reports everything as skipped duplicates

### Requirement: Reminders are rescheduled after import
After an import completes (in either mode), the system SHALL rebuild reminder alarms from the database state so that reminders on imported records fire. In Replace mode, alarms for pre-import records SHALL be cancelled. Rescheduling SHALL also run when an import fails after alarms were cancelled, restoring alarms from the unchanged database.

#### Scenario: Imported reminders fire
- **WHEN** an import containing tasks or habits with future reminder dates completes successfully
- **THEN** alarms for those reminders are scheduled

#### Scenario: Failed replace restores alarms
- **WHEN** a Replace import cancels existing alarms and the database transaction subsequently fails
- **THEN** rescheduling runs against the rolled-back (unchanged) data and the original alarms are restored

### Requirement: Import outcomes are surfaced in the Settings UI
The Settings Backup section SHALL drive the import flow: file picker, mode chooser (Merge default, Replace destructive-styled), progress indication while running, and a result dialog showing the summary (counts and conflict list) on success or the typed error on failure. All user-facing strings SHALL be localized string resources present in every supported locale.

#### Scenario: Summary dialog after merge
- **WHEN** a merge import finishes
- **THEN** a dialog shows counts of imported, skipped, and conflicted records and lists each conflict with entity name and reason
