## ADDED Requirements

### Requirement: Exported backups are encrypted with a user-supplied passphrase
The system SHALL require the user to supply a passphrase when exporting a backup, and SHALL encrypt the exported payload using a key derived from that passphrase. No option to export an unencrypted backup SHALL be offered.

#### Scenario: Export requires a passphrase
- **WHEN** the user initiates a backup export and picks a destination
- **THEN** the system prompts for a passphrase (with a confirmation field) before writing any file, and does not write the export until a matching passphrase is confirmed

#### Scenario: Mismatched confirmation blocks export
- **WHEN** the user enters a passphrase and a confirmation value that do not match
- **THEN** the system SHALL NOT proceed with the export and SHALL indicate the mismatch to the user

#### Scenario: Exported file is not plainly readable
- **WHEN** an exported backup file is opened as plain text
- **THEN** the original task/habit/category data is not present in readable form

### Requirement: Encrypted exports use a distinct file extension
The system SHALL name newly created encrypted backup exports with a `.tempo` extension rather than `.json`, to signal that the file is not plain JSON.

#### Scenario: Suggested export filename uses the new extension
- **WHEN** the user exports a backup
- **THEN** the suggested filename ends in `.tempo`

### Requirement: Importing an encrypted backup requires the export passphrase
The system SHALL detect when a picked import file is an encrypted backup and SHALL prompt the user for the passphrase before attempting to decrypt and import it.

#### Scenario: Correct passphrase imports successfully
- **WHEN** the user picks an encrypted backup file and enters the passphrase that was used to create it
- **THEN** the system decrypts the payload and proceeds with the existing import flow (mode selection, validation, summary)

#### Scenario: Incorrect passphrase is rejected distinctly
- **WHEN** the user enters a passphrase that does not match the one used to encrypt the file
- **THEN** the system reports a distinct "incorrect passphrase" error, separate from "corrupt file" or "unsupported version" errors, and allows the user to retry entering a passphrase

### Requirement: Legacy plaintext backups remain importable
The system SHALL continue to support importing backup files created before this change (plain, unencrypted JSON), without requiring a passphrase for those files.

#### Scenario: Old plaintext export still imports
- **WHEN** the user picks a `.json` backup file created by a version of the app prior to this change
- **THEN** the system detects it as a legacy plaintext backup, does not prompt for a passphrase, and imports it using the existing import behavior

### Requirement: Backup record schema is unaffected by encryption
The system SHALL treat encryption as a wrapper around the existing backup payload, independent of the backup record schema version (`schemaVersion` / `BACKUP_SCHEMA_VERSION`).

#### Scenario: Record schema version is unchanged by this change
- **WHEN** a new encrypted backup is decrypted
- **THEN** the resulting plaintext payload has the same `schemaVersion` and record structure as it would have without encryption
