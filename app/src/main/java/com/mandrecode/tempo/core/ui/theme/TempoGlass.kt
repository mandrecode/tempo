package com.mandrecode.tempo.core.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The glass material (#423): every token the ambient backdrop, the floating chrome and the modal
 * surfaces draw with, derived from the active [ColorScheme] so dynamic colour, the Tempo palette
 * and both themes all get a material that belongs to them rather than a fixed frost.
 *
 * The page keeps the app's two-tone layout, with glass on top of it:
 * - **Ambient** — the top tone: the scheme's background with one smooth brand gradient behind the
 *   header, fading out before the content.
 * - **Panel** — the content block below: solid surface, with a faint, smooth
 *   wash of the secondary tone down from its top edge.
 * - **Chrome** — the floating bar, rail and modal sheets: real backdrop blur where the platform can
 *   do it, a denser tint where it cannot, and a flat hairline rim.
 * - **Finish** — the components on those (cards, buttons, chips): their own pre-glass colours,
 *   opaque, so the list keeps its contrast. Nothing is outlined, lit or shadowed.
 *
 * Below API 31, on low-RAM devices and in previews nothing blurs: chrome falls back to its denser
 * tint and modals to an opaque surface with the full scrim.
 */
@Immutable
data class TempoGlass(
    /** Whether chrome samples and blurs what is behind it. False below API 31, in previews and on low-RAM devices. */
    val blurEnabled: Boolean,
    val blurRadius: Dp,
    /**
     * The blur over the app while a modal is open: far heavier than the chrome's, so the app dissolves
     * into a wash of its colours. A lighter one left the list readable as smudges, which slid about
     * under the form whenever the sheet moved (the keyboard opening, a title wrapping).
     */
    val windowBlurRadius: Dp,
    /** Neutral chrome fill when the backdrop is blurred behind it. */
    val chromeTint: Color,
    /** Neutral chrome fill when there is no blur: denser, so text over busy content stays legible. */
    val chromeFallbackTint: Color,
    /** Alpha applied to a coloured chrome fill (the add button, a selected control) over blur. */
    val tintedAlpha: Float,
    val tintedFallbackAlpha: Float,
    /** The chrome's edge; transparent, as nothing in the glass is outlined. */
    val rimTop: Color,
    val rimBottom: Color,
    val rimWidth: Dp,
    /** A faint sheen across the upper half of a chrome surface, so it reads as a lens, not a sticker. */
    val sheen: Color,
    val panelAlpha: Float,
    /**
     * A very smooth wash of the secondary tone down the content panel, from its top edge: the one
     * colour inside the two-tone page, under the cards.
     */
    val panelWash: Color,
    /** How much of its own colour a card keeps over what is behind it: a touch of glass, not a veil. */
    val cardAlpha: Float,
    /**
     * A content card's fill: the scheme's lightest (or, in the dark, brightest) container, partly
     * translucent, so the glow behind the panel reads through it as frost. A card tints it with
     * its own colour; a selected card turns opaque.
     */
    val cardFrost: Color,
    val panelRimTop: Color,
    /**
     * Fill of a modal sheet while the window behind it is blurred. Nearly opaque: a sheet holds a
     * form, and text fields need a clean surface; the frost shows around it, through the scrim.
     */
    val sheetAlpha: Float,
    /** How much of the modal scrim is kept once the blur is doing the separating. */
    val blurredScrimFraction: Float,
    val ambient: TempoAmbient,
    /** The finish tier's rim: top-lit like the chrome's, settling on a defining shade at the bottom. */
    val componentRim: BorderStroke,
    /** The finish tier's sheen, laid over a component's upper half; faint, as it sits over text. */
    val componentSheen: Color,
)

/**
 * The page behind the content: the scheme's background, with one smooth gradient of the brand tone
 * behind the header (the top of the two-tone page), fading out before the content panel's body.
 */
@Immutable
data class TempoAmbient(
    val base: Color,
    val topGlow: Color,
)

internal val GlassBlurRadius = 28.dp
internal val GlassWindowBlurRadius = 56.dp
internal val GlassRimWidth = 1.dp

/**
 * Cards keep all of their own colour: the list's contrast (cards on the panel)
 * matters more than seeing through them. The glass lives in the floating chrome and the sheets.
 */
private const val GLASS_CARD_ALPHA = 1f

internal fun tempoGlass(
    colorScheme: ColorScheme,
    darkTheme: Boolean,
    blurEnabled: Boolean,
): TempoGlass =
    if (darkTheme) {
        TempoGlass(
            blurEnabled = blurEnabled,
            blurRadius = GlassBlurRadius,
            windowBlurRadius = GlassWindowBlurRadius,
            chromeTint = colorScheme.surfaceContainerHighest.copy(alpha = 0.72f),
            chromeFallbackTint = colorScheme.surfaceContainerHighest.copy(alpha = 0.94f),
            tintedAlpha = 0.78f,
            tintedFallbackAlpha = 0.96f,
            rimTop = Color.Transparent,
            rimBottom = Color.Transparent,
            rimWidth = GlassRimWidth,
            sheen = Color.Transparent,
            panelAlpha = 1f,
            panelWash = colorScheme.secondaryContainer.copy(alpha = 0.10f),
            cardAlpha = GLASS_CARD_ALPHA,
            cardFrost = colorScheme.background.copy(alpha = GLASS_CARD_ALPHA),
            panelRimTop = Color.Transparent,
            sheetAlpha = 0.95f,
            blurredScrimFraction = 0.55f,
            ambient = tempoAmbient(colorScheme, darkTheme),
            componentRim = NoRim,
            componentSheen = Color.Transparent,
        )
    } else {
        TempoGlass(
            blurEnabled = blurEnabled,
            blurRadius = GlassBlurRadius,
            windowBlurRadius = GlassWindowBlurRadius,
            // A step darker than the near-white panel it floats over, so the bar keeps its shape there.
            chromeTint = colorScheme.surfaceContainerHigh.copy(alpha = 0.86f),
            chromeFallbackTint = colorScheme.surfaceContainerHigh.copy(alpha = 0.97f),
            tintedAlpha = 0.80f,
            tintedFallbackAlpha = 0.97f,
            rimTop = Color.Transparent,
            rimBottom = Color.Transparent,
            rimWidth = GlassRimWidth,
            sheen = Color.Transparent,
            panelAlpha = 1f,
            panelWash = colorScheme.secondaryContainer.copy(alpha = 0.18f),
            cardAlpha = GLASS_CARD_ALPHA,
            cardFrost = colorScheme.background.copy(alpha = GLASS_CARD_ALPHA),
            panelRimTop = Color.Transparent,
            sheetAlpha = 0.96f,
            blurredScrimFraction = 0.5f,
            ambient = tempoAmbient(colorScheme, darkTheme),
            componentRim = NoRim,
            componentSheen = Color.Transparent,
        )
    }

/** The header gradient: a little more chroma in the light, where a pale container would vanish. */
private fun tempoAmbient(
    colorScheme: ColorScheme,
    darkTheme: Boolean,
): TempoAmbient =
    TempoAmbient(
        base = colorScheme.background,
        topGlow =
            if (darkTheme) {
                glow(colorScheme.primaryContainer, colorScheme.primary, mix = 0.10f, alpha = 0.55f)
            } else {
                glow(colorScheme.primaryContainer, colorScheme.primary, mix = 0.20f, alpha = 0.70f)
            },
    )

/** A glow: the [container] colour pulled [mix] of the way toward its [accent], so it has some chroma. */
private fun glow(
    container: Color,
    accent: Color,
    mix: Float,
    alpha: Float,
) = lerp(container, accent, mix).copy(alpha = alpha)

/**
 * Nothing draws an outline: content components are set apart by their own colours on the two-tone
 * page, and the floating chrome by a tone of its own.
 */
private val NoRim = BorderStroke(0.dp, Color.Transparent)

private val DefaultTempoGlass =
    tempoGlass(
        colorScheme = androidx.compose.material3.lightColorScheme(),
        darkTheme = false,
        blurEnabled = false,
    )

val LocalTempoGlass = staticCompositionLocalOf { DefaultTempoGlass }
