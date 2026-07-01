## Context

Tempo's route top bars currently expose Settings through a plain `IconButton` in the navigation shell. Didi's comparable entry point uses a compact `Surface` with a rounded shape, border, and press-driven shape/color animations. This change is UI-only and should stay inside Compose presentation/navigation code.

## Goals / Non-Goals

**Goals:**
- Match Didi's Settings entry point treatment in Tempo: shaped surface, visible border, and animated pressed/unpressed colors.
- Preserve the current Settings navigation path and content description.
- Keep the implementation reusable enough for route top bars without introducing feature state or data-layer changes.

**Non-Goals:**
- Redesign Settings screen content.
- Change theme persistence, app settings data, navigation routes, or domain behavior.
- Add new dependencies.

## Decisions

- Add a reusable Compose Settings action component near shared UI/navigation code.
  - Rationale: the Settings affordance belongs to route chrome and can be reused wherever a top bar needs the same action.
  - Alternative considered: inline the animation inside `RouteTopBar`; rejected because the interaction details are easy to duplicate incorrectly.

- Use Didi's interaction model as the visual contract, tuned to Tempo's top-bar proportions.
  - Visual size: `40.dp`; unpressed radius: `20.dp`; pressed radius: `14.dp`; shape tween duration: `220ms`.
  - Unpressed colors: `surfaceContainerLow` container, `primary` content, `primary.copy(alpha = 0.48f)` border.
  - Pressed colors: `primaryContainer` container, `onPrimaryContainer` content, `primary` border.
  - Rationale: this directly satisfies the request to replicate the style, shape, and color animation.

- Keep the component accessible via `R.string.settings`.
  - Rationale: the existing label is localized and already describes the action.
  - Alternative considered: adding a new string; unnecessary because the action is still Settings.

## Risks / Trade-offs

- [Risk] The button is 40.dp, slightly under the typical 48.dp touch target guidance. → Mitigation: match the approved Tempo visual size for this issue and keep the action isolated to the top bar where surrounding spacing remains generous.
- [Risk] Compose UI tests do not directly assert animated colors. → Mitigation: cover click/navigation behavior and rely on static analysis plus manual/preview inspection for the visual animation.
