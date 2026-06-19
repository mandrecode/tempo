# Habit Chain Card Visual Design

## Collapsed State (Default View)

```
┌──────────────────────────────────────────────────────────────┐
│  ╭──────╮                                                     │
│  │ 2/5  │  Morning Routine           ✎  🗑                   │
│  │ ⚪⚪⚪ │  40% complete                                      │
│  ╰──────╯                                                     │
│                                                               │
│  Start your day with these essential habits                  │
│                                                               │
│  🔔 Has period reminder                                      │
│                                                               │
│  ● ● ○ ● ● ○ ● ○ ● ○ ○ ● ● ● ● ○ ● ● ○ ○ ● ● ● ● ● ○ ● ● ● ○ │
│  (Last 30 days completion history)                           │
└──────────────────────────────────────────────────────────────┘
```

### Visual Elements:
1. **Circular Progress Indicator** (top-left)
   - Shows "2/5" (completed/total)
   - Circle fills clockwise based on progress
   - Color matches chain's assigned color
   - 48dp diameter

2. **Title & Status** (top-center)
   - "Morning Routine" - Title in bold
   - "40% complete" - Small text, secondary color

3. **Action Buttons** (top-right)
   - ✎ Edit icon - Opens edit sheet
   - 🗑 Delete icon - Shows confirmation dialog

4. **Description** (middle)
   - Up to 2 lines when collapsed
   - Truncated with ellipsis if longer

5. **Reminder Badge** (lower-middle)
   - Shows if periodic reminder is set
   - Icon + text indicator

6. **History View** (bottom)
   - GitHub-style dots for last 30 days
   - Shows chain completion history

## Expanded State (After Click)

```
┌──────────────────────────────────────────────────────────────┐
│  ╭──────╮                                                     │
│  │ 2/5  │  Morning Routine           ✎  🗑                   │
│  │ ⚪⚪⚪ │  40% complete                                      │
│  ╰──────╯                                                     │
│                                                               │
│  Start your day with these essential habits to boost         │
│  productivity and wellness throughout the day.               │
│                                                               │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│  ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░   │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━   │
│                                                               │
│  Habits in chain:                                            │
│                                                               │
│  ☑  ~~Morning walk~~                                         │
│  ☐  Meditation                                               │
│  ☑  ~~Healthy breakfast~~                                    │
│  ☐  Journal writing                                          │
│  ☐  Review goals                                             │
│                                                               │
│  🔔 Has period reminder                                      │
│                                                               │
│  ● ● ○ ● ● ○ ● ○ ● ○ ○ ● ● ● ● ○ ● ● ○ ○ ● ● ● ● ● ○ ● ● ● ○ │
│  (Last 30 days completion history)                           │
└──────────────────────────────────────────────────────────────┘
```

### Additional Elements When Expanded:

1. **Full Description**
   - No truncation, shows complete text
   - Up to 5 lines visible

2. **Linear Progress Bar**
   - Full-width bar showing percentage
   - Filled portion uses chain color
   - Track uses surface variant color
   - 8dp height

3. **Habits List Section**
   - Header: "Habits in chain:"
   - Interactive checkboxes for each habit
   - Strikethrough text for completed habits
   - Checkbox colors match individual habit colors

4. **Interactive Checkboxes**
   - ☑ Checked (completed today)
   - ☐ Unchecked (not completed today)
   - Click to toggle completion

## Color Palette Examples

### Morning Routine (Blue Theme)
- Background: Blue 15% opacity
- Progress Indicator: Blue
- Checkbox: Blue

### Workout Routine (Orange Theme)
- Background: Orange 15% opacity
- Progress Indicator: Orange
- Checkbox: Orange

### Evening Routine (Purple Theme)
- Background: Purple 15% opacity
- Progress Indicator: Purple
- Checkbox: Purple

## Interaction States

### Hover/Press States
- Card background slightly darkens on press
- Buttons show ripple effect
- Smooth transition animations

### Animation Specifications
- Expand/collapse: 300ms ease-in-out
- Progress updates: 200ms ease-out
- Checkbox toggle: 150ms ease-in-out
- Color transitions: 300ms ease-in-out

## Responsive Behavior

### Phone (Portrait)
- Full-width cards
- Stacked layout
- 16dp horizontal padding

