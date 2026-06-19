# Agent Documentation System

This directory contains specialized documentation files designed to guide AI agents in building and maintaining the Tempo & Habits Android application.

## Purpose

The modular documentation system reduces context size and hallucinations by allowing AI agents to load only the relevant information needed for their specific task.

## Structure

### [TECH_STACK.md](TECH_STACK.md)
**When to use:** Setting up the project, understanding libraries, checking directory structure

**Contains:**
- Project context (Kotlin, SDK versions)
- Core stack (Compose, Hilt, Room, Navigation 3)
- Mandatory libraries (kotlinx-datetime, serialization, etc.)
- Complete directory structure
- Operational commands (build, test, lint)
- Git conventions

### [UI_UX.md](UI_UX.md)
**When to use:** Building screens, composables, implementing MVI patterns

**Contains:**
- MVI + UDF architecture details
- Contract pattern (UiState, UiEvent, UiEffect)
- Screen/Content separation rules
- Compose best practices
- Navigation 3 type-safe routes
- Preview requirements
- Accessibility standards
- UI extension guidelines

### [DOMAIN.md](DOMAIN.md)
**When to use:** Creating business logic, UseCases, repository interfaces

**Contains:**
- Pure Kotlin principles (no Android imports)
- Model definitions and structure
- Repository interfaces
- UseCase patterns and best practices
- Result types for error handling
- Dispatcher injection
- Date/time handling with kotlinx-datetime
- Scheduler interface patterns

### [DATA.md](DATA.md)
**When to use:** Working with database, implementing repositories, creating mappers

**Contains:**
- Room database setup (Entities, DAOs, TypeConverters)
- Repository implementation patterns
- Entity ↔ Domain mapping strategies
- Dependency injection bindings
- SharedPreferences repositories
- Error handling in data layer

### [TESTING.md](TESTING.md)
**When to use:** Writing unit tests, UI tests, setting up test infrastructure

**Contains:**
- Testing strategy and philosophy
- Unit test patterns (MockK, Truth, Turbine)
- ViewModel testing with coroutines
- Compose UI testing
- Test naming conventions
- Test organization structure
- Coverage requirements

## How to Use

### For AI Agents

1. **Start at the root [AGENTS.md](../../AGENTS.md)** - This is the orchestrator
2. **Identify your task type** using the reference table
3. **Load the relevant specialized document(s)**
4. **Follow the layer-specific rules and checklists**

### For Human Developers

These documents serve as:
- **Architectural reference** - Quick lookup for patterns and conventions
- **Onboarding guide** - Understanding project structure and standards
- **Code review checklist** - Ensuring consistency across contributions

## Maintenance

When updating architectural decisions:
1. Update the relevant specialized document
2. Ensure consistency across all related documents
3. Run the validation script to check for broken references
4. Update examples if patterns have changed

## Validation

To validate the documentation structure:

```bash
cd /home/runner/work/tempo/tempo
bash -c "
  for file in docs/agents/{TECH_STACK,UI_UX,DOMAIN,DATA,TESTING}.md; do
    [ -f \$file ] && echo \"✓ \$file exists\" || echo \"✗ \$file missing\"
  done
"
```

## Philosophy

> **"Context is king, but too much context is chaos."**

By splitting comprehensive guidelines into focused, task-specific documents, we enable AI agents to:
- Load only relevant context
- Reduce hallucinations
- Maintain better adherence to standards
- Work more efficiently

The system is inspired by the principle of **separation of concerns** applied to documentation.
