package com.mandrecode.tempo.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mandrecode.tempo.R

// Google Sans Flex — one variable font file (axes: wght, opsz, wdth, slnt, GRAD, ROND) standing
// in for what used to be 9 separate pre-instanced static files. opsz is pinned to match those
// static files' own frozen optical size, so swapping to this doesn't shift text rendering.
// ROND is maxed out app-wide for a consistently rounder, friendlier letterform.
private const val GOOGLE_SANS_OPTICAL_SIZE = 24f
private const val GOOGLE_SANS_ROUNDNESS = 100f
private const val GOOGLE_SANS_WIDTH_NORMAL = 100f
private const val GOOGLE_SANS_TITLE_WIDTH = 120f

private fun googleSansFont(
    weight: FontWeight,
    width: Float = GOOGLE_SANS_WIDTH_NORMAL,
): Font =
    Font(
        R.font.google_sans_flex_variable,
        weight = weight,
        variationSettings =
            FontVariation.Settings(
                FontVariation.weight(weight.weight),
                FontVariation.width(width),
                FontVariation.Setting("opsz", GOOGLE_SANS_OPTICAL_SIZE),
                FontVariation.Setting("ROND", GOOGLE_SANS_ROUNDNESS),
            ),
    )

private val GoogleSansFontFamily =
    FontFamily(
        googleSansFont(FontWeight.Thin),
        googleSansFont(FontWeight.ExtraLight),
        googleSansFont(FontWeight.Light),
        googleSansFont(FontWeight.Normal),
        googleSansFont(FontWeight.Medium),
        googleSansFont(FontWeight.SemiBold),
        googleSansFont(FontWeight.Bold),
        googleSansFont(FontWeight.ExtraBold),
        googleSansFont(FontWeight.Black),
    )

// Same variable file, widened (wdth axis) for a bolder, more expressive app-bar title. Only the
// weight actually used (Black) is declared; add more entries here if another style adopts this.
private val GoogleSansFlexExpressiveFontFamily =
    FontFamily(
        googleSansFont(FontWeight.Black, width = GOOGLE_SANS_TITLE_WIDTH),
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
    get() =
        headlineMedium.copy(
            fontFamily = GoogleSansFlexExpressiveFontFamily,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.25).sp,
        )

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

/** Habit chain card title — larger than [cardTitle] since a chain groups multiple habits. */
val Typography.chainTitle: TextStyle
    get() = headlineSmall.copy(fontWeight = FontWeight.Bold)

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

/**
 * Onboarding welcome screen app-name title, short-height layout (`isShort`).
 * Named for the height-driven `isShort` axis, not the width-driven `isExpanded` one — do not
 * confuse with a width breakpoint.
 */
val Typography.onboardingWelcomeTitleShort: TextStyle
    get() = headlineLarge.copy(fontWeight = FontWeight.SemiBold)

/** Onboarding welcome screen app-name title, regular-height layout. */
val Typography.onboardingWelcomeTitleRegular: TextStyle
    get() = displayMedium.copy(fontWeight = FontWeight.SemiBold)

/**
 * Onboarding page headline (education/appearance/setup pages), short-height layout
 * (`isShort`). Named for the height-driven `isShort` axis, not the
 * width-driven `isExpanded` one — do not confuse with a width breakpoint.
 */
val Typography.onboardingPageHeadlineShort: TextStyle
    get() = headlineMedium.copy(fontWeight = FontWeight.SemiBold)

/** Onboarding page headline (education/appearance/setup pages), regular-height layout. */
val Typography.onboardingPageHeadlineRegular: TextStyle
    get() = headlineLarge.copy(fontWeight = FontWeight.SemiBold)

/** Settings top bar title, collapsed state (normal weight, distinct from the bold [topBarTitle] used by root tabs). */
val Typography.settingsTopBarTitleCollapsed: TextStyle
    get() = headlineSmall.copy(fontWeight = FontWeight.Normal)

/** Settings top bar title, expanded state. */
val Typography.settingsTopBarTitleExpanded: TextStyle
    get() = displayMedium.copy(fontWeight = FontWeight.Normal)