### Tablet (Landscape)
- Multi-column grid possible
- Larger progress indicators
- More visible habits

## Dark Mode Adaptations

### Colors
- Card background: colorScheme.secondaryContainer
- Text: colorScheme.onSurface
- Progress: Adjusted opacity for readability
- Checkboxes: Higher contrast

### Contrast Ratios
- All text meets WCAG AA standards
- Progress indicators visible in all modes
- Icon colors adjusted for clarity

## Progress Indicator States

### Empty Chain (0%)
```
╭──────╮
│ 0/0  │
│      │
╰──────╯
```

### No Completion (0/5)
```
╭──────╮
│ 0/5  │
│ ⚪   │
╰──────╯
```

### Partial Completion (2/5 = 40%)
```
╭──────╮
│ 2/5  │
│ ⚪⚪  │
╰──────╯
```

### Nearly Complete (4/5 = 80%)
```
╭──────╮
│ 4/5  │
│ ⚪⚪⚪⚪│
╰──────╯
```

### Fully Complete (5/5 = 100%)
```
╭──────╮
│ 5/5  │
│ ⚪⚪⚪⚪│
│ ⚪   │
╰──────╯
```

## Spacing Specifications

- **Card Padding**: 16dp all around
- **Element Spacing**: 12dp vertical gaps
- **Progress Indicator**: 48dp diameter, 4dp stroke width
- **Linear Progress Bar**: 8dp height
- **Checkbox Size**: 24dp
- **Icon Sizes**: 
  - Edit/Delete: 20dp
  - Reminder: 16dp
- **Text Sizes**:
  - Title: titleMedium (16sp, semibold)
  - Description: bodyMedium (14sp)
  - Percentage: bodySmall (12sp)
  - Habit items: bodyMedium (14sp)

## Accessibility Features

### Screen Reader Announcements
- "Morning Routine, 2 of 5 habits completed, 40 percent"
- "Expand to see habit details"
- "Morning walk, checkbox, checked"
- "Meditation, checkbox, unchecked"

### Semantic Labeling
- Progress indicator: "Completion progress"
- Edit button: "Edit Morning Routine"
- Delete button: "Delete Morning Routine"
- Checkboxes: "[Habit name], mark as complete"

### Keyboard Navigation
- Tab through interactive elements
- Enter/Space to activate buttons
- Focus indicators visible

## Error States

### No Habits in Chain
```
┌──────────────────────────────────────────────────────────────┐
│  ╭──────╮                                                     │
│  │ 0/0  │  Empty Routine             ✎  🗑                   │
│  │      │  No habits added                                   │
│  ╰──────╯                                                     │
│                                                               │
│  ⚠ No habits in this chain                                   │
│  Add habits to start tracking progress                       │
└──────────────────────────────────────────────────────────────┘
```

### Missing Habits (Deleted)
- Chain shows only existing habits
- Progress calculated on available habits only
- No error shown to user (graceful degradation)

## Loading States

### Initial Load
- Skeleton placeholders for cards
- Animated shimmer effect
- Placeholder circles for progress

### Updating Progress
- Smooth transition animation
- No loading spinner (optimistic UI)
- Instant visual feedback

## Comparison: Regular Habit vs Habit Chain

### Regular Habit Card
```
┌──────────────────────────────────────────────────────────────┐
│  Running 🏃                                             🗑    │
│  Morning exercise routine                                    │
│                                                               │
│  🔔 Has reminder                                             │
│                                                               │
│  ● ● ○ ● ● ○ ● ○ ● ○ ○ ● ● ● ● ○ ● ● ○ ○ ● ● ● ● ● ○ ● ● ● ○ │
└──────────────────────────────────────────────────────────────┘
```

### Habit Chain Card
```
┌──────────────────────────────────────────────────────────────┐
│  ╭──────╮                                                     │
│  │ 2/5  │  Morning Routine           ✎  🗑                   │
│  │ ⚪⚪⚪ │  40% complete                                      │
│  ╰──────╯                                                     │
│  ...                                                          │
└──────────────────────────────────────────────────────────────┘
```

**Key Differences:**
- Chains have circular progress indicator
- Chains show completion ratio and percentage
- Chains can be expanded to show member habits
- Chains use edit icon instead of just delete
- Chains have secondary container color by default
