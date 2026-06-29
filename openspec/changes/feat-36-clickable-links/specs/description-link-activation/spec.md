# description-link-activation Specification

## Purpose
Define how links embedded in user-authored descriptions behave when rendered in Tempo cards.

## ADDED Requirements
### Requirement: Description links are recognized in cards and task editor
The system SHALL detect URI-like text in task, habit, and habit-chain descriptions rendered on cards and task editor surfaces.

#### Scenario: Web URL in a task description
- **WHEN** a task card displays a description containing `https://example.com`
- **THEN** the URL text is visually styled as a link
- **AND** tapping that URL launches Android external handling for `https://example.com`

#### Scenario: Web URL in the task bottom sheet
- **WHEN** the task bottom sheet displays a task description containing `https://example.com`
- **THEN** the URL text inside the description field is visually styled as a link

#### Scenario: Non-web URI in a habit description
- **WHEN** a habit card displays a description containing a supported URI such as `mailto:hello@example.com`, `tel:+34123456789`, `geo:40.4,-3.7`, `content://provider/item`, or `file:///storage/emulated/0/Documents/note.pdf`
- **THEN** the URI text is visually styled as a link
- **AND** tapping that URI launches Android external handling for the matching URI

### Requirement: Bare web links normalize to HTTPS
The system SHALL normalize bare web links that start with `www.` to HTTPS before opening them.

#### Scenario: Bare www link
- **WHEN** a card description contains `www.example.com`
- **AND** the user taps that link
- **THEN** Android external handling is launched for `https://www.example.com`

### Requirement: Card interactions remain intact
The system SHALL preserve existing card interactions outside detected link ranges.

#### Scenario: Tap description text outside a link
- **WHEN** the user taps non-link text in a card description
- **THEN** the card keeps its existing edit/open behavior
- **AND** no external URI is launched

#### Scenario: Expand collapsed linked description
- **WHEN** a description containing a link overflows the collapsed card text
- **THEN** the existing expand control remains available
- **AND** links in the visible text remain tappable
