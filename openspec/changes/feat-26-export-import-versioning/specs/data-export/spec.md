# data-export

## ADDED Requirements

### Requirement: User can export all app data to a versioned JSON file
The system SHALL let the user export all categories, tasks, habits, habit chains, and habit-chain memberships (including their reminder configuration fields), plus the app settings configuration (theme, color scheme, tab configuration, completed-task retention), into a single JSON document written to a location the user chooses via the system file picker (Storage Access Framework).

#### Scenario: Successful export
- **WHEN** the user taps "Export data" in the Settings Backup section and picks a destination in the system file picker
- **THEN** the app writes a JSON file containing every category, task, habit, habit chain, and chain membership, and shows a success confirmation

#### Scenario: Export destination suggested name
- **WHEN** the system file picker opens for export
- **THEN** the suggested file name is `tempo-backup-<yyyyMMdd-HHmm>.json`

#### Scenario: Export fails to write
- **WHEN** writing to the chosen destination fails (e.g. the provider rejects the write)
- **THEN** the app shows an error message and no partial success is reported

### Requirement: Export payload embeds schema version and provenance metadata
The export file SHALL begin with an envelope containing `schemaVersion` (integer, `1` for this change), `appVersion` (the app version string), and `exportedAt` (ISO-8601 local date-time), followed by the entity collections. Original database ids SHALL be included on every record.

#### Scenario: Envelope fields present
- **WHEN** an export file is produced
- **THEN** it contains `schemaVersion = 1`, the current `appVersion`, an `exportedAt` timestamp, and the five entity collections with their database ids

### Requirement: Export format is stable and documented
The export format SHALL be defined by dedicated serialization DTOs (decoupled from Room entities and domain models) and documented in `docs/BACKUP_FORMAT.md`, including the format-evolution rules: additive fields with defaults keep the same `schemaVersion`; renames, removals, or semantic changes MUST bump it.

#### Scenario: Round-trip stability
- **WHEN** an export file is produced and then imported in Replace mode on the same app version
- **THEN** the restored database content equals the exported content

#### Scenario: Frozen v1 fixture keeps decoding
- **WHEN** the checked-in schema-version-1 fixture file is decoded by the current code
- **THEN** decoding succeeds with the expected values (guarding the format against accidental drift)
