## ADDED Requirements

### Requirement: Task auto-save detects reverted field values
The task edit auto-save mechanism SHALL persist a field change whenever the current form value differs from the last value actually saved to the database, regardless of whether that current value matches the value the sheet originally opened with.

#### Scenario: Category changed away then reverted to the original
- **WHEN** a task open in the edit sheet with category A is changed to category B (auto-save persists B), and then changed back to category A
- **THEN** auto-save persists category A, and the task's stored category matches the UI selection

#### Scenario: Field changed once, no redundant save
- **WHEN** a task field is changed from its original value to a new value and the debounced auto-save fires
- **THEN** the new value is persisted exactly once and no further save is triggered until another change occurs

#### Scenario: Dismissing the sheet after reverting a field
- **WHEN** the user reverts a changed field back to its original value and then dismisses the edit sheet before the debounce fires
- **THEN** the dismiss-triggered save persists the reverted value if it differs from what was last saved
