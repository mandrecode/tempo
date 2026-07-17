package com.mandrecode.tempo.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mandrecode.tempo.R

// Google Sans Flex – bundled static fonts (24 pt optical size).
private val GoogleSansFontFamily =
    FontFamily(
        Font(R.font.google_sans_flex_thin, weight = FontWeight.Thin),
        Font(R.font.google_sans_flex_extralight, weight = FontWeight.ExtraLight),
        Font(R.font.google_sans_flex_light, weight = FontWeight.Light),
        Font(R.font.google_sans_flex_regular, weight = FontWeight.Normal),
        Font(R.font.google_sans_flex_medium, weight = FontWeight.Medium),
        Font(R.font.google_sans_flex_semibold, weight = FontWeight.SemiBold),
        Font(R.font.google_sans_flex_bold, weight = FontWeight.Bold),
        Font(R.font.google_sans_flex_extrabold, weight = FontWeight.ExtraBold),
        Font(R.font.google_sans_flex_black, weight = FontWeight.Black),
    )

/**
 * Material 3 typography scale with Google Sans Flex.
 * Following standard M3 scale values while leveraging the premium typeface.
 */
val Typography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = GoogleSansFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 57.sp,
                lineHeight = 64.sp,
                letterSpacing = (-0.25).sp,
            ),
        displayMedium =
            TextStyle(
                fontFamily = GoogleSansFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 45.sp,
                lineHeight = 52.sp,
                letterSpacing = 0.sp,
            ),
        displaySmall =
            TextStyle(
                fontFamily = GoogleSansFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 36.sp,
                lineHeight = 44.sp,
                letterSpacing = 0.sp,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = GoogleSansFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = 0.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = GoogleSansFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                letterSpacing = 0.sp,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = GoogleSansFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = 0.sp,
            ),
        titleLarge =
            TextStyle(
                fontFamily = GoogleSansFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = GoogleSansFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.15.sp,
            ),
        titleSmall =
            TextStyle(
                fontFamily = GoogleSansFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = GoogleSansFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = GoogleSansFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.25.sp,
            ),
        bodySmall =
            TextStyle(
                fontFamily = GoogleSansFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.4.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = GoogleSansFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = GoogleSansFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = GoogleSansFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
            ),
    )

// -- Semantic typography tokens -------------------------------------------------------------------
// Use these instead of .copy(fontWeight = ...) in composables.

/** Top-level screen title in the app bar. */
val Typography.topBarTitle: TextStyle
    get() = headlineMedium.copy(fontWeight = FontWeight.Bold)

/** Title text field in bottom sheets (task/habit creation). */
val Typography.inputTitle: TextStyle
    get() = titleLarge.copy(fontWeight = FontWeight.Bold)

/** Dialog title text. */
val Typography.dialogTitle: TextStyle
    get() = headlineSmall.copy(fontWeight = FontWeight.Bold)

/** Primary action button label in dialogs and bottom sheets. */
val Typography.dialogAction: TextStyle
    get() = labelLarge.copy(fontWeight = FontWeight.Bold)

/** Card / list-item title (bold). */
val Typography.cardTitle: TextStyle
    get() = titleMedium.copy(fontWeight = FontWeight.Bold)

/** Uppercase-like section header (e.g., "Completed", "Unscheduled habits"). */
val Typography.sectionHeader: TextStyle
    get() = titleSmall.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)

/** Group divider label (e.g., "Today", "Overdue"). */
val Typography.groupLabel: TextStyle
    get() = labelMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)

/** Bold metadata label (dates, counters on cards). */
val Typography.metadataLabel: TextStyle
    get() = labelSmall.copy(fontWeight = FontWeight.Bold)

/** Settings section title. */
val Typography.settingsSectionTitle: TextStyle
    get() = titleSmall.copy(fontWeight = FontWeight.Medium)

/** Empty-state headline. */
val Typography.emptyStateTitle: TextStyle
    get() = headlineSmall.copy(fontWeight = FontWeight.SemiBold)

/** Chip primary action label (e.g., "Add habit"). */
val Typography.chipActionLabel: TextStyle
    get() = labelLarge.copy(fontWeight = FontWeight.SemiBold)

/** Bottom sheet / dialog sort title. */
val Typography.sheetTitle: TextStyle
    get() = titleLarge.copy(fontWeight = FontWeight.Bold)

/** Small badge count overlay. */
val Typography.badgeCount: TextStyle
    get() = labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)

/** Category chip selected label. */
val Typography.categoryChipSelected: TextStyle
    get() = titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.2.sp)

/** Category chip unselected label. */
val Typography.categoryChipUnselected: TextStyle
    get() = titleSmall.copy(fontWeight = FontWeight.Medium)

/** Subtask / secondary item title (medium-weight body). */
val Typography.subtaskTitle: TextStyle
    get() = bodyLarge.copy(fontWeight = FontWeight.Medium)

/** Selected state for filter / expressive chips. */
val Typography.filterChipSelected: TextStyle
    get() = labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)

/** Onboarding welcome screen app-name title, compact layout. */
val Typography.onboardingWelcomeTitleCompact: TextStyle
    get() = headlineLarge.copy(fontWeight = FontWeight.SemiBold)

/** Onboarding welcome screen app-name title, expanded layout. */
val Typography.onboardingWelcomeTitleExpanded: TextStyle
    get() = displayMedium.copy(fontWeight = FontWeight.SemiBold)

/** Onboarding page headline (education/appearance/setup pages), compact layout. */
val Typography.onboardingPageHeadlineCompact: TextStyle
    get() = headlineMedium.copy(fontWeight = FontWeight.SemiBold)

/** Onboarding page headline (education/appearance/setup pages), expanded layout. */
val Typography.onboardingPageHeadlineExpanded: TextStyle
    get() = headlineLarge.copy(fontWeight = FontWeight.SemiBold)

/** Settings top bar title, collapsed state (normal weight, distinct from the bold [topBarTitle] used by root tabs). */
val Typography.settingsTopBarTitleCollapsed: TextStyle
    get() = headlineSmall.copy(fontWeight = FontWeight.Normal)

/** Settings top bar title, expanded state. */
val Typography.settingsTopBarTitleExpanded: TextStyle
    get() = displayMedium.copy(fontWeight = FontWeight.Normal)
