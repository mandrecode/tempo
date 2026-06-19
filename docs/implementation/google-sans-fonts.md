# Google Sans Flex Font Implementation

## Overview
This document describes the implementation of Google Sans Flex (the modern Material Design 3 variable font) in the Tempo & Habits application.

## Background
Google Sans Flex is the official typeface for Material Design 3, providing a modern, flexible, and readable font family. It's bundled directly in the app for instant, reliable rendering — no runtime download or Google Play Services dependency.

## Implementation Details

### 1. Bundled Static Fonts
- **Location**: `app/src/main/res/font/google_sans_flex_*.ttf`
- **Optical size**: 24 pt (general-purpose, covers 11–57 sp UI text)
- **Weights**: Thin, ExtraLight, Light, Regular, Medium, SemiBold, Bold, ExtraBold, Black
- **APK impact**: ~1.1 MB (9 files × ~125 KB)
- **License**: Open Font License (OFL)

### 2. Typography System
The typography is aligned with the "expressive" style found in modern Google apps (e.g., Google Drive, Google Keep), prioritizing readability and hierarchy while maintaining a premium, "designed" feel.

**File:** `app/src/main/java/com/mandrecode/tempo/core/ui/theme/Type.kt`

Key characteristics:
- **Readable Weight References**: Uses named constants (`FontWeight.Bold`, `Medium`, `SemiBold`) instead of numeric `WXXX` patterns for better code maintainability.
- **Font Family**: Includes all weights from `Thin` to `Black` to leverage the full "Flex" range.
- **Compact & Punchy**: Slightly reduced font sizes combined with bold weights to create a high-density, modern interface.

### Material 3 Typography Scale (Google Apps Style)
The scale is designed to be expressive yet balanced:

#### Display Styles (Hero text)
- `displayLarge`: 48sp (**Bold**)
- `displayMedium`: 36sp (**Bold**)
- `displaySmall`: 28sp (**Bold**)

#### Headline Styles (Section headers / TopBars)
- `headlineLarge`: 24sp (**Bold**)
- `headlineMedium`: 20sp (**Bold**)
- `headlineSmall`: 18sp (**Bold**)

#### Title Styles (Card titles / Subheaders)
- `titleLarge`: 18sp (**Medium**)
- `titleMedium`: 14sp (**Medium**)
- `titleSmall`: 12sp (**Medium**)

#### Body Styles (Main content)
- `bodyLarge`: 16sp (**Normal**)
- `bodyMedium`: 14sp (**Normal**)
- `bodySmall`: 12sp (**Normal**)

#### Label Styles (Buttons / Chips / Navigation)
- `labelLarge`: 14sp (**SemiBold**)
- `labelMedium`: 12sp (**SemiBold**)
- `labelSmall`: 11sp (**SemiBold**)

## Benefits
- **Consistency**: Matches the look and feel of top-tier Google ecosystem applications.
- **Reliability**: Bundled fonts render instantly — no network download or GMS dependency.
- **Modernity**: Uses Google Sans Flex, the latest M3 typeface from Google.