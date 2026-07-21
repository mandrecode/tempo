# Tempo Backup Format

Tempo exports all user data as a single JSON document via **Settings → Backup → Export data**.
This document is the stable, versioned contract for that file. Backup/sync features (present
and future) must read and write this format.

> Exports are **encrypted** with a passphrase the user sets at export time (see
> [Encryption](#encryption) below). The user chooses where the file is stored via the system
> file picker.

## Encryption

Every export is encrypted — there is no plaintext export option. At export time, the user sets
a passphrase (with a confirmation field, since it cannot be recovered if forgotten); at import
time, they re-enter it. The passphrase is never stored by the app.

The written file is a JSON **envelope** distinct from the record schema below, marked by an
`encryptionVersion` field. This document describes **encryption version 1**:

| Field | Type | Description |
|:---|:---|:---|
| `encryptionVersion` | int | Encryption envelope format version. |
| `kdf` | string | Key derivation function, currently always `"PBKDF2WithHmacSHA256"`. |
| `iterations` | int | KDF iteration count (200,000 for envelope version 1). |
| `salt` | string | Base64-encoded random salt, unique per export. |
| `iv` | string | Base64-encoded AES-GCM initialization vector. |
| `ciphertext` | string | Base64-encoded AES-256-GCM ciphertext of the plaintext record JSON described below. |

Decrypting `ciphertext` with the passphrase-derived key yields the plaintext JSON document
described in [Envelope](#envelope) onward. Encryption is a wrapper around that document, not a
replacement for it — `encryptionVersion` and `schemaVersion` evolve independently. A wrong
passphrase is detected via AES-GCM authentication failure, not garbled output.

The suggested export filename uses the `.tempo` extension (e.g. `tempo-backup-20260721-1000.tempo`)
rather than `.json`, signaling that the file is this encrypted envelope, not plain JSON. Import
still accepts older plaintext `.json` exports (files written before this envelope existed):
the importer detects the format by attempting to decode the encrypted envelope first, falling
back to plain-JSON decoding when that fails, so no passphrase is requested for those files.

The encryption DTOs live in `BackupEncryptedEnvelopeDto` in the same `BackupFileDto.kt` file, and
the KDF/cipher logic lives in `infrastructure/security/BackupEncryptionService.kt`.

## Envelope

| Field | Type | Description |
|:---|:---|:---|
| `schemaVersion` | int | Format version. This document describes **version 1**. |
| `appVersion` | string | `versionName` of the app that produced the export (provenance only). |
| `exportedAt` | string | ISO-8601 local date-time of the export (provenance only). |
| `categories` | array | See [Category](#category). |
| `tasks` | array | See [Task](#task). |
| `habits` | array | See [Habit](#habit). |
| `habitChains` | array | See [Habit chain](#habit-chain). |
| `habitChainMembers` | array | See [Chain member](#chain-member). |
| `settings` | object? | See [Settings](#settings). Optional; absent in files written before it existed. |

All collections default to empty. Original database ids are included on every record; they are
required for referential integrity inside the file and are preserved verbatim by Replace
imports (Merge imports assign fresh ids and remap references).

## Value conventions

- **Dates**: ISO-8601 local date-time strings without offset, e.g. `"2026-07-21T10:00"`.
- **Enums**: serialized by name — `priority`: `HIGH | MEDIUM | LOW`; `periodicity`:
  `HOURLY | DAILY | WEEKLY | MONTHLY | YEARLY`; `monthDayOption`:
  `SAME_DAY | FIRST_DAY | LAST_DAY`; `habitType`: `BUILD | QUIT`.
- **Repeat days**: array of ISO day numbers, Monday = 1 … Sunday = 7.
- Optional fields are nullable and default to `null` (or the stated default) when absent.

## Records

### Category

`id`, `name`, `color?`, `icon?`, `isDefault` (default `false`), `sortOrder` (default `0`).
The seeded Inbox category exports with `id = -1` and `isDefault = true`.

### Task

`id`, `title`, `description` (default `""`), `isCompleted` (default `false`), `categoryId`
(must reference a category in the file), `priority?`, `reminderDate?`, `periodicity?`,
`periodicityInterval` (default `1`), `repeatDays?`, `monthDayOption?`, `parentTaskId?`
(must reference a task in the file), `sortOrder` (default `0`), `completedAt?`,
`nextInstanceId?` (dangling references are nulled on import, not fatal).

### Habit

`id`, `title`, `description` (default `""`), `icon?`, `colorKey?`, `reminderDate?`,
`isCompleted` (default `false`), `habitType` (default `"BUILD"`), `createdDate` (required),
`completionHistory` (default `""`), `repeatDays?`.

### Habit chain

`id`, `title`, `description` (default `""`), `colorKey?`, `icon?`, `periodicReminder?`,
`createdDate` (required), `completionHistory` (default `""`), `repeatDays?`.

### Chain member

`chainId`, `habitId`, `sortOrder`. Both ids must reference records in the file.

### Settings

App configuration snapshot: `themeMode` (`LIGHT | DARK | SYSTEM`), `useTempoColors`,
`routinesTabEnabled`, `tasksTabEnabled`, `defaultTab` (`ROUTINES | TASKS`),
`autoRemoveCompletedTasks`, `completedTaskRetentionDays`. Applied **only by Replace
imports** (a full restore); Merge never changes local configuration. On apply, app
invariants are re-established: at least one tab stays enabled, the default tab must be
an enabled tab, and retention days snap to the nearest supported value.

## Import behavior

- The importer reads `schemaVersion` first and rejects files whose version is **greater** than
  the highest version the app supports, reporting both versions.
- Unknown JSON fields are ignored (`ignoreUnknownKeys`), so files written by later app builds
  at the same schema version still import.
- The payload is validated (referential integrity, duplicate ids) before any database write;
  all writes happen in a single transaction.
- **Replace** restores the file exactly (original ids preserved; Inbox re-seeded if the file
  carries no default category). **Merge** adds records, skipping exact duplicates and
  reporting conflicts by natural key (category name; task title within category/parent;
  habit/chain title).

## Evolution rules

- **Additive changes only** under the same `schemaVersion`: new fields may be added if they
  have a default value, and importers must keep ignoring unknown fields.
- **Anything else bumps `schemaVersion`**: renaming or removing a field, changing a type or
  semantic meaning, or changing a serialized enum name.
- Every released schema version keeps a frozen fixture under `app/src/test/resources/backup/`
  (e.g. `v1-backup.json`); compatibility tests decode these fixtures to guard against
  accidental drift.
- The serialization DTOs live in
  `app/src/main/java/com/mandrecode/tempo/features/backup/data/model/BackupFileDto.kt` and are
  deliberately decoupled from Room entities and domain models — refactors there must not
  change this file format.
- `encryptionVersion` (see [Encryption](#encryption)) is a separate version number from
  `schemaVersion` and only bumps when the encryption envelope or crypto parameters change, not
  when the record schema changes.
