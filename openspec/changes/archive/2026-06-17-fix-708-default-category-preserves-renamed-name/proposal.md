## Why

GitHub issue [#708](https://github.com/mandrecode/tempo/issues/708) reports that the default category name reverts on every app start. The default category should use the current localized initial name only when it is created, then persist user edits like any other category.

## What Changes

- Preserve user edits to the default category name across app restarts.
- Stop startup/open-time logic from renaming an existing default category row.
- Keep fresh-install seeding of the default category with the current localized initial name.

Non-goals:

- Do not redesign category editing, deletion, sorting, or default-selection UI.
- Do not introduce a new category identity model or change task-category relationships.
- Do not re-localize already-created default category names after creation.

## Capabilities

### New Capabilities

- `default-category-persistence`: Defines how the seeded default category is created and how its user-editable fields persist afterward.

### Modified Capabilities

- None. No existing OpenSpec specs are present.

## Impact

- Room database callback behavior for default category seeding/opening.
- Category persistence expectations for the default category row.
- Regression tests around preserving a renamed default category.
