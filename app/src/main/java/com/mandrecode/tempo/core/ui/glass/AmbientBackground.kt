package com.mandrecode.tempo.core.ui.glass

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.dp
import com.mandrecode.tempo.core.ui.theme.LocalTempoGlass

/**
 * Paints the page's backdrop: the scheme's `background` (the top tone of the two-tone page) with one
 * smooth gradient of the brand tone behind the header, fading out over [TopGlowExtent] so the
 * content panel below sits on the plain tone.
 *
 * Opaque, like the flat `background` it replaces, so a screen still covers whatever is behind it
 * and the tab crossfade blends two identical pictures instead of flashing the root through. Paint it
 * once, on a page's outermost container, and leave the layers inside it transparent.
 */
fun Modifier.ambientBackground(): Modifier = this then AmbientBackgroundElement

private data object AmbientBackgroundElement : ModifierNodeElement<AmbientBackgroundNode>() {
    override fun create() = AmbientBackgroundNode()

    override fun update(node: AmbientBackgroundNode) = Unit

    override fun InspectorInfo.inspectableProperties() {
        name = "ambientBackground"
    }
}

private class AmbientBackgroundNode :
    Modifier.Node(),
    DrawModifierNode,
    CompositionLocalConsumerModifierNode {
    override fun ContentDrawScope.draw() {
        val ambient = currentValueOf(LocalTempoGlass).ambient
        drawRect(ambient.base)
        if (ambient.topGlow.alpha > 0f) {
            drawRect(brush = topGlow(ambient.topGlow, TopGlowExtent.toPx()))
        }
        drawContent()
    }
}

/**
 * Eased rather than linear, so there is no line where the gradient ends: most of the colour stays
 * behind the title, and it thins out gradually over the chips toward the panel.
 */
internal fun topGlow(
    color: Color,
    extent: Float,
): Brush =
    Brush.verticalGradient(
        0f to color,
        GLOW_HOLD to color.copy(alpha = color.alpha * GLOW_HOLD_ALPHA),
        GLOW_THIN to color.copy(alpha = color.alpha * GLOW_THIN_ALPHA),
        1f to color.copy(alpha = 0f),
        startY = 0f,
        endY = extent,
    )

/** How far down the page the header gradient reaches: past the title and the chips. */
private val TopGlowExtent = 360.dp

private const val GLOW_HOLD = 0.35f
private const val GLOW_HOLD_ALPHA = 0.75f
private const val GLOW_THIN = 0.7f
private const val GLOW_THIN_ALPHA = 0.25f
