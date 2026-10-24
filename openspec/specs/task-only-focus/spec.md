# task-only-focus Specification

## Purpose
Define a task-only Focus agenda, deliberate planning of undated work, and consistent task progress while preserving historical activity.
## Requirements
### Requirement: Focus contains tasks across categories
Focus SHALL display tasks due today and unfinished overdue tasks across categories, with Today before Overdue, and SHALL exclude habits and chains from all agenda and editing/completion surfaces. Existing task/subtask deduplication SHALL remain in force.

#### Scenario: Mixed work exists
- **WHEN** tasks in multiple categories, scheduled habits and chains exist
- **THEN** Focus displays only eligible tasks and their categories

#### Scenario: Future and completed overdue tasks
- **WHEN** a task is future-dated or overdue and completed
- **THEN** it has no standalone Focus row

#### Scenario: Parent and subtask are eligible
- **WHEN** a parent task and its subtask belong to the day
- **THEN** the subtask appears inside the parent rather than as a duplicate standalone row

### Requirement: Undated work is deliberately planned
Focus SHALL retain the undated open top-level task count and existing planning picker. Undated tasks SHALL enter Today only when assigned today's date through existing planning behavior.

#### Scenario: Undated backlog exists
- **WHEN** open undated tasks exist
- **THEN** Focus offers the planning picker without adding the entire backlog to its agenda or progress denominator

### Requirement: Focus progress and new activity records are task-only
The hero and newly recomputed daily activity SHALL count only agenda tasks. Routine completion SHALL NOT record Focus activity. Recomputing today SHALL preserve focus minutes and SHALL NOT rewrite earlier daily records.

#### Scenario: Only routines exist
- **WHEN** today has scheduled habits or chains but no eligible tasks
- **THEN** Focus shows its task empty state and zero scheduled/completed tasks

#### Scenario: Task progress matches the agenda
- **WHEN** eligible tasks are completed or rescheduled
- **THEN** the hero and today's recorded counts follow the same membership rule

#### Scenario: Existing historical aggregates
- **WHEN** task-only counts are recorded for today after an update
- **THEN** earlier aggregate records and today's accumulated focus minutes remain intact

### Requirement: Task sessions remain available
Focus SHALL retain task session controls and draw Up next from incomplete Today tasks before incomplete Overdue tasks.

#### Scenario: Work is available today and overdue
- **WHEN** both sections contain incomplete tasks
- **THEN** Up next offers today's tasks first and existing session controls remain available

