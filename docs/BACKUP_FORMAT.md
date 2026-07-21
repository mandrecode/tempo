# Tempo Backup Format

Tempo exports all user data as a single JSON document via **Settings → Backup → Export data**.
This document is the stable, versioned contract for that file. Backup/sync features (present
and future) must read and write this format.

> Exports are **unencrypted** plain JSON. The user chooses where the file is stored via the
> system file picker; anyone with access to the file can read its contents.

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
