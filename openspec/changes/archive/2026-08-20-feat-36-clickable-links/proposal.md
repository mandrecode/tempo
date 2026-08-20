# feat: clickable description links

## Summary
Allow users to tap web links written in task, habit, and habit-chain descriptions so the Android system can open the matching web page.

## Motivation
Descriptions can already hold free-form text, but web URL-like text is rendered as inert copy. Users who keep web references in Tempo descriptions should be able to follow those references directly from the displayed cards.

## Scope
- Detect web link targets in displayed task, habit, quit-habit, and habit-chain descriptions.
- Visually distinguish detected links from surrounding description text.
- Open detected links with Android's external `ACTION_VIEW` handling.
- Preserve existing card edit, completion, expansion, and subtask interactions.

## Non-Goals
- Rich Markdown rendering.
- Persisting attachments or granting app-owned file access.
- Opening non-web URI schemes such as `file://`, `content://`, `mailto:`, `tel:`, or `geo:`.
- Validating that external targets exist before launch.
