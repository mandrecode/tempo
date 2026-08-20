# fix-773-completed-task-priority-disabled

## Summary

GitHub issue [#773](https://github.com/mandrecode/tempo/issues/773) reports that completed tasks expose priority editing controls even though priority updates are not applied. Disable priority modification for completed tasks, matching the existing completed-task restrictions for date and reminder fields.

## Scope

- Hide or disable priority controls when editing a completed task.
- Preserve existing priority display for completed tasks.
- Do not change persistence or completion behavior.
