## ADDED Requirements

### Requirement: Database file is encrypted at rest
The system SHALL encrypt the local Room/SQLite database file on disk using SQLCipher, such that its contents are not readable without the database passphrase.

#### Scenario: Fresh install creates an encrypted database
- **WHEN** the app is launched for the first time on a device with no existing `tempo_database` file
- **THEN** the app creates a new SQLCipher-encrypted database keyed by a freshly generated passphrase, and no plaintext SQLite file is ever written

#### Scenario: Database is unreadable without the passphrase
- **WHEN** the raw database file is opened by a tool that does not supply the correct SQLCipher passphrase
- **THEN** the file's contents are not interpretable as plaintext SQLite data

### Requirement: Database access remains transparent to the user
The system SHALL manage the database passphrase automatically, without requiring the user to set, remember, or enter any secret for normal app use.

#### Scenario: App opens without prompting for a database password
- **WHEN** the user launches the app on any previously-used device
- **THEN** the app opens and displays data normally without any passphrase or password prompt related to the database

### Requirement: Database passphrase is protected by Android Keystore
The system SHALL generate a random database passphrase on first run and protect it using an Android Keystore-backed key that never leaves the device and does not require user authentication to use.

#### Scenario: Passphrase persists across app restarts
- **WHEN** the app is closed and reopened
- **THEN** the same Keystore-protected passphrase is retrieved and used to open the existing encrypted database, without generating a new one

#### Scenario: Passphrase key becomes unrecoverable
- **WHEN** the Keystore key needed to decrypt the stored database passphrase is no longer available, but an encrypted database and passphrase blob already exist on disk
- **THEN** the system SHALL NOT generate a new passphrase or silently recreate the database; it SHALL surface a distinct, explicit failure rather than corrupting or discarding existing data

### Requirement: Existing plaintext databases are migrated automatically
The system SHALL detect an existing plaintext (unencrypted) database from a prior app version and migrate it to an encrypted database automatically, exactly once, before the database is opened for normal use.

#### Scenario: Upgrade from a pre-encryption version preserves all data
- **WHEN** the app is launched after upgrading from a version that stored the database in plaintext
- **THEN** all existing tasks, categories, habits, chains, and chain memberships are present and unchanged in the resulting encrypted database

#### Scenario: Migration detection is idempotent
- **WHEN** the app is launched and the database file is already encrypted (either freshly created or previously migrated)
- **THEN** the system SHALL NOT attempt migration again

### Requirement: Database migration is crash-safe
The system SHALL ensure that an interrupted migration never corrupts or loses existing data, and that a subsequent launch safely retries or recovers.

#### Scenario: Process is killed before the encrypted copy is verified
- **WHEN** the app process is terminated after starting migration but before the new encrypted database file has been verified as valid
- **THEN** the original plaintext database file remains intact and unmodified, and the next launch retries migration from scratch

#### Scenario: Process is killed between the two file swap steps
- **WHEN** the app process is terminated after the original database file has been renamed aside but before the new encrypted file has been renamed into its place
- **THEN** the next launch detects this state, restores the original database file, and retries migration

### Requirement: Database schema is unaffected by encryption
The system SHALL NOT change the Room database's logical schema (entities, columns, indices, or schema version) as part of introducing encryption; encryption only affects how the database file is stored on disk.

#### Scenario: Room schema version is unchanged by this change
- **WHEN** the encrypted database is opened by Room
- **THEN** Room reports the same schema version and table structure as before encryption was introduced
