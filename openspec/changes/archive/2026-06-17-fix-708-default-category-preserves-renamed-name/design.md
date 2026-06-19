## Context

The default task category is persisted in the `categories` table as a normal category row, currently seeded with `id = -1`, `isDefault = true`, `icon = inbox`, and `sortOrder = -1`. Fresh databases seed this row through the Room database callback using `R.string.category_inbox`.

The current callback also runs an `onOpen` update that renames the row from the legacy hardcoded English value to the current localized string. That startup mutation conflicts with the product rule that the default category name is only an initial value and must remain user-editable afterward.

## Goals / Non-Goals

**Goals:**

- Treat the default category display name as persisted user data after creation.
- Keep fresh-install seeding localized to the current app language at creation time.
- Avoid schema changes for a behavior-only persistence fix.

**Non-Goals:**

- Do not re-localize existing default category names when the app locale changes.
- Do not change category identity, task foreign keys, or default-category selection behavior.
- Do not alter category editing UI or validation rules.

## Decisions

- Remove open-time default category renaming. This makes database open idempotent and prevents startup from overwriting user-edited names.
- Keep create-time seeding. Fresh installs still get the current localized initial default name without requiring a migration or UI fallback.
- Do not add a migration. The stored row already contains all needed data; the bug is caused by callback behavior, not schema shape.
- Keep default-category behavior keyed by persisted identity fields (`id`/`isDefault`) rather than name text. The name remains a mutable label.

## Risks / Trade-offs

- Legacy databases with the English `Inbox` value will no longer be auto-localized on open. This is intentional because ongoing localization would continue to make the name non-user-owned.
- Users who never renamed the default category will keep whatever value was already stored. Fresh installs still use the current localized initial value.
- Unit-level coverage may need to focus on the callback helper or repository behavior because full Room callback execution is Android-dependent.
