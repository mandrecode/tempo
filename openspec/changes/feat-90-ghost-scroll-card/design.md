## Context

Task and habit lists are scrollable Compose surfaces that sit above app navigation chrome. When the list reaches its final real item, the bottom navigation area can make that item harder to read or interact with because there is little trailing scroll space.

## Goals / Non-Goals

**Goals:**

- Provide invisible trailing scroll clearance after the last real task and habit item.
- Keep the clearance within UI layer list composition.
- Preserve current empty states, item semantics, and user actions.

**Non-Goals:**

- Changing task or habit domain models, persistence, sorting, filtering, or recurrence behavior.
- Introducing a visible placeholder card, new copy, or new settings.
- Changing navigation bar implementation.

## Decisions

- Use list-level trailing spacing/content padding where the task and habit lists are composed.
  - Rationale: the behavior is purely visual and belongs near the list layout, without touching ViewModels or domain/data layers.
  - Alternative considered: adding fake list items to UI state. Rejected because it would make non-domain content part of state and risks item action/semantics confusion.
- Keep the trailing area invisible and non-interactive.
  - Rationale: users need breathing room for the final real card, not a new element to understand or target.
  - Alternative considered: rendering a ghost card. Rejected unless existing code makes padding impractical, because a rendered card could look selectable or like missing content.

## Risks / Trade-offs

- Clearance could be too small on devices with larger bottom bars -> Use dimension resources or existing spacing tokens that can be adjusted consistently.
- Clearance could duplicate existing bottom padding in one list -> Inspect current task and habit list layouts before implementation and apply the smallest scoped adjustment.
