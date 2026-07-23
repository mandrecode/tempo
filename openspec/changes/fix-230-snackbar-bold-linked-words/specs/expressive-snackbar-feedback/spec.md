## ADDED Requirements

### Requirement: Bold emphasis for a linked entity name within a snackbar message

The shared snackbar SHALL support emphasizing exactly one linked entity name (such as a category name) in bold within its message, composed from a prefix, the bold word, and a suffix, instead of quoting the name with literal punctuation. Snackbars that do not need this emphasis SHALL continue to render as plain text, unaffected.

#### Scenario: Category name emphasized in a success message

- **WHEN** a category is successfully added or deleted and its confirmation snackbar is shown
- **THEN** the category's name renders in bold within the snackbar message, with the surrounding text in the message's normal weight, and no literal quote characters wrap the name

#### Scenario: Category name emphasized in a duplicate-name error message

- **WHEN** the user attempts to create or rename a category to a name that already exists and the duplicate-name snackbar is shown
- **THEN** the existing category's name renders in bold within the snackbar message, with no literal quote characters wrapping it

#### Scenario: Plain snackbar messages remain unaffected

- **WHEN** a snackbar is shown for a message that does not emphasize a linked entity name
- **THEN** the snackbar renders its message as plain text exactly as before, with no change in appearance or behavior

#### Scenario: Bold-emphasized snackbar with an Undo action

- **WHEN** a bold-emphasized snackbar also has an Undo action (e.g. deleting a category)
- **THEN** the Undo action and its dismissal/selection reporting behave identically to a plain-text actionable snackbar
